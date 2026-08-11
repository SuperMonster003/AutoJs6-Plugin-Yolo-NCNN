#include <jni.h>

#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <limits>
#include <string>
#include <vector>

#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>

#include <mat.h>
#include <net.h>
#include <platform.h>

namespace {

constexpr int kInputSize = 640;
constexpr int kClassCount = 80;
constexpr int kOutputRows = 4 + kClassCount;
constexpr int kOutputColumns = 8400;
constexpr int kDetectionFields = 6;

struct Detector {
    ncnn::Net net;
    std::string input_name;
    std::string output_name;
    std::atomic<jlong> cancelled_sequence{0};
    std::atomic<jlong> active_sequence{0};
};

struct Detection {
    float left;
    float top;
    float right;
    float bottom;
    float confidence;
    int class_id;
};

class ScopedUtfChars {
public:
    ScopedUtfChars(JNIEnv* env, jstring value)
        : env_(env), value_(value), chars_(value == nullptr ? nullptr : env->GetStringUTFChars(value, nullptr)) {}

    ~ScopedUtfChars() {
        if (chars_ != nullptr) {
            env_->ReleaseStringUTFChars(value_, chars_);
        }
    }

    const char* get() const { return chars_; }

private:
    JNIEnv* env_;
    jstring value_;
    const char* chars_;
};

class ScopedMapping {
public:
    ScopedMapping(void* address, size_t length) : address_(address), length_(length) {}

    ~ScopedMapping() {
        if (address_ != MAP_FAILED) {
            munmap(address_, length_);
        }
    }

