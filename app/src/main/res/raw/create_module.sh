mkdir -p /data/adb/modules/qacc-mobile/system/product/overlay/AccentColorCustom
touch /data/adb/modules/qacc-mobile/update
touch /data/adb/modules/qacc-mobile/auto_mount
cat << EOF > /data/adb/modules/qacc-mobile/module.prop
id=qacc-mobile
name=Accent colour creator
version=1.0
versionCode=1
author=Akilesh
description=Lets you create a custom system accent colour on Android 10!
EOF
cp -f /data/data/app.akilesh.qacc/files/AccentColorCustomOverlay.apk /data/adb/modules/qacc-mobile/system/product/overlay/AccentColorCustom
chmod 644 /data/adb/modules/qacc-mobile/system/product/overlay/AccentColorCustom/AccentColorCustomOverlay.apk
