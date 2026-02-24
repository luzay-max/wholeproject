$ErrorActionPreference = "Stop"

$profilePath = $PROFILE.CurrentUserCurrentHost
$profileDir = Split-Path -Path $profilePath -Parent
$startMarker = "# >>> UTF-8 Encoding Config (Codex Fix) >>>"
$endMarker = "# <<< UTF-8 Encoding Config <<<"

if (-not (Test-Path $profileDir)) {
    New-Item -Path $profileDir -ItemType Directory -Force | Out-Null
}

$utf8Block = @"
$startMarker
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new(`$false)
`$OutputEncoding = [System.Text.UTF8Encoding]::new(`$false)
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new(`$false)
if (Get-Command Set-PSReadLineOption -ErrorAction SilentlyContinue) {
    `$psReadLineCmd = Get-Command Set-PSReadLineOption
    if (`$psReadLineCmd.Parameters.ContainsKey('PredictionSource')) {
        Set-PSReadLineOption -PredictionSource None
    }
}
if (`$PSStyle -and `$PSStyle.PSObject.Properties.Name -contains 'OutputRendering') {
    `$PSStyle.OutputRendering = 'PlainText'
}
$endMarker
"@

if (Test-Path $profilePath) {
    $existing = Get-Content -Path $profilePath -Raw -ErrorAction SilentlyContinue
    if ($existing -match [regex]::Escape($startMarker) -and $existing -match [regex]::Escape($endMarker)) {
        $pattern = "(?s)$([regex]::Escape($startMarker)).*?$([regex]::Escape($endMarker))"
        $updated = [regex]::Replace(
            $existing,
            $pattern,
            [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $utf8Block }
        )
        Set-Content -Path $profilePath -Value $updated -Encoding UTF8
        Write-Host "Replaced existing UTF-8 config in: $profilePath"
    } elseif ($existing -notmatch "UTF-8 Encoding Config \(Codex Fix\)") {
        Add-Content -Path $profilePath -Value "`r`n$utf8Block`r`n" -Encoding UTF8
        Write-Host "Appended UTF-8 config to: $profilePath"
    } else {
        Set-Content -Path $profilePath -Value $utf8Block -Encoding UTF8
        Write-Host "Overwrote UTF-8 config in: $profilePath"
    }
} else {
    Set-Content -Path $profilePath -Value $utf8Block -Encoding UTF8
    Write-Host "Created profile and wrote UTF-8 config: $profilePath"
}

. $profilePath
Write-Host "UTF-8 profile loaded successfully."
