package Domain;

import Data.DataLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic game engine for a minimal Splendor-like game. Parses the provided
 * database.json (very forgiving parsing) and provides simple moves.
 */
public class GameBoy {
    private List<Card> cards = new ArrayList<>();
    private List<Player> players = new ArrayList<>();
    private Player currPlayer;
    private int currentPlayerIndex = 0;
    private Map<Character, Integer> currChips = new java.util.HashMap<>();
    private boolean chipDrawn = false;
    private DataLoader dataLoader = new DataLoader();

    public List<Player> getPlayers() { return players; }
    public List<Card> getCards() { return cards; }
//    public int getCurrentPlayerIndex() { return currentPlayerIndex; }

    public void newGame() {
        // reset players' chips and VP, reset current turn, generate random cards
        for (Player p : players) {
            p.resetChips();
            p.resetVP();
        }
        currentPlayerIndex = 0;
        currPlayer = players.isEmpty() ? null : players.get(0);
        currChips.clear();
        generateRandomCards();
        saveGameState();
    }

    private void generateRandomCards() {
        cards.clear();
        java.util.Random rand = new java.util.Random();
        String[] colors = {"R", "B", "G", "K", "W"};
        for (int i = 0; i < 15; i++) {
            // pick 1-3 colors for this card
            int numColors = 1 + rand.nextInt(3); // 1, 2, or 3 colors
            java.util.List<String> selectedColors = new java.util.ArrayList<>();
            while (selectedColors.size() < numColors) {
                String color = colors[rand.nextInt(colors.length)];
                if (!selectedColors.contains(color)) {
                    selectedColors.add(color);
                }
            }
            
            // generate costs: each color gets 0-3 tokens, but at least one color must have 2+
            StringBuilder cost = new StringBuilder();
            int[] costs = new int[selectedColors.size()];
            int minIdx = rand.nextInt(selectedColors.size());
            costs[minIdx] = 2 + rand.nextInt(2); // ensure at least one color has 2-3 tokens
            for (int j = 0; j < costs.length; j++) {
                if (j != minIdx) {
                    costs[j] = rand.nextInt(4); // 0-3 tokens
                }
            }
            
            // build cost string
            for (int j = 0; j < selectedColors.size(); j++) {
                if (costs[j] > 0) {
                    cost.append(costs[j]).append(selectedColors.get(j));
                }
            }
            
            // calculate VP based on total cost and concentration
            // higher total cost = harder; repeated high amounts of one color = harder
            int totalCost = 0;
            int maxSingleColor = 0;
            for (int c : costs) {
                totalCost += c;
                maxSingleColor = Math.max(maxSingleColor, c);
            }
            // VP = base (from total cost) + bonus (for concentration in one color)
            // concentration bonus: each point above 2 of a single color adds VP
            int baseCost = totalCost / 3; // scale down for wider range
            int concentrationBonus = Math.max(0, maxSingleColor - 2);
            int vp = 1 + baseCost + concentrationBonus;
            
            cards.add(new Card(vp, cost.toString(), i));
        }
    }

    public void loadGame() {
        String json = dataLoader.loadprevGame();
        // if (json == null || json.isEmpty()) {
        //     // create default data
        //     Player p1 = new Player("Alice");
        //     Player p2 = new Player("Bob");
        //     players.add(p1);
        //     players.add(p2);
        //     // add some sample cards
        //     cards.add(new Card(1, "1B2W"));
        //     cards.add(new Card(2, "2R1G"));
        //     cards.add(new Card(3, "3R"));
        //     currPlayer = p1;
        //     return;
        // }

        // Very small ad-hoc JSON parsing tailored to the repo's database.json
        parsePlayers(json);
        parseCards(json);
        // System.out.println(this);
        if (!players.isEmpty()) currPlayer = players.get(0);
    }

