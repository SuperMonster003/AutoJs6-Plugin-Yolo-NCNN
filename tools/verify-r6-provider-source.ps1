# R6 source/build/package preflight. It intentionally performs no ADB or production signing.
[CmdletBinding()]
param(
    [switch] $SkipBuild,
    [switch] $RequireClean,
    [string] $ReportPath = "",
    [switch] $SelfTestFailAfterReportInvalidation
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
if (Test-Path variable:PSNativeCommandUseErrorActionPreference) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repository = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot ".."))
$applicationId = "io.github.supermonster003.autojs6.plugin.yolo.ncnn"
$runtimeService = "$applicationId.provider.YoloProviderService"
$runtimeComponent = "$applicationId/$runtimeService"
$expectedIdentity = [ordered]@{
    plugin_id = "yolo-ncnn"
    plugin_engine = "yolo"
    plugin_variant = "ncnn"
    plugin_requires_host_version = "5275"
    plugin_runtime_component = $runtimeComponent
    plugin_protocol_api_min = "1.0"
    plugin_protocol_api_max = "1.0"
    plugin_backend = "ncnn"
    plugin_task = "detect"
    plugin_decoder = "ultralytics-detect"
    plugin_supported_abis = "arm64-v8a"
}
$expectedManifestContract = [ordered]@{
    "requiresHostVersion" = $expectedIdentity.plugin_requires_host_version
    "org.autojs.plugin.contract.RUNTIME_COMPONENT" = "@string/plugin_runtime_component"
    "org.autojs.plugin.contract.PROTOCOL_API_MIN" = "@string/plugin_protocol_api_min"
    "org.autojs.plugin.contract.PROTOCOL_API_MAX" = "@string/plugin_protocol_api_max"
    "org.autojs.plugin.contract.BACKEND" = "@string/plugin_backend"
    "org.autojs.plugin.contract.TASK" = "@string/plugin_task"
    "org.autojs.plugin.contract.DECODER" = "@string/plugin_decoder"
    "org.autojs.plugin.contract.SUPPORTED_ABIS" = "@string/plugin_supported_abis"
}
$expectedRuntimeContractConstants = [ordered]@{
    RELEASE_RUNTIME_SERVICE = $runtimeService
    RELEASE_RUNTIME_COMPONENT = $runtimeComponent
    RELEASE_PROTOCOL_API_MIN = $expectedIdentity.plugin_protocol_api_min
    RELEASE_PROTOCOL_API_MAX = $expectedIdentity.plugin_protocol_api_max
    RELEASE_BACKEND = $expectedIdentity.plugin_backend
    RELEASE_TASK = $expectedIdentity.plugin_task
    RELEASE_DECODER = $expectedIdentity.plugin_decoder
    RELEASE_SUPPORTED_ABI = $expectedIdentity.plugin_supported_abis
}
$expectedNativeEntry = "lib/arm64-v8a/libautojs_yolo.so"
$expectedApache20Length = 11358L
$expectedApache20Sha256 = "cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30"
$expectedProtocolHandoff = [ordered]@{
    sourceRevision = "7c48add4a5a77efcee7a0fa782749312d3eee5f1"
    sourceSnapshotSha256 = "4cd47305c70b5c1533fbb16efe85bd9c572e571d05b841770976ee3593dd9174"
    artifacts = [ordered]@{
        "common-plugin-api.aar" = [ordered]@{
            length = 10882L
            sha256 = "d745bb24d6a6995e68ebea27d592faec162f0bad4f9bf70cb038a480ad8c6df2"
        }
        "protocol-wire-api.aar" = [ordered]@{
            length = 30717L
            sha256 = "6c597ec095852eac7e6277574191141100dc96b6c5a803725ed540f42aa7d871"
        }
        "yolo-api.aar" = [ordered]@{
            length = 129532L
            sha256 = "ce9763faa62977ef90af65ca42cf96670a8d59e68a057e309b92e9615ba06a37"
        }
    }
}
$requiredApkAssets = [ordered]@{
    "assets/THIRD_PARTY_NOTICES.md" = "app/src/main/assets/THIRD_PARTY_NOTICES.md"
    "assets/licenses/Apache-2.0.txt" = "app/src/main/assets/licenses/Apache-2.0.txt"
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
$expectedTestSuites = [ordered]@{
    "io.github.supermonster003.autojs6.plugin.yolo.ncnn.YoloPluginIdentityTest" = @(
        "releaseIdentityRemainsPinnedForOfflineIndexGeneration"
    )
    "io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider.StaleModelSessionCleanupTest" = @(
        "failsClosedWhenAStaleEntryCannotBeDeleted",
        "refusesSymbolicEntryWithoutTouchingAnythingOutsideTheRoot",
        "removesNestedCrashResidueAndKeepsOnlyTheFixedRoot"
    )
    "io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider.YoloProviderDiagnosticsTest" = @(
        "nativeHandleCounterTracksCreateAndDestroy",
        "nativeHandleCounterDecrementsWhenDestroyThrows",
        "nativeHandleCounterRejectsUnderflowWithoutMutation",
        "formatterIsOneDeterministicNonSensitiveJsonLine"
    )
    "io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider.YoloProviderServiceErrorsTest" = @(
        "capabilityAndProtocolIncompatibilityKeepStableOpenFailureCodes"
    )
}

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

function Get-R6ProviderProtocolAarHandoff {
    $lockPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "libs/protocol-aars.lock.json"
    ) "protocol AAR handoff lock"
    $readmePath = Get-R6ProviderRequiredFile (
        Join-Path $repository "libs/README.md"
    ) "protocol AAR handoff README"
    $lock = Get-Content -LiteralPath $lockPath -Raw | ConvertFrom-Json

    Assert-R6ProviderCondition ([int]$lock.schemaVersion -eq 1) "Protocol AAR lock schemaVersion must be 1"
    Assert-R6ProviderCondition ([string]$lock.state -ceq "staged") "Protocol AAR lock state must be staged"
    Assert-R6ProviderCondition (
        [string]$lock.sourceRevision -ceq $expectedProtocolHandoff.sourceRevision
    ) "Protocol AAR lock sourceRevision differs from the exact frozen source commit"
    Assert-R6ProviderCondition (
        [string]$lock.sourceSnapshotSha256 -ceq $expectedProtocolHandoff.sourceSnapshotSha256
    ) "Protocol AAR lock source snapshot SHA-256 differs"

    $readmeText = Get-Content -LiteralPath $readmePath -Raw
    Assert-R6ProviderCondition (
        [regex]::Matches($readmeText, [regex]::Escape($expectedProtocolHandoff.sourceRevision)).Count -eq 1
    ) "Protocol AAR README must identify the exact frozen source commit once"
    Assert-R6ProviderCondition (
        [regex]::Matches($readmeText, [regex]::Escape($expectedProtocolHandoff.sourceSnapshotSha256)).Count -eq 1
    ) "Protocol AAR README must identify the corroborating source snapshot once"
    Assert-R6ProviderCondition (
        $readmeText -cmatch 'exact frozen AutoJs6 source commit' -and
            $readmeText -cmatch 'corroborates that source identity'
    ) "Protocol AAR README must describe exact-source and snapshot-corroboration semantics"

    $lockArtifacts = @($lock.artifacts)
    Assert-R6ProviderCondition (
        $lockArtifacts.Count -eq $expectedProtocolHandoff.artifacts.Count
    ) "Protocol AAR lock must contain exactly three artifacts"
    $seenNames = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $artifactReceipts = [System.Collections.Generic.List[object]]::new()
    foreach ($entry in $lockArtifacts) {
        $name = [string]$entry.name
        Assert-R6ProviderCondition ($seenNames.Add($name)) "Protocol AAR lock contains a duplicate artifact: $name"
        Assert-R6ProviderCondition (
            $expectedProtocolHandoff.artifacts.Contains($name)
        ) "Protocol AAR lock contains an unexpected artifact: $name"
        $expected = $expectedProtocolHandoff.artifacts[$name]
        $lockedSha256 = ([string]$entry.sha256).ToLowerInvariant()
        Assert-R6ProviderCondition (
            [long]$entry.length -eq [long]$expected.length
        ) "Protocol AAR lock length differs for $name"
        Assert-R6ProviderCondition (
            $lockedSha256 -ceq [string]$expected.sha256
        ) "Protocol AAR lock SHA-256 differs for $name"

        $artifactPath = Get-R6ProviderRequiredFile (
            Join-Path $repository "libs/$name"
        ) "locked protocol AAR $name"
        $actualLength = (Get-Item -LiteralPath $artifactPath).Length
        $actualSha256 = Get-R6ProviderSha256 $artifactPath
        Assert-R6ProviderCondition (
            $actualLength -eq [long]$entry.length
        ) "Protocol AAR byte length does not match the lock for $name"
        Assert-R6ProviderCondition (
            $actualSha256 -ceq $lockedSha256
        ) "Protocol AAR SHA-256 does not match the lock for $name"
        $artifactReceipts.Add([ordered]@{
            name = $name
            path = $artifactPath
            length = $actualLength
            sha256 = $actualSha256
        })
    }
    foreach ($name in $expectedProtocolHandoff.artifacts.Keys) {
        Assert-R6ProviderCondition ($seenNames.Contains($name)) "Protocol AAR lock is missing $name"
    }

    return [pscustomobject][ordered]@{
        status = "PASS"
        lockPath = $lockPath
        lockSha256 = Get-R6ProviderSha256 $lockPath
        readmePath = $readmePath
        readmeSha256 = Get-R6ProviderSha256 $readmePath
        sourceRevision = [string]$lock.sourceRevision
        sourceSnapshotSha256 = [string]$lock.sourceSnapshotSha256
        artifacts = @($artifactReceipts)
    }
}

