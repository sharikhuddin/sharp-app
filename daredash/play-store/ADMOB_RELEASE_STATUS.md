# DareDash — AdMob monetization release checklist

## Current verified repository configuration (15 October 2026)

| Item | Value/status |
|---|---|
| AdMob App ID | `ca-app-pub-5654555743041629~9552640046` — release source wired |
| Banner unit | `ca-app-pub-5654555743041629/2510834956` — release source wired, ad format requires checking in AdMob |
| Rewarded unit | **Missing** — the supplied ID duplicates the Banner ID; release feature safely disabled |
| Debug ads | Google sample/test units only |
| Premium purchases | **Disabled** until backend verification configured and explicitly enabled |
| Account payout / AdMob approval | Not verified through connected account; Google controls approval |
| Play Store submission | Not submitted; needs permanent upload key and completed store forms |

## AdMob actions

1. In AdMob > Apps > DareDash > Ad units, confirm `2510834956` is of **Banner** format. If not, create a Banner unit and override `DDA_ADMOB_BANNER_UNIT_ID` (or update source).
2. Create a **Rewarded** ad unit for DareDash. Copy its **different** `ca-app-pub-.../... ` ID to GitHub Actions repository variable `DDA_ADMOB_REWARDED_UNIT_ID`. Do not reuse Banner ID.
3. Configure Privacy & messaging / consent according to target users and regions; keep the UMP consent flow enabled.
4. Complete AdMob payee/tax/account verification securely on Google's dashboard; do not put payout data into GitHub.
5. After public Play release, link the Play listing in AdMob. Host `app-ads.txt` on the domain you put in the Play Store developer website field as `https://YOUR-DOMAIN/app-ads.txt`. Use the enclosed starter line, but confirm Google's official app-ads.txt snippet in your AdMob settings first.
6. Request app verification and await app readiness review. Until Google approves the app, full ad serving is not assured.

## Signed AAB in GitHub

1. Create and **securely back up** an upload keystore once. Never commit it. Configure Actions secrets: `UPLOAD_KEYSTORE_BASE64`, `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_ALIAS`, `UPLOAD_KEY_PASSWORD`.
2. Optional Actions repository variables: `DDA_ADMOB_APP_ID`, `DDA_ADMOB_BANNER_UNIT_ID` (the repo already has the two public IDs), and `DDA_ADMOB_REWARDED_UNIT_ID` only when a distinct rewarded unit exists.
3. For ads-first publication, leave `DDA_ENABLE_PURCHASES` unset, so unavailable purchases cannot charge anyone. Enable only after backend deployment with `DDA_PURCHASE_VERIFY_URL` and proper Play Billing testing.
4. Actions > Signed Google Play AAB > Run workflow; download signed AAB and upload in Play Console internal testing; complete policies and submit for production when eligible.

## app-ads.txt

Copy `daredash/play-store/app-ads.txt` into the **root of your actual public domain**. Saving it only in this GitHub repo does **not** let Google verify ownership. The publisher ID in the supplied App ID is used here; confirm the snippet against AdMob's generated instructions.
