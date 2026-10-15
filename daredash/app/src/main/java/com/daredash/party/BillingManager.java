package com.daredash.party;

import android.app.Activity;
import android.content.SharedPreferences;
import com.android.billingclient.api.*;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Never grant a new purchase without Google Play server verification. */
final class BillingManager {
    static final String ALL_PACKS = "all_packs";
    static final String REMOVE_ADS = "remove_ads";
    private static final long CACHE_MILLIS = 24L * 60L * 60L * 1000L;

    interface Listener {
        void entitlementsChanged(boolean allPacks, boolean removeAds);
        void message(String text);
    }

    private final Activity activity;
    private final Listener listener;
    private final BillingClient client;
    // Versioned store prevents insecure entitlement data from older builds carrying forward.
    private final SharedPreferences prefs;
    private final Map<String, ProductDetails> products = new HashMap<>();
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private boolean connected;

    BillingManager(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
        prefs = activity.getSharedPreferences("verified_purchases_v2", Activity.MODE_PRIVATE);
        client = BillingClient.newBuilder(activity)
            .setListener((result, purchases) -> {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (Purchase p : purchases) processPurchase(p, true);
                } else if (result.getResponseCode() != BillingClient.BillingResponseCode.USER_CANCELED)
                    notifyMessage("Google Play: " + result.getDebugMessage());
            })
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection().build();
        connect();
    }

    private boolean owned(String product) {
        long stamp = prefs.getLong(product + "_at", 0L);
        return prefs.getBoolean(product, false) && stamp > 0 &&
            System.currentTimeMillis() - stamp < CACHE_MILLIS;
    }
    boolean ownsPacks() { return owned(ALL_PACKS); }
    boolean ownsNoAds() { return owned(REMOVE_ADS); }

    String displayPrice(String id) {
        ProductDetails details = products.get(id);
        ProductDetails.OneTimePurchaseOfferDetails offer = pickOffer(details);
        return offer == null ? "Google Play" : offer.getFormattedPrice();
    }

    private ProductDetails.OneTimePurchaseOfferDetails pickOffer(ProductDetails details) {
        if (details == null) return null;
        List<ProductDetails.OneTimePurchaseOfferDetails> offers = details.getOneTimePurchaseOfferDetailsList();
        if (offers != null && !offers.isEmpty()) return offers.get(0);
        return details.getOneTimePurchaseOfferDetails();
    }

    private void notifyMessage(String s) { activity.runOnUiThread(() -> listener.message(s)); }
    private void publish() { activity.runOnUiThread(() -> listener.entitlementsChanged(ownsPacks(), ownsNoAds())); }

    private void connect() {
        client.startConnection(new BillingClientStateListener() {
            @Override public void onBillingSetupFinished(BillingResult r) {
                if (r.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connected = true;
                    loadProducts();
                    restore();
                } else notifyMessage("Play Billing is not available yet.");
            }
            @Override public void onBillingServiceDisconnected() { connected = false; }
        });
    }

    private void loadProducts() {
        List<QueryProductDetailsParams.Product> queries = new ArrayList<>();
        for (String id : Arrays.asList(ALL_PACKS, REMOVE_ADS))
            queries.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id).setProductType(BillingClient.ProductType.INAPP).build());
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(queries).build(),
            (result, response) -> {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (ProductDetails details : response.getProductDetailsList())
                        products.put(details.getProductId(), details);
                }
            });
    }

    void buy(String id) {
        if (!BuildConfig.BILLING_ENABLED) { notifyMessage("Premium purchases are not available yet."); return; }
        if (!connected) { notifyMessage("Connect to Google Play to purchase."); return; }
        ProductDetails product = products.get(id);
        if (product == null) { notifyMessage("This item is not active in Play Console yet."); return; }
        BillingFlowParams.ProductDetailsParams.Builder purchaseBuilder =
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product);
        ProductDetails.OneTimePurchaseOfferDetails offer = pickOffer(product);
        if (offer != null && offer.getOfferToken() != null && !offer.getOfferToken().isEmpty())
            purchaseBuilder.setOfferToken(offer.getOfferToken());
        BillingFlowParams.ProductDetailsParams requested = purchaseBuilder.build();
        BillingResult r = client.launchBillingFlow(activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(
                Collections.singletonList(requested)).build());
        if (r.getResponseCode() != BillingClient.BillingResponseCode.OK)
            notifyMessage("Google Play checkout was unavailable: " + r.getDebugMessage());
    }

    void restore() {
        if (!connected) { notifyMessage("Connect to Google Play to restore purchases."); return; }
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP).build(), (result, purchases) -> {
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                Set<String> current = new HashSet<>();
                for (Purchase p : purchases) {
                    if (p.getPurchaseState() != Purchase.PurchaseState.PURCHASED) continue;
                    current.addAll(p.getProducts());
                    processPurchase(p, false);
                }
                // Remove entitlements no longer present on the current Google Play account
                // (refunds, revoked transactions, account changes).
                SharedPreferences.Editor e = prefs.edit();
                for (String id : Arrays.asList(ALL_PACKS, REMOVE_ADS)) {
                    if (!current.contains(id)) e.remove(id).remove(id + "_at");
                }
                e.apply();
                publish();
            });
    }

    private void processPurchase(Purchase purchase, boolean notify) {
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
            if (notify) notifyMessage("Payment pending. Content unlocks when payment completes.");
            return;
        }
        String url = BuildConfig.PURCHASE_VERIFY_URL;
        if (url == null || !url.startsWith("https://")) {
            if (notify) notifyMessage("Purchases are not yet activated by the developer.");
            return;  // Never trust client-side token alone.
        }
        for (String id : purchase.getProducts()) {
            if (!ALL_PACKS.equals(id) && !REMOVE_ADS.equals(id)) continue;
            io.execute(() -> {
                boolean verified = verify(url, id, purchase.getPurchaseToken());
                if (!verified) {
                    if (notify) notifyMessage("Payment could not be verified. Try Restore Purchases later.");
                    return;
                }
                prefs.edit().putBoolean(id, true)
                    .putLong(id + "_at", System.currentTimeMillis()).apply();
                publish();
                if (!purchase.isAcknowledged()) acknowledge(purchase);
                if (notify) notifyMessage("Purchase verified and unlocked!");
            });
        }
    }

    private boolean verify(String url, String productId, String purchaseToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            JSONObject body = new JSONObject();
            body.put("productId", productId);
            body.put("purchaseToken", purchaseToken);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            if (connection.getResponseCode() != 200) return false;
            try (InputStream in = connection.getInputStream()) {
                byte[] data = new byte[2048];
                int read = in.read(data);
                if (read < 1) return false;
                JSONObject result = new JSONObject(new String(data, 0, read, StandardCharsets.UTF_8));
                return result.optBoolean("verified", false) && productId.equals(result.optString("productId"));
            }
        } catch (Exception ignored) { return false; }
        finally { if (connection != null) connection.disconnect(); }
    }

    private void acknowledge(Purchase p) {
        client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(p.getPurchaseToken()).build(), r -> {
                if (r.getResponseCode() != BillingClient.BillingResponseCode.OK)
                    notifyMessage("Verification passed, but acknowledgement is pending. Reopen the app to retry.");
            });
    }

    void close() { io.shutdownNow(); client.endConnection(); }
}