    const unsigned char* bytes() const {
        return static_cast<const unsigned char*>(address_);
    }

private:
    void* address_;
    size_t length_;
};

void throw_java(JNIEnv* env, const char* class_name, const std::string& message) {
    if (env->ExceptionCheck()) return;
    jclass error_class = env->FindClass(class_name);
    if (error_class != nullptr) {
        env->ThrowNew(error_class, message.c_str());
    }
}

void throw_argument(JNIEnv* env, const std::string& message) {
    throw_java(env, "java/lang/IllegalArgumentException", message);
}

void throw_state(JNIEnv* env, const std::string& message) {
    throw_java(env, "java/lang/IllegalStateException", message);
}

void throw_cancelled(JNIEnv* env) {
    throw_java(env, "java/util/concurrent/CancellationException", "YOLO detection was cancelled");
}

Detector* detector_from(jlong handle) {
    return reinterpret_cast<Detector*>(static_cast<intptr_t>(handle));
}

bool is_cancelled(const Detector* detector, jlong sequence) {
    return detector->cancelled_sequence.load(std::memory_order_acquire) == sequence;
}

float intersection_over_union(const Detection& first, const Detection& second) {
    const float overlap_width = std::max(0.f, std::min(first.right, second.right) -
        std::max(first.left, second.left));
    const float overlap_height = std::max(0.f, std::min(first.bottom, second.bottom) -
        std::max(first.top, second.top));
    const float intersection = overlap_width * overlap_height;
    const float first_area = std::max(0.f, first.right - first.left) *
        std::max(0.f, first.bottom - first.top);
    const float second_area = std::max(0.f, second.right - second.left) *
        std::max(0.f, second.bottom - second.top);
    const float union_area = first_area + second_area - intersection;
    return union_area > 0.f ? intersection / union_area : 0.f;
}

bool validate_image_file(
    JNIEnv* env,
    int fd,
    int width,
    int height,
    int row_stride,
    jlong declared_size,
    size_t* mapped_size
) {
    const int64_t minimum_stride = static_cast<int64_t>(width) * 4;
    if (fd < 0 || width <= 0 || width > 16384 || height <= 0 || height > 16384 ||
        row_stride < minimum_stride || declared_size <= 0 || declared_size > 256LL * 1024LL * 1024LL) {
        throw_argument(env, "Invalid RGBA image metadata");
        return false;
    }
    const int64_t expected_size = static_cast<int64_t>(row_stride) * static_cast<int64_t>(height);
    if (expected_size != declared_size ||
        static_cast<uint64_t>(declared_size) > std::numeric_limits<size_t>::max()) {
        throw_argument(env, "RGBA image size must equal rowStride * height");
        return false;
    }
    struct stat file_status {};
    if (fstat(fd, &file_status) != 0) {
        throw_argument(env, "Cannot stat the RGBA image descriptor");
        return false;
    }
    if (file_status.st_size != declared_size) {
        throw_argument(env, "RGBA image descriptor length does not match declared size");
        return false;
    }
    *mapped_size = static_cast<size_t>(declared_size);
    return true;
}

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_io_github_supermonster003_autojs6_plugin_yolo_ncnn_provider_NativeYoloRuntime_nativeBackendVersion(
    JNIEnv* env,
    jobject /* self */
) {
    return env->NewStringUTF("ncnn/" NCNN_VERSION_STRING);
}

extern "C" JNIEXPORT jlong JNICALL
Java_io_github_supermonster003_autojs6_plugin_yolo_ncnn_provider_NativeYoloRuntime_nativeCreate(
    JNIEnv* env,
    jobject /* self */,
    jstring param_path,
    jstring bin_path,
    jstring input_name,
    jstring output_name,
    jint cpu_threads
) {
    if (param_path == nullptr || bin_path == nullptr || input_name == nullptr || output_name == nullptr) {
        throw_argument(env, "NCNN model paths and tensor names are required");
        return 0;
    }
    if (cpu_threads < 1 || cpu_threads > 64) {
        throw_argument(env, "NCNN CPU thread count is invalid");
        return 0;
    }

    ScopedUtfChars param_chars(env, param_path);
    ScopedUtfChars bin_chars(env, bin_path);
    ScopedUtfChars input_chars(env, input_name);
    ScopedUtfChars output_chars(env, output_name);
    if (param_chars.get() == nullptr || bin_chars.get() == nullptr ||
        input_chars.get() == nullptr || output_chars.get() == nullptr) {
        if (!env->ExceptionCheck()) throw_state(env, "Cannot read NCNN model arguments");
        return 0;
    }
    if (std::strcmp(input_chars.get(), "in0") != 0 || std::strcmp(output_chars.get(), "out0") != 0) {
        throw_argument(env, "R1 supports only in0 and out0 tensor names");
        return 0;
    }

    Detector* detector = new Detector();
    detector->input_name = input_chars.get();
    detector->output_name = output_chars.get();
    detector->net.opt.num_threads = cpu_threads;
    detector->net.opt.use_vulkan_compute = false;
    detector->net.opt.use_fp16_packed = false;
    detector->net.opt.use_fp16_storage = false;
    detector->net.opt.use_fp16_arithmetic = false;

    const int param_status = detector->net.load_param(param_chars.get());
    if (param_status != 0) {
        delete detector;
        throw_state(env, "NCNN rejected model.ncnn.param (status " +
            std::to_string(param_status) + ")");
        return 0;
    }
    const int model_status = detector->net.load_model(bin_chars.get());
    if (model_status != 0) {
        delete detector;
        throw_state(env, "NCNN rejected model.ncnn.bin (status " +
            std::to_string(model_status) + ")");
        return 0;
    }
    return static_cast<jlong>(reinterpret_cast<intptr_t>(detector));
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_io_github_supermonster003_autojs6_plugin_yolo_ncnn_provider_NativeYoloRuntime_nativeDetect(
    JNIEnv* env,
    jobject /* self */,
    jlong handle,
    jlong sequence,
    jint fd,
    jint width,
    jint height,
    jint row_stride,
    jlong size_bytes,
    jfloat confidence_threshold,
    jfloat iou_threshold,
    jint max_detections
) {
    Detector* detector = detector_from(handle);
    if (detector == nullptr) {
        throw_state(env, "NCNN detector handle is closed");
        return nullptr;
    }
    if (sequence <= 0 || !std::isfinite(confidence_threshold) || confidence_threshold < 0.f ||
        confidence_threshold > 1.f || !std::isfinite(iou_threshold) || iou_threshold < 0.f ||
        iou_threshold > 1.f || max_detections < 1 || max_detections > 400) {
        throw_argument(env, "Invalid YOLO detect options");
        return nullptr;
    }

    size_t mapped_size = 0;
    if (!validate_image_file(env, fd, width, height, row_stride, size_bytes, &mapped_size)) {
        return nullptr;
    }
    void* address = mmap(nullptr, mapped_size, PROT_READ, MAP_SHARED, fd, 0);
    if (address == MAP_FAILED) {
        throw_argument(env, "Cannot map the RGBA image descriptor");
        return nullptr;
    }
    ScopedMapping mapping(address, mapped_size);

    detector->active_sequence.store(sequence, std::memory_order_release);
    if (is_cancelled(detector, sequence)) {
        detector->active_sequence.store(0, std::memory_order_release);
        throw_cancelled(env);
        return nullptr;
    }

    const float scale = std::min(
        static_cast<float>(kInputSize) / static_cast<float>(width),
        static_cast<float>(kInputSize) / static_cast<float>(height)
    );
    const int resized_width = std::max(1, static_cast<int>(std::round(width * scale)));
    const int resized_height = std::max(1, static_cast<int>(std::round(height * scale)));
    const int pad_left = (kInputSize - resized_width) / 2;
    const int pad_right = kInputSize - resized_width - pad_left;
    const int pad_top = (kInputSize - resized_height) / 2;
    const int pad_bottom = kInputSize - resized_height - pad_top;

    ncnn::Mat resized = ncnn::Mat::from_pixels_resize(
        mapping.bytes(),
        ncnn::Mat::PIXEL_RGBA2RGB,
        width,
        height,
        row_stride,
        resized_width,
        resized_height
    );
    if (resized.empty()) {
        detector->active_sequence.store(0, std::memory_order_release);
        throw_state(env, "NCNN could not preprocess the RGBA image");
        return nullptr;
    }
    ncnn::Mat input;
    ncnn::copy_make_border(
        resized,
        input,
        pad_top,
        pad_bottom,
        pad_left,
        pad_right,
        ncnn::BORDER_CONSTANT,
        114.f
    );
    if (input.empty() || input.w != kInputSize || input.h != kInputSize || input.c != 3) {
        detector->active_sequence.store(0, std::memory_order_release);
        throw_state(env, "NCNN letterbox preprocessing produced an invalid tensor");
        return nullptr;
    }
    const float normalization[3] = {1.f / 255.f, 1.f / 255.f, 1.f / 255.f};
    input.substract_mean_normalize(nullptr, normalization);

    if (is_cancelled(detector, sequence)) {
        detector->active_sequence.store(0, std::memory_order_release);
        throw_cancelled(env);
        return nullptr;
    }
    ncnn::Extractor extractor = detector->net.create_extractor();
    extractor.set_light_mode(true);
    const int input_status = extractor.input(detector->input_name.c_str(), input);
    if (input_status != 0) {
        detector->active_sequence.store(0, std::memory_order_release);
        throw_state(env, "NCNN rejected input tensor (status " + std::to_string(input_status) + ")");
        return nullptr;
    }
    ncnn::Mat output;
    const int output_status = extractor.extract(detector->output_name.c_str(), output);
    if (output_status != 0) {
        detector->active_sequence.store(0, std::memory_order_release);
        throw_state(env, "NCNN inference failed (status " + std::to_string(output_status) + ")");
        return nullptr;
    }
    if (is_cancelled(detector, sequence)) {
        detector->active_sequence.store(0, std::memory_order_release);
        throw_cancelled(env);
        return nullptr;
    }
    if (output.dims != 2 || output.w != kOutputColumns || output.h != kOutputRows ||
        output.elemsize != sizeof(float) || output.elempack != 1) {
        detector->active_sequence.store(0, std::memory_order_release);
        throw_state(
            env,
            "NCNN out0 must be float32 [84,8400], got dims=" + std::to_string(output.dims) +
                " w=" + std::to_string(output.w) + " h=" + std::to_string(output.h)
        );
        return nullptr;
    }

    const float* center_x = output.row(0);
    const float* center_y = output.row(1);
    const float* box_width = output.row(2);
    const float* box_height = output.row(3);
    std::vector<Detection> candidates;
    candidates.reserve(256);
    for (int column = 0; column < kOutputColumns; ++column) {
        if ((column & 0xff) == 0 && is_cancelled(detector, sequence)) {
            detector->active_sequence.store(0, std::memory_order_release);
            throw_cancelled(env);
            return nullptr;
        }
        int class_id = 0;
        float confidence = output.row(4)[column];
        for (int class_index = 1; class_index < kClassCount; ++class_index) {
            const float score = output.row(4 + class_index)[column];
            if (score > confidence) {
                confidence = score;
                class_id = class_index;
            }
        }
        if (!std::isfinite(confidence) || confidence < confidence_threshold) continue;

        const float left = (center_x[column] - box_width[column] * 0.5f - pad_left) / scale;
        const float top = (center_y[column] - box_height[column] * 0.5f - pad_top) / scale;
        const float right = (center_x[column] + box_width[column] * 0.5f - pad_left) / scale;
        const float bottom = (center_y[column] + box_height[column] * 0.5f - pad_top) / scale;
        if (!std::isfinite(left) || !std::isfinite(top) ||
            !std::isfinite(right) || !std::isfinite(bottom)) continue;

        Detection detection{
            std::clamp(left, 0.f, static_cast<float>(width)),
            std::clamp(top, 0.f, static_cast<float>(height)),
            std::clamp(right, 0.f, static_cast<float>(width)),
            std::clamp(bottom, 0.f, static_cast<float>(height)),
            std::clamp(confidence, 0.f, 1.f),
            class_id,
        };
        if (detection.right <= detection.left || detection.bottom <= detection.top) continue;
        candidates.push_back(detection);
    }

    std::stable_sort(candidates.begin(), candidates.end(), [](const Detection& first, const Detection& second) {
        return first.confidence > second.confidence;
    });
    std::vector<Detection> picked;
    picked.reserve(std::min(static_cast<size_t>(max_detections), candidates.size()));
    for (const Detection& candidate : candidates) {
        if (is_cancelled(detector, sequence)) {
            detector->active_sequence.store(0, std::memory_order_release);
            throw_cancelled(env);
            return nullptr;
        }
        bool suppressed = false;
        for (const Detection& accepted : picked) {
            if (accepted.class_id == candidate.class_id &&
                intersection_over_union(accepted, candidate) > iou_threshold) {
                suppressed = true;
                break;
            }
        }
        if (!suppressed) {
            picked.push_back(candidate);
            if (static_cast<int>(picked.size()) >= max_detections) break;
        }
    }

    const jsize result_length = static_cast<jsize>(picked.size() * kDetectionFields);
    jfloatArray result = env->NewFloatArray(result_length);
    if (result == nullptr) {
        detector->active_sequence.store(0, std::memory_order_release);
        return nullptr;
    }
    std::vector<jfloat> encoded(static_cast<size_t>(result_length));
    for (size_t index = 0; index < picked.size(); ++index) {
        const Detection& detection = picked[index];
        const size_t offset = index * kDetectionFields;
        encoded[offset] = static_cast<jfloat>(detection.class_id);
        encoded[offset + 1] = detection.confidence;
        encoded[offset + 2] = detection.left;
        encoded[offset + 3] = detection.top;
        encoded[offset + 4] = detection.right;
        encoded[offset + 5] = detection.bottom;
    }
    if (result_length > 0) {
        env->SetFloatArrayRegion(result, 0, result_length, encoded.data());
    }
    detector->active_sequence.store(0, std::memory_order_release);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_io_github_supermonster003_autojs6_plugin_yolo_ncnn_provider_NativeYoloRuntime_nativeCancel(
    JNIEnv* /* env */,
    jobject /* self */,
    jlong handle,
    jlong sequence
) {
    Detector* detector = detector_from(handle);
    if (detector != nullptr && sequence > 0) {
        detector->cancelled_sequence.store(sequence, std::memory_order_release);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_io_github_supermonster003_autojs6_plugin_yolo_ncnn_provider_NativeYoloRuntime_nativeDestroy(
    JNIEnv* /* env */,
    jobject /* self */,
    jlong handle
) {
    delete detector_from(handle);
}
