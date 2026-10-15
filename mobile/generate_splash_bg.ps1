Add-Type -AssemblyName System.Drawing
$srcPath = "C:/Users/vanan/.gemini/antigravity/brain/fa21b222-7834-486a-afd6-b6b72ba5acc5/.user_uploaded/media_1792052675026_e122a92e.png"
$src = [System.Drawing.Bitmap]::FromFile((Resolve-Path $srcPath).Path)

$destW = 1080
$destH = 1920
$scaled = New-Object System.Drawing.Bitmap $destW, $destH
$g = [System.Drawing.Graphics]::FromImage($scaled)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$g.DrawImage($src, 0, 0, $destW, $destH)
$g.Dispose()
$src.Dispose()

$paths = @(
    "mobile/assets/images/splash_bg.png",
    "mobile/android/app/src/main/res/drawable/splash_bg.png",
    "mobile/android/app/src/main/res/drawable-v21/splash_bg.png"
)

foreach ($p in $paths) {
    $dir = Split-Path $p
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $scaled.Save($p, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Host "Saved high-res gradient background: $p"
}
$scaled.Dispose()
