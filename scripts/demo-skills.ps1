param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "AI Agent Platform CLI - Skills Demo" -ForegroundColor Cyan

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

Write-Host "`n[1] GET /api/skills"
$null = Invoke-JsonApi GET "$BaseUrl/api/skills"

Write-Host "`n[2] GET /api/skills/stats"
$null = Invoke-JsonApi GET "$BaseUrl/api/skills/stats"

Write-Host "`n[3] POST /api/chat to trigger calculator"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-skill-stats-demo"
    modelId = "siliconflow-deepseek"
    message = "Calculate 1 + 2 * 3"
}

Write-Host "`n[4] GET /api/skills/stats again"
$null = Invoke-JsonApi GET "$BaseUrl/api/skills/stats"

Write-Host "`n[5] POST /api/skills/calculator/disable"
$null = Invoke-JsonApi POST "$BaseUrl/api/skills/calculator/disable"

Write-Host "`n[6] GET /api/skills and check calculator enabled=false"
$null = Invoke-JsonApi GET "$BaseUrl/api/skills"

Write-Host "`n[7] POST /api/chat with disabled calculator"
Write-Host "Expected: Skill is disabled: calculator"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-skill-disable-demo"
    modelId = "siliconflow-deepseek"
    message = "Calculate 1 + 2"
}

Write-Host "`n[8] POST /api/skills/calculator/enable"
$null = Invoke-JsonApi POST "$BaseUrl/api/skills/calculator/enable"

Write-Host "`n[9] POST /api/chat calculate 1 + 2 again"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-skill-enable-demo"
    modelId = "siliconflow-deepseek"
    message = "Calculate 1 + 2"
}
