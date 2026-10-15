package com.daredash.party;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class ChallengeBankTest {
    @Test public void freeBankContainsAtLeast120UniqueChallenges() {
        List<ChallengeBank.Challenge> cards = ChallengeBank.all(false);
        assertTrue(cards.size() >= 120);
        Set<String> keys = new HashSet<>();
        for (ChallengeBank.Challenge card : cards) {
            assertNotNull(card.prompt);
            assertFalse(card.prompt.trim().isEmpty());
            assertTrue("Duplicate challenge: " + card.key(), keys.add(card.key()));
        }
    }
    @Test public void premiumBankAddsContent() {
        List<ChallengeBank.Challenge> free = ChallengeBank.all(false);
        List<ChallengeBank.Challenge> plus = ChallengeBank.all(true);
        assertTrue(plus.size() > free.size());
    }
}
