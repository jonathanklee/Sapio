<p align="left"><img src="https://github.com/jonathanklee/Sapio/blob/main/app/src/main/icon.png" width="200"/></p>

# Sapio

Sapio is the anagram of Open Source API.

Sapio provides the compatibility matrix of an Android application with deGoogled bare Android Open Source Project (AOSP) devices, coupled or not with microG.

Sapio can serve as a lobbying tool by sharing compatibility matrices on social media to raise awareness among app developers about respecting users' personal data.

Evaluations in Sapio are given to the community by the community.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.klee.sapio/) [<img src="ghbadge.png" alt="Get it on GitHub" height="80">](https://github.com/jonathanklee/Sapio/releases)

<p><img src="https://github.com/jonathanklee/Sapio/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200"/>&nbsp&nbsp<img src="https://github.com/jonathanklee/Sapio/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200"/>&nbsp&nbsp<img src="https://github.com/jonathanklee/Sapio/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200"/>

# Rating

🟢 The app works perfectly without Google Play Services

🟡 The app works partially: at least one feature (notifications, in-app purchases, login methods etc) does not work without Google Play Services

🔴 The app does not work at all or crashes without Google Play Services

**bareAOSP** The device is a bare AOSP device 

**microG** The device has microG installed

**root** The device is considered rooted

**user** The device is not considered rooted

NB: ratings for rooted devices are less visible as they are of secondary importance.

# 🔨 Build
## Get the sources

```
git clone git@github.com:jonathanklee/Sapio.git
```
## Build Sapio
```
cd Sapio
./gradlew assembleDebug
````
# 📱 Install
```
adb install ./app/build/outputs/apk/debug/app-debug.apk
```

# :earth_africa: Public API

## Base url
```
https://server.sapio.ovh/api
```
## Endpoints

### List evaluations

- Endpoint: /sapio-applications
- Method: GET
- Description: List evaluations
- Parameters: https://docs.strapi.io/dev-docs/api/rest/parameters
- Result:
    - https://docs.strapi.io/dev-docs/api/rest#requests
    - attributes:
        - microg: 1 for microG, 2 for bareAOSP
        - rooted: 3 for user, 4 for root
        - rating: 1 for green, 2 for yellow, 3 for red
- Example: Get the latest 100 evaluations

```
curl -X GET "https://server.sapio.ovh/api/sapio-applications?pagination\[pageSize\]=100&sort=updatedAt:Desc"
```

### Search evaluations

- Endpoint: /sapio-applications
- Method: GET
- Description: Search evaluations
- Parameters: https://docs.strapi.io/dev-docs/api/rest/filters-locale-publication#filtering
- Result:
    - https://docs.strapi.io/dev-docs/api/rest#requests
    - attributes:
        - microg: 1 for microG, 2 for bareAOSP
        - rooted: 3 for user, 4 for root
        - rating: 1 for green, 2 for yellow, 3 for red
- Example: Search evaluations for an app called ChatGPT
 ```
 curl -X GET "https://server.sapio.ovh/api/sapio-applications?filters\[name\]\[\$eq\]=ChatGPT"
 ```

### Get icons

- Endpoint: /upload/files
- Method: GET
- Description: Get icons
- Parameters: https://docs.strapi.io/dev-docs/api/rest/parameters
- Example: Get ChatGPT icon
 ```
curl -X GET "https://server.sapio.ovh/api/upload/files?filters\[name\]\[\$eq\]=com.openai.chatgpt.png"
 ```

# ☕ Coffee

If you want to offer me a coffee for the maintenance of the server part:

<a href='https://ko-fi.com/Y8Y5191O6Z' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi6.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

# 👏 Credits

<a href="https://www.flaticon.com/free-icons/brain" title="brain icons">Brain icons created by Freepik - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/search" title="search icons">Search icons created by Smashicons - Flaticon</a>
