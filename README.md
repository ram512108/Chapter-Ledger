# Chapterly

Chapterly is an offline-first Android reading tracker built with Kotlin and Jetpack Compose. It stores the user's library, chapter progress, notes, quotes, ratings, goals, and reading sessions locally on the device.

## Build

1. Open the project in Android Studio.
2. Let Android Studio sync the Gradle project.
3. Use **Build > Make Project** to verify the debug build.
4. Use **Build > Generate Signed Bundle / APK > Android App Bundle** to create the Play Store `.aab`.
5. Configure your own upload keystore when generating the signed release bundle. Never commit the keystore or passwords.

## Package ID

The production application ID is currently `com.ramkumar.chapterledger`. Change it **before the first Play Store publication** if you want a different permanent package name.

## External service

The app uses Open Library for user-initiated book search/ISBN lookup and for book-cover URLs. The app should identify itself to Open Library and should use the service at low volume, with caching where practical.

Before production, set your Open Library contact email in `OpenLibraryService.kt` (`CONTACT_EMAIL`).

## Privacy

See `PRIVACY_POLICY.md` and `docs/privacy-policy.html`. The HTML file is a ready-to-host draft; replace the placeholder contact email before publishing.

## Release checklist

- [ ] Confirm final package/application ID
- [ ] Configure release signing/upload key
- [ ] Build and install a release AAB on a real device
- [ ] Test barcode scanning
- [ ] Test Open Library search and ISBN lookup
- [ ] Test JSON export/import
- [ ] Test Goodreads CSV import
- [ ] Test offline behavior
- [ ] Host the privacy policy at a public HTTPS URL if required by your Play Console declarations
- [ ] Complete Play Console Data Safety and content declarations accurately
- [ ] Review Open Library usage/attribution requirements
- [ ] Complete closed testing requirements for a new personal developer account, if applicable
