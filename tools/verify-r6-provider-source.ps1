# R6 source/build/package preflight. It intentionally performs no ADB or production signing.
[CmdletBinding()]
param(
    [switch] $SkipBuild,
    [switch] $RequireClean,
    [string] $ReportPath = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
if (Test-Path variable:PSNativeCommandUseErrorActionPreference) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repository = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot ".."))
$applicationId = "io.github.supermonster003.autojs6.plugin.yolo.ncnn"
$expectedIdentity = [ordered]@{
    plugin_id = "yolo-ncnn"
    plugin_engine = "yolo"
    plugin_variant = "ncnn"
    plugin_requires_host_version = "5274"
}
$expectedNativeEntry = "lib/arm64-v8a/libautojs_yolo.so"
$requiredApkAssets = [ordered]@{
    "assets/THIRD_PARTY_NOTICES.md" = "app/src/main/assets/THIRD_PARTY_NOTICES.md"
    "assets/licenses/MPL-2.0.txt" = "app/src/main/assets/licenses/MPL-2.0.txt"
    "assets/licenses/ncnn-20260526.txt" = "app/src/main/assets/licenses/ncnn-20260526.txt"
    "assets/third_party/ncnn/provenance.lock.json" = "app/src/main/assets/third_party/ncnn/provenance.lock.json"
}
$knownModelLeaves = @(
    "model.json",
    "model-manifest-v1.json",
    "model.ncnn.param",
    "model.ncnn.bin",
    "metadata.yaml",
    "fixture.lock.json",
    "expected-bus-detections.json",
    "expected-synthetic-detection.json",
    "yolo11n.pt",
    "bus.jpg",
    "synthetic-heldout.png"
)

function Assert-R6ProviderCondition {
    param(
        [Parameter(Mandatory = $true)][bool] $Condition,
        [Parameter(Mandatory = $true)][string] $Message
    )
    if (-not $Condition) { throw $Message }
}

function Get-R6ProviderRequiredFile {
    param(
        [Parameter(Mandatory = $true)][string] $Path,
        [Parameter(Mandatory = $true)][string] $Label
    )
    $fullPath = [System.IO.Path]::GetFullPath($Path)
    if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
        throw "$Label is missing: $fullPath"
    }
    return $fullPath
}

