import javax.swing.*;

public class SpaceGame {
    public static void main(String[] args) {
        // Garante que a interface gráfica é criada na thread correta do Swing
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zoozve: Last Farm of Earth");

            // Cria a instância do painel de jogo principal
            GamePanel gamePanel = new GamePanel();

            frame.add(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack(); // Ajusta o tamanho da janela ao tamanho do GamePanel (900x600)
            frame.setLocationRelativeTo(null); // Centraliza a janela no ecrã
            frame.setVisible(true);
        });
    }
}