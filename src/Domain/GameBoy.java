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
    private java.util.Set<String> currChips = new java.util.HashSet<>();
    private Player currPlayer;
    private int currentPlayerIndex = 0;
    private Map<Character, Integer> drawnThisTurn = new java.util.HashMap<>();
    private boolean actionTakenThisTurn = false;

    public List<Player> getPlayers() { return players; }
    public List<Card> getCards() { return cards; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }

    public void loadGame() {
        DataLoader loader = new DataLoader();
        String json = loader.loadprevGame();
        if (json == null || json.isEmpty()) {
            // create default data
            Player p1 = new Player("Alice");
            Player p2 = new Player("Bob");
            players.add(p1);
            players.add(p2);
            // add some sample cards
            cards.add(new Card(1, "1B2W"));
            cards.add(new Card(2, "2R1G"));
            cards.add(new Card(3, "3R"));
            currPlayer = p1;
            return;
        }

        // Very small ad-hoc JSON parsing tailored to the repo's database.json
        parsePlayers(json);
        parseCards(json);
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
            Player pl = new Player("Player" + (players.size() + 1));
            // chips
            Pattern chips = Pattern.compile("\"chips\"\s*:\s*\\{(.*?)\\}", Pattern.DOTALL);
            Matcher mch = chips.matcher(body);
            if (mch.find()) {
                String chipsBlock = mch.group(1);
                Pattern chipEntry = Pattern.compile("\"(\\w)\"\s*:\s*(\\d+)");
                Matcher mce = chipEntry.matcher(chipsBlock);
                while (mce.find()) {
                    char color = mce.group(1).charAt(0);
                    int count = Integer.parseInt(mce.group(2));
                    for (int i = 0; i < count; i++) pl.drawChip(String.valueOf(color));
                }
            }
            // set VP
            // Because Player currently only adds VP when buying, set directly via reflection-like workaround
            try {
                java.lang.reflect.Field f = Player.class.getDeclaredField("totalVP");
                f.setAccessible(true);
                f.setInt(pl, totalVP);
            } catch (Exception e) {
                // ignore
            }
            players.add(pl);
        }
    }

    private void parseCards(String json) {
        cards.clear();
        Pattern p = Pattern.compile("\"cards\"\s*:\s*\\{(.*?)\\}(,|\\})", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (!m.find()) return;
        String cardsBlock = m.group(1);
        Pattern pCard = Pattern.compile("\"\\d+\"\s*:\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher mc = pCard.matcher(cardsBlock);
        while (mc.find()) {
            String body = mc.group(1);
            Pattern vp = Pattern.compile("\"victoryPoint\"\s*:\s*(\\d+)");
            Matcher mvp = vp.matcher(body);
            int v = mvp.find() ? Integer.parseInt(mvp.group(1)) : 0;
            Pattern cost = Pattern.compile("\"cost\"\s*:\s*\"(.*?)\"");
            Matcher mco = cost.matcher(body);
            String c = mco.find() ? mco.group(1) : "";
            cards.add(new Card(v, c));
        }
    }

    /**
     * perform a move. move strings supported: "draw:R" or "buy:idx" (idx = index in cards list)
     * Auto-swaps turn if: 2 tokens of same color drawn, 3 total tokens drawn, or card bought.
     * Player cannot both draw and buy in same turn.
     */
    public boolean makeMove(int movIndex, String move) {
        if (currPlayer == null) return false;
        if (move == null) return false;
        move = move.trim();
        if (move.startsWith("draw:")) {
            // cannot draw if already took an action (bought) this turn
            if (actionTakenThisTurn) return false;
            
            String color = move.substring("draw:".length());
            boolean ok = currPlayer.drawChip(color);
            if (ok) {
                actionTakenThisTurn = true;
                // track chip drawn this turn
                char c = Character.toUpperCase(color.charAt(0));
                drawnThisTurn.put(c, drawnThisTurn.getOrDefault(c, 0) + 1);
                
                // check turn-swap conditions
                int totalDrawn = drawnThisTurn.values().stream().mapToInt(Integer::intValue).sum();
                boolean shouldSwap = false;
                
                // condition 1: 2 tokens of same color
                for (int count : drawnThisTurn.values()) {
                    if (count >= 2) {
                        shouldSwap = true;
                        break;
                    }
                }
                // condition 2: 3 total tokens drawn
                if (totalDrawn >= 3) {
                    shouldSwap = true;
                }
                
                if (shouldSwap) {
                    nextTurn();
                }
            }
            return ok;
        } else if (move.startsWith("buy:")) {
            // cannot buy if already took an action (drew chips) this turn
            if (actionTakenThisTurn) return false;
            
            String idxStr = move.substring("buy:".length());
            try {
                int idx = Integer.parseInt(idxStr);
                if (idx < 0 || idx >= cards.size()) return false;
                Card c = cards.get(idx);
                boolean ok = currPlayer.buyCard(c);
                if (ok) {
                    actionTakenThisTurn = true;
                    cards.remove(idx);
                    // condition 3: card was bought, swap turn
                    nextTurn();
                }
                return ok;
            } catch (NumberFormatException e) {
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
            actionTakenThisTurn = false;
            drawnThisTurn.clear();
        }
    }

    private boolean gameOver() {
        for (Player p : players) if (p.getTotalVP() >= 15) return true;
        return cards.isEmpty();
    }

    private String loadCards() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) sb.append(i).append(": ").append(cards.get(i)).append("\n");
        return sb.toString();
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
