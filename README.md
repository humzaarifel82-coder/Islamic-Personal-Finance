# Islamic Personal Finance — Android App

Installment & DBR calculator, packaged as an Android app using
[Capacitor](https://capacitorjs.com/). The APK is built automatically
by GitHub Actions — no local Android Studio setup required.

## Build the APK (via GitHub Actions)

1. Push this repository to GitHub (see the chat guide for exact steps).
2. Go to the **Actions** tab of your repository.
3. Open the **Build Android APK** workflow and click **Run workflow**
   (or just push a commit — it runs automatically).
4. Wait for the run to finish (a few minutes).
5. Open the finished run → **Artifacts** → download
   `islamic-personal-finance-apk`.
6. Unzip it — you'll get `app-debug.apk`. Transfer it to your Android
   phone and install it (enable "Install unknown apps" for your file
   manager/browser first).

## Build locally instead (optional)

```bash
npm install
npx cap add android
npx cap sync android
npx @capacitor/assets generate --android
cd android
./gradlew assembleDebug
```

The APK will be at `android/app/build/outputs/apk/debug/app-debug.apk`.

## Project structure

```
www/index.html          the app itself (HTML/CSS/JS)
resources/icon.png       source app icon (1024x1024)
resources/splash.png     source splash screen (2732x2732)
capacitor.config.json    Capacitor app config (app id, name)
.github/workflows/       GitHub Actions build pipeline
```

## Notes

- App ID: `com.humzaarif.islamicfinance`
- App Name: `Islamic Personal Finance`
- To change the app name or ID, edit `capacitor.config.json` and the
  `android/app/src/main/res/values/strings.xml` after adding the
  Android platform.
