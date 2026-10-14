package com.daredash.party;

import android.app.Activity;
import android.content.SharedPreferences;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Real Google Play one-time product integration; products need Play Console setup first.
 * Production hardening: verify tokens on a trusted backend before granting entitlements.
 */
final class BillingManager {
    static final String ALL_PACKS = "all_packs";
    static final String REMOVE_ADS = "remove_ads";

    interface Listener {
        void entitlementsChanged(boolean allPacks, boolean removeAds);
        void message(String text);
    }

    private final Activity activity;
    private final Listener listener;
    private final BillingClient client;
    private final SharedPreferences prefs;
    private final Map<String, ProductDetails> products = new HashMap<>();
    private boolean connected;

    BillingManager(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
        prefs = activity.getSharedPreferences("purchases", Activity.MODE_PRIVATE);
        client = BillingClient.newBuilder(activity)
            .setListener((billingResult, purchases) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (Purchase purchase : purchases) processPurchase(purchase);
                } else if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.USER_CANCELED) {
                    notifyMessage("Purchase unavailable: " + billingResult.getDebugMessage());
                }
            })
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build();
        connect();
    }

    boolean ownsPacks() { return prefs.getBoolean(ALL_PACKS, false); }
    boolean ownsNoAds() { return prefs.getBoolean(REMOVE_ADS, false); }

    private void notifyMessage(String message) { activity.runOnUiThread(() -> listener.message(message)); }

    private void connect() {
        client.startConnection(new BillingClientStateListener() {
            @Override public void onBillingSetupFinished(BillingResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connected = true;
                    loadProducts();
                    restore();
                }
            }
            @Override public void onBillingServiceDisconnected() { connected = false; }
        });
    }

    private void loadProducts() {
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();
        for (String id : Arrays.asList(ALL_PACKS, REMOVE_ADS)) {
            list.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id).setProductType(BillingClient.ProductType.INAPP).build());
        }
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(list).build(),
            (result, response) -> {
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                for (ProductDetails detail : response.getProductDetailsList()) products.put(detail.getProductId(), detail);
            });
    }

    void buy(String productId) {
        if (!connected) { notifyMessage("Google Play is not ready. Try again after connecting to the Play Store."); return; }
        ProductDetails detail = products.get(productId);
        if (detail == null) {
            notifyMessage("Create and activate product '" + productId + "' in Play Console first.");
            return;
        }
        List<BillingFlowParams.ProductDetailsParams> requested = new ArrayList<>();
        requested.add(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(detail).build());
        BillingResult response = client.launchBillingFlow(activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(requested).build());
        if (response.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            notifyMessage("Could not open Google Play checkout: " + response.getDebugMessage());
        }
    }

    void restore() {
        if (!connected) { notifyMessage("Connect to Google Play to restore purchases."); return; }
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP).build(), (result, purchases) -> {
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                boolean packs = false, noAds = false;
                for (Purchase p : purchases) {
                    if (p.getPurchaseState() != Purchase.PurchaseState.PURCHASED) continue;
                    if (p.getProducts().contains(ALL_PACKS)) packs = true;
                    if (p.getProducts().contains(REMOVE_ADS)) noAds = true;
                    if (!p.isAcknowledged()) acknowledge(p);
                }
                // Entitlements cached locally for offline use; Play refreshes on reconnect.
                prefs.edit().putBoolean(ALL_PACKS, packs).putBoolean(REMOVE_ADS, noAds).apply();
                publish();
            });
    }

    private void acknowledge(Purchase purchase) {
        client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.getPurchaseToken()).build(), result -> {
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK)
                    notifyMessage("Purchase acknowledgement pending. Open the app again to retry.");
            });
    }

    private void processPurchase(Purchase purchase) {
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
            notifyMessage("Your purchase is pending. It unlocks after payment completes.");
            return;
        }
        SharedPreferences.Editor e = prefs.edit();
        if (purchase.getProducts().contains(ALL_PACKS)) e.putBoolean(ALL_PACKS, true);
        if (purchase.getProducts().contains(REMOVE_ADS)) e.putBoolean(REMOVE_ADS, true);
        e.apply();
        if (!purchase.isAcknowledged()) acknowledge(purchase);
        publish();
    }

    private void publish() { activity.runOnUiThread(() -> listener.entitlementsChanged(ownsPacks(), ownsNoAds())); }
    void close() { client.endConnection(); }
}
