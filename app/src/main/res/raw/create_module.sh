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