function Get-R6ProviderSha256 {
    param([Parameter(Mandatory = $true)][string] $Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-R6ProviderOnlyFile {
    param(
        [Parameter(Mandatory = $true)][string] $Directory,
        [Parameter(Mandatory = $true)][string] $Filter,
        [Parameter(Mandatory = $true)][string] $Label
    )
    $matches = @(Get-ChildItem -LiteralPath $Directory -Filter $Filter -ErrorAction SilentlyContinue |
        Where-Object { -not $_.PSIsContainer })
    Assert-R6ProviderCondition ($matches.Count -eq 1) "$Label must resolve to exactly one file; found $($matches.Count)"
    return $matches[0].FullName
}

function Invoke-R6ProviderCapture {
    param(
        [Parameter(Mandatory = $true)][string] $FilePath,
        [Parameter(Mandatory = $true)][string[]] $Arguments
    )
    $output = @(& $FilePath @Arguments 2>&1 | ForEach-Object { [string]$_ })
    return [pscustomobject][ordered]@{
        exitCode = $LASTEXITCODE
        output = $output
    }
}

function Resolve-R6ProviderApkSigner {
    $sdkCandidates = [System.Collections.Generic.List[string]]::new()
    $localProperties = Join-Path $repository "local.properties"
    if (Test-Path -LiteralPath $localProperties -PathType Leaf) {
        $sdkLine = Get-Content -LiteralPath $localProperties |
            Where-Object { $_ -match '^sdk\.dir=' } |
            Select-Object -First 1
        if ($null -ne $sdkLine) {
            $value = ($sdkLine -replace '^sdk\.dir=', '') -replace '\\:', ':' -replace '\\\\', '\'
            if (-not [string]::IsNullOrWhiteSpace($value)) { $sdkCandidates.Add($value.Trim()) }
        }
    }
    foreach ($environmentName in @("ANDROID_SDK_ROOT", "ANDROID_HOME")) {
        $value = [Environment]::GetEnvironmentVariable($environmentName)
        if (-not [string]::IsNullOrWhiteSpace($value)) { $sdkCandidates.Add($value.Trim()) }
    }
    foreach ($sdkRoot in @($sdkCandidates | Select-Object -Unique)) {
        if (-not (Test-Path -LiteralPath $sdkRoot -PathType Container)) { continue }
        $directories = @(Get-ChildItem -LiteralPath (Join-Path $sdkRoot "build-tools") -ErrorAction SilentlyContinue |
            Where-Object { $_.PSIsContainer } |
            Sort-Object { [version]($_.Name -replace '-.*$', '') } -Descending)
        foreach ($directory in $directories) {
            $candidate = Join-Path $directory.FullName "apksigner.bat"
            if (Test-Path -LiteralPath $candidate -PathType Leaf) { return $candidate }
        }
    }
    throw "Unable to resolve apksigner.bat from local.properties or Android SDK environment variables"
}

function Get-R6ProviderZipSnapshot {
    param([Parameter(Mandatory = $true)][string] $ApkPath)
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead($ApkPath)
    try {
        $entries = @($archive.Entries | ForEach-Object { $_.FullName.Replace('\', '/') })
        $assetHashes = [ordered]@{}
        foreach ($entryName in $requiredApkAssets.Keys) {
            $entry = $archive.GetEntry($entryName)
            Assert-R6ProviderCondition ($null -ne $entry) "Required release asset is absent from APK: $entryName"
            $algorithm = [System.Security.Cryptography.SHA256]::Create()
            try {
                $stream = $entry.Open()
                try {
                    $hash = $algorithm.ComputeHash($stream)
                    $assetHashes[$entryName] = (($hash | ForEach-Object { $_.ToString("x2") }) -join "")
                } finally {
                    $stream.Dispose()
                }
            } finally {
                $algorithm.Dispose()
            }
        }
        return [pscustomobject][ordered]@{
            entries = $entries
            assetHashes = $assetHashes
        }
    } finally {
        $archive.Dispose()
    }
}

function Get-R6ProviderGeneratedString {
    param(
        [Parameter(Mandatory = $true)][xml] $Resources,
        [Parameter(Mandatory = $true)][string] $Name
    )
    $node = @($Resources.resources.string | Where-Object { $_.name -eq $Name })
    Assert-R6ProviderCondition ($node.Count -eq 1) "Generated resource '$Name' must occur exactly once"
    return [string]$node[0].InnerText
}

Push-Location -LiteralPath $repository
try {
    Assert-R6ProviderCondition (
        -not (Test-Path -LiteralPath (Join-Path $repository "sign.properties"))
    ) "R6 Provider source preflight refuses to run while production signing material is present"

    $sourceRevision = ((& git rev-parse HEAD) -join "").Trim()
    Assert-R6ProviderCondition (
        $LASTEXITCODE -eq 0 -and $sourceRevision -match '^[0-9a-f]{40}$'
    ) "Unable to resolve Provider Git revision"
    $sourceStatus = @(& git status --porcelain=v1 --untracked-files=all | ForEach-Object { [string]$_ })
    if ($RequireClean) {
        Assert-R6ProviderCondition (
            $sourceStatus.Count -eq 0
        ) "Provider worktree must be clean when -RequireClean is used"
    }

    if (-not $SkipBuild) {
        $gradleArguments = @(
            "--no-daemon",
            ":app:testDebugUnitTest",
            ":app:assembleRelease",
            ":app:assembleRc"
        )
        Write-Host "> .\gradlew.bat $($gradleArguments -join ' ')"
        & .\gradlew.bat @gradleArguments
        Assert-R6ProviderCondition ($LASTEXITCODE -eq 0) "Focused Provider R6 Gradle build failed"
    }

    $releaseApk = Get-R6ProviderOnlyFile -Directory (
        Join-Path $repository "app/build/outputs/apk/release"
    ) -Filter "*.apk" -Label "release APK"
    $rcApk = Get-R6ProviderOnlyFile -Directory (
        Join-Path $repository "app/build/outputs/apk/rc"
    ) -Filter "*.apk" -Label "RC APK"

    $shrinkOutputs = @(
        (Get-R6ProviderRequiredFile (Join-Path $repository "app/build/outputs/mapping/release/mapping.txt") "release R8 mapping"),
        (Get-R6ProviderRequiredFile (Join-Path $repository "app/build/outputs/mapping/release/resources.txt") "release resource-shrinker report"),
        (Get-R6ProviderRequiredFile (Join-Path $repository "app/build/outputs/mapping/rc/mapping.txt") "RC R8 mapping"),
        (Get-R6ProviderRequiredFile (Join-Path $repository "app/build/outputs/mapping/rc/resources.txt") "RC resource-shrinker report")
    )
    foreach ($shrinkOutput in $shrinkOutputs) {
        Assert-R6ProviderCondition (
            (Get-Item -LiteralPath $shrinkOutput).Length -gt 0
        ) "Shrinker output is empty: $shrinkOutput"
    }

    $generatedResourcesPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/build/generated/res/resValues/release/values/gradleResValues.xml"
    ) "release generated resource values"
    [xml]$generatedResources = Get-Content -LiteralPath $generatedResourcesPath -Raw
    foreach ($entry in $expectedIdentity.GetEnumerator()) {
        $actual = Get-R6ProviderGeneratedString -Resources $generatedResources -Name $entry.Key
        Assert-R6ProviderCondition (
            $actual -ceq $entry.Value
        ) "Generated resource '$($entry.Key)' expected '$($entry.Value)' but was '$actual'"
    }

    $mergedManifestPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml"
    ) "release merged manifest"
    [xml]$mergedManifest = Get-Content -LiteralPath $mergedManifestPath -Raw
    $androidNamespace = "http://schemas.android.com/apk/res/android"
    $requiredServices = @(
        "$applicationId.YoloPluginInfoService",
        "$applicationId.provider.YoloProviderService"
    )
    foreach ($serviceName in $requiredServices) {
        $services = @($mergedManifest.manifest.application.service | Where-Object {
            $_.GetAttribute("name", $androidNamespace) -ceq $serviceName
        })
        Assert-R6ProviderCondition (
            $services.Count -eq 1
        ) "Merged manifest service missing or duplicated: $serviceName"
        $metadata = @($services[0].'meta-data' | Where-Object {
            $_.GetAttribute("name", $androidNamespace) -ceq "requiresHostVersion"
        })
        Assert-R6ProviderCondition (
            $metadata.Count -eq 1
        ) "Merged manifest requiresHostVersion missing or duplicated for $serviceName"
        Assert-R6ProviderCondition (
            $metadata[0].GetAttribute("value", $androidNamespace) -ceq "@string/plugin_requires_host_version"
        ) "Merged manifest requiresHostVersion must reference the offline-index resource for $serviceName"
    }

    $identitySourcePath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/src/main/java/io/github/supermonster003/autojs6/plugin/yolo/ncnn/YoloPlugin.kt"
    ) "runtime identity source"
    $identitySource = Get-Content -LiteralPath $identitySourcePath -Raw
    $hostVersionMatch = [regex]::Match(
        $identitySource,
        'const\s+val\s+REQUIRED_HOST_VERSION\s*=\s*([0-9_]+)L'
    )
    Assert-R6ProviderCondition $hostVersionMatch.Success "Runtime REQUIRED_HOST_VERSION is not a literal Long"
    $runtimeHostVersion = [long]($hostVersionMatch.Groups[1].Value -replace '_', '')
    Assert-R6ProviderCondition (
        $runtimeHostVersion -eq 5274L
    ) "Runtime minimum Host version must match release/index resource value 5274"

    $noticePath = Get-R6ProviderRequiredFile (Join-Path $repository "THIRD_PARTY_NOTICES.md") "notice index"
    $noticeAssetPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/src/main/assets/THIRD_PARTY_NOTICES.md"
    ) "packaged notice index"
    Assert-R6ProviderCondition (
        (Get-R6ProviderSha256 $noticePath) -ceq (Get-R6ProviderSha256 $noticeAssetPath)
    ) "Repository and APK-source third-party notice indexes differ"
    $modelPolicyPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "docs/model-license-policy.md"
    ) "model license policy"
    $releaseNotesPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "docs/release-notes/0.1.0.md"
    ) "release notes draft"

    $releaseZip = Get-R6ProviderZipSnapshot -ApkPath $releaseApk
    $rcZip = Get-R6ProviderZipSnapshot -ApkPath $rcApk
    foreach ($snapshot in @($releaseZip, $rcZip)) {
        $nativeEntries = @($snapshot.entries | Where-Object { $_ -match '^lib/' })
        Assert-R6ProviderCondition (
            $nativeEntries.Count -eq 1 -and $nativeEntries[0] -ceq $expectedNativeEntry
        ) "Provider APK must contain only $expectedNativeEntry"
        $forbiddenPayloads = @($snapshot.entries | Where-Object {
            $leaf = [System.IO.Path]::GetFileName($_).ToLowerInvariant()
            $knownModelLeaves -contains $leaf -or
                $_ -match '(?i)(^|/)(fixtures?|models?|datasets?|training)(/|$)' -or
                $_ -match '(?i)\.(pt|pth|onnx|tflite|weights|param|bin|jpg|jpeg|png|bmp|webp)$'
        })
        Assert-R6ProviderCondition (
            $forbiddenPayloads.Count -eq 0
        ) "Provider APK contains forbidden model/image payloads: $($forbiddenPayloads -join ', ')"
        foreach ($assetEntry in $requiredApkAssets.GetEnumerator()) {
            $sourceAsset = Get-R6ProviderRequiredFile (
                Join-Path $repository $assetEntry.Value
            ) "source for $($assetEntry.Key)"
            Assert-R6ProviderCondition (
                $snapshot.assetHashes[$assetEntry.Key] -ceq (Get-R6ProviderSha256 $sourceAsset)
            ) "Packaged asset differs from source: $($assetEntry.Key)"
        }
    }

    $apkSigner = Resolve-R6ProviderApkSigner
    $releaseSignature = Invoke-R6ProviderCapture -FilePath $apkSigner -Arguments @(
        "verify", "--verbose", "--print-certs", $releaseApk
    )
    Assert-R6ProviderCondition (
        $releaseSignature.exitCode -ne 0 -and
            (($releaseSignature.output -join [Environment]::NewLine) -match '(?i)DOES NOT VERIFY|not signed|Missing META-INF')
    ) "Local release APK must remain explicitly unsigned in the source preflight"

    $rcSignature = Invoke-R6ProviderCapture -FilePath $apkSigner -Arguments @(
        "verify", "--verbose", "--print-certs", $rcApk
    )
    Assert-R6ProviderCondition ($rcSignature.exitCode -eq 0) "TEST-SIGNED RC APK signature does not verify"
    $rcSignatureText = $rcSignature.output -join [Environment]::NewLine
    $signerCount = [regex]::Match($rcSignatureText, '(?im)^Number of signers:\s*([0-9]+)\s*$')
    Assert-R6ProviderCondition (
        $signerCount.Success -and [int]$signerCount.Groups[1].Value -eq 1
    ) "TEST-SIGNED RC APK must have exactly one signer"
    $signerDigest = [regex]::Match(
        $rcSignatureText,
        '(?im)^(?:Signer #[0-9]+|V[0-9.]+ Signer(?: #[0-9]+)?)\s*:?\s*certificate SHA-256 digest:\s*([0-9a-f]{64})\s*$'
    )
    Assert-R6ProviderCondition $signerDigest.Success "Unable to resolve RC signer SHA-256 digest"
    $rcSignerSha256 = $signerDigest.Groups[1].Value.ToLowerInvariant()

    $releaseMetadataPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/build/outputs/apk/release/output-metadata.json"
    ) "release APK metadata"
    $rcMetadataPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/build/outputs/apk/rc/output-metadata.json"
    ) "RC APK metadata"
    $releaseMetadata = Get-Content -LiteralPath $releaseMetadataPath -Raw | ConvertFrom-Json
    $rcMetadata = Get-Content -LiteralPath $rcMetadataPath -Raw | ConvertFrom-Json
    Assert-R6ProviderCondition (
        $releaseMetadata.applicationId -ceq $applicationId
    ) "Release application ID is not pinned"
    Assert-R6ProviderCondition (
        $rcMetadata.applicationId -ceq $applicationId
    ) "RC application ID is not pinned"
    Assert-R6ProviderCondition (
        [string]$rcMetadata.elements[0].versionName -match '-rc-test-signed$'
    ) "RC version name must identify the TEST-SIGNED channel"

    if ([string]::IsNullOrWhiteSpace($ReportPath)) {
        $ReportPath = Join-Path $repository "build/reports/yolo/r6-provider-source.generated.json"
    } elseif (-not [System.IO.Path]::IsPathRooted($ReportPath)) {
        $ReportPath = Join-Path $repository $ReportPath
    }
    $ReportPath = [System.IO.Path]::GetFullPath($ReportPath)
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $ReportPath) | Out-Null

    $report = [ordered]@{
        schemaVersion = 1
        gate = "yolo-r6-provider-source"
        result = "PASS"
        generatedAtUtc = [DateTime]::UtcNow.ToString("o")
        evidenceLevel = @("source", "jvm-test", "android-build", "apk-package")
        source = [ordered]@{
            repository = $repository
            revision = $sourceRevision
            dirty = $sourceStatus.Count -ne 0
        }
        identity = [ordered]@{
            applicationId = $applicationId
            pluginId = $expectedIdentity.plugin_id
            engine = $expectedIdentity.plugin_engine
            variant = $expectedIdentity.plugin_variant
            requiresHostVersion = 5274
        }
        release = [ordered]@{
            path = $releaseApk
            sha256 = Get-R6ProviderSha256 $releaseApk
            bytes = (Get-Item -LiteralPath $releaseApk).Length
            versionName = [string]$releaseMetadata.elements[0].versionName
            signed = $false
            minified = $true
            resourcesShrunk = $true
            abi = "arm64-v8a"
            modelOrImagePayloads = $false
        }
        rc = [ordered]@{
            path = $rcApk
            sha256 = Get-R6ProviderSha256 $rcApk
            bytes = (Get-Item -LiteralPath $rcApk).Length
            versionName = [string]$rcMetadata.elements[0].versionName
            signed = $true
            signingClass = "TEST_SIGNED"
            signerSha256 = $rcSignerSha256
            minified = $true
            resourcesShrunk = $true
            abi = "arm64-v8a"
            modelOrImagePayloads = $false
        }
        compliance = [ordered]@{
            thirdPartyNoticesSha256 = Get-R6ProviderSha256 $noticePath
            modelLicensePolicySha256 = Get-R6ProviderSha256 $modelPolicyPath
            releaseNotesDraftSha256 = Get-R6ProviderSha256 $releaseNotesPath
            ncnnVersion = "20260526"
            modelsPublished = $false
            validationImagesPublished = $false
        }
        exclusions = [ordered]@{
            productionSigned = $false
            publishable = $false
            published = $false
            deviceVerified = $false
            upgradeRollbackVerified = $false
        }
    }
    $report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $ReportPath -Encoding utf8NoBOM
    Write-Host "YOLO R6 Provider source preflight PASS"
    Write-Host "Report: $ReportPath"
} finally {
    Pop-Location
}
