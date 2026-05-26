param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$VisionModelId = "siliconflow-qwen-vl",
    [string]$ImagePath = (Join-Path $PSScriptRoot "demo-assets\vision_test_scene.jpg")
)

Write-Host "AI Agent Platform CLI - Vision Chat Demo" -ForegroundColor Cyan

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

Write-Host "`n[1] GET /api/models/$VisionModelId"
$null = Invoke-JsonApi GET "$BaseUrl/api/models/$VisionModelId"

Write-Host "`n[2] Check image file"
if (-not (Test-Path -LiteralPath $ImagePath)) {
    Write-Host "Put a test image at scripts/demo-assets/vision_test_scene.jpg, or edit ImagePath in this script." -ForegroundColor Yellow
    Write-Host "Current path: $ImagePath"
    exit 0
}
Write-Host "Image found: $ImagePath"

Write-Host "`n[3] POST /api/models/vision-chat multipart"
Write-Host "Disabled or unsupported vision model errors are acceptable MVP validation results."
try {
    $raw = & curl.exe -s -X POST "$BaseUrl/api/models/vision-chat" `
        -F "modelId=$VisionModelId" `
        -F "message=Please describe this image and read any English text in it." `
        -F "image=@$ImagePath"
    try {
        $raw | ConvertFrom-Json | ConvertTo-Json -Depth 20
    } catch {
        $raw
    }
} catch {
    Write-Host "Request failed: $($_.Exception.Message)" -ForegroundColor Yellow
}
