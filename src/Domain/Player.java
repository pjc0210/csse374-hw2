package Domain;

import java.util.HashMap;
import java.util.Map;

/** Player with chips (colors represented by single chars) and VP. */
public class Player {
    private String name;
    private int totalVP;
    private Map<Character, Integer> chips; // e.g., 'R' -> 2, 'B' -> 1, 'K' -> gold

    public Player(String name, int initVP, Map<Character, Integer> initialChips){
        System.out.println("Initial chips map: " + initialChips);
        this.name = name == null ? "Player" : name;
        this.totalVP = initVP;
        this.chips = new HashMap<>(initialChips);
    }

    public Player(String name){
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
        for (Map.Entry<Character, Integer> e : cost.entrySet()) {
            char color = e.getKey();
            int need = e.getValue();
            int have = chips.getOrDefault(color, 0);
            int deficit = Math.max(0, need - have);
            if (deficit > 0) return false;
        }

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
