# Android downloads

[Download Lessgo muss! 2.0.0](https://github.com/HiraniMustansir/Study-sessions-track/raw/refs/heads/main/releases/lessgo-muss-2.0.0.apk)

- Android 8.0 (API 26) or newer.
- Signed release, application ID `com.lessgomuss.tracker`, version code `2`.
- Uses the existing local release signing configuration; the signing key is not included in the repository.
- See [what changed](../CHANGELOG.md).

## Install or upgrade

1. In v1, finish or discard your current timer and export a JSON backup. Keep a separate copy.
2. Download the APK to your phone, open it, and allow installation from that download source if Android asks.
3. Install over the previous signed release when possible. An in-place update retains existing local data and reads the v1 data format automatically.
4. On a fresh installation, open **Settings → Import backup** and select your exported v1 JSON. Import replaces all data on that installation. If your history is already present after updating, you do not need to import again.
5. Check your history, then enable **Settings → Reminders** and grant notification permission if you want alerts. Configure summary time, recurring reminders, and quiet hours there.

A debug-signed v1 APK cannot be updated by this release-signed APK. If Android reports a signature mismatch, preserve your exported backup before uninstalling the debug build, then install this release and import it. Do not uninstall an app containing unexported history.

New backups use format v2 and cannot be imported into v1.

## Verification

The APK passed Android `apksigner verify`; its version and package were checked with `aapt`. SHA-256 is in [SHA256SUMS](SHA256SUMS).

From this directory:

```sh
sha256sum -c SHA256SUMS
```

Source checks passed: `scripts/build-local.sh testDebugUnitTest assembleDebug assembleRelease lintDebug` (14 unit tests; lint: no errors). No device or emulator was connected, so screen rendering, installation over v1, and real notification delivery still need on-device verification.
