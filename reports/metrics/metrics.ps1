# Generates reports/metrics/metrics.csv for the text editor project.
# Metrics per class:
#   LOC  - lines of code (empty lines and comment lines are skipped)
#   NOM  - number of methods (constructors included)
#   CC   - cyclomatic complexity (decision points + 1)
#   CBO  - coupling between objects (imports plus references to other
#          classes in the same package)
#
# Run with:  powershell -ExecutionPolicy Bypass -File metrics.ps1

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$srcDir = Join-Path $root "src\main\java"
$outCsv = Join-Path $PSScriptRoot "metrics.csv"

$rows = @()

Get-ChildItem $srcDir -Recurse -Filter *.java | ForEach-Object {
    $file = $_.FullName
    $lines = Get-Content $file

    # --- LOC ---
    $loc = 0
    $inBlockComment = $false
    foreach ($line in $lines) {
        $t = $line.Trim()
        if ($t -eq "") { continue }
        if ($inBlockComment) {
            if ($t -match "\*/") { $inBlockComment = $false }
            continue
        }
        if ($t -match "^/\*") {
            if ($t -notmatch "\*/") { $inBlockComment = $true }
            continue
        }
        if ($t -match "^(//|\*|\*/)") { continue }
        $loc++
    }

    $text = Get-Content $file -Raw

    # --- NOM ---
    $nom = ([regex]::Matches($text, "(?m)^\s*(public|private|protected)\s+.*\b[\w<>\[\]]+\s+\w+\s*\(")).Count
    $nom += ([regex]::Matches($text, "(?m)^\s*static\s+.*\b[\w<>\[\]]+\s+\w+\s*\(")).Count

    # --- Cyclomatic complexity ---
    $decisions = ([regex]::Matches($text, "\b(if|else if|for|while|do|case|catch)\b")).Count
    $decisions += ([regex]::Matches($text, "&&|\|\||\?")).Count
    $cc = $decisions + 1

    # --- CBO ---
    $cbo = ([regex]::Matches($text, "(?m)^\s*import\s+[^;]+;")).Count
    # references to classes of this project's own package
    $cbo += ([regex]::Matches($text, "\b(Settings|FileHelper|SyntaxHighlighter|SearchDialog|AboutDialog|EditorFrame|Main)\b")).Count

    $name = $_.Name
    $rows += [PSCustomObject]@{
        Class = $name
        LOC   = $loc
        NOM   = $nom
        CC    = $cc
        CBO   = $cbo
    }
}

$rows | Export-Csv -Path $outCsv -NoTypeInformation -Encoding UTF8
Write-Host "metrics.csv written to $outCsv"
$rows | Format-Table -AutoSize
