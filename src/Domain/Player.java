package Domain;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the Splendor game.
 * Each player has chips, cards, and victory points.
 */
public class Player {
    private int totalVP;
    private Map<Character, Integer> chips;
    private Map<Character, Integer> cardBonuses; // Bonuses from purchased cards
    private List<Card> purchasedCards;
    private String name;

    public Player(String name) {
        this.name = name;
        this.totalVP = 0;
        this.chips = new HashMap<>();
        this.cardBonuses = new HashMap<>();
        this.purchasedCards = new ArrayList<>();

        // Initialize chip counts to 0
        initializeChips();
    }

    private void initializeChips() {
        // Common gem colors in Splendor: R(red), G(green), B(blue), W(white), K(black), Y(yellow/gold)
        chips.put('R', 0);
        chips.put('G', 0);
        chips.put('B', 0);
        chips.put('W', 0);
        chips.put('K', 0);
        chips.put('Y', 0); // Gold/wild

        cardBonuses.put('R', 0);
        cardBonuses.put('G', 0);
        cardBonuses.put('B', 0);
        cardBonuses.put('W', 0);
        cardBonuses.put('K', 0);
    }

    /**
     * Draw a chip of the specified color.
     * @param color The color of the chip to draw
     * @return true if successful, false otherwise
     */
    public boolean drawChip(String color) {
        if (color == null || color.isEmpty()) {
            return false;
        }

        char colorChar = color.charAt(0);
        if (!chips.containsKey(colorChar)) {
            return false;
        }

        // Check if player has room (typical limit is 10 chips)
        int totalChips = getTotalChips();
        if (totalChips >= 10) {
            return false;
        }

        chips.put(colorChar, chips.get(colorChar) + 1);
        return true;
    }

    /**
     * Attempt to buy a card.
     * @param card The card to purchase
     * @return true if purchase successful, false otherwise
     */
    public boolean buyCard(Card card) {
        if (card == null) {
            return false;
        }

        // Check if player can afford the card
        if (!canAfford(card)) {
            return false;
        }

        // Pay for the card
        payForCard(card);

        // Add card to player's collection
        purchasedCards.add(card);
        totalVP += card.getVictoryPoints();

        // Add card bonus
        char gemType = card.getGemType();
        if (cardBonuses.containsKey(gemType)) {
            cardBonuses.put(gemType, cardBonuses.get(gemType) + 1);
        }

        return true;
    }

    /**
     * Check if player can afford a card.
     */
    private boolean canAfford(Card card) {
        Map<Character, Integer> cost = card.getCost();
        int goldNeeded = 0;

        for (Map.Entry<Character, Integer> entry : cost.entrySet()) {
            char color = entry.getKey();
            int needed = entry.getValue();

            // Account for card bonuses
            int bonus = cardBonuses.getOrDefault(color, 0);
            int chipsAvailable = chips.getOrDefault(color, 0);

            int deficit = needed - bonus - chipsAvailable;
            if (deficit > 0) {
                goldNeeded += deficit;
            }
        }

        // Check if we have enough gold chips to cover the deficit
        return goldNeeded <= chips.getOrDefault('Y', 0);
    }

    /**
     * Pay for a card by spending chips.
     */
    private void payForCard(Card card) {
        Map<Character, Integer> cost = card.getCost();
        int goldUsed = 0;

        for (Map.Entry<Character, Integer> entry : cost.entrySet()) {
            char color = entry.getKey();
            int needed = entry.getValue();

            // Apply card bonuses first
            int bonus = cardBonuses.getOrDefault(color, 0);
            needed -= bonus;

            if (needed <= 0) continue;

            // Then use chips
            int chipsAvailable = chips.getOrDefault(color, 0);
            int chipsToSpend = Math.min(needed, chipsAvailable);
            chips.put(color, chipsAvailable - chipsToSpend);
            needed -= chipsToSpend;

            // Use gold for remainder
            goldUsed += needed;
        }

        // Spend gold chips
        if (goldUsed > 0) {
            chips.put('Y', chips.get('Y') - goldUsed);
        }
    }

    private int getTotalChips() {
        int total = 0;
        for (int count : chips.values()) {
            total += count;
        }
        return total;
    }

    public String getName() {
        return name;
    }

    public int getTotalVP() {
        return totalVP;
    }

    public Map<Character, Integer> getChips() {
        return new HashMap<>(chips);
    }

    public List<Card> getPurchasedCards() {
        return new ArrayList<>(purchasedCards);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player: ").append(name);
        sb.append(" | VP: ").append(totalVP);
        sb.append(" | Chips: ");
        for (Map.Entry<Character, Integer> entry : chips.entrySet()) {
            if (entry.getValue() > 0) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
            }
        }
        sb.append("| Cards: ").append(purchasedCards.size());
        return sb.toString();
    }
}