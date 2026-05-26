param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "AI Agent Platform CLI - Models Demo" -ForegroundColor Cyan

function Show-Json($Value) {
    $Value | ConvertTo-Json -Depth 20
}

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

Write-Host "`n[1] GET /api/models"
$null = Invoke-JsonApi GET "$BaseUrl/api/models"

Write-Host "`n[2] GET /api/models/siliconflow-deepseek"
$null = Invoke-JsonApi GET "$BaseUrl/api/models/siliconflow-deepseek"

Write-Host "`n[3] POST /api/chat with siliconflow-qwen"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-model-qwen-demo"
    modelId = "siliconflow-qwen"
    message = "Please introduce yourself in one sentence."
}

Write-Host "`n[4] POST /api/chat with siliconflow-deepseek"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-model-deepseek-demo"
    modelId = "siliconflow-deepseek"
    message = "Please introduce yourself in one sentence."
}

Write-Host "`n[5] POST /api/chat with disabled local-ollama"
Write-Host "Expected: Model is disabled: local-ollama"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-model-disabled-demo"
    modelId = "local-ollama"
    message = "hello"
}
