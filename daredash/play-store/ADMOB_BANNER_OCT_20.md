# DareDash AdMob: banner update (20 October 2026)

The owner-supplied **Google AdMob.pdf** confirms the following AdMob configuration:

- **App:** DareDash (Android)
- **Ad unit name:** DareDash Home Banner
- **Format:** Banner
- **App ID:** `ca-app-pub-5654555743041629~9552640046`
- **Banner unit:** `ca-app-pub-5654555743041629/1705194756`

DareDash Android v**1.5.1** (version code **7**) includes the new Banner ID as the release default. The former /2510834956 banner is no longer the release default.

## Check GitHub Actions override

In repository **Settings → Secrets and variables → Actions → Variables**, check `DDA_ADMOB_BANNER_UNIT_ID`.

- If this variable exists, change it to `ca-app-pub-5654555743041629/1705194756`, or delete it to use the app's compiled-in release default.
- Keep `DDA_ADMOB_APP_ID` equal to the App ID above if set.
- `DDA_ADMOB_REWARDED_UNIT_ID` must be a **different Rewarded** unit ID, not the Banner ID. If you do not have one, leave it blank.
- Do **not** enable purchases until purchase verification is deployed and tested.

## Google Play / AdMob launch steps

1. Publish a **signed** v1.5.1 Google Play AAB with package `com.daredash.party`, version code 7 (only if Play Console's current highest version code is below 7).
2. Complete the Google Play store listing, Ads declaration, Data safety, privacy policy, target audience, content rating, and testing requirements.
3. Add a valid developer website to your Play listing and publish the exact AdMob `app-ads.txt` seller line at **the root of that domain**.
4. In AdMob, link DareDash to its Play Store listing, complete app-ads.txt verification, and wait for app readiness review/approval.
5. Complete payout and tax information in AdMob through the official website; **do not share bank credentials or keystores** through GitHub.
6. Test only using the debug APK with Google demo ads or registered test devices. Do not repeatedly click real ads to test monetization.

## Status distinctions

- Integration in source does not mean ad serving or payout is active. AdMob's account/app review controls live serving.
- Debug always uses demo ads. Release uses the real Banner ID with UMP privacy handling.
- Rewarded production ads remain disabled until a distinct Rewarded unit is configured.
- Premium purchases remain disabled until secure Google Play purchase verification is implemented.
