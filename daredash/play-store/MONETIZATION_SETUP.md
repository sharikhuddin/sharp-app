# DareDash account-specific AdMob values (2026-10-15)

- **Production App ID:** `ca-app-pub-5654555743041629~9552640046`
- **Production Banner:** `ca-app-pub-5654555743041629/2510834956` (as labeled by owner; AdMob format not independently verified)
- **Production Rewarded:** **MISSING.** The owner supplied the same ID for Banner and Rewarded. Do not reuse it; create a separate Rewarded format unit in AdMob. Production app hides rewarded videos until configured.
- **Purchases:** disabled in release until an HTTPS purchase verifier is deployed and `DDA_ENABLE_PURCHASES=true`. Do not charge users without working receipt verification.
- **To publish:** set secure signing secrets in GitHub, generate a signed AAB, complete Google Play declarations and testing, publish, link app in AdMob, host and verify app-ads.txt and pass app readiness review. All these remaining steps require actual account actions.

# DareDash monetization & Google Play launch guide (October 2026)

## State of this repository

- AdMob integration (banner, rewarded video) is present; Android debug ALWAYS uses Google's sample ads. **No production ads are active until you add verified production IDs, consent message and enable publishing.**
- Google Play Billing one-time products are `all_packs` and `remove_ads`. **No product is available until you create and activate it in the app's Play Console.**
- The client POSTs purchase tokens to a trusted HTTPS verification endpoint and will not grant a new purchase if verification is missing/unreachable. Server reference in `backend/worker/index.js`. **Deploy it before accepting real money.**
- One-phone local invites share a player roster; they do NOT synchronize a multiplayer session between phones.

## 1. Google Play Console

Open https://play.google.com/console/ and create/select **DareDash**, package `com.daredash.party` (permanent; verify availability). Finish developer account verification, app content, financial/merchant account if prompted and identity/tax/payout verification. Under **Monetize > Products > One-time products** create:

| Product ID (exact) | Type | Example base price | Purpose |
|---|---|---|---|
| `all_packs` | Non-consumable one-time | USD 3.99 | Unlock Chaos and Duos card packs |
| `remove_ads` | Non-consumable one-time | USD 2.99 | Remove banner ads, reward rerolls free |

Set product availability, activate purchase options and use license testers for test payments. Actual localized price shown in the app comes from Google Play. On certain Play Console UIs the path is **Monetize with Play > Products > One-time products**.

## 2. AdMob

At https://admob.google.com/ create/verify the publisher account, finish payout/payment/tax identity steps, and create an **Android app** (`com.daredash.party`, add store listing when available). Create two ad units: **Banner** and **Rewarded**. Note these three *public identifiers*:

- `DDA_ADMOB_APP_ID` (`ca-app-pub-…~…`)
- `DDA_ADMOB_BANNER_UNIT_ID` (`ca-app-pub-…/…`)
- `DDA_ADMOB_REWARDED_UNIT_ID` (`ca-app-pub-…/…`)

Under **Privacy & messaging**, publish a GDPR/US state privacy consent message as appropriate. The Android app invokes Google UMP every launch, waits for canRequestAds, and offers privacy choices. Ensure your privacy policy and Play Data safety form accurately describe SDK collection. Test with demo ad IDs; *never click your own live ads.*

## 3. Deploy receipt verification (Cloudflare free tier possible)

This is a **standalone** Worker backend. No Supabase or reuse of the MaMa Salt database is required.

1. In Google Cloud Console, select the project linked to your Play Developer account; enable **Google Play Android Developer API**; create a service account and grant it Play Console permissions to access financial/order data for DareDash. Use the minimum permissions necessary to verify purchases. Never share credentials in chat or GitHub.
2. Create a Cloudflare account and Worker from the `backend/worker` folder using Cloudflare Wrangler. Set the `GOOGLE_SERVICE_ACCOUNT_JSON` secret via `wrangler secret put GOOGLE_SERVICE_ACCOUNT_JSON`. Set `PLAY_PACKAGE_NAME` in `wrangler.toml`.
3. Deploy: `npx wrangler deploy`. Visit `https://<your-worker>.workers.dev/health` and confirm `configured: true`. `/health` alone is NOT a real verification test; make a license tester purchase and confirm `/verify` returns verified for that Google purchase token.
4. Save the HTTPS verifier endpoint as **GitHub Actions repository variable** `DDA_PURCHASE_VERIFY_URL`, e.g. `https://<your-worker>.workers.dev/verify`. In production, add rate limits, log filtering (never log purchase tokens), and monitor Google purchase void/refund notifications (RTDN/Voided Purchases API).

The Worker reads only Play transaction tokens and calls Google's purchase product v2 endpoint, validates the product and purchase state, and does not store personal details.

**Important:** This example is a starting backend, not a payment gateway or proof of compliance. It requires a valid service account, security testing, operational monitoring, and careful handling of voided/refunded orders before production scale.

## 4. GitHub configuration and release bundle

In https://github.com/sharikhuddin/sharp-app/settings/variables/actions add **repository variables**:

- `DDA_ADMOB_APP_ID`
- `DDA_ADMOB_BANNER_UNIT_ID`
- `DDA_ADMOB_REWARDED_UNIT_ID`
- `DDA_PURCHASE_VERIFY_URL`

In https://github.com/sharikhuddin/sharp-app/settings/secrets/actions add **repository secrets** (don't share the values):

- `UPLOAD_KEYSTORE_BASE64` — permanently backed up upload JKS file, base64 encoded
- `UPLOAD_STORE_PASSWORD`
- `UPLOAD_KEY_ALIAS`
- `UPLOAD_KEY_PASSWORD`

Then go to Actions > **Signed Google Play AAB** > **Run workflow** and download the generated `.aab` (artifact). The workflow intentionally fails closed if live ad units or HTTPS receipt verification URL are missing. Do NOT commit JKS/service-account/private key or passwords to public GitHub. Keep the same signing key forever or follow Play's reset procedure if needed.

## 5. Play Store publication

- New app submissions need Android 16 / target API 36 as of Aug 31 2026; DareDash targets 36.
- Host an actual public privacy policy URL, not just markdown in GitHub. Publish a support email and actual screenshots.
- Fill **App content** (ads, target audience, data safety, content rating, app access, privacy policy), create a store listing, and complete internal/closed tests before production.
- For personal accounts created after November 13, 2023, Google generally requires a closed test with 12 opted-in testers continuously for 14 days and a production access application.
- Submit a signed AAB to internal testing and test real ad behavior (registered test devices) plus successful, pending, cancelled, refunded and restored Play Billing transactions before promoting to production.

## Monetization health metrics

Avoid excessive interstitials. Track DAU, session length, returning players, ad impressions, rewarded completion, conversion to paid, refund rate and support requests. Actual income is **not guaranteed**; a free app with only a handful of downloads may generate very little revenue.
