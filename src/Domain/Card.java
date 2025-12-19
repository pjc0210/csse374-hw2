package Domain;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a card in the Splendor game.
 * Each card has victory points and a cost in gem chips.
 */
public class Card {
    private int victoryPoints;
    private Map<Character, Integer> cost; // Color -> amount mapping
    private char gemType; // The gem type this card provides as a bonus

    public Card(int victoryPoints, String costString, char gemType) {
        this.victoryPoints = victoryPoints;
        this.cost = parseCost(costString);
        this.gemType = gemType;
    }

    /**
     * Parses a cost string into a map of gem colors to amounts.
     * Format example: "R3,G2,B1" means 3 red, 2 green, 1 blue
     */
    private Map<Character, Integer> parseCost(String costString) {
        Map<Character, Integer> costMap = new HashMap<>();
        if (costString == null || costString.isEmpty()) {
            return costMap;
        }

        String[] parts = costString.split(",");
        for (String part : parts) {
            if (part.length() >= 2) {
                char color = part.charAt(0);
                int amount = Integer.parseInt(part.substring(1));
                costMap.put(color, amount);
            }
        }
        return costMap;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public Map<Character, Integer> getCost() {
        return new HashMap<>(cost);
    }

    public char getGemType() {
        return gemType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Card[VP:").append(victoryPoints);
        sb.append(", Gem:").append(gemType);
        sb.append(", Cost:");
        if (cost.isEmpty()) {
            sb.append("Free");
        } else {
            for (Map.Entry<Character, Integer> entry : cost.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}