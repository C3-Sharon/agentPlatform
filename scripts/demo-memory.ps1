param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$ConversationId = "cli-memory-demo"
)

Write-Host "AI Agent Platform CLI - Memory Persistence Demo" -ForegroundColor Cyan

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

Write-Host "`n[1] POST /api/chat first turn: save memory"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = $ConversationId
    modelId = "siliconflow-deepseek"
    message = "Please remember that I am working on an AI Agent platform assessment."
}

Write-Host "`n[2] POST /api/chat second turn: read short-term memory"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = $ConversationId
    modelId = "siliconflow-deepseek"
    message = "What did I just say I am working on?"
}

Write-Host "`n[3] GET /api/memory/conversations/$ConversationId/messages"
$null = Invoke-JsonApi GET "$BaseUrl/api/memory/conversations/$ConversationId/messages"

Write-Host "`nTip: restart Spring Boot, then rerun the second turn to verify persisted short-term memory." -ForegroundColor Yellow
