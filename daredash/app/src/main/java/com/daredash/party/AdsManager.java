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

/** Google-provided DEMO inventory only. Replace both unit IDs after consent setup. */
final class AdsManager {
    static final String BANNER_TEST = "ca-app-pub-3940256099942544/6300978111";
    static final String REWARD_TEST = "ca-app-pub-3940256099942544/5224354917";
    private RewardedAd rewarded;
    private boolean loading;
    private final Activity activity;

    AdsManager(Activity activity) {
        this.activity = activity;
        new Thread(() -> MobileAds.initialize(activity, status -> {})).start();
        loadReward();
    }

    AdView makeBanner() {
        AdView view = new AdView(activity);
        view.setAdUnitId(BANNER_TEST);
        view.setAdSize(AdSize.BANNER);
        view.loadAd(new AdRequest.Builder().build());
        return view;
    }

    private void loadReward() {
        if (loading || rewarded != null) return;
        loading = true;
        RewardedAd.load(activity, REWARD_TEST, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
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
        if (rewarded == null) {
            Toast.makeText(activity, "Test video unavailable right now. Try again later.", Toast.LENGTH_LONG).show();
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
