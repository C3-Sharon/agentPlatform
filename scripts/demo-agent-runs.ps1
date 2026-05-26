param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "AI Agent Platform CLI - Agent Run History Demo" -ForegroundColor Cyan

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

Write-Host "`n[1] POST /api/chat to create one Agent run"
$null = Invoke-JsonApi POST "$BaseUrl/api/chat" @{
    conversationId = "cli-run-history-demo"
    modelId = "siliconflow-deepseek"
    message = "Briefly introduce the core modules of an AI Agent platform."
}

Write-Host "`n[2] GET /api/agent/runs"
$runs = Invoke-JsonApi GET "$BaseUrl/api/agent/runs"

Write-Host "`n[3] Read first runId and get detail"
$runId = $null
if ($runs -and $runs.data -and $runs.data.Count -gt 0) {
    $runId = $runs.data[0].runId
}

if ($runId) {
    $null = Invoke-JsonApi GET "$BaseUrl/api/agent/runs/$runId"
} else {
    Write-Host "Could not parse runId. Copy one manually and call:" -ForegroundColor Yellow
    Write-Host "GET $BaseUrl/api/agent/runs/{runId}"
}
