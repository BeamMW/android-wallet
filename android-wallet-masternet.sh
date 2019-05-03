#!/bin/bash
jnilibs="./app/src/masternet/jniLibs"
libwallet="libwallet-jni.tar.gz" 
builds="https://builds.beam.mw/master/latest/Release"
if [ "" == "`which wget`" ]; then echo "wget Not Found"; if [ -n "`which apt-get`" ]; then sudo apt-get -y install wget ; elif [ -n "`which yum`" ]; then sudo yum -y install wget ; fi ; fi
mkdir -p $jnilibs/{arm64-v8a,armeabi,armeabi-v7a,armv8,x86,x86_64}
wget -P $jnilibs/arm64-v8a/ $builds/android-arm64-v8a/$libwallet &&  tar -xvf $jnilibs/arm64-v8a/$libwallet -C $jnilibs/arm64-v8a/ && rm -rf $jnilibs/arm64-v8a/$libwallet && rm -rf $jnilibs/arm64-v8a/com
wget -P $jnilibs/armv8/  $builds/android-arm64-v8a/$libwallet && tar -xvf $jnilibs/armv8/$libwallet -C $jnilibs/armv8/ && rm -rf $jnilibs/armv8/$libwallet && rm -rf $jnilibs/armv8/com
wget -P $jnilibs/armeabi/  $builds/android-arm64-v8a/$libwallet && tar -xvf $jnilibs/armeabi/$libwallet -C $jnilibs/armeabi/ && rm -rf $jnilibs/armeabi/$libwallet && rm -rf $jnilibs/armeabi/com
wget -P $jnilibs/armeabi-v7a/  $builds/android-armeabi-v7a/$libwallet &&  tar -xvf $jnilibs/armeabi-v7a/$libwallet -C $jnilibs/armeabi-v7a/ && rm -rf $jnilibs/armeabi-v7a/$libwallet && rm -rf $jnilibs/armeabi-v7a/com
wget -P $jnilibs/x86/  $builds/android-x86/$libwallet && tar -xvf $jnilibs/x86/$libwallet -C $jnilibs/x86/ && rm -rf $jnilibs/x86/$libwallet && rm -rf $jnilibs/x86/com
wget -P $jnilibs/x86_64/ $builds/android-x86_64/$libwallet && tar -xvf $jnilibs/x86_64/$libwallet -C $jnilibs/x86_64/ && rm -rf $jnilibs/x86_64/$libwallet && rm -rf $jnilibs/x86_64/com
