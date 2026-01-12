import Presentation.Splendor;

/** Small launcher for the Splendor mini-game. */
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Splendor s = new Splendor();
            s.loadGame();
            s.playGame();
        });
    }
}