function Resolve-R6ProviderReportPath {
    param([string] $RequestedPath)
    $reportRoot = [System.IO.Path]::GetFullPath((Join-Path $repository "build/reports/yolo"))
    $candidate = if ([string]::IsNullOrWhiteSpace($RequestedPath)) {
        Join-Path $reportRoot "r6-provider-source.generated.json"
    } elseif ([System.IO.Path]::IsPathRooted($RequestedPath)) {
        $RequestedPath
    } else {
        Join-Path $repository $RequestedPath
    }
    $candidate = [System.IO.Path]::GetFullPath($candidate)
    $rootPrefix = $reportRoot.TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar
    ) + [System.IO.Path]::DirectorySeparatorChar
    Assert-R6ProviderCondition (
        $candidate.StartsWith($rootPrefix, [System.StringComparison]::OrdinalIgnoreCase)
    ) "R6 Provider report must remain under the fixed build/reports/yolo directory"
    Assert-R6ProviderCondition (
        [System.IO.Path]::GetExtension($candidate) -ceq ".json"
    ) "R6 Provider report must be a JSON file"
    return $candidate
}

function Assert-R6ProviderRetainedReport {
    param(
        [Parameter(Mandatory = $true)][string] $Path,
        [Parameter(Mandatory = $true)][string] $ExpectedResult,
        [Parameter(Mandatory = $true)][bool] $ExpectedBuildIdentity
    )
    $parsed = Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
    Assert-R6ProviderCondition ([int]$parsed.schemaVersion -eq 2) "Retained Provider report schema differs"
    Assert-R6ProviderCondition ([string]$parsed.gate -ceq "yolo-r6-provider-source") "Retained Provider report gate differs"
    Assert-R6ProviderCondition ([string]$parsed.result -ceq $ExpectedResult) "Retained Provider report result differs"
    Assert-R6ProviderCondition ([bool]$parsed.buildIdentityProven -eq $ExpectedBuildIdentity) (
        "Retained Provider report build-identity boundary differs"
    )
    Assert-R6ProviderCondition (
        [string]$parsed.protocolAarHandoff.status -ceq "PASS" -and
            [string]$parsed.protocolAarHandoff.sourceRevision -ceq $expectedProtocolHandoff.sourceRevision -and
            [string]$parsed.protocolAarHandoff.sourceSnapshotSha256 -ceq $expectedProtocolHandoff.sourceSnapshotSha256 -and
            @($parsed.protocolAarHandoff.artifacts).Count -eq 3
    ) "Retained Provider report protocol AAR handoff differs"
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
        $assetEntries = @($entries | Where-Object {
            $_.StartsWith("assets/", [StringComparison]::Ordinal) -and -not $_.EndsWith("/", [StringComparison]::Ordinal)
        } | Sort-Object)
        $rawMarkdownEntries = @($archive.Entries | Where-Object {
            $_.FullName.Replace('\', '/') -match '^res/[^/]+\.md$'
        })
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
        $rawMarkdownHash = $null
        if ($rawMarkdownEntries.Count -eq 1) {
            $algorithm = [System.Security.Cryptography.SHA256]::Create()
            try {
                $stream = $rawMarkdownEntries[0].Open()
                try {
                    $rawMarkdownHash = (($algorithm.ComputeHash($stream) | ForEach-Object {
                        $_.ToString("x2")
                    }) -join "")
                } finally {
                    $stream.Dispose()
                }
            } finally {
                $algorithm.Dispose()
            }
        }
        return [pscustomobject][ordered]@{
            entries = $entries
            assetEntries = $assetEntries
            assetHashes = $assetHashes
            rawMarkdownEntries = @($rawMarkdownEntries | ForEach-Object { $_.FullName.Replace('\', '/') })
            rawMarkdownSha256 = $rawMarkdownHash
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

function Get-R6ProviderGitSnapshot {
    $revision = ((& git rev-parse HEAD) -join "").Trim()
    Assert-R6ProviderCondition (
        $LASTEXITCODE -eq 0 -and $revision -match '^[0-9a-f]{40}$'
    ) "Unable to resolve Provider Git revision"
    $status = @(& git status --porcelain=v1 --untracked-files=all | ForEach-Object { [string]$_ })
    return [pscustomobject][ordered]@{
        revision = $revision
        status = $status
        statusText = $status -join [Environment]::NewLine
        clean = $status.Count -eq 0
    }
}

function Get-R6ProviderArtifactRecord {
    param(
        [Parameter(Mandatory = $true)][string] $Path,
        [Parameter(Mandatory = $true)][string] $Label,
        [Nullable[DateTime]] $BuildStartedUtc,
        [Parameter(Mandatory = $true)][bool] $BuildIdentityProven
    )
    $fullPath = Get-R6ProviderRequiredFile $Path $Label
    $item = Get-Item -LiteralPath $fullPath
    $freshAfterBuildStart = if ($BuildIdentityProven) {
        $item.LastWriteTimeUtc -ge $BuildStartedUtc
    } else {
        $null
    }
    if ($BuildIdentityProven) {
        Assert-R6ProviderCondition $freshAfterBuildStart "$Label was not freshly produced after build start: $fullPath"
    }
    return [pscustomobject][ordered]@{
        path = $fullPath
        bytes = $item.Length
        sha256 = Get-R6ProviderSha256 $fullPath
        lastWriteUtc = $item.LastWriteTimeUtc.ToString("o")
        freshAfterBuildStart = $freshAfterBuildStart
    }
}

function Get-R6ProviderTestReceipt {
    param(
        [Parameter(Mandatory = $true)][DateTime] $BuildStartedUtc,
        [Parameter(Mandatory = $true)][bool] $BuildIdentityProven
    )
    if (-not $BuildIdentityProven) {
        return [pscustomobject][ordered]@{
            status = "NOT_EVALUATED"
            suites = @()
            totalTests = $null
            totalFailures = $null
            totalErrors = $null
            totalSkipped = $null
        }
    }

    $testRoot = Join-Path $repository "app/build/test-results/testDebugUnitTest"
    $xmlFiles = @(Get-ChildItem -LiteralPath $testRoot -Filter "TEST-*.xml" -ErrorAction SilentlyContinue |
        Where-Object { -not $_.PSIsContainer } |
        Sort-Object Name)
    Assert-R6ProviderCondition (
        $xmlFiles.Count -eq $expectedTestSuites.Count
    ) "Expected exactly $($expectedTestSuites.Count) JVM test XML files; found $($xmlFiles.Count)"

    $suiteReceipts = [System.Collections.Generic.List[object]]::new()
    $seenSuites = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $totalTests = 0
    $totalFailures = 0
    $totalErrors = 0
    $totalSkipped = 0
    foreach ($xmlFile in $xmlFiles) {
        Assert-R6ProviderCondition (
            $xmlFile.LastWriteTimeUtc -ge $BuildStartedUtc
        ) "JVM test XML was not freshly produced after build start: $($xmlFile.FullName)"
        [xml]$document = Get-Content -LiteralPath $xmlFile.FullName -Raw
        $suite = $document.testsuite
        $suiteName = [string]$suite.name
        Assert-R6ProviderCondition (
            $expectedTestSuites.Contains($suiteName)
        ) "Unexpected JVM test suite: $suiteName"
        Assert-R6ProviderCondition (
            $seenSuites.Add($suiteName)
        ) "Duplicate JVM test suite: $suiteName"

        $testNames = @($suite.testcase | ForEach-Object { [string]$_.name } | Sort-Object)
        $expectedNames = @($expectedTestSuites[$suiteName] | Sort-Object)
        Assert-R6ProviderCondition (
            ($testNames -join "`n") -ceq ($expectedNames -join "`n")
        ) "JVM test cases differ for suite $suiteName"

        $tests = [int]$suite.tests
        $failures = [int]$suite.failures
        $errors = [int]$suite.errors
        $skipped = [int]$suite.skipped
        Assert-R6ProviderCondition (
            $tests -eq $expectedNames.Count -and $failures -eq 0 -and $errors -eq 0 -and $skipped -eq 0
        ) "JVM test suite is not an exact clean pass: $suiteName"
        $totalTests += $tests
        $totalFailures += $failures
        $totalErrors += $errors
        $totalSkipped += $skipped
        $suiteReceipts.Add([pscustomobject][ordered]@{
            name = $suiteName
            tests = $tests
            failures = $failures
            errors = $errors
            skipped = $skipped
            testCases = $testNames
            xmlPath = $xmlFile.FullName
            xmlSha256 = Get-R6ProviderSha256 $xmlFile.FullName
            xmlLastWriteUtc = $xmlFile.LastWriteTimeUtc.ToString("o")
        })
    }
    Assert-R6ProviderCondition (
        $seenSuites.Count -eq $expectedTestSuites.Count
    ) "One or more expected JVM test suites were not observed"
    return [pscustomobject][ordered]@{
        status = "PASS"
        suites = @($suiteReceipts)
        totalTests = $totalTests
        totalFailures = $totalFailures
        totalErrors = $totalErrors
        totalSkipped = $totalSkipped
    }
}

$gateLock = $null
$resolvedReportPath = $null
$pendingReportPath = $null
$bodyCompleted = $false
Push-Location -LiteralPath $repository
try {
    $gateLockPath = Join-Path $repository "build/locks/yolo-r6-provider-source.lock"
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $gateLockPath) | Out-Null
    try {
        $gateLock = [System.IO.File]::Open(
            $gateLockPath,
            [System.IO.FileMode]::OpenOrCreate,
            [System.IO.FileAccess]::ReadWrite,
            [System.IO.FileShare]::None
        )
    } catch [System.IO.IOException] {
        throw "Another YOLO R6 Provider gate already holds the build lock: $gateLockPath"
    }

    # Invalidate an older receipt only after acquiring this gate's own lock. A contender that
    # fails to acquire the lock must not delete the active holder's output.
    $resolvedReportPath = Resolve-R6ProviderReportPath -RequestedPath $ReportPath
    if (Test-Path -LiteralPath $resolvedReportPath) {
        Assert-R6ProviderCondition (
            Test-Path -LiteralPath $resolvedReportPath -PathType Leaf
        ) "R6 Provider report path is not a file: $resolvedReportPath"
        Remove-Item -LiteralPath $resolvedReportPath -Force
    }
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $resolvedReportPath) | Out-Null
    $ReportPath = $resolvedReportPath
    if ($SelfTestFailAfterReportInvalidation) {
        throw "Intentional R6 Provider receipt invalidation self-test failure"
    }

    Assert-R6ProviderCondition (
        -not (Test-Path -LiteralPath (Join-Path $repository "sign.properties"))
    ) "R6 Provider source preflight refuses to run while production signing material is present"

    $protocolAarHandoff = Get-R6ProviderProtocolAarHandoff
    $preBuildGit = Get-R6ProviderGitSnapshot
    $gradleExecutable = ".\gradlew.bat"
    $gradleArguments = @(
        "--no-daemon",
        ":app:clean",
        ":app:testDebugUnitTest",
        ":app:assembleRelease",
        ":app:assembleRc"
    )
    $buildStartedUtc = $null
    $buildFinishedUtc = $null
    $buildIdentityProven = $false

    if ($RequireClean) {
        Assert-R6ProviderCondition (
            $preBuildGit.clean
        ) "Provider worktree must be clean when -RequireClean is used"
    }

    if (-not $SkipBuild) {
        Assert-R6ProviderCondition (
            $preBuildGit.clean
        ) "A full R6 Provider source gate requires a clean worktree before build"
        $buildStartedUtc = [DateTime]::UtcNow
        Write-Host "> $gradleExecutable $($gradleArguments -join ' ')"
        & $gradleExecutable @gradleArguments
        $buildFinishedUtc = [DateTime]::UtcNow
        Assert-R6ProviderCondition ($LASTEXITCODE -eq 0) "Focused Provider R6 Gradle build failed"
        $postBuildGit = Get-R6ProviderGitSnapshot
        Assert-R6ProviderCondition (
            $postBuildGit.revision -ceq $preBuildGit.revision
        ) "Provider HEAD changed during the R6 build"
        Assert-R6ProviderCondition (
            $postBuildGit.statusText -ceq $preBuildGit.statusText -and $postBuildGit.clean
        ) "Provider worktree status changed or became dirty during the R6 build"
        $buildIdentityProven = $true
    } else {
        $postBuildGit = Get-R6ProviderGitSnapshot
    }

    $releaseApk = Get-R6ProviderOnlyFile -Directory (
        Join-Path $repository "app/build/outputs/apk/release"
    ) -Filter "*.apk" -Label "release APK"
    $rcApk = Get-R6ProviderOnlyFile -Directory (
        Join-Path $repository "app/build/outputs/apk/rc"
    ) -Filter "*.apk" -Label "RC APK"

    $artifactBuildStart = if ($buildIdentityProven) { [Nullable[DateTime]]$buildStartedUtc } else { $null }
    $releaseApkRecord = Get-R6ProviderArtifactRecord $releaseApk "release APK" $artifactBuildStart $buildIdentityProven
    $rcApkRecord = Get-R6ProviderArtifactRecord $rcApk "RC APK" $artifactBuildStart $buildIdentityProven
    $releaseMappingRecord = Get-R6ProviderArtifactRecord (
        Join-Path $repository "app/build/outputs/mapping/release/mapping.txt"
    ) "release R8 mapping" $artifactBuildStart $buildIdentityProven
    $releaseResourcesRecord = Get-R6ProviderArtifactRecord (
        Join-Path $repository "app/build/outputs/mapping/release/resources.txt"
    ) "release resource-shrinker report" $artifactBuildStart $buildIdentityProven
    $rcMappingRecord = Get-R6ProviderArtifactRecord (
        Join-Path $repository "app/build/outputs/mapping/rc/mapping.txt"
    ) "RC R8 mapping" $artifactBuildStart $buildIdentityProven
    $rcResourcesRecord = Get-R6ProviderArtifactRecord (
        Join-Path $repository "app/build/outputs/mapping/rc/resources.txt"
    ) "RC resource-shrinker report" $artifactBuildStart $buildIdentityProven
    foreach ($shrinkRecord in @(
        $releaseMappingRecord,
        $releaseResourcesRecord,
        $rcMappingRecord,
        $rcResourcesRecord
    )) {
        Assert-R6ProviderCondition (
            $shrinkRecord.bytes -gt 0
        ) "Shrinker output is empty: $($shrinkRecord.path)"
    }
    $testBuildStartedUtc = if ($buildIdentityProven) { $buildStartedUtc } else { [DateTime]::MinValue }
    $testReceipt = Get-R6ProviderTestReceipt `
        -BuildStartedUtc $testBuildStartedUtc `
        -BuildIdentityProven $buildIdentityProven

    $generatedResourcesRecord = Get-R6ProviderArtifactRecord (
        Join-Path $repository "app/build/generated/res/resValues/release/values/gradleResValues.xml"
    ) "release generated resource values" $artifactBuildStart $buildIdentityProven
    $generatedResourcesPath = $generatedResourcesRecord.path
    [xml]$generatedResources = Get-Content -LiteralPath $generatedResourcesPath -Raw
    foreach ($entry in $expectedIdentity.GetEnumerator()) {
        $actual = Get-R6ProviderGeneratedString -Resources $generatedResources -Name $entry.Key
        Assert-R6ProviderCondition (
            $actual -ceq $entry.Value
        ) "Generated resource '$($entry.Key)' expected '$($entry.Value)' but was '$actual'"
    }
    $buildGradlePath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/build.gradle.kts"
    ) "Provider Gradle release metadata source"
    $buildGradleText = Get-Content -LiteralPath $buildGradlePath -Raw
    foreach ($entry in $expectedIdentity.GetEnumerator()) {
        $literalPattern = 'resValue\(\s*"string"\s*,\s*"' +
            [regex]::Escape($entry.Key) + '"\s*,\s*"' +
            [regex]::Escape($entry.Value) + '"\s*\)'
        Assert-R6ProviderCondition (
            [regex]::Matches($buildGradleText, $literalPattern).Count -eq 1
        ) "Index-consumed literal resValue '$($entry.Key)' is missing or duplicated"
    }

    $mergedManifestRecord = Get-R6ProviderArtifactRecord (
        Join-Path $repository "app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml"
    ) "release merged manifest" $artifactBuildStart $buildIdentityProven
    $mergedManifestPath = $mergedManifestRecord.path
    [xml]$mergedManifest = Get-Content -LiteralPath $mergedManifestPath -Raw
    $androidNamespace = "http://schemas.android.com/apk/res/android"
    $requiredServices = @(
        "$applicationId.YoloPluginInfoService",
        $runtimeService
    )
    foreach ($serviceName in $requiredServices) {
        $services = @($mergedManifest.manifest.application.service | Where-Object {
            $_.GetAttribute("name", $androidNamespace) -ceq $serviceName
        })
        Assert-R6ProviderCondition (
            $services.Count -eq 1
        ) "Merged manifest service missing or duplicated: $serviceName"
        $actualMetadataNames = @($services[0].'meta-data' | ForEach-Object {
            $_.GetAttribute("name", $androidNamespace)
        } | Sort-Object)
        $expectedMetadataNames = @($expectedManifestContract.Keys | Sort-Object)
        Assert-R6ProviderCondition (
            ($actualMetadataNames -join "`n") -ceq ($expectedMetadataNames -join "`n")
        ) "Merged manifest contract metadata set differs for $serviceName"
        foreach ($contractEntry in $expectedManifestContract.GetEnumerator()) {
            $metadata = @($services[0].'meta-data' | Where-Object {
                $_.GetAttribute("name", $androidNamespace) -ceq $contractEntry.Key
            })
            Assert-R6ProviderCondition (
                $metadata.Count -eq 1
            ) "Merged manifest metadata '$($contractEntry.Key)' missing or duplicated for $serviceName"
            $actual = $metadata[0].GetAttribute("value", $androidNamespace)
            Assert-R6ProviderCondition (
                $actual -ceq $contractEntry.Value
            ) "Merged manifest metadata '$($contractEntry.Key)' expected '$($contractEntry.Value)' but was '$actual' for $serviceName"
        }
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
        $runtimeHostVersion -eq 5275L
    ) "Runtime minimum Host version must match release/index resource value 5275"
    foreach ($contractEntry in $expectedRuntimeContractConstants.GetEnumerator()) {
        $constantMatch = [regex]::Match(
            $identitySource,
            "const\s+val\s+$([regex]::Escape($contractEntry.Key))\s*=\s*`"([^`"]+)`"(?:\s*\+\s*`"([^`"]+)`")?"
        )
        Assert-R6ProviderCondition $constantMatch.Success "Runtime release contract constant is not a literal: $($contractEntry.Key)"
        $actual = $constantMatch.Groups[1].Value + $constantMatch.Groups[2].Value
        Assert-R6ProviderCondition (
            $actual -ceq $contractEntry.Value
        ) "Runtime release contract '$($contractEntry.Key)' expected '$($contractEntry.Value)' but was '$actual'"
    }

    $noticePath = Get-R6ProviderRequiredFile (Join-Path $repository "THIRD_PARTY_NOTICES.md") "notice index"
    $noticeAssetPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/src/main/assets/THIRD_PARTY_NOTICES.md"
    ) "packaged notice index"
    Assert-R6ProviderCondition (
        (Get-R6ProviderSha256 $noticePath) -ceq (Get-R6ProviderSha256 $noticeAssetPath)
    ) "Repository and APK-source third-party notice indexes differ"
    $noticeText = Get-Content -LiteralPath $noticePath -Raw
    Assert-R6ProviderCondition (
        -not [string]::IsNullOrWhiteSpace($noticeText) -and
            $noticeText -cmatch 'Apache-2\.0' -and
            $noticeText -cmatch 'assets/licenses/Apache-2\.0\.txt' -and
            $noticeText -cmatch 'MPL-2\.0' -and
            $noticeText -cmatch 'NCNN' -and
            $noticeText -cmatch 'BSD-3-Clause' -and
            $noticeText -cmatch '(?is)model\s+weights.*not\s+distributed' -and
            $noticeText -cmatch '(?i)users\s+are\s+responsible'
    ) "Third-party notice must identify the packaged Apache-2.0 text, MPL-2.0, NCNN BSD-3-Clause, model non-distribution, and user responsibility"
    $apache20Path = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/src/main/assets/licenses/Apache-2.0.txt"
    ) "packaged Apache-2.0 license text"
    Assert-R6ProviderCondition (
        (Get-Item -LiteralPath $apache20Path).Length -eq $expectedApache20Length -and
            (Get-R6ProviderSha256 $apache20Path) -ceq $expectedApache20Sha256
    ) "Packaged Apache-2.0 text must be the complete canonical 11358-byte LF text"
    $modelPolicyPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "docs/model-license-policy.md"
    ) "model license policy"
    $releaseNotesPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "docs/release-notes/0.1.0.md"
    ) "release notes draft"
    $releaseNotesText = Get-Content -LiteralPath $releaseNotesPath -Raw
    foreach ($releaseBoundaryPattern in @(
        'Provider version code: `2`; this is the first release',
        'No version code `1`\s+predecessor is produced or retained',
        '`UPGRADE_RUNTIME` and `VERSION_ROLLBACK` are\s+`NOT_RUN_BY_PRODUCT_DECISION`',
        'same-version recovery, not rollback',
        'forward-fix version code `3`',
        '`NATIVE_LOAD_16K_DEVICE=NOT_RUN_NO_16K_DEVICE`',
        '`API36_ARM64_RUNTIME=NOT_RUN_NO_AVAILABLE_ENVIRONMENT`',
        'neither is a first-release\s+publication gate',
        'complete Apache-2\.0 text for the\s+Kotlin runtime'
    )) {
        Assert-R6ProviderCondition (
            $releaseNotesText -cmatch $releaseBoundaryPattern
        ) "Release notes draft is missing the first-release boundary: $releaseBoundaryPattern"
    }
    $pluginInstructionPath = Get-R6ProviderRequiredFile (
        Join-Path $repository "app/src/main/res/raw/plugin_instruction.md"
    ) "plugin instruction source"

    $releaseZip = Get-R6ProviderZipSnapshot -ApkPath $releaseApk
    $rcZip = Get-R6ProviderZipSnapshot -ApkPath $rcApk
    foreach ($snapshot in @($releaseZip, $rcZip)) {
        $expectedAssetEntries = @($requiredApkAssets.Keys | Sort-Object)
        Assert-R6ProviderCondition (
            ($snapshot.assetEntries -join "`n") -ceq ($expectedAssetEntries -join "`n")
        ) "Provider APK assets must exactly match the five-entry release allowlist; found: $($snapshot.assetEntries -join ', ')"
        Assert-R6ProviderCondition (
            $snapshot.rawMarkdownEntries.Count -eq 1 -and
                $snapshot.rawMarkdownSha256 -ceq (Get-R6ProviderSha256 $pluginInstructionPath)
        ) "Provider APK must contain exactly one dynamically named compiled raw Markdown entry matching plugin_instruction.md"
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

    $releaseMetadataRecord = Get-R6ProviderArtifactRecord (
        Join-Path $repository "app/build/outputs/apk/release/output-metadata.json"
    ) "release APK metadata" $artifactBuildStart $buildIdentityProven
    $releaseMetadataPath = $releaseMetadataRecord.path
    $rcMetadataRecord = Get-R6ProviderArtifactRecord (
        Join-Path $repository "app/build/outputs/apk/rc/output-metadata.json"
    ) "RC APK metadata" $artifactBuildStart $buildIdentityProven
    $rcMetadataPath = $rcMetadataRecord.path
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

    $report = [ordered]@{
        schemaVersion = 2
        gate = "yolo-r6-provider-source"
        mode = if ($buildIdentityProven) { "FULL_CLEAN_BUILD" } else { "EXISTING_ARTIFACT_DIAGNOSTIC" }
        result = if ($buildIdentityProven) { "PASS" } else { "DIAGNOSTIC_COMPLETE" }
        generatedAtUtc = [DateTime]::UtcNow.ToString("o")
        evidenceLevel = if ($buildIdentityProven) {
            @("SOURCE", "JVM_TEST", "ANDROID_BUILD", "APK_PACKAGE")
        } else {
            @("SOURCE_STATIC", "EXISTING_ARTIFACT_DIAGNOSTIC")
        }
        buildIdentityProven = $buildIdentityProven
        source = [ordered]@{
            repository = $repository
            preBuild = [ordered]@{
                revision = $preBuildGit.revision
                clean = $preBuildGit.clean
                status = @($preBuildGit.status)
            }
            postBuild = [ordered]@{
                revision = $postBuildGit.revision
                clean = $postBuildGit.clean
                status = @($postBuildGit.status)
            }
            unchangedAcrossBuild = if ($buildIdentityProven) {
                $preBuildGit.revision -ceq $postBuildGit.revision -and
                    $preBuildGit.statusText -ceq $postBuildGit.statusText
            } else {
                $null
            }
        }
        build = [ordered]@{
            status = if ($buildIdentityProven) { "PASS" } else { "NOT_RUN" }
            startedAtUtc = if ($null -ne $buildStartedUtc) { $buildStartedUtc.ToString("o") } else { $null }
            finishedAtUtc = if ($null -ne $buildFinishedUtc) { $buildFinishedUtc.ToString("o") } else { $null }
            executable = if ($buildIdentityProven) { $gradleExecutable } else { $null }
            arguments = if ($buildIdentityProven) { @($gradleArguments) } else { @() }
            invocation = if ($buildIdentityProven) {
                "$gradleExecutable $($gradleArguments -join ' ')"
            } else {
                $null
            }
            cleanTaskIncluded = if ($buildIdentityProven) { $true } else { $null }
        }
        tests = $testReceipt
        protocolAarHandoff = $protocolAarHandoff
        buildArtifacts = [ordered]@{
            generatedResources = $generatedResourcesRecord
            mergedManifest = $mergedManifestRecord
            releaseMetadata = $releaseMetadataRecord
            rcMetadata = $rcMetadataRecord
        }
        identity = [ordered]@{
            applicationId = $applicationId
            pluginId = $expectedIdentity.plugin_id
            engine = $expectedIdentity.plugin_engine
            variant = $expectedIdentity.plugin_variant
            requiresHostVersion = 5275
            runtimeComponent = $expectedIdentity.plugin_runtime_component
            protocolApiMin = $expectedIdentity.plugin_protocol_api_min
            protocolApiMax = $expectedIdentity.plugin_protocol_api_max
            backend = $expectedIdentity.plugin_backend
            task = $expectedIdentity.plugin_task
            decoder = $expectedIdentity.plugin_decoder
            supportedAbis = @($expectedIdentity.plugin_supported_abis)
        }
        release = [ordered]@{
            artifact = $releaseApkRecord
            versionName = [string]$releaseMetadata.elements[0].versionName
            signed = $false
            minified = if ($buildIdentityProven) { $true } else { $null }
            resourcesShrunk = if ($buildIdentityProven) { $true } else { $null }
            mapping = $releaseMappingRecord
            resourceShrinkerReport = $releaseResourcesRecord
            packageVerification = if ($buildIdentityProven) { "PASS" } else { "NOT_EVALUATED" }
            abi = "arm64-v8a"
            modelOrImagePayloads = $false
            assetEntries = @($releaseZip.assetEntries)
            pluginInstructionEntry = $releaseZip.rawMarkdownEntries[0]
            pluginInstructionSha256 = $releaseZip.rawMarkdownSha256
        }
        rc = [ordered]@{
            artifact = $rcApkRecord
            versionName = [string]$rcMetadata.elements[0].versionName
            signed = $true
            signingClass = "TEST_SIGNED"
            signerSha256 = $rcSignerSha256
            minified = if ($buildIdentityProven) { $true } else { $null }
            resourcesShrunk = if ($buildIdentityProven) { $true } else { $null }
            mapping = $rcMappingRecord
            resourceShrinkerReport = $rcResourcesRecord
            packageVerification = if ($buildIdentityProven) { "PASS" } else { "NOT_EVALUATED" }
            abi = "arm64-v8a"
            modelOrImagePayloads = $false
            assetEntries = @($rcZip.assetEntries)
            pluginInstructionEntry = $rcZip.rawMarkdownEntries[0]
            pluginInstructionSha256 = $rcZip.rawMarkdownSha256
        }
        compliance = [ordered]@{
            thirdPartyNoticesSha256 = Get-R6ProviderSha256 $noticePath
            modelLicensePolicySha256 = Get-R6ProviderSha256 $modelPolicyPath
            releaseNotesDraftSha256 = Get-R6ProviderSha256 $releaseNotesPath
            ncnnVersion = "20260526"
            modelsPublished = $false
            validationImagesPublished = $false
        }
        releaseBoundary = [ordered]@{
            firstReleaseVersionCode = 2
            predecessorArtifactApplicable = $false
            upgradeRuntimeEvidence = "NOT_RUN_BY_PRODUCT_DECISION"
            versionRollbackEvidence = "NOT_RUN_BY_PRODUCT_DECISION"
            sameVersionRecovery = "ARCHIVED_EXACT_V2_REINSTALL_ONLY"
            withdrawalMitigation = "DISABLE_PROVIDER_WITHDRAW_INDEX_PUBLISH_FORWARD_FIX_V3"
            nativeLoad16kDevice = "NOT_RUN_NO_16K_DEVICE"
            api36Arm64Runtime = "NOT_RUN_NO_AVAILABLE_ENVIRONMENT"
        }
        exclusions = [ordered]@{
            productionSigned = $false
            publishable = $false
            published = $false
            deviceVerified = $false
        }
    }
    $expectedResult = if ($buildIdentityProven) { "PASS" } else { "DIAGNOSTIC_COMPLETE" }
    $pendingReportPath = "$ReportPath.pending-$PID-$([Guid]::NewGuid().ToString('N'))"
    $report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $pendingReportPath -Encoding utf8NoBOM
    Assert-R6ProviderRetainedReport `
        -Path $pendingReportPath `
        -ExpectedResult $expectedResult `
        -ExpectedBuildIdentity $buildIdentityProven
    Move-Item -LiteralPath $pendingReportPath -Destination $ReportPath
    $pendingReportPath = $null
    Assert-R6ProviderRetainedReport `
        -Path $ReportPath `
        -ExpectedResult $expectedResult `
        -ExpectedBuildIdentity $buildIdentityProven
    $bodyCompleted = $true
    if ($buildIdentityProven) {
        Write-Host "YOLO R6 Provider full clean-build preflight PASS"
    } else {
        Write-Host "YOLO R6 Provider existing-artifact diagnostic complete; build identity NOT PROVEN"
    }
    Write-Host "Report: $ReportPath"
} finally {
    $cleanupErrors = [System.Collections.Generic.List[string]]::new()
    if ($null -ne $pendingReportPath -and (Test-Path -LiteralPath $pendingReportPath)) {
        try { Remove-Item -LiteralPath $pendingReportPath -Force } catch { $cleanupErrors.Add($_.Exception.Message) }
    }
    if (-not $bodyCompleted -and $null -ne $resolvedReportPath -and (Test-Path -LiteralPath $resolvedReportPath)) {
        try { Remove-Item -LiteralPath $resolvedReportPath -Force } catch { $cleanupErrors.Add($_.Exception.Message) }
    }
    try { if ($null -ne $gateLock) { $gateLock.Dispose() } } catch { $cleanupErrors.Add($_.Exception.Message) }
    try { Pop-Location } catch { $cleanupErrors.Add($_.Exception.Message) }
    if ($cleanupErrors.Count -gt 0) {
        if ($null -ne $resolvedReportPath -and (Test-Path -LiteralPath $resolvedReportPath)) {
            try { Remove-Item -LiteralPath $resolvedReportPath -Force } catch { $cleanupErrors.Add($_.Exception.Message) }
        }
        throw "R6 Provider gate cleanup failed: $($cleanupErrors -join '; ')"
    }
}
