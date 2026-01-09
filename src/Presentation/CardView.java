package Presentation;

import Domain.Card;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Small visual component to render a Card with VP and cost breakdown and a Buy button.
 */
public class CardView extends JPanel {
    private Card card;
    private int index;

    public CardView(Card c, int index, ActionListener buyListener) {
        this.card = c;
        this.index = index;
        initUI(buyListener);
    }

    private void initUI(ActionListener buyListener) {
        setLayout(new BorderLayout());
        setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
        setBackground(new Color(250, 250, 240));
        setPreferredSize(new Dimension(160, 110));

        JLabel title = new JLabel("Card " + index);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        center.setOpaque(false);

        JLabel vp = new JLabel("VP: " + card.getVictoryPoints());
        vp.setFont(vp.getFont().deriveFont(12f));
        center.add(vp);

        // costs
        for (java.util.Map.Entry<Character, Integer> e : card.getCostMap().entrySet()) {
            char color = e.getKey();
            int count = e.getValue();
            JLabel cl = new JLabel(count + " " + color);
            cl.setOpaque(true);
            cl.setBorder(new LineBorder(Color.GRAY));
            cl.setHorizontalAlignment(SwingConstants.CENTER);
            cl.setPreferredSize(new Dimension(36, 22));
            cl.setBackground(colorOf(color));
            if (color == 'K') {
                // set text to white
                cl.setForeground(Color.WHITE);
            }
            center.add(cl);
        }

        add(center, BorderLayout.CENTER);

        JButton buy = new JButton("Buy");
        buy.setActionCommand(String.valueOf(index));
        buy.addActionListener(buyListener);
        add(buy, BorderLayout.SOUTH);
    }

    private Color colorOf(char c) {
        switch (Character.toUpperCase(c)) {
            case 'R': return new Color(220, 60, 60);
            case 'B': return new Color(80, 130, 220);
            case 'G': return new Color(100, 180, 100);
            case 'W': return new Color(240, 240, 230);
            case 'K': return new Color(0, 0, 0);
            default: return new Color(200, 200, 200);
        }
    }
}
