<p align="left"><img src="https://raw.githubusercontent.com/jonathanklee/Sapio/main/app/src/main/icon.png" width="200"/></p>

# Sapio

Sapio is the anagram of Open Source API.

Sapio provides the compatibility of an Android application running on a device without Google Play Services (i.e. deGoogled bare Android Open Source Project (AOSP) devices, coupled or not with microG).

Sapio can serve as a lobbying tool by sharing compatibility on social media to raise awareness among app developers about respecting users' personal data.

Evaluations in Sapio are given to the community by the community.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.klee.sapio/) [<img src="https://raw.githubusercontent.com/jonathanklee/Sapio/main/ghbadge.png" alt="Get it on GitHub" height="80">](https://github.com/jonathanklee/Sapio/releases)

<p><img src="https://raw.githubusercontent.com/jonathanklee/Sapio/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200"/>&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/jonathanklee/Sapio/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200"/>&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/jonathanklee/Sapio/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200"/></p>

# Rating

| | Label | Description |
|-|-------|-------------|
| 🟢 | **Perfect** | The app works fully without Google Play Services |
| 🟡 | **Partial** | At least one feature (notifications, in-app purchases, login, etc.) does not work |
| 🔴 | **Unusable** | The app does not work at all or crashes |

| Label | Description |
|-------|-------------|
| **microG** | The device has microG installed |
| **bareAOSP** | The device is a bare AOSP device |
| **secure** | The device is considered secured |
| **unsafe** | The device is considered unsafe |

# 🌐 Website 

**[Website](https://sapio.ovh)** to browse and search evaluations from your browser.

# 🔒 No email, no account, no spam

Contributing takes no email, no account and no sign-up. Sapio has no way to ever contact or spam you, and there's no user database to breach.

# 🌍 Public API

**Base URL:** `https://server.sapio.ovh/api`

Pagination, filtering and sorting follow the [Strapi v4 REST API](https://docs.strapi.io/dev-docs/api/rest) conventions.

## Response attributes

| Field | Type | Values |
|-------|------|--------|
| `name` | string | App name |
| `packageName` | string | Android package name |
| `versionName` | string | App version evaluated |
| `updatedAt` | string | ISO 8601 timestamp |
| `microg` | integer | `1` = microG · `2` = bareAOSP |
| `rooted` | integer | `3` = secure · `4` = unsafe |
| `rating` | integer | `1` = Perfect · `2` = Partial · `3` = Unusable |
| `brokenFeatures` | string[] \| null | Non-working features: `notifications`, `in_app_purchase`, `login`, `maps`, `location`, `payments`, `cast`, `augmented_reality` |

## Endpoints

### List evaluations

```
GET /sapio-applications
```

**Example** — latest 100 evaluations sorted by most recent:

```sh
curl "https://server.sapio.ovh/api/sapio-applications?pagination[pageSize]=100&sort=updatedAt:Desc"
```

### Search evaluations

Use [Strapi filters](https://docs.strapi.io/dev-docs/api/rest/filters-locale-publication#filtering) to narrow results by any attribute.

```
GET /sapio-applications
```

**Example** — search by app name:

```sh
curl "https://server.sapio.ovh/api/sapio-applications?filters[name][$eq]=ChatGPT"
```

### Get icons

```
GET /upload/files
```

**Example** — get the ChatGPT icon:

```sh
curl "https://server.sapio.ovh/api/upload/files?filters[name][$eq]=com.openai.chatgpt.png"
```
# ⚠️ Disclaimer

Evaluations are community-contributed and may be inaccurate, incomplete, or device-specific. Sapio and its maintainers are not responsible for any issues arising from relying on these evaluations.

# ☕ Coffee

If you want to offer me a coffee for the maintenance of the server part:

<a href='https://ko-fi.com/Y8Y5191O6Z' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi6.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

# 👏 Credits

<a href="https://www.flaticon.com/free-icons/brain" title="brain icons">Brain icons created by Freepik - Flaticon</a>

<a href="https://www.flaticon.com/free-icons/search" title="search icons">Search icons created by Smashicons - Flaticon</a>
