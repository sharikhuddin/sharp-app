package com.daredash.party;

import android.app.Activity;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/** Respect Google UMP consent before making any ad request. */
final class AdsManager {
    private final Activity activity;
    private final ConsentInformation consent;
    private RewardedAd rewarded;
    private boolean initialized;
    private boolean loading;
    private boolean adsReady;

    AdsManager(Activity activity) {
        this.activity = activity;
        consent = UserMessagingPlatform.getConsentInformation(activity);
    }

    void start(Runnable onAvailabilityChanged) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        // This request happens on every launch; never infer consent from cached strings.
        consent.requestConsentInfoUpdate(activity, params, () -> {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, error -> {
                maybeEnableAds();
                onAvailabilityChanged.run();
            });
            // UMP can already have valid consent from a previous launch.
            maybeEnableAds();
            onAvailabilityChanged.run();
        }, error -> {
            // Only serve ads if UMP says ads can be requested on this launch.
            maybeEnableAds();
            onAvailabilityChanged.run();
        });
    }

    boolean canShowAds() { return adsReady && consent.canRequestAds(); }

    boolean privacyOptionsRequired() {
        return consent.getPrivacyOptionsRequirementStatus() ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    void showPrivacyOptions(Runnable onChanged) {
        if (!privacyOptionsRequired()) {
            Toast.makeText(activity, "Privacy options are not required for your region or configuration.", Toast.LENGTH_LONG).show();
            return;
        }
        UserMessagingPlatform.showPrivacyOptionsForm(activity, error -> {
            if (!consent.canRequestAds()) {
                rewarded = null;
                adsReady = false;
            } else maybeEnableAds();
            onChanged.run();
        });
    }

    private void maybeEnableAds() {
        if (!consent.canRequestAds()) return;
        adsReady = true;
        if (!initialized) {
            initialized = true;
            new Thread(() -> MobileAds.initialize(activity, status ->
                activity.runOnUiThread(this::loadReward))).start();
        } else loadReward();
    }

    AdView makeBanner() {
        if (!canShowAds()) return null;
        AdView view = new AdView(activity);
        view.setAdUnitId(BuildConfig.BANNER_AD_UNIT_ID);
        view.setAdSize(AdSize.BANNER);
        view.loadAd(new AdRequest.Builder().build());
        return view;
    }

    private void loadReward() {
        if (!canShowAds() || loading || rewarded != null) return;
        loading = true;
        RewardedAd.load(activity, BuildConfig.REWARDED_AD_UNIT_ID,
            new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                @Override public void onAdLoaded(RewardedAd ad) {
                    loading = false;
                    rewarded = ad;
                }
                @Override public void onAdFailedToLoad(LoadAdError error) {
                    loading = false;
                    rewarded = null;
                }
            });
    }

    void showReward(Runnable onEarned) {
        if (!canShowAds() || rewarded == null) {
            Toast.makeText(activity, "Video unavailable. You can continue playing for free.", Toast.LENGTH_LONG).show();
            loadReward();
            return;
        }
        RewardedAd ad = rewarded;
        rewarded = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() { loadReward(); }
            @Override public void onAdFailedToShowFullScreenContent(AdError error) { loadReward(); }
        });
        ad.show(activity, item -> onEarned.run());
    }
}
