package Domain;

import Data.DataLoader;
import java.util.*;

/**
 * Main game controller for Splendor.
 * Manages game state, players, cards, and turn logic.
 */
public class GameBoy {
    private List<Card> cards;
    private List<Player> players;
    private Set<String> currChips; // Available chips in the bank
    private Map<Character, Integer> chipBank; // Chip counts in bank
    private Player currPlayer;
    private int currentPlayerIndex;
    private DataLoader dataLoader;
    private boolean gameLoaded;

    private static final int WINNING_SCORE = 15;

    public GameBoy() {
        this.cards = new ArrayList<>();
        this.players = new ArrayList<>();
        this.currChips = new HashSet<>();
        this.chipBank = new HashMap<>();
        this.dataLoader = new DataLoader();
        this.currentPlayerIndex = 0;
        this.gameLoaded = false;

        initializeChipBank();
    }

    /**
     * Initialize the chip bank with starting chips.
     */
    private void initializeChipBank() {
        // For 2-3 players: 4 of each gem, 5 gold
        // For 4 players: 5 of each gem, 5 gold
        int numPlayers = players.size() == 0 ? 2 : players.size();
        int chipsPerColor = numPlayers >= 4 ? 7 : 4;

        chipBank.put('R', chipsPerColor); // Red
        chipBank.put('G', chipsPerColor); // Green
        chipBank.put('B', chipsPerColor); // Blue
        chipBank.put('W', chipsPerColor); // White
        chipBank.put('K', chipsPerColor); // Black
        chipBank.put('Y', 5);               // Gold (wild)

        // Update currChips set
        currChips.clear();
        for (char color : chipBank.keySet()) {
            if (chipBank.get(color) > 0) {
                currChips.add(String.valueOf(color));
            }
        }
    }

    /**
     * Load the game - either from save or start new.
     */
    public void loadGame() {
        // Try to load previous game
        String savedGame = dataLoader.loadPrevGame();

        if (savedGame != null && !savedGame.isEmpty()) {
            System.out.println("Loading saved game...");
            parseSavedGame(savedGame);
            gameLoaded = true;
        } else {
            System.out.println("Starting new game...");
            startNewGame();
            gameLoaded = true;
        }
    }

