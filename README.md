# 🚴 Strata

**Strata** is a premium Android application designed for athletes who want to transform their Strava activities into beautiful, shareable stories. It combines real-time activity tracking with a powerful image editor and a secure cloud backend.

![App Logo](app/src/main/res/drawable/logo.png) *(Ensure logo exists at this path)*

---

## ✨ Key Features

- **🔄 Strava Synchronization:** Seamlessly sync your runs, rides, and swims directly from the Strava API.
- **🎨 Creative Editor:** Customize your activity photos with overlays, stats, and professional filters.
- **📱 One-Tap Sharing:** Export your creations directly to Instagram Stories or other social platforms.
- **🔒 Security First:** Hardened with Android Keystore, EncryptedSharedPreferences, and Edge Function secret management.
- **⚡ Performance Optimized:** Low startup latency, R8 obfuscated, and smooth 60fps Compose UI.

---

## 🛠 Tech Stack

- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
- **Architecture:** MVVM (Model-View-ViewModel) with the Repository pattern.
- **Backend:** [Supabase](https://supabase.com/) (Auth, PostgreSQL, Edge Functions).
- **Networking:** [Ktor](https://ktor.io/) for high-performance asynchronous API calls.
- **Local Storage:** [Room Database](https://developer.android.com/training/data-storage/room) for offline caching + [EncryptedSharedPreferences](https://developer.android.com/topic/security/data).
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/) for fast, asynchronous image processing.
- **Logging:** [Timber](https://github.com/JakeWharton/timber) for extensible and secure logging.

---

## 🏗 Architecture & Security

Strata follows a secure, scalable architecture:
- **Secret Management:** Sensitive keys (like the Strava Client Secret) are handled exclusively by **Supabase Edge Functions**. The Android client never touches raw secrets.
- **Network Security:** Enforced HTTPS-only traffic via Android Network Security Config.
- **Offline First:** Uses Room as a local cache, enabling a fast and responsive experience even on poor connections.
- **Obfuscation:** Fully configured R8/ProGuard rules to protect the code and shrink the APK size.

---

## 🚀 Getting Started

### 1. Prerequisites
- [Android Studio Ladybug](https://developer.android.com/studio) or newer.
- [Supabase CLI](https://supabase.com/docs/guides/cli) installed.
- A [Strava Developer](https://www.strava.com/settings/api) account.

### 2. Local Configuration
Create a `local.properties` file in the root directory and add the following:
```properties
strava_client_id=YOUR_CLIENT_ID
strava_redirect_uri=strata://callback
supabase_url=YOUR_SUPABASE_PROJECT_URL
supabase_anon_key=YOUR_SUPABASE_ANON_KEY
```

### 3. Backend Deployment
Deploy the Edge Functions to handle the secure OAuth flow:
```bash
supabase init
supabase secrets set STRAVA_CLIENT_ID=...
supabase secrets set STRAVA_CLIENT_SECRET=...
supabase functions deploy strava-auth
supabase functions deploy strava-webhook
```

### 4. Build & Run
Open the project in Android Studio and run the `:app` module on an emulator or physical device.

---

## 📜 License

Created with ❤️ by the Strata team.
