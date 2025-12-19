package Data;

import java.io.*;
import java.util.Scanner;

/**
 * Handles loading game data from files.
 */
public class DataLoader {

    private static final String SAVE_FILE = "savegame.txt";
    private static final String CARDS_FILE = "cards.txt";

    /**
     * Load a previously saved game state.
     * @return The saved game data as a string, or null if no save exists
     */
    public String loadPrevGame() {
        try {
            File file = new File(SAVE_FILE);
            if (!file.exists()) {
                return null;
            }

            StringBuilder content = new StringBuilder();
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }

            scanner.close();
            return content.toString();

        } catch (FileNotFoundException e) {
            System.err.println("Error loading saved game: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load card definitions from file.
     * Format: victoryPoints,costString,gemType
     * Example: 3,R3G2B1,R
     * @return The card data as a string
     */
    public String loadCards() {
        try {
            File file = new File(CARDS_FILE);
            if (!file.exists()) {
                // Return default cards if file doesn't exist
                return getDefaultCards();
            }

            StringBuilder content = new StringBuilder();
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }

            scanner.close();
            return content.toString();

        } catch (FileNotFoundException e) {
            System.err.println("Error loading cards: " + e.getMessage());
            return getDefaultCards();
        }
    }

    /**
     * Provides default card set if file is missing.
     */
    private String getDefaultCards() {
        StringBuilder cards = new StringBuilder();

        // Level 1 cards (lower cost, fewer points)
        cards.append("0,R3,R\n");
        cards.append("0,G3,G\n");
        cards.append("0,B3,B\n");
        cards.append("0,W3,W\n");
        cards.append("0,K3,K\n");
        cards.append("1,R4,R\n");
        cards.append("1,G4,G\n");
        cards.append("1,B4,B\n");

        // Level 2 cards (medium cost)
        cards.append("2,R5,R\n");
        cards.append("2,G5,G\n");
        cards.append("2,B5,B\n");
        cards.append("2,R3G3W2,W\n");
        cards.append("2,B3K3R2,K\n");

        // Level 3 cards (high cost, high points)
        cards.append("3,R7,R\n");
        cards.append("3,G7,G\n");
        cards.append("4,R6G3B3,W\n");
        cards.append("4,G6B3K3,R\n");
        cards.append("5,R7G3,B\n");

        return cards.toString();
    }

    /**
     * Save game state to file.
     * @param gameState The game state to save
     * @return true if save successful
     */
    public boolean saveGame(String gameState) {
        try {
            FileWriter writer = new FileWriter(SAVE_FILE);
            writer.write(gameState);
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
            return false;
        }
    }
}