    private void parsePlayers(String json) {
        players.clear();
        Pattern p = Pattern.compile("\"player\"\s*:\s*\\{(.*?)\\},\\s*\\\"cards\"", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (!m.find()) return;
        String playersBlock = m.group(1);
        // find each player: "0": { "totalVP": 10, "chips": { "R": 1, ... } }
        Pattern pPlayer = Pattern.compile("\"\\d+\"\s*:\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher mp = pPlayer.matcher(playersBlock);
        while (mp.find()) {
            String body = mp.group(1);
            // totalVP
            Pattern vp = Pattern.compile("\"totalVP\"\s*:\s*(\\d+)");
            Matcher mvp = vp.matcher(body);
            int totalVP = mvp.find() ? Integer.parseInt(mvp.group(1)) : 0;
            // chips
            Map<Character, Integer> initialChips = new java.util.HashMap<>();
            Pattern chips = Pattern.compile("\"chips\"\s*:\s*\\{(\\s*\".\": \\d,)+\\s*\".\": \\d", Pattern.DOTALL);
            Matcher mch = chips.matcher(body);
            if (mch.find()) {
                for(int i = 0; i < mch.groupCount(); i++) {
                    String chipsBlock = mch.group(i);
                    System.out.println("Chips block: " + chipsBlock);
                    Pattern chipEntry = Pattern.compile("\"(\\w)\"\s*:\s*(\\d+)");
                    System.out.println("Chip entry pattern: " + chipEntry);
                    Matcher mce = chipEntry.matcher(chipsBlock);
                    while (mce.find()) {
                        char color = mce.group(1).charAt(0);
                        int count = Integer.parseInt(mce.group(2));
                        initialChips.put(color, count);
                    }
                }
            }
            // set VP
            // Because Player currently only adds VP when buying, set directly via reflection-like workaround
            
            Player pl = new Player("Player" + (players.size() + 1), totalVP, initialChips);
            players.add(pl);
        }
    }

    private void parseCards(String json) {
        // System.out.println(json);
        cards.clear();
        int ind = 0;
        if (json == null) return;
        int idx = json.indexOf("\"cards\"");
        if (idx == -1) return;
        int start = json.indexOf('{', idx);
        if (start == -1) return;
        // find matching closing brace for the cards object
        int depth = 0;
        int end = -1;
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '{') depth++;
            else if (ch == '}') {
                depth--;
                if (depth == 0) { end = i; break; }
            }
        }
        if (end == -1) return;
        String cardsBlock = json.substring(start + 1, end);
        // System.out.println(cardsBlock);
        // find each card entry by locating the key and matching its object braces
        Pattern keyPattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{");
        Matcher keyMatcher = keyPattern.matcher(cardsBlock);
        while (keyMatcher.find()) {
            int bracePos = cardsBlock.indexOf('{', keyMatcher.end() - 1);
            if (bracePos == -1) continue;
            int d = 0;
            int objEnd = -1;
            for (int i = bracePos; i < cardsBlock.length(); i++) {
                char ch = cardsBlock.charAt(i);
                if (ch == '{') d++;
                else if (ch == '}') {
                    d--;
                    if (d == 0) { objEnd = i; break; }
                }
            }
            if (objEnd == -1) continue;
            String body = cardsBlock.substring(bracePos + 1, objEnd);
            // System.out.println(body);
            Pattern vp = Pattern.compile("\"victoryPoint\"\\s*:\\s*(\\d+)");
            Matcher mvp = vp.matcher(body);
            int v = mvp.find() ? Integer.parseInt(mvp.group(1)) : 0;
            Pattern cost = Pattern.compile("\"cost\"\\s*:\\s*\"(.*?)\"");
            Matcher mco = cost.matcher(body);
            String c = mco.find() ? mco.group(1) : "";
            cards.add(new Card(v, c, ind++));
        }
    }

    /**
     * perform a move. move strings supported: "R" (draw) or "idx" (buy) (idx = index in cards list)
     * Drawing: can draw up to 3 total tokens (different colors) OR 2 of same color, then turn swaps.
     * Buying: ends the turn immediately.
     * Player cannot both draw and buy in same turn.
     */
    public boolean makeMove(int movIndex, String move) {
        if (currPlayer == null) return false;
        if (move == null) return false;
        move = move.trim();
        if (movIndex == 1) { //(move.startsWith("draw:")) {
            // cannot draw if already bought this turn
            
            String color = move.substring("draw:".length());
            char c = Character.toUpperCase(color.charAt(0));
            
            // if we already have 2 different colors, cannot draw any more
            // (valid draws are: 3 different colors OR 2 of same color, not mixed)
            if (currChips.size() >= 2 && currChips.containsKey(c)) {
                return false;
            }
            
            boolean ok = currPlayer.drawChip(color);
            
            boolean shouldSwap = false;
            if (ok) {
                // System.out.println("Player drew chip: " + chipDrawn);
                // condition 1: 2 tokens of same color
                if (chipDrawn && currChips.size() == 1 && currChips.containsKey(c)) {
                    shouldSwap = true;
                }

                // track chip drawn this turn
                currChips.put(c, currChips.getOrDefault(c, 0) + 1);
                
                // check turn-swap conditions
                int totalDrawn = currChips.size();
                
                // condition 2: 3 total tokens drawn
                if (totalDrawn >= 3) {
                    shouldSwap = true;
                }
            }
            
            if(!chipDrawn) chipDrawn = true;

            if (shouldSwap) {
                    // System.out.println("Turn ends after drawing chips");
                    nextTurn();
                }
                
            saveGameState();

            return ok;
        } else if (movIndex == 2) { // (move.startsWith("buy:")) {
            // cannot buy if already took an action (drew chips) this turn
            if (chipDrawn) {
                // System.out.println("Cannot buy after drawing chips"); 
                return false;
            }

            String idxStr = move.substring("buy:".length());
            try {
                int idx = Integer.parseInt(idxStr);
                if (idx < 0 || idx >= cards.size()) return false;
                Card c = cards.get(idx);
                boolean ok = currPlayer.buyCard(c);
                if (ok) {
                    cards.remove(idx);
                    // condition 3: card was bought, swap turn
                    nextTurn();
                    saveGameState();
                }
                return ok;
            } catch (NumberFormatException e) {
                // System.out.println("Failed to parse buy index: " + idxStr);
                return false;
            }
        }
        return false;
    }

    public Player getCurrentPlayer() { return currPlayer; }

    public void nextTurn() {
        if (players.size() > 0) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            currPlayer = players.get(currentPlayerIndex);
            // reset action and drawn chips tracker for new turn
            currChips.clear();
            chipDrawn = false;
        }
    }

