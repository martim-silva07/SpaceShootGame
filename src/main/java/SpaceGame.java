import javax.swing.*;

public class SpaceGame extends JFrame {
    public SpaceGame() {
        setTitle("ZO0ZVE: Last Farm of Earth");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceGame::new);
    }
}