Add-Type -AssemblyName System.Drawing

$srcLogoPath = Resolve-Path "mobile/assets/images/fpt_logo.png"
$srcImg = [System.Drawing.Bitmap]::FromFile($srcLogoPath.Path)

# Bounding box of the 3 wings emblem from earlier measurement
$cropX = 270
$cropY = 0
$cropW = 490
$cropH = 300

$emblem = new-object System.Drawing.Bitmap $cropW, $cropH
$gEmblem = [System.Drawing.Graphics]::FromImage($emblem)
$gEmblem.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$gEmblem.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$gEmblem.DrawImage($srcImg, [System.Drawing.Rectangle]::new(0, 0, $cropW, $cropH), [System.Drawing.Rectangle]::new($cropX, $cropY, $cropW, $cropH), [System.Drawing.GraphicsUnit]::Pixel)
$gEmblem.Dispose()

# Helper to generate a square app icon with white background and centered emblem
function New-AppIcon([int]$size, [string]$destPath) {
    $bmp = new-object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::White)

    # 6% safe padding so the FPT emblem is bold, prominent, and fills the canvas
    $targetW = [int]($size * 0.88)
    $targetH = [int]($targetW * ($cropH / $cropW))
    $destX = [int](($size - $targetW) / 2)
    $destY = [int](($size - $targetH) / 2)

    $g.DrawImage($emblem, [System.Drawing.Rectangle]::new($destX, $destY, $targetW, $targetH), [System.Drawing.Rectangle]::new(0, 0, $cropW, $cropH), [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()

    $dir = Split-Path $destPath
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $bmp.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Created App Icon: $destPath ($size x $size)"
}

# Helper to generate splash screen logo (full FPT Schools logo)
function New-SplashLogo([int]$targetW, [int]$targetH, [string]$destPath) {
    $bmp = new-object System.Drawing.Bitmap $targetW, $targetH
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.DrawImage($srcImg, [System.Drawing.Rectangle]::new(0, 0, $targetW, $targetH), [System.Drawing.Rectangle]::new(0, 0, $srcImg.Width, $srcImg.Height), [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()

    $dir = Split-Path $destPath
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $bmp.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Created Splash Logo: $destPath ($targetW x $targetH)"
}

# 1. Android Mipmap Icons
New-AppIcon 48  "mobile/android/app/src/main/res/mipmap-mdpi/ic_launcher.png"
New-AppIcon 72  "mobile/android/app/src/main/res/mipmap-hdpi/ic_launcher.png"
New-AppIcon 96  "mobile/android/app/src/main/res/mipmap-xhdpi/ic_launcher.png"
New-AppIcon 144 "mobile/android/app/src/main/res/mipmap-xxhdpi/ic_launcher.png"
New-AppIcon 192 "mobile/android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"

# 2. Android Splash Screen Logo (enlarged for modern phone displays)
New-SplashLogo 720 346 "mobile/android/app/src/main/res/drawable/splash_logo.png"
New-SplashLogo 720 346 "mobile/android/app/src/main/res/drawable-v21/splash_logo.png"

# 3. Web Favicon and Icons
New-AppIcon 48  "mobile/web/favicon.png"
New-AppIcon 192 "mobile/web/icons/Icon-192.png"
New-AppIcon 512 "mobile/web/icons/Icon-512.png"
New-AppIcon 192 "mobile/web/icons/Icon-maskable-192.png"
New-AppIcon 512 "mobile/web/icons/Icon-maskable-512.png"

# 4. iOS App Icons
$iosDir = "mobile/ios/Runner/Assets.xcassets/AppIcon.appiconset"
New-AppIcon 20   "$iosDir/Icon-App-20x20@1x.png"
New-AppIcon 40   "$iosDir/Icon-App-20x20@2x.png"
New-AppIcon 60   "$iosDir/Icon-App-20x20@3x.png"
New-AppIcon 29   "$iosDir/Icon-App-29x29@1x.png"
New-AppIcon 58   "$iosDir/Icon-App-29x29@2x.png"
New-AppIcon 87   "$iosDir/Icon-App-29x29@3x.png"
New-AppIcon 40   "$iosDir/Icon-App-40x40@1x.png"
New-AppIcon 80   "$iosDir/Icon-App-40x40@2x.png"
New-AppIcon 120  "$iosDir/Icon-App-40x40@3x.png"
New-AppIcon 120  "$iosDir/Icon-App-60x60@2x.png"
New-AppIcon 180  "$iosDir/Icon-App-60x60@3x.png"
New-AppIcon 76   "$iosDir/Icon-App-76x76@1x.png"
New-AppIcon 152  "$iosDir/Icon-App-76x76@2x.png"
New-AppIcon 167  "$iosDir/Icon-App-83.5x83.5@2x.png"
New-AppIcon 1024 "$iosDir/Icon-App-1024x1024@1x.png"

# 5. iOS Launch Image
$iosSplashDir = "mobile/ios/Runner/Assets.xcassets/LaunchImage.imageset"
New-SplashLogo 320 154 "$iosSplashDir/LaunchImage.png"
New-SplashLogo 640 308 "$iosSplashDir/LaunchImage@2x.png"
New-SplashLogo 960 462 "$iosSplashDir/LaunchImage@3x.png"

$emblem.Dispose()
$srcImg.Dispose()
Write-Host "All FPT icons and splash assets successfully generated!"