    /**
     * Start a new game with default setup.
     */
    private void startNewGame() {
        // Create players
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));

        // Load cards
        String cardData = dataLoader.loadCards();
        parseCards(cardData);

        // Shuffle cards
        Collections.shuffle(cards);

        // Set current player
        currentPlayerIndex = 0;
        currPlayer = players.get(currentPlayerIndex);

        // Initialize chip bank
        initializeChipBank();

        System.out.println("New game started with " + players.size() + " players");
        System.out.println("Loaded " + cards.size() + " cards");
    }

    /**
     * Parse card data from string format.
     */
    private void parseCards(String cardData) {
        if (cardData == null || cardData.isEmpty()) {
            return;
        }

        String[] lines = cardData.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            if (parts.length >= 3) {
                int vp = Integer.parseInt(parts[0]);
                String cost = parts[1];
                char gemType = parts[2].charAt(0);
                cards.add(new Card(vp, cost, gemType));
            }
        }
    }

    /**
     * Parse saved game data.
     */
    private void parseSavedGame(String savedGame) {
        // Basic parsing - extend as needed
        String[] sections = savedGame.split("---");

        if (sections.length > 0) {
            // Parse player data
            String[] playerLines = sections[0].split("\n");
            for (String line : playerLines) {
                if (line.startsWith("Player:")) {
                    // Parse player info
                    String name = line.substring(7).trim();
                    players.add(new Player(name));
                }
            }
        }

        if (sections.length > 1) {
            // Parse card data
            parseCards(sections[1]);
        }

        // Set current player
        if (!players.isEmpty()) {
            currPlayer = players.get(0);
        }
    }

    /**
     * Make a move in the game.
     * @param moveIndex The index/identifier of the move
     * @param move The move type (e.g., "draw", "buy")
     * @return true if move was valid and executed
     */
    public boolean makeMove(int moveIndex, String move) {
        if (currPlayer == null || gameOver()) {
            return false;
        }

        boolean moveSuccess = false;

        switch (move.toLowerCase()) {
            case "draw":
                moveSuccess = handleDrawChips(moveIndex);
                break;
            case "buy":
                moveSuccess = handleBuyCard(moveIndex);
                break;
            case "reserve":
                moveSuccess = handleReserveCard(moveIndex);
                break;
            default:
                System.out.println("Unknown move type: " + move);
                return false;
        }

        if (moveSuccess) {
            // Check if game is over
            if (gameOver()) {
                announceWinner();
            } else {
                // Switch to next player
                nextPlayer();
            }
        }

        return moveSuccess;
    }

    /**
     * Handle drawing chips from the bank.
     */
    private boolean handleDrawChips(int colorCode) {
        // colorCode could represent which colors to draw
        // Simplified: draw single chip of specified color
        char color = getColorFromCode(colorCode);

        if (!chipBank.containsKey(color) || chipBank.get(color) <= 0) {
            System.out.println("No chips available of that color");
            return false;
        }

        if (currPlayer.drawChip(String.valueOf(color))) {
            chipBank.put(color, chipBank.get(color) - 1);
            updateCurrChips();
            return true;
        }

        return false;
    }

    /**
     * Handle buying a card.
     */
    private boolean handleBuyCard(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= cards.size()) {
            System.out.println("Invalid card index");
            return false;
        }

        Card card = cards.get(cardIndex);
        if (currPlayer.buyCard(card)) {
            // Return chips to bank
            returnChipsToBank(card);
            // Remove card from available cards
            cards.remove(cardIndex);
            System.out.println(currPlayer.getName() + " bought a card!");
            return true;
        } else {
            System.out.println("Cannot afford this card");
            return false;
        }
    }

    /**
     * Handle reserving a card (simplified implementation).
     */
    private boolean handleReserveCard(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= cards.size()) {
            return false;
        }

        // Simplified: just take a gold chip
        if (chipBank.get('Y') > 0 && currPlayer.drawChip("Y")) {
            chipBank.put('Y', chipBank.get('Y') - 1);
            updateCurrChips();
            return true;
        }

        return false;
    }

    /**
     * Return chips to bank after purchasing card.
     */
    private void returnChipsToBank(Card card) {
        // Chips are returned when buying - this is handled in Player.buyCard()
        // Could track specifically which chips were spent and return them here
    }

    /**
     * Convert numeric code to color character.
     */
    private char getColorFromCode(int code) {
        char[] colors = {'R', 'G', 'B', 'W', 'K', 'Y'};
        if (code >= 0 && code < colors.length) {
            return colors[code];
        }
        return 'R'; // Default
    }

    /**
     * Update the currChips set based on chip bank.
     */
    private void updateCurrChips() {
        currChips.clear();
        for (Map.Entry<Character, Integer> entry : chipBank.entrySet()) {
            if (entry.getValue() > 0) {
                currChips.add(String.valueOf(entry.getKey()));
            }
        }
    }

    /**
     * Get the current player.
     */
    private Player getCurrentPlayer() {
        return currPlayer;
    }

    /**
     * Advance to the next player.
     */
    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        currPlayer = players.get(currentPlayerIndex);
        System.out.println("\n" + currPlayer.getName() + "'s turn");
    }

    /**
     * Check if the game is over.
     */
    private boolean gameOver() {
        for (Player player : players) {
            if (player.getTotalVP() >= WINNING_SCORE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Announce the winner.
     */
    private void announceWinner() {
        Player winner = null;
        int highestVP = -1;

        for (Player player : players) {
            if (player.getTotalVP() > highestVP) {
                highestVP = player.getTotalVP();
                winner = player;
            }
        }

        if (winner != null) {
            System.out.println("\n=== GAME OVER ===");
            System.out.println("Winner: " + winner.getName() + " with " + winner.getTotalVP() + " points!");
        }
    }

    // Getters
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public Player getCurrentPlayerPublic() {
        return currPlayer;
    }

    public Set<String> getCurrChips() {
        return new HashSet<>(currChips);
    }

    public Map<Character, Integer> getChipBank() {
        return new HashMap<>(chipBank);
    }

    public boolean isGameLoaded() {
        return gameLoaded;
    }
}