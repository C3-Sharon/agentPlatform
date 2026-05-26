param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$ExternalMcpUrl = "http://localhost:8090/mcp/rpc"
)

Write-Host "AI Agent Platform CLI - External MCP Demo" -ForegroundColor Cyan

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

Write-Host "`nStart standalone mcp-demo-server first:" -ForegroundColor Yellow
Write-Host "cd D:\agent\mcp-demo-server"
Write-Host "mvn spring-boot:run"

Write-Host "`n[1] Direct tools/list against external server"
$externalList = Invoke-JsonApi POST $ExternalMcpUrl @{
    jsonrpc = "2.0"
    id = 1
    method = "tools/list"
}
if (-not $externalList) {
    Write-Host "Please confirm mcp-demo-server is running on port 8090." -ForegroundColor Yellow
    exit 0
}

Write-Host "`n[2] Register external MCP server"
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$serverName = "external-demo-cli-$timestamp"
$registerResponse = Invoke-JsonApi POST "$BaseUrl/api/mcp/external/servers" @{
    name = $serverName
    baseUrl = $ExternalMcpUrl
    enabled = $true
}

$serverId = $null
if ($registerResponse -and $registerResponse.data -and $registerResponse.data.serverId) {
    $serverId = $registerResponse.data.serverId
}
if (-not $serverId) {
    Write-Host "Could not parse serverId. Check the register response." -ForegroundColor Yellow
    exit 0
}

Write-Host "`n[3] POST /api/mcp/external/servers/$serverId/sync-tools"
$null = Invoke-JsonApi POST "$BaseUrl/api/mcp/external/servers/$serverId/sync-tools"

Write-Host "`n[4] GET /api/mcp/external/servers/$serverId"
$detail = Invoke-JsonApi GET "$BaseUrl/api/mcp/external/servers/$serverId"

Write-Host "`n[5] Find demo.uppercase toolId and call it"
$toolId = $null
if ($detail -and $detail.data -and $detail.data.tools) {
    $tool = $detail.data.tools | Where-Object { $_.remoteName -eq "demo.uppercase" } | Select-Object -First 1
    if ($tool) { $toolId = $tool.toolId }
}

if ($toolId) {
    $null = Invoke-JsonApi POST "$BaseUrl/api/mcp/external/tools/$toolId/call" @{
        arguments = @{ text = "hello from cli external mcp" }
    }
} else {
    Write-Host "Could not auto-find demo.uppercase. Copy a toolId and call manually:" -ForegroundColor Yellow
    Write-Host "POST $BaseUrl/api/mcp/external/tools/{toolId}/call"
}
