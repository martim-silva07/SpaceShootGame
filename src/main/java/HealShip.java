import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class HealShip {
    public int x, y;
    public boolean alive = true;
    public int width = 50;  // Largura ideal para o sprite
    public int height = 50; // Altura ideal para o sprite
    private BufferedImage sprite;
    private double waveTimer = 0;

    public HealShip(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            // Carrega o png a partir da tua pasta de recursos
            sprite = ImageIO.read(getClass().getResourceAsStream("/images/heal_ship.png"));
        } catch (Exception e) {
            System.out.println("Erro ao carregar heal_ship.png: " + e.getMessage());
        }
    }

    public void update() {
        // Comportamento de mob: o HealShip agora desce pelo ecrã
        y += 2;

        // Movimento extra: oscilação horizontal leve para parecer uma nave a pairar
        waveTimer += 0.05;
        x += Math.sin(waveTimer) * 1.2;
    }

    public void draw(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, x, y, width, height, null);
        } else {
            // Desenho alternativo caso a imagem falhe
            g.setColor(Color.GREEN);
            g.fillRect(x, y, width, height);
        }
    }
}