Add-Type -AssemblyName System.Drawing

$srcLogoPath = Resolve-Path "mobile/assets/images/fpt_logo.png"
$srcImg = [System.Drawing.Bitmap]::FromFile($srcLogoPath.Path)

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

# Create 512x512 transparent splash icon for Android 12+ (Circle Mask safe zone is diameter ~340px)
$size = 512
$bmp = new-object System.Drawing.Bitmap $size, $size
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$g.Clear([System.Drawing.Color]::Transparent)

# Fit emblem inside safe circle (diameter ~340)
$targetW = 320
$targetH = [int]($targetW * ($cropH / $cropW))
$destX = [int](($size - $targetW) / 2)
$destY = [int](($size - $targetH) / 2)

$g.DrawImage($emblem, [System.Drawing.Rectangle]::new($destX, $destY, $targetW, $targetH), [System.Drawing.Rectangle]::new(0, 0, $cropW, $cropH), [System.Drawing.GraphicsUnit]::Pixel)
$g.Dispose()

$destPath1 = "mobile/android/app/src/main/res/drawable/splash_icon_v31.png"
$destPath2 = "mobile/android/app/src/main/res/drawable-v21/splash_icon_v31.png"
$bmp.Save($destPath1, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Save($destPath2, [System.Drawing.Imaging.ImageFormat]::Png)

$bmp.Dispose()
$emblem.Dispose()
$srcImg.Dispose()
Write-Host "Generated splash_icon_v31.png successfully!"
