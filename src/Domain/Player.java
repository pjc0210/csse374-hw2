package Domain;

import java.util.HashMap;
import java.util.Map;

/** Player with chips (colors represented by single chars) and VP. */
public class Player {
    private String name;
    private int totalVP;
    private Map<Character, Integer> chips; // e.g., 'R' -> 2, 'B' -> 1, 'K' -> gold

    public Player(String name) {
        this.name = name == null ? "Player" : name;
        this.totalVP = 0;
        this.chips = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public int getTotalVP() {
        return totalVP;
    }

    public Map<Character, Integer> getChips() {
        return chips;
    }

    /** Draw a chip of color (single-letter string). Returns true if successful. */
    public boolean drawChip(String color) {
        if (color == null || color.isEmpty()) return false;
        char c = Character.toUpperCase(color.charAt(0));
        chips.put(c, chips.getOrDefault(c, 0) + 1);
        return true;
    }

    /**
     * Try to buy a card. Returns true if purchase succeeded (chips and VP updated).
     */
    public boolean buyCard(Card card) {
        Map<Character, Integer> cost = card.getCostMap();
        // compute deficit after using existing chips; gold 'K' can be used as wildcard
        int goldAvailable = chips.getOrDefault('K', 0);
        int totalDeficit = 0;
        Map<Character, Integer> deficits = new HashMap<>();
        for (Map.Entry<Character, Integer> e : cost.entrySet()) {
            char color = e.getKey();
            int need = e.getValue();
            int have = chips.getOrDefault(color, 0);
            int deficit = Math.max(0, need - have);
            deficits.put(color, deficit);
            totalDeficit += deficit;
        }
        if (totalDeficit > goldAvailable) return false; // not enough even using gold

        // deduct specific colors first
        for (Map.Entry<Character, Integer> e : cost.entrySet()) {
            char color = e.getKey();
            int need = e.getValue();
            int have = chips.getOrDefault(color, 0);
            int use = Math.min(have, need);
            if (use > 0) {
                chips.put(color, have - use);
            }
        }
        // spend gold for remaining deficits
        int goldToSpend = 0;
        for (int d : deficits.values()) goldToSpend += d;
        if (goldToSpend > 0) {
            chips.put('K', goldAvailable - goldToSpend);
        }
        // award VP
        totalVP += card.getVictoryPoints();
        return true;
    }

    public void resetChips() {
        chips.clear();
    }

    public void resetVP() {
        totalVP = 0;
    }

    @Override
    public String toString() {
        return name + "(VP=" + totalVP + ", chips=" + chips + ")";
    }
}
