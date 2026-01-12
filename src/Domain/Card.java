package Domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple Card model (victoryPoints and cost string like "1B2W").
 */
public class Card {
    public int id;
    private int victoryPoints;
    private String cost; // format example: "1B2W" meaning 1 Blue, 2 White

    public Card(int victoryPoints, String cost, int id) {
        this.victoryPoints = victoryPoints;
        this.cost = cost == null ? "" : cost;
        this.id = id;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public String getCost() {
        return cost;
    }

    /**
     * Parse the cost string into a map of color char -> required count.
     */
    public Map<Character, Integer> getCostMap() {
        Map<Character, Integer> m = new HashMap<>();
        String s = cost.trim();
        int i = 0;
        while (i < s.length()) {
            // read number
            int start = i;
            while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            if (start == i) break;
            int count = Integer.parseInt(s.substring(start, i));
            if (i >= s.length()) break;
            char color = s.charAt(i++);
            m.put(color, m.getOrDefault(color, 0) + count);
        }
        return m;
    }

    @Override
    public String toString() {
        return "Card[vp=" + victoryPoints + ", cost=" + cost + "]";
    }
}
