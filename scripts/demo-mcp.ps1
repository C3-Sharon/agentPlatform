param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "AI Agent Platform CLI - Internal MCP Demo" -ForegroundColor Cyan

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

Write-Host "`n[1] GET /api/mcp/tools"
$null = Invoke-JsonApi GET "$BaseUrl/api/mcp/tools"

Write-Host "`n[2] POST /api/mcp/tools/filesystem.search/call"
$null = Invoke-JsonApi POST "$BaseUrl/api/mcp/tools/filesystem.search/call" @{
    params = @{ keyword = "README" }
}

Write-Host "`n[3] POST /api/mcp/tools/database.recent_agent_runs/call"
$null = Invoke-JsonApi POST "$BaseUrl/api/mcp/tools/database.recent_agent_runs/call" @{
    params = @{ limit = 5 }
}

Write-Host "`n[4] POST /api/mcp/rpc tools/list"
$null = Invoke-JsonApi POST "$BaseUrl/api/mcp/rpc" @{
    jsonrpc = "2.0"
    id = 1
    method = "tools/list"
}

Write-Host "`n[5] POST /api/mcp/rpc tools/call filesystem.search"
$null = Invoke-JsonApi POST "$BaseUrl/api/mcp/rpc" @{
    jsonrpc = "2.0"
    id = 2
    method = "tools/call"
    params = @{
        name = "filesystem.search"
        arguments = @{ keyword = "README" }
    }
}

Write-Host "`n[6] POST /api/mcp/rpc tools/call database.recent_agent_runs"
$null = Invoke-JsonApi POST "$BaseUrl/api/mcp/rpc" @{
    jsonrpc = "2.0"
    id = 3
    method = "tools/call"
    params = @{
        name = "database.recent_agent_runs"
        arguments = @{ limit = 5 }
    }
}
