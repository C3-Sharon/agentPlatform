param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "AI Agent Platform CLI - Chat and Pending Skill Demo" -ForegroundColor Cyan

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

Write-Host "`n[1] Normal chat"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-chat-demo"
    modelId = "siliconflow-deepseek"
    message = "Hello. What can you do?"
}

Write-Host "`n[2] Calculator natural-language call"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-chat-calc-demo"
    modelId = "siliconflow-deepseek"
    message = "Calculate 10 * 24 + 6"
}

Write-Host "`n[3] Check whether text_reverse exists and is enabled"
$skills = Invoke-JsonApi GET "$BaseUrl/api/skills"
$hasTextReverse = $false
if ($skills -and $skills.data) {
    $hasTextReverse = ($skills.data | Where-Object { $_.name -eq "text_reverse" -and $_.enabled -eq $true } | Select-Object -First 1) -ne $null
}

if (-not $hasTextReverse) {
    Write-Host "text_reverse is missing or disabled. Run demo-plugin-market.ps1 first." -ForegroundColor Yellow
    exit 0
}

Write-Host "`n[4] Explicit text_reverse call with missing required param"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-chat-pending-demo"
    modelId = "siliconflow-deepseek"
    message = "use text_reverse"
}

Write-Host "`n[5] Provide missing param"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-chat-pending-demo"
    modelId = "siliconflow-deepseek"
    message = "text=hello skill"
}
