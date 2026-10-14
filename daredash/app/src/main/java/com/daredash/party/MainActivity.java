package com.daredash.party;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Entire app works offline (except optional ads/purchases). No server or login required. */
public final class MainActivity extends Activity implements BillingManager.Listener {
    private static final int BG = Color.rgb(18, 17, 47);
    private static final int PANEL = Color.rgb(36, 34, 81);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(186, 188, 217);
    private static final int PURPLE = Color.rgb(118, 85, 255);
    private static final int GOLD = Color.rgb(255, 201, 101);

    private final List<String> players = new ArrayList<>();
    private final Map<String, Integer> scores = new HashMap<>();
    private final Set<String> seen = new HashSet<>();
    private final Random random = new Random();
    private String mode = ChallengeBank.MIX;
    private ChallengeBank.Challenge challenge;
    private int turn, round, skips = 3;
    private boolean premium, noAds;
    private boolean inGame = false;
    private LinearLayout main;
    private EditText input;
    private CountDownTimer countdown;
    private AdView banner;
    private AdsManager ads;
    private BillingManager billing;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        players.add("Player 1");
        players.add("Player 2");
        ads = new AdsManager(this);
        billing = new BillingManager(this, this);
        showHome();
        handleInvite(getIntent());
        ads.start(() -> {
            // Consent changes are reflected on the next screen; do not interrupt a live challenge.
            if (!inGame && !isFinishing()) showHome();
        });
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleInvite(intent);
    }

    private void handleInvite(Intent intent) {
        if (intent == null || intent.getData() == null) return;
        Uri uri = intent.getData();
        if (!"daredash".equals(uri.getScheme()) || !"invite".equals(uri.getHost())) return;
        String names = uri.getQueryParameter("players");
        if (names == null || names.length() > 500) return;
        int added = 0;
        for (String candidate : names.split(",")) {
            String name = candidate.trim();
            if (name.length() < 1 || name.length() > 24 || players.size() >= 8) continue;
            boolean duplicate = false;
            for (String existing : players) if (existing.equalsIgnoreCase(name)) duplicate = true;
            if (!duplicate) { players.add(name); added++; }
        }
        if (added > 0) {
            showHome();
            toast("Imported " + added + " player(s). This invite sets up a local game; it does not sync live turns.");
        }
    }

    private void shareInvite() {
        StringBuilder names = new StringBuilder();
        for (String player : players) {
            if (names.length() > 0) names.append(',');
            names.append(player.replace(",", " "));
        }
        String invitation = "daredash://invite?players=" + Uri.encode(names.toString());
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT,
            "Join our DareDash party! We play together on one phone. " +
            "Add your name to the host's game. You can also import our player list on your phone if DareDash is installed: " + invitation);
        startActivity(Intent.createChooser(share, "Invite friends with WhatsApp, SMS or more"));
    }

    private void addMultiplePlayers() {
        EditText field = new EditText(this);
        field.setHint("Amina, Sami, Hassan");
        field.setSingleLine(false);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Add several friends")
            .setMessage("Separate names with commas. Maximum 8 total players.")
            .setView(field)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add players", (d, which) -> {
                int added = 0;
                for (String raw : field.getText().toString().split(",")) {
                    String name = raw.trim();
                    if (name.isEmpty() || name.length() > 24 || players.size() >= 8) continue;
                    boolean exists = false;
                    for (String existing : players) if (existing.equalsIgnoreCase(name)) exists = true;
                    if (!exists) { players.add(name); added++; }
                }
                toast("Added " + added + " friends.");
                showHome();
            }).show();
    }

    private int dp(float value) { return Math.round(getResources().getDisplayMetrics().density * value); }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(color);
        v.setTextSize(sp);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setLineSpacing(dp(2), 1.04f);
        return v;
    }

    private void gap(LinearLayout c, int size) {
        View spacer = new View(this);
        c.addView(spacer, new LinearLayout.LayoutParams(1, dp(size)));
    }

    private LinearLayout column(int pad) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(pad), dp(pad), dp(pad), dp(pad));
        return c;
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        return r;
    }

    private void attach(LinearLayout parent, View view, int weight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(weight > 0 ? 0 : -1, -2, weight);
        parent.addView(view, params);
    }

    private TextView action(String label, int background, int foreground, Runnable click) {
        TextView v = text(label, 15, foreground, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(13), dp(15), dp(13), dp(15));
        v.setBackground(shape(background, 18));
        v.setOnClickListener(view -> click.run());
        return v;
    }

    private void showPage(String subtitle) {
        if (countdown != null) { countdown.cancel(); countdown = null; }
        if (banner != null) { banner.destroy(); banner = null; }
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        main = column(22);
        main.setPadding(dp(22), dp(25), dp(22), dp(36));
        scroll.addView(main);
        setContentView(scroll);
        TextView logo = text("✦  DARE DASH", 23, WHITE, true);
        main.addView(logo);
        gap(main, 8);
        main.addView(text(subtitle, 14, MUTED, false));
        gap(main, 24);
    }

    private void tile(String title, String detail, String emoji) {
        LinearLayout c = column(17);
        c.setBackground(shape(PANEL, 20));
        c.addView(text(emoji + "  " + title, 19, WHITE, true));
        gap(c, 6);
        c.addView(text(detail, 13, MUTED, false));
        main.addView(c);
    }

    private void addBanner() {
        if (noAds) return;
        gap(main, 18);
        TextView label = text(BuildConfig.DEBUG ? "AD · TEST INVENTORY" : "ADVERTISEMENT", 10, MUTED, false);
        label.setGravity(Gravity.CENTER);
        main.addView(label);
        gap(main, 5);
        try {
            banner = ads.makeBanner();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(55));
            params.gravity = Gravity.CENTER_HORIZONTAL;
            if (banner != null) main.addView(banner, params);
            else main.removeView(label);
        } catch (Exception ignored) { /* Offline party play remains usable. */ }
    }

    private void showHome() {
        inGame = false;
        showPage("Your next unforgettable game night starts here.");
        tile("Play together", "Pick a mode, take turns, earn points, and laugh. Safe, lighthearted challenges for friends and families.", "🎉");
        gap(main, 20);
        main.addView(text("YOUR PLAYERS", 13, GOLD, true));
        gap(main, 10);
        for (String player : new ArrayList<>(players)) {
            LinearLayout r = row();
            TextView name = text("●  " + player, 16, WHITE, true);
            r.addView(name, new LinearLayout.LayoutParams(0, dp(47), 1));
            name.setGravity(Gravity.CENTER_VERTICAL);
            TextView remove = action("×", PANEL, MUTED, () -> { players.remove(player); showHome(); });
            r.addView(remove, new LinearLayout.LayoutParams(dp(48), dp(43)));
            main.addView(r);
            gap(main, 4);
        }
        LinearLayout add = row();
        input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(15);
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);
        input.setHint("Add a friend's name");
        input.setPadding(dp(12), dp(8), dp(8), dp(8));
        input.setBackground(shape(PANEL, 15));
        add.addView(input, new LinearLayout.LayoutParams(0, dp(51), 1));
        TextView plus = action("+ Add", PURPLE, WHITE, this::addPlayer);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(51));
        p.leftMargin = dp(8);
        add.addView(plus, p);
        main.addView(add);
        gap(main, 10);
        main.addView(action("👥  Add several friends", PANEL, WHITE, this::addMultiplePlayers));
        gap(main, 10);
        main.addView(action("📨  Invite via WhatsApp, SMS or any app", PANEL, WHITE, this::shareInvite));
        gap(main, 7);
        main.addView(text("Invitations are for pass-and-play on one phone. Remote synchronized rooms need an online backend.", 12, MUTED, false));
        gap(main, 20);
        main.addView(text("CHOOSE A GAME", 13, GOLD, true));
        gap(main, 9);
        addModes();
        gap(main, 14);
        main.addView(action("LET'S PLAY  →", PURPLE, WHITE, this::startGame));
        gap(main, 10);
        main.addView(action("🔒  Premium packs & upgrades", PANEL, WHITE, this::showShop));
        gap(main, 9);
        main.addView(action("⚙  Advertising privacy choices", PANEL, WHITE,
            () -> ads.showPrivacyOptions(() -> { if (!inGame) showHome(); })));
        gap(main, 14);
        main.addView(text("No sign-up needed  •  120 free cards  •  Play offline", 12, MUTED, false));
        addBanner();
    }

    private void addPlayer() {
        String player = input.getText().toString().trim();
        if (player.length() > 24) { toast("Names must be 24 characters or fewer."); return; }
        if (player.isEmpty()) { toast("Enter a player name first."); return; }
        if (players.size() >= 8) { toast("Maximum 8 players per game."); return; }
        for (String p : players) if (p.equalsIgnoreCase(player)) { toast("Name already in use."); return; }
        players.add(player);
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
        showHome();
    }

    private void addModes() {
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = row();
        String[] ids = {ChallengeBank.MIX, ChallengeBank.TRUTH, ChallengeBank.DARE,
            ChallengeBank.WOULD, ChallengeBank.MOST, ChallengeBank.ICE,
            ChallengeBank.CHAOS, ChallengeBank.DUO};
        String[] labels = {"🎲 Mix", "💬 Truth", "⚡ Dare", "🤔 Would you", "👑 Most likely", "👋 Icebreaker", "🔒 Chaos", "🔒 Duos"};
        for (int i = 0; i < ids.length; i++) {
            final String id = ids[i];
            TextView chip = action(labels[i], mode.equals(id) ? PURPLE : PANEL, WHITE, () -> {
                if (!premium && (id.equals(ChallengeBank.CHAOS) || id.equals(ChallengeBank.DUO))) {
                    showShop();
                    return;
                }
                mode = id;
                showHome();
            });
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
            cp.rightMargin = dp(8);
            chips.addView(chip, cp);
        }
        hsv.addView(chips);
        main.addView(hsv);
    }

    private void startGame() {
        if (players.size() < 2) { toast("Add at least two players to start."); return; }
        turn = 0;
        round = 1;
        skips = 3;
        scores.clear();
        seen.clear();
        for (String p : players) scores.put(p, 0);
        inGame = true;
        nextChallenge();
    }

    private void nextChallenge() {
        challenge = ChallengeBank.pick(mode, premium, seen, random);
        showGame();
    }

    private void showGame() {
        inGame = true;
        showPage("Round " + round + "   •   " + players.size() + " players   •   " + skips + " re-rolls");
        LinearLayout top = column(16);
        top.setBackground(shape(PANEL, 18));
        top.addView(text("IT'S YOUR TURN", 12, GOLD, true));
        gap(top, 7);
        top.addView(text(players.get(turn) + " ✨", 26, WHITE, true));
        main.addView(top);
        gap(main, 14);
        LinearLayout card = column(22);
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(109, 76, 255), Color.rgb(204, 85, 167)});
        gradient.setCornerRadius(dp(22));
        card.setBackground(gradient);
        card.addView(text("✦  " + friendly(challenge.category).toUpperCase(Locale.ROOT), 12, GOLD, true));
        gap(card, 24);
        card.addView(text(challenge.prompt, 24, WHITE, true));
        gap(card, 30);
        TextView time = text("⏱  30 seconds", 14, WHITE, true);
        card.addView(time);
        main.addView(card);
        countdown = new CountDownTimer(31000, 1000) {
            @Override public void onTick(long remaining) { time.setText("⏱  " + (remaining / 1000) + " seconds"); }
            @Override public void onFinish() { time.setText("⏰  Time's up! Complete or pass."); }
        }.start();
        gap(main, 15);
        main.addView(action("✅  Complete challenge   +10", GOLD, BG, () -> { scoreCurrent(); advance(); }));
        gap(main, 9);
        main.addView(action("↪  Pass & next player's turn", PANEL, WHITE, this::advance));
        gap(main, 9);
        main.addView(action("🎲  Re-roll this card (" + skips + " left)", PANEL, WHITE, () -> {
            if (skips > 0) { skips--; nextChallenge(); }
            else toast("No re-rolls left. Watch an optional video for 3 more.");
        }));
        gap(main, 9);
        main.addView(action(noAds ? "🎁  Claim +3 free re-rolls" : "▶  Watch a video for +3 re-rolls", PANEL, WHITE, () -> {
            if (noAds) { skips += 3; toast("+3 re-rolls added! Thanks for upgrading."); showGame(); }
            else ads.showReward(() -> runOnUiThread(() -> {
                skips += 3;
                toast("You earned +3 re-rolls!");
                showGame();
            }));
        }));
        gap(main, 20);
        main.addView(text("LEADERBOARD", 13, GOLD, true));
        gap(main, 8);
        for (String player : players) {
            LinearLayout r = row();
            TextView name = text((player.equals(players.get(turn)) ? "▶  " : "   ") + player, 14, WHITE, true);
            TextView points = text(scores.getOrDefault(player, 0) + " pts", 14, GOLD, true);
            r.addView(name, new LinearLayout.LayoutParams(0, dp(33), 1));
            name.setGravity(Gravity.CENTER_VERTICAL);
            r.addView(points);
            main.addView(r);
        }
        gap(main, 13);
        main.addView(action("Finish game & show winner", PANEL, WHITE, this::showResults));
        addBanner();
    }

    private String friendly(String category) {
        switch (category) {
            case ChallengeBank.WOULD: return "Would you rather";
            case ChallengeBank.MOST: return "Most likely to";
            case ChallengeBank.ICE: return "Icebreaker";
            case ChallengeBank.DUO: return "Duos";
            default: return category.substring(0, 1) + category.substring(1).toLowerCase(Locale.ROOT);
        }
    }

    private void scoreCurrent() {
        String player = players.get(turn);
        scores.put(player, scores.getOrDefault(player, 0) + 10);
    }

    private void advance() {
        turn++;
        if (turn >= players.size()) { turn = 0; round++; }
        nextChallenge();
    }

    private void showResults() {
        inGame = false;
        showPage("Great games deserve a great finale.");
        int best = -1;
        for (String p : players) best = Math.max(best, scores.getOrDefault(p, 0));
        List<String> winners = new ArrayList<>();
        for (String p : players) if (scores.getOrDefault(p, 0) == best) winners.add(p);
        tile(winners.size() == 1 ? "🏆 " + winners.get(0) + " wins!" : "🏆 It's a tie!",
            "Top score: " + best + " points. Played " + round + " rounds with a lot of laughs.", "🎊");
        gap(main, 18);
        for (String p : players) {
            LinearLayout r = row();
            TextView name = text(p, 16, WHITE, true);
            r.addView(name, new LinearLayout.LayoutParams(0, dp(38), 1));
            r.addView(text(scores.getOrDefault(p, 0) + " points", 16, GOLD, true));
            main.addView(r);
        }
        gap(main, 16);
        final int winningScore = best;
        main.addView(action("Share the winner 🏆", PURPLE, WHITE, () -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "We played DareDash! 🏆 Winner: " + android.text.TextUtils.join(", ", winners)
                + " with " + winningScore + " points. Challenge your friends next!");
            startActivity(Intent.createChooser(intent, "Share your score"));
        }));
        gap(main, 10);
        main.addView(action("Play again", PANEL, WHITE, this::startGame));
        gap(main, 10);
        main.addView(action("Home", PANEL, WHITE, this::showHome));
        addBanner();
    }

    private void showShop() {
        inGame = false;
        showPage("Keep the laughs going with optional upgrades.");
        tile("✨ All premium challenge packs", "Unlock Chaos Mode and Duos: 24 more original challenges. One-time purchase, not a subscription.", "🎁");
        gap(main, 11);
        main.addView(action(premium ? "✓ Premium packs unlocked" : "Unlock packs · " + billing.displayPrice(BillingManager.ALL_PACKS), PURPLE, WHITE,
            () -> { if (!premium) billing.buy(BillingManager.ALL_PACKS); }));
        gap(main, 18);
        tile("🚫 Remove banner ads", "One-time upgrade. Rewarded videos become optional free re-roll claims instead.", "⚡");
        gap(main, 11);
        main.addView(action(noAds ? "✓ Banner ads removed" : "Remove ads · " + billing.displayPrice(BillingManager.REMOVE_ADS), PURPLE, WHITE,
            () -> { if (!noAds) billing.buy(BillingManager.REMOVE_ADS); }));
        gap(main, 14);
        main.addView(action("Restore Google Play purchases", PANEL, WHITE, billing::restore));
        gap(main, 10);
        main.addView(action("← Back to game setup", PANEL, WHITE, this::showHome));
        gap(main, 18);
        main.addView(text("Products and prices appear only after the two items are activated in Google Play Console. No purchase is needed to play.", 12, MUTED, false));
        addBanner();
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }

    @Override public void entitlementsChanged(boolean packs, boolean adsRemoved) {
        boolean changed = packs != premium || adsRemoved != noAds;
        premium = packs;
        noAds = adsRemoved;
        if (!premium && (ChallengeBank.CHAOS.equals(mode) || ChallengeBank.DUO.equals(mode))) mode = ChallengeBank.MIX;
        if (changed && !inGame) showHome();
    }

    @Override public void message(String text) { toast(text); }

    @Override public void onBackPressed() {
        if (inGame) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Leave this game?")
                .setMessage("Current round scores will be lost.")
                .setNegativeButton("Keep playing", null)
                .setPositiveButton("Leave", (dialog, which) -> showHome())
                .show();
        } else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (countdown != null) countdown.cancel();
        if (banner != null) banner.destroy();
        if (billing != null) billing.close();
        super.onDestroy();
    }
}
