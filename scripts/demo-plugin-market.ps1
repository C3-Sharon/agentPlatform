param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$PluginJar = "D:\agent\plugin-demo\target\plugin-demo-1.0.0.jar"
)

Write-Host "AI Agent Platform CLI - Plugin Skill Market Demo" -ForegroundColor Cyan

function Show-Json($Value) { $Value | ConvertTo-Json -Depth 20 }

function Invoke-JsonApi($Method, $Url, $Body = $null) {
    try {
        $params = @{ Method = $Method; Uri = $Url }
        if ($null -ne $Body) {
            $params.ContentType = "application/json"
            $params.Body = ($Body | ConvertTo-Json -Depth 20)
        }
        $response = Invoke-RestMethod @params
        Show-Json $response
        return $response
    } catch {
        Write-Host "Request failed: $($_.Exception.Message)" -ForegroundColor Yellow
        return $null
    }
}

Write-Host "`n[1] Check plugin jar"
if (-not (Test-Path -LiteralPath $PluginJar)) {
    Write-Host "Build plugin-demo jar first, or edit PluginJar in this script." -ForegroundColor Yellow
    Write-Host "Current path: $PluginJar"
    exit 0
}
Write-Host "Plugin jar found: $PluginJar"

Write-Host "`n[2] POST /api/plugins/skills/upload"
$PluginId = $null
try {
    $raw = & curl.exe -s -X POST "$BaseUrl/api/plugins/skills/upload" -F "file=@$PluginJar"
    try {
        $uploadResponse = $raw | ConvertFrom-Json
        $uploadResponse | ConvertTo-Json -Depth 20
        if ($uploadResponse.data -and $uploadResponse.data.pluginId) {
            $PluginId = $uploadResponse.data.pluginId
        }
        if ($uploadResponse.success -eq $false -and $uploadResponse.message -like "*already exists*") {
            Write-Host "Hint: a plugin with the same skill name may already be uploaded." -ForegroundColor Yellow
        }
    } catch {
        $raw
    }
} catch {
    Write-Host "Upload failed: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host "`n[3] GET /api/plugins"
$null = Invoke-JsonApi GET "$BaseUrl/api/plugins"

Write-Host "`n[4] GET /api/skills and check text_reverse"
$null = Invoke-JsonApi GET "$BaseUrl/api/skills"

if ($PluginId) {
    Write-Host "`n[5] POST /api/plugins/$PluginId/disable"
    $null = Invoke-JsonApi POST "$BaseUrl/api/plugins/$PluginId/disable"

    Write-Host "`n[6] GET /api/skills"
    $null = Invoke-JsonApi GET "$BaseUrl/api/skills"

    Write-Host "`n[7] POST /api/plugins/$PluginId/enable"
    $null = Invoke-JsonApi POST "$BaseUrl/api/plugins/$PluginId/enable"

    Write-Host "`n[8] GET /api/skills"
    $null = Invoke-JsonApi GET "$BaseUrl/api/skills"
} else {
    Write-Host "`nCould not parse pluginId. Manual commands:" -ForegroundColor Yellow
    Write-Host "POST $BaseUrl/api/plugins/{pluginId}/disable"
    Write-Host "POST $BaseUrl/api/plugins/{pluginId}/enable"
}
