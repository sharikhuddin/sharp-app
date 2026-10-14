# 🎉 DareDash – Native Android party game

Free-to-build, offline-first Java/Android Studio app. Created October 2026.

## What is playable now

- 2–8 locally named players, no login or backend
- 120 hand-written general-audience prompts: Truth, Dare, Would You Rather, Most Likely, Icebreaker
- Mixed mode, 30-second challenge countdown, re-rolls and turn rotation
- +10 points per completed challenge, round counter, scoreboard, winner screen and sharing
- Premium Chaos and Duos packs (24 extra prompts) with Google Play Billing hooks
- Google AdMob DEMO banner and opt-in rewarded video for 3 extra re-rolls
- Remove-ads Google Play Billing product; installed app plays offline even if network or billing is unavailable

**Important:** UI/game code is provided; no Android APK has been validated in a device/emulator in this workspace. GitHub Actions compiles a debug APK. The app uses Google DEMO ads, NOT revenue-generating inventory. Play purchases need configured Play Console products and purchase-verification hardening before real-money release.

## Build free in the cloud from your phone

1. Open the `Actions` tab of the GitHub repository. The `Android APK build` workflow runs on each push to the `daredash/` directory, or can be started with `Run workflow`.
2. Open the most recent successful `Android APK build` run.
3. Under **Artifacts** tap **DareDash-debug-APK**. GitHub downloads a ZIP containing `app-debug.apk`.
4. Unzip on your Android phone and open `app-debug.apk`. Allow trusted app installation for your file manager if prompted. **Only install APKs you trust.**
5. The debug APK is for local testing; Google Play normally requires a signed Android App Bundle (`.aab`).

From Android Studio: open this folder as a Gradle project, sync, run on a device. CI uses AGP 8.13.2, Gradle 8.13, JDK 17 and Android API 36.

## Monetization: what is implemented versus still required

| Item | Code state | Required to earn actual revenue |
|---|---|---|
| Banner ads | AdMob SDK wired to Google's **test** banner | Create AdMob app/banner unit, replace both demo IDs, configure UMP consent and privacy disclosure |
| Rewarded videos | SDK wired to Google's **test** rewarded unit | Create AdMob rewarded unit, replace demo ID, configure consent |
| Premium packs | BillingClient 8.3.0 wired to INAPP ID `all_packs` | Create/activate a **one-time, non-consumable** product in Play Console, configure localized pricing, test and verify purchases securely |
| Remove ads | BillingClient wired to INAPP ID `remove_ads` | Create/activate matching one-time product; verify purchase tokens server-side prior to launch |

Suggested initial prices: `all_packs` $3.99 and `remove_ads` $2.99, localized by Google Play. **These are examples, not live prices.** Purchases are processed through Google Play Billing, not a personal e-wallet. Never put banking, password, upload-key, or ad account credentials in source control.

**Billing security:** The starter stores locally cached entitlements for offline access and re-checks Play purchases on connection. Before public monetization, verify purchase tokens on a trusted server using Google Play Developer API, process refunds/voided purchases, and only grant after verified PURCHASED state. Confirm the product and pricing shown at checkout.

**Ad consent:** The SDK uses test ads today. Before distributing production ad builds, integrate Google UMP, respect opt-in/opt-out and age/country-specific consent, and make the Play Console Data Safety declaration accurately. Do not use live AdMob unit IDs during development unless registering test devices.

## Package / play submission

- Application ID: `com.daredash.party` — verify availability and choose your permanent ID **before** first Play release. You cannot change it for an existing Play listing.
- App targets **API 36** (Android 16), required for new Android apps as of August 31, 2026.
- Min API 24 (Android 7.0+), JDK 17.
- Google Play Console > All apps > Create app. Suggested category: **Game > Trivia** (confirm best category in console).
- Complete privacy policy URL, ads declaration, content rating, Data Safety, target audience, app access, store listing, screenshots and testing details.
- **Internal testing** first. New personal Play developer accounts created after Nov 13, 2023 generally require **12 opted-in testers continuously for 14 days** in a closed test to request production access.
- Create a permanent upload keystore **and back it up securely**; Google's Play App Signing recommended for app signing.
- Store base64 of JKS file in repo Actions secret `UPLOAD_KEYSTORE_BASE64`; put credentials in `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_ALIAS` and `UPLOAD_KEY_PASSWORD`. Then Actions > **Signed Google Play AAB** > **Run workflow**. Download the AAB artifact and upload to Play Console internal testing. These steps require secrets YOU set, not ChatGPT.
- Never commit `.jks`, credentials or production tokens.

## Store listing starter

**Title:** DareDash: Party Games & Challenges

**Short description:** Truth, dares, quizzes and laughs with friends. Play together, anytime!

**Full description:** Bring your friends together with DareDash, the lightweight offline party game! Add two to eight players, choose Truth, Dare, Would You Rather, Most Likely, or Icebreakers, and take turns completing playful, family-friendly challenges. Earn points, race the timer, and crown a winner. Enjoy 120 free challenge cards, with optional extra packs and upgrades. No sign-up required. Players can choose to pass on any challenge. App includes ads and optional purchases.

**Suggested screenshots:** setup screen, colorful prompt card, 30-second timer, group leaderboard, win screen, store screen. Capture REAL emulator/device screenshots before listing; don't present mockups as actual gameplay.

## Compliance & launch checklist

- [ ] Build and test APK on physical device and small Android screen, rotate screen, test Android 16 edge-to-edge
- [ ] Confirm stable package ID and review uniqueness of DareDash branding
- [ ] Publish an accurate accessible privacy policy (starter in `PRIVACY_POLICY_TEMPLATE.md`)
- [ ] Configure AdMob + UMP and switch off DEMO units **only after consenting test**
- [ ] Configure both Play Billing products; trusted-server validation and refund handling
- [ ] Test purchases as Google Play license tester, including cancellations and restores
- [ ] Create/back up upload key and configure GitHub Actions secrets
- [ ] Submit signed release AAB to internal/closed testing
- [ ] Complete Play Console forms and obtain production access
- [ ] Monitor crashes, customer support, ratings and ad results after release

**No ongoing hosting is needed** for the offline gameplay; AdMob, Google Play and purchase-verification hosting may have their own requirements/costs.
