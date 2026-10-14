package com.daredash.party;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Offline, hand-written, general-audience prompts. No account, network or AI API needed. */
final class ChallengeBank {
    static final String MIX = "MIX";
    static final String TRUTH = "TRUTH";
    static final String DARE = "DARE";
    static final String WOULD = "WOULD";
    static final String MOST = "MOST";
    static final String ICE = "ICE";
    static final String CHAOS = "CHAOS";
    static final String DUO = "DUO";

    static class Challenge {
        final String category;
        final String prompt;
        Challenge(String category, String prompt) { this.category = category; this.prompt = prompt; }
        String key() { return category + ":" + prompt; }
    }

    private static void add(List<Challenge> out, String category, String[] values) {
        for (String s : values) out.add(new Challenge(category, s));
    }

    static List<Challenge> all(boolean pro) {
        List<Challenge> result = new ArrayList<>();
        add(result, TRUTH, new String[]{
            "What is a small thing that makes your day better?",
            "What song do you know every word to?",
            "What is your most-used emoji?",
            "Which fictional character would be your best friend?",
            "What is your funniest harmless habit?",
            "What is a skill you wish you had instantly?",
            "What is the most unexpected food you enjoy?",
            "What is the most embarrassing song on your playlist?",
            "What childhood game do you still love?",
            "What is the silliest thing you believed as a kid?",
            "Who in this group could make you laugh on a bad day?",
            "What is a trend you never understood?",
            "What is your most memorable birthday surprise?",
            "What is something you're surprisingly good at?",
            "What is the most ridiculous nickname you've had?",
            "What is one harmless secret talent you have?",
            "What would your autobiography be titled?",
            "What is one thing you'd love to learn this year?",
            "Which movie can you rewatch endlessly?",
            "What is your comfort meal?",
            "What is the nicest compliment you've received?",
            "What is your most unusual collection?",
            "Which app do you open first every morning?",
            "If you could swap jobs for a day, which job would you pick?"
        });
        add(result, DARE, new String[]{
            "Speak like a movie trailer narrator for your next turn.",
            "Do a 15-second dance without music.",
            "Invent a handshake with the player on your left.",
            "Make your best animal impression; let everyone guess.",
            "Say the alphabet in your fanciest accent.",
            "Pretend you're accepting an award for being awesome.",
            "Tell a 20-second story using the words pizza, moon and shoes.",
            "Draw a cat with your eyes closed.",
            "Balance a book on your head for 10 seconds, if comfortable.",
            "Give every player a fun superhero name.",
            "Pretend to host a cooking show for an invisible meal.",
            "Make a funny face and hold it for five seconds.",
            "Sing the chorus of a song as a robot.",
            "Try a tongue twister three times quickly.",
            "Walk across the room like a friendly penguin.",
            "Invent a new emoji and explain what it means.",
            "Tell a joke in a serious newsreader voice.",
            "Act out a hobby without talking; have others guess.",
            "Do a dramatic slow-motion high five with a volunteer.",
            "Give a one-minute tour of an imaginary spaceship.",
            "Pretend your shoe is a famous celebrity.",
            "Describe the room like a nature documentary.",
            "Create a five-word slogan for this group.",
            "Teach everyone a silly seated dance move."
        });
        add(result, WOULD, new String[]{
            "Would you rather travel to the past or the future?",
            "Would you rather have a pet dragon or a pet unicorn?",
            "Would you rather fly or breathe underwater?",
            "Would you rather only whisper or only sing for one day?",
            "Would you rather live by a beach or in the mountains?",
            "Would you rather have free pizza or free ice cream for a year?",
            "Would you rather understand animals or speak every language?",
            "Would you rather be great at painting or playing piano?",
            "Would you rather visit the moon or the deep ocean?",
            "Would you rather have a personal chef or a personal driver?",
            "Would you rather watch a comedy or a mystery tonight?",
            "Would you rather have a rewind button or a pause button?",
            "Would you rather always be early or exactly on time?",
            "Would you rather lose your phone or your favorite shoes for a day?",
            "Would you rather live in a treehouse or a houseboat?",
            "Would you rather control the weather or speak to plants?",
            "Would you rather eat breakfast for dinner or dinner for breakfast?",
            "Would you rather be an astronaut or an explorer?",
            "Would you rather have a tiny elephant or giant hamster?",
            "Would you rather win a dance contest or a cooking contest?",
            "Would you rather have a movie made about you or write a bestseller?",
            "Would you rather always have perfect hair or perfect timing?",
            "Would you rather visit every country or learn every language?",
            "Would you rather go camping or take a city break?"
        });
        add(result, MOST, new String[]{
            "Who is most likely to become famous for something funny?",
            "Who is most likely to start a dance party?",
            "Who is most likely to forget why they entered a room?",
            "Who is most likely to win a game show?",
            "Who is most likely to adopt an unusual pet?",
            "Who is most likely to accidentally become a meme?",
            "Who is most likely to plan a surprise trip?",
            "Who is most likely to befriend a stranger on vacation?",
            "Who is most likely to write a bestselling novel?",
            "Who is most likely to get lost in a museum?",
            "Who is most likely to invent an amazing snack?",
            "Who is most likely to laugh during a serious movie scene?",
            "Who is most likely to survive a week without a phone?",
            "Who is most likely to start a successful band?",
            "Who is most likely to remember everyone's birthday?",
            "Who is most likely to win a costume contest?",
            "Who is most likely to become a famous chef?",
            "Who is most likely to organize a reunion?",
            "Who is most likely to have the best travel stories?",
            "Who is most likely to talk to a houseplant?",
            "Who is most likely to learn a new skill overnight?",
            "Who is most likely to win at charades?",
            "Who is most likely to name every neighborhood cat?",
            "Who is most likely to take the best group photo?"
        });
        add(result, ICE, new String[]{
            "What is your dream weekend?",
            "What three words describe your personality?",
            "What is one place you want to visit?",
            "What is a meal you could eat every week?",
            "What would your dream home include?",
            "What is a recent small win you're proud of?",
            "Which book, show or movie would you recommend?",
            "What is your favorite way to spend a rainy day?",
            "What is a tradition you'd like to start?",
            "What sport or game should everyone try?",
            "What is your favorite smell?",
            "If you opened a café, what would you name it?",
            "What animal matches your personality?",
            "What is the best gift you ever gave someone?",
            "What is one skill you can teach the group?",
            "Which season fits your personality?",
            "What is a hobby you've always wanted to try?",
            "If you owned a tiny island, what would you call it?",
            "What is your favorite childhood snack?",
            "What would you put in a time capsule?",
            "If you could open any shop, what would it sell?",
            "What is the best advice you have heard?",
            "What is your ideal theme party?",
            "What always makes you smile?"
        });
        if (pro) {
            add(result, CHAOS, new String[]{
                "Every player invents a dance move. Perform them in order.",
                "Make up a commercial for an imaginary product in 20 seconds.",
                "The group creates a story one word at a time for 30 seconds.",
                "Pretend you're all trapped in an elevator in a comedy movie.",
                "Invent a new sport and explain its three rules.",
                "Speak only in questions for your next full turn.",
                "Give a dramatic speech about why socks deserve awards.",
                "Everyone poses like a rock band album cover.",
                "Have a 15-second pretend debate about pineapple on pizza.",
                "Sell a paperclip like it's a luxury item.",
                "Create a group chant in under 20 seconds.",
                "Act out an alien trying to order ice cream."
            });
            add(result, DUO, new String[]{
                "Tell a friend one quality you admire about them.",
                "Invent a funny duo nickname together.",
                "Take turns listing your dream destinations.",
                "Plan an imaginary weekend adventure together.",
                "Act out two detectives discovering a missing cookie.",
                "Take turns naming three activities you both enjoy.",
                "Try to say the same random word at the same time.",
                "Give each other a positive compliment.",
                "Design a two-person superhero team and its powers.",
                "Recreate a movie poster pose together.",
                "Describe a dream road trip in three stops.",
                "Invent a friendly greeting only you two understand."
            });
        }
        return result;
    }

    static Challenge pick(String mode, boolean pro, Set<String> used, Random random) {
        List<Challenge> pool = new ArrayList<>();
        for (Challenge c : all(pro)) {
            if (MIX.equals(mode) || mode.equals(c.category)) pool.add(c);
        }
        if (pool.isEmpty()) return new Challenge(TRUTH, "Share one fun fact about yourself.");
        List<Challenge> fresh = new ArrayList<>();
        for (Challenge c : pool) if (!used.contains(c.key())) fresh.add(c);
        if (fresh.isEmpty()) {
            for (Challenge c : pool) used.remove(c.key());
            fresh.addAll(pool);
        }
        Challenge chosen = fresh.get(random.nextInt(fresh.size()));
        used.add(chosen.key());
        return chosen;
    }
}
