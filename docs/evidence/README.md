# Runtime evidence

This directory contains sanitized, source-bound runtime receipts that are safe
to keep with the repository. Each receipt states its exact claim boundary. A
debug or instrumentation result is not production-signing, Host Binder/PFD
end-to-end, public-release, or unsupported-platform evidence unless the receipt
explicitly says otherwise.

Local model weights and validation images remain under the ignored
`fixtures/local/` tree. Evidence files record their lengths and SHA-256 digests,
not their payloads.
