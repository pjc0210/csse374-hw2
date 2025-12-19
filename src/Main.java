import Presentation.Splendor;

/**
 * Main entry point for the Splendor game application.
 */
public class main {

    public static void main(String[] args) {
        // Set look and feel to system default for better appearance
        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        // Create and display the game on the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting Splendor game...");
                Splendor game = new Splendor();
                game.playGame();
                System.out.println("Game initialized successfully!");
            }
        });
    }
}
