set execute=D:\Programs\Android\Sdk\ndk-bundle\toolchains\aarch64-linux-android-4.9\prebuilt\windows-x86_64\bin\aarch64-linux-android-addr2line
set path=%~dp0autolua\src\main\libs\armeabi-v7a\libscreen.so
set /p addr=addr is
%execute%  -f -e %path% %addr%
