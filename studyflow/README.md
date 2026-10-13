# Studyflow

A local-first React Native study tracker built with Expo SDK 57 and TypeScript.

## Features

- Start, pause, resume, finish, or discard a study timer. The timer uses saved timestamps and continues accurately while the app is in the background.
- Organize sessions by subject. Add subjects with a color and icon.
- View today's time, session count, daily goal, and current streak.
- Browse a monthly study calendar and inspect sessions from a selected day.
- Compare this week's or month's focus time, longest session, daily average, and subject mix.
- Opt in to a local daily summary notification. Its text is updated whenever the app saves a session before the chosen reminder time.
- All data is stored on the device with AsyncStorage. No account or server is required.

## Run locally

Requires Node.js 22.13 or newer.

```bash
npm install
npx expo start
```

Open the QR code with Expo Go on Android or iOS. For a browser preview, run `npm run web`. The daily notification is available on a device, not in the browser.

## Checks

```bash
npm run lint
npm run typecheck
npx expo-doctor
```

## Android APK later

The `preview` profile in `eas.json` is configured for an installable APK. After signing in to an Expo account, run:

```bash
npx eas-cli@latest build --platform android --profile preview
```

The `production` profile creates an Android App Bundle for eventual Play Store distribution. Neither build has been submitted yet.
