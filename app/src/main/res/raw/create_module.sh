api=$(getprop ro.build.version.sdk)
if [ $api == 29 ]; then
  path=/data/adb/modules/qacc-mobile/system/product/overlay/AccentColorCustom
else
  path=/data/adb/modules/qacc-mobile/system/vendor/overlay/AccentColorCustom
fi

mkdir -p $path
touch /data/adb/modules/qacc-mobile/update
touch /data/adb/modules/qacc-mobile/auto_mount

cat << EOF > /data/adb/modules/qacc-mobile/module.prop
id=qacc-mobile
name=Accent colour creator
version=1.0
versionCode=1
author=Akilesh
description=Lets you create a custom system accent colour on Android 8+!
EOF

cp -f /data/data/app.akilesh.qacc/files/AccentColorCustomOverlay.apk $path
chmod 644 $path/AccentColorCustomOverlay.apk
