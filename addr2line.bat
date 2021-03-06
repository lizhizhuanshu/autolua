set execute=D:\Programs\Android\Sdk\ndk-bundle\toolchains\aarch64-linux-android-4.9\prebuilt\windows-x86_64\bin\aarch64-linux-android-addr2line
set path=%~dp0androidlua\src\main\libs\arm64-v8a\libluajava.so
set /p addr=addr is
%execute%  -f -e %path% %addr%