    public boolean gameOver() {
        for (Player p : players) if (p.getTotalVP() >= 15) return true;
        return cards.isEmpty();
    }
    // Public helper so UI can check game-over state
    public boolean isGameOver() {
        return gameOver();
    }

    // Determine winner: if a player has >= 15 VP, return them; otherwise return player with max VP (or null if no players)
    public Player getWinner() {
        if (players.isEmpty()) return null;
        for (Player p : players) if (p.getTotalVP() >= 15) return p;
        Player best = players.get(0);
        for (Player p : players) {
            if (p.getTotalVP() > best.getTotalVP()) best = p;
        }
        return best;
    }
    private String loadCards() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) sb.append(i).append(": ").append(cards.get(i)).append("\n");
        return sb.toString();
    }

    private void saveGameState() {
        // serialize current game state to JSON and save
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"player\": {\n");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            json.append("    \"").append(i).append("\": {\n");
            json.append("      \"totalVP\": ").append(p.getTotalVP()).append(",\n");
            json.append("      \"chips\": {\n");
            // use the player's existing chip map
            Map<Character, Integer> chipCounts = p.getChips();
            // System.out.println(chipCounts);
            List<Character> colors = java.util.Arrays.asList('R', 'B', 'G', 'K', 'W');
            for (int ci = 0; ci < colors.size(); ci++) {
                char color = colors.get(ci);
                int count = chipCounts.getOrDefault(color, 0);
                // System.out.println(count);
                json.append("        \"").append(color).append("\": ").append(count);
                if (ci < colors.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("      }\n");
            json.append("    }");
            if (i < players.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  },\n");
        json.append("  \"cards\": {\n");
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            json.append("    \"").append(i).append("\": {\"victoryPoint\": ").append(c.getVictoryPoints()).append(", \"cost\": \"").append(c.getCost()).append("\"}");
            if (i < cards.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  },\n");
        json.append("  \"cardsRemaining\": [");
        List<Card> cards = getCards();
        json.append(cards.get(0).id);
        for(int j = 1; j < cards.size(); j++){
            json.append(", " + cards.get(j).id);
        } 
        json.append( "],\n");
        json.append("  \"currTurn\": ").append(currentPlayerIndex).append("\n");
        json.append("}\n");
        // System.out.println("Saving game state:\n" + json.toString());
        dataLoader.saveGame(json.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Players:\n");
        for (Player p : players) sb.append(p).append("\n");
        sb.append("Cards:\n");
        sb.append(loadCards());
        return sb.toString();
    }
}
