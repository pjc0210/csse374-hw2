package Presentation;

import Domain.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

/**
 * Main GUI class for the Splendor game.
 * Handles display and user interaction.
 */
public class Splendor extends JPanel {
    private JFrame frame;
    private GameBoy gameBoy;
    private Graphics g;

    // UI Components
    private JTextArea gameInfoArea;
    private JTextArea playerInfoArea;
    private JPanel cardPanel;
    private JPanel chipPanel;
    private JButton[] cardButtons;
    private JButton[] chipButtons;

    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;

    public Splendor() {
        this.gameBoy = new GameBoy();
        this.frame = new JFrame("Splendor");

        setupFrame();
        loadGame();
    }

    /**
     * Setup the main frame and UI components.
     */
    private void setupFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLayout(new BorderLayout());

        // Create main panel
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(20, 30, 40));

        // Top panel - game info
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel - cards
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Right panel - player info
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Bottom panel - chips and actions
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        frame.add(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Create the top panel with game information.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 40, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("SPLENDOR", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        gameInfoArea = new JTextArea(2, 50);
        gameInfoArea.setEditable(false);
        gameInfoArea.setBackground(new Color(40, 50, 60));
        gameInfoArea.setForeground(Color.WHITE);
        gameInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(gameInfoArea, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the center panel with available cards.
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(20, 30, 40));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Available Cards",
                0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE
        ));

        cardPanel = new JPanel();
        cardPanel.setLayout(new GridLayout(0, 4, 10, 10));
        cardPanel.setBackground(new Color(20, 30, 40));

        JScrollPane scrollPane = new JScrollPane(cardPanel);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the right panel with player information.
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 40, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(250, 0));

        JLabel playerLabel = new JLabel("Players", JLabel.CENTER);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playerLabel.setForeground(Color.WHITE);

        playerInfoArea = new JTextArea(30, 20);
        playerInfoArea.setEditable(false);
        playerInfoArea.setBackground(new Color(40, 50, 60));
        playerInfoArea.setForeground(Color.WHITE);
        playerInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(playerInfoArea);

        panel.add(playerLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the bottom panel with chip selection.
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(30, 40, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel chipLabel = new JLabel("Available Chips (Click to Draw)", JLabel.CENTER);
        chipLabel.setFont(new Font("Arial", Font.BOLD, 14));
        chipLabel.setForeground(Color.WHITE);

        chipPanel = new JPanel();
        chipPanel.setLayout(new FlowLayout());
        chipPanel.setBackground(new Color(30, 40, 50));

        panel.add(chipLabel, BorderLayout.NORTH);
        panel.add(chipPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Load the game and initialize UI.
     */
    public void loadGame() {
        gameBoy.loadGame();
        updateDisplay();
    }

    /**
     * Start playing the game.
     */
    public void playGame() {
        updateDisplay();
    }

    /**
     * Update all display elements.
     */
    private void updateDisplay() {
        updateGameInfo();
        updatePlayerInfo();
        updateCards();
        updateChips();
    }

    /**
     * Update game information display.
     */
    private void updateGameInfo() {
        Player currentPlayer = gameBoy.getCurrentPlayerPublic();
        if (currentPlayer != null) {
            gameInfoArea.setText(
                    "Current Turn: " + currentPlayer.getName() + "\n" +
                            "Victory Points: " + currentPlayer.getTotalVP() + " / 15"
            );
        }
    }

    /**
     * Update player information display.
     */
    private void updatePlayerInfo() {
        StringBuilder sb = new StringBuilder();
        List<Player> players = gameBoy.getPlayers();

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            sb.append("=== ").append(p.getName()).append(" ===\n");
            sb.append(p.toString()).append("\n\n");
        }

        playerInfoArea.setText(sb.toString());
    }

    /**
     * Update card display.
     */
    private void updateCards() {
        cardPanel.removeAll();
        List<Card> cards = gameBoy.getCards();

        cardButtons = new JButton[Math.min(cards.size(), 12)]; // Show first 12 cards

        for (int i = 0; i < cardButtons.length && i < cards.size(); i++) {
            final int cardIndex = i;
            Card card = cards.get(i);

            JButton cardButton = new JButton("<html>" + formatCardText(card) + "</html>");
            cardButton.setPreferredSize(new Dimension(140, 100));
            cardButton.setBackground(getCardColor(card.getGemType()));
            cardButton.setForeground(Color.BLACK);
            cardButton.setFont(new Font("Arial", Font.PLAIN, 10));
            cardButton.setFocusPainted(false);

            cardButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleCardClick(cardIndex);
                }
            });

            cardButtons[i] = cardButton;
            cardPanel.add(cardButton);
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    /**
     * Format card text for display.
     */
    private String formatCardText(Card card) {
        StringBuilder sb = new StringBuilder();
        sb.append("<center>");
        sb.append("<b>VP: ").append(card.getVictoryPoints()).append("</b><br>");
        sb.append("Gem: ").append(card.getGemType()).append("<br>");

        Map<Character, Integer> cost = card.getCost();
        if (cost.isEmpty()) {
            sb.append("Cost: Free");
        } else {
            sb.append("Cost:<br>");
            for (Map.Entry<Character, Integer> entry : cost.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("<br>");
            }
        }
        sb.append("</center>");
        return sb.toString();
    }

    /**
     * Get color for card based on gem type.
     */
    private Color getCardColor(char gemType) {
        switch (gemType) {
            case 'R': return new Color(220, 100, 100); // Red
            case 'G': return new Color(100, 200, 100); // Green
            case 'B': return new Color(100, 150, 220); // Blue
            case 'W': return new Color(240, 240, 240); // White
            case 'K': return new Color(80, 80, 80);    // Black
            default: return new Color(200, 200, 200);   // Default
        }
    }

    /**
     * Update chip display.
     */
    private void updateChips() {
        chipPanel.removeAll();
        Map<Character, Integer> chipBank = gameBoy.getChipBank();

        char[] colors = {'R', 'G', 'B', 'W', 'K', 'Y'};
        String[] colorNames = {"Red", "Green", "Blue", "White", "Black", "Gold"};
        Color[] buttonColors = {
                new Color(200, 50, 50),
                new Color(50, 180, 50),
                new Color(50, 100, 200),
                new Color(220, 220, 220),
                new Color(60, 60, 60),
                new Color(255, 215, 0)
        };

        chipButtons = new JButton[colors.length];

        for (int i = 0; i < colors.length; i++) {
            final int colorCode = i;
            char color = colors[i];
            int count = chipBank.get(color);

            JButton chipButton = new JButton(colorNames[i] + ": " + count);
            chipButton.setPreferredSize(new Dimension(100, 40));
            chipButton.setBackground(buttonColors[i]);
            chipButton.setForeground(color == 'K' ? Color.WHITE : Color.BLACK);
            chipButton.setFont(new Font("Arial", Font.BOLD, 12));
            chipButton.setEnabled(count > 0);

            chipButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleChipClick(colorCode);
                }
            });

            chipButtons[i] = chipButton;
            chipPanel.add(chipButton);
        }

        chipPanel.revalidate();
        chipPanel.repaint();
    }

    /**
     * Handle card button click.
     */
    private void handleCardClick(int cardIndex) {
        boolean success = gameBoy.makeMove(cardIndex, "buy");

        if (success) {
            JOptionPane.showMessageDialog(frame, "Card purchased successfully!");
        } else {
            JOptionPane.showMessageDialog(frame, "Cannot purchase this card.",
                    "Invalid Move", JOptionPane.WARNING_MESSAGE);
        }

        updateDisplay();
    }

    /**
     * Handle chip button click.
     */
    private void handleChipClick(int colorCode) {
        boolean success = gameBoy.makeMove(colorCode, "draw");

        if (success) {
            updateDisplay();
        } else {
            JOptionPane.showMessageDialog(frame, "Cannot draw this chip.",
                    "Invalid Move", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.g = g;
    }
}