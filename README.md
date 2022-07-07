# Sapio

Sapio is the anagram of Open Source API.

Sapio aims to provide an estimation of how Google-dependent an Android application is, informing the community on how it behaves on bare Android Open Source Project (AOSP), couple or not with microG.

Evaluations in Sapio are given by the community to the community.

<p><img src="https://github.com/jonathanklee/Sapio/blob/main/screenshot.png" width="200"/>

# Build
## Get the sources

```
git clone git@github.com:jonathanklee/Sapio.git
```
## Build Sapio
```
cd Sapio
./gradlew assembleDebug
````
# Install
```
adb install ./app/build/outputs/apk/debug/app-debug.apk
```
