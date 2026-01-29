param(
    [switch]$SkipIntegration,
    [switch]$ContinueOnFailure
)

Set-Location -Path $PSScriptRoot

$root = (Resolve-Path "src/test/java").Path
$tests = Get-ChildItem -Recurse -Filter "*Test.java" "src/test/java" | ForEach-Object {
    $_.FullName.Substring($root.Length + 1) -replace '\\','.' -replace '\.java$',''
}

if ($SkipIntegration) {
    $tests = $tests | Where-Object { $_ -ne "com.example.demo.DemoApplicationTests" }
}

if (-not $tests -or $tests.Count -eq 0) {
    Write-Error "No tests found under src/test/java"
    exit 1
}

$failed = @()
foreach ($t in $tests) {
    Write-Host "=== Running $t ==="
    mvn "-Dtest=$t" test
    if ($LASTEXITCODE -ne 0) {
        $failed += $t
        if (-not $ContinueOnFailure) {
            exit $LASTEXITCODE
        }
    }
}

if ($failed.Count -gt 0) {
    Write-Host "Failed tests:"
    $failed | ForEach-Object { Write-Host "- $_" }
    exit 1
}

Write-Host "All tests passed."