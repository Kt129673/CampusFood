# PowerShell script for setting up AWS credentials
# Run this before starting the backend server

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "CampusFood Backend - AWS Credentials Setup" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Check if credentials are already set
if ($env:AWS_ACCESS_KEY -and $env:AWS_SECRET_KEY) {
    Write-Host "✅ AWS credentials are already set!" -ForegroundColor Green
    Write-Host "   AWS_ACCESS_KEY: $($env:AWS_ACCESS_KEY.Substring(0, [Math]::Min(10, $env:AWS_ACCESS_KEY.Length)))..."
    Write-Host "   AWS_SECRET_KEY: $($env:AWS_SECRET_KEY.Substring(0, [Math]::Min(10, $env:AWS_SECRET_KEY.Length)))..."
    Write-Host ""
    Write-Host "To update credentials, remove them first:" -ForegroundColor Yellow
    Write-Host "   Remove-Item Env:\AWS_ACCESS_KEY" -ForegroundColor Yellow
    Write-Host "   Remove-Item Env:\AWS_SECRET_KEY" -ForegroundColor Yellow
    exit 0
}

# Prompt for credentials
Write-Host "Please enter your AWS credentials:" -ForegroundColor Yellow
Write-Host ""
$accessKey = Read-Host "AWS Access Key"
$secretKey = Read-Host "AWS Secret Key" -AsSecureString
$secretKeyPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secretKey)
)

# Validate input
if ([string]::IsNullOrWhiteSpace($accessKey) -or [string]::IsNullOrWhiteSpace($secretKeyPlain)) {
    Write-Host "❌ Error: Both Access Key and Secret Key are required!" -ForegroundColor Red
    exit 1
}

# Set environment variables
$env:AWS_ACCESS_KEY = $accessKey
$env:AWS_SECRET_KEY = $secretKeyPlain

Write-Host ""
Write-Host "✅ AWS credentials have been set!" -ForegroundColor Green
Write-Host ""
Write-Host "⚠️  Note: These credentials are only set for this PowerShell session." -ForegroundColor Yellow
Write-Host ""
Write-Host "To make them permanent, add to your PowerShell profile:" -ForegroundColor Cyan
Write-Host "   `$env:AWS_ACCESS_KEY = `"$accessKey`"" -ForegroundColor Gray
Write-Host "   `$env:AWS_SECRET_KEY = `"***hidden***`"" -ForegroundColor Gray
Write-Host ""
Write-Host "Or set them as system environment variables in Windows Settings." -ForegroundColor Cyan
Write-Host ""
Write-Host "Now you can start the backend server:" -ForegroundColor Green
Write-Host "   .\mvnw.cmd spring-boot:run" -ForegroundColor Gray
Write-Host ""
