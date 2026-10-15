# DareDash v1.4 — release-readiness checklist (October 15, 2026)

## Implemented in this change
- Up to **24 players** on one device; add individually or add a comma-separated list.
- Local player rosters now survive app restarts; removal persists.
- Invite friends through Android share sheet to download when DareDash is published to Google Play.
- Explicit opt-in before sharing the roster of player names; roster import works only on a device with DareDash installed. **This is not live/remote multiplayer.**
- Edge-to-edge insets for Android 16; adaptive banner fits small phones.
- JVM ChallengeBank unit tests and Android lint added to GitHub CI.
- Version 1.4 / versionCode 5.
- Draft public support/privacy/developer website under `docs/` for optional GitHub Pages deployment.

## Publishing steps you must still do
1. **Review game on real Android phones** (especially 360dp wide phone, landscape, Android 16, SMS/WhatsApp invite, guest roster after restart, banner privacy settings).
2. **Signing key**: create a permanent user-owned upload keystore; keep it and its passwords out of this public repository. Set GitHub repository secrets `UPLOAD_KEYSTORE_BASE64`, `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_ALIAS`, `UPLOAD_KEY_PASSWORD`.
3. **App privacy**: publish and review the policy, add your actual public support email in the support page and Play Console, and complete AdMob consent/privacy message. GitHub Pages: Settings > Pages > Deploy from branch `main` and folder `/docs` (if this is permitted by GitHub UI). Your proposed website is `https://sharikhuddin.github.io/sharp-app/` once verified live. Keep `app-ads.txt` at the developer website domain root: for a GitHub Pages project site it is `https://sharikhuddin.github.io/app-ads.txt` (**not** `/sharp-app/app-ads.txt`), so `/docs/app-ads.txt` alone does **not** meet AdMob discovery. Prefer a custom-domain site with `https://your-domain.com/app-ads.txt`, or a separate user-site repository named `sharikhuddin.github.io` and root app-ads.txt.
4. **AdMob**: banner ID is wired but confirm it is Banner format; you still need a DIFFERENT Rewarded unit ID. Rewarded stays disabled until configured. AdMob readiness requires a published Play app, linked listing and verified app-ads.txt.
5. **Purchases**: disabled for this launch. Activate later only after reliable Google Play Billing verification and licensed purchase tests.
6. **Store listing**: prepare final icon 512x512 PNG, feature graphic 1024x500 PNG/JPEG, and at least two real game screenshots from emulator/phone, plus accurate description, content rating, Data Safety and target audience forms. See `play-store/STORE_LISTING.md`.
7. **Play Console**: create/select DareDash with package ID `com.daredash.party` (verify availability), upload signed AAB to internal testing and collect tester feedback. Closed-test requirement depends on account type/creation date; eligible new personal accounts must complete 12 continuous opt-in testers for 14 days before requesting production access.
8. **Policy/privacy**: app includes AdMob and UMP; ensure accurate data-safety declarations. No unverified claims of a live backend, online game rooms or actual revenue.

> Signed AAB has NOT been generated without your keystore secrets. GitHub debug/release compile checks do NOT equal physical device, purchase, policy or Play Console acceptance.
