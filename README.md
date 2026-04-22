# N400 WhatMean Flashcard 2026

Native Android app configured for EAS Build from the repository root.

## Build context

EAS must be run from the repository root, which contains:

- `eas.json`
- `package.json`
- `settings.gradle.kts`
- the Android module at `app/`

Do not point EAS at the `app/` subfolder. The correct project root is the top-level repository folder.

## EAS profiles

- `preview`: internal Android APK build
- `production`: Android App Bundle (`.aab`) for Play Store upload

## First build

1. Commit and push the repository to GitHub.
2. Log in to Expo:

```bash
npx eas-cli login
```

3. Trigger a build from the repo root:

```bash
npm run eas:build:preview
```

or

```bash
npm run eas:build:production
```

## Versioning

Android app version is controlled locally in `app/build.gradle.kts`:

- `versionCode`
- `versionName`

EAS is configured with `"appVersionSource": "local"` in `eas.json`, so cloud builds use the version committed in this repository.
