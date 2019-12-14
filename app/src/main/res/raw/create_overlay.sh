chmod +x aapt
./aapt p -f -M AndroidManifest.xml -I  /system/framework/framework-res.apk -S src -F qacc.apk
cd /