import javax.swing.*;
import java.awt.*;

/**
 * Hill Climb Racing - Main Application Window.
 * 
 * Features:
 * - 60 FPS Smooth Game Loop with Java2D Vector Graphics.
 * - Arrow Keys & WASD keyboard controls.
 * - Data Structures & Algorithms integration (Stack, Queue, Linked List, Sorting, Searching, Recursion).
 */
public class HillClimbGame extends JFrame {
    private static final long serialVersionUID = 1L;

    public HillClimbGame() {
        super("Hill Climb Racing 2 - Countryside Stage 4 (KDU IT22013 DSA Mini Game)");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 640);
        setMinimumSize(new Dimension(960, 540));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(15, 23, 42));

        GamePanel panel = new GamePanel();
        add(panel, BorderLayout.CENTER);

        // Ensure window receives key focus immediately upon launching
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                panel.requestFocusInWindow();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HillClimbGame game = new HillClimbGame();
            game.setVisible(true);
        });
    }
}
