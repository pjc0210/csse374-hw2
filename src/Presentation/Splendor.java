package Presentation;

import Domain.GameBoy;
import Domain.Player;
import Domain.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Improved Swing UI that displays cards and player info and allows buying by clicking.
 */
public class Splendor extends JFrame {
    private JFrame frame;
    private GameBoy gameBoy;

    private JTextArea logArea;
    private JPanel cardsPanel;
    private JLabel statusLabel;

    public Splendor() {
        super("Mini Splendor");
        this.gameBoy = new GameBoy();
        initUI();
    }

    private void initUI() {
        frame = this;
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(8, 8));

        // Top status
        statusLabel = new JLabel("No game loaded");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        frame.add(statusLabel, BorderLayout.NORTH);

        // Center: cards grid inside a scroll pane
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 8, 8));
        JScrollPane cardsScroll = new JScrollPane(cardsPanel);
        frame.add(cardsScroll, BorderLayout.CENTER);

        // Right: log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(260, 0));
        frame.add(logScroll, BorderLayout.EAST);

        // Bottom: controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JButton loadBtn = new JButton("Load Game");
        
        JLabel drawLabel = new JLabel("Draw chips:");
        controls.add(loadBtn);
        controls.add(drawLabel);
        
        // Draw buttons for each color: W (White), G (Green), B (Blue), K (Black), R (Red)
        String[] colors = {"W", "G", "B", "K", "R"};
        String[] colorNames = {"White", "Green", "Blue", "Black", "Red"};
        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            String name = colorNames[i];
            JButton btn = new JButton(name);
            btn.setActionCommand(color);
            btn.addActionListener(e -> {
                boolean ok = gameBoy.makeMove(0, "draw:" + e.getActionCommand());
                log("Draw " + e.getActionCommand() + " -> " + ok);
                refreshUI();
            });
            controls.add(btn);
        }
        
        JButton refreshBtn = new JButton("Refresh");
        controls.add(refreshBtn);
        frame.add(controls, BorderLayout.SOUTH);

        loadBtn.addActionListener(e -> {
            loadGame();
        });
        refreshBtn.addActionListener(e -> refreshUI());
    }

    private void log(String s) {
        logArea.append(s + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // loads game via GameBoy
    public void loadGame() {
        gameBoy.loadGame();
        log("Game loaded.");
        refreshUI();
    }

    private void refreshUI() {
        Player p = gameBoy.getCurrentPlayer();
        String playerText = p == null ? "No player" : p.toString();
        statusLabel.setText("Current player: " + playerText);

        cardsPanel.removeAll();
        List<Card> cards = gameBoy.getCards();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            CardView cv = new CardView(c, i, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String idxStr = e.getActionCommand();
                    try {
                        int idx = Integer.parseInt(idxStr);
                        boolean ok = gameBoy.makeMove(0, "buy:" + idx);
                        log("Buy card " + idx + " -> " + ok);
                        refreshUI();
                    } catch (NumberFormatException ex) {
                        log("Failed to parse card index: " + idxStr);
                    }
                }
            });
            cardsPanel.add(cv);
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    // show UI
    public void playGame() {
        frame.setVisible(true);
    }
}
