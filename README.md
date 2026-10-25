DareDash Android source lives in [daredash/](daredash/).

Debug APK via [Actions](../../actions).


## YapThing website (static GitHub hosting)
YapThing's public information pages live in [docs/yapthing/](docs/yapthing/). The private Android source, on-device AI model, and upload signing materials remain outside this public repository.

### Turn on free GitHub Pages once
1. Open [Settings → Pages](https://github.com/sharikhuddin/sharp-app/settings/pages).
2. Under **Build and deployment**, select **Deploy from a branch**.
3. Choose **main** and **/docs**, then **Save**.
4. Once GitHub reports a successful deployment, open the project's Pages website and navigate to **/yapthing/**.

This is GitHub's free, branch-based static site hosting route. The current GitHub Pages workflow is manual-only to avoid failed automatic runner jobs. The Android app is distributed via Google Play separately; GitHub Pages cannot run the Supabase content-report backend or sign/publish Google Play apps. This repository does not contain the YapThing model or private app signing credentials.
