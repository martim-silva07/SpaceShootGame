import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Enemy {
    public int x, y, type, size, enemyHp;
    public boolean alive = true;
    private int startX; // Guarda a posição inicial X para o ziguezague

    private static BufferedImage[] sprites = new BufferedImage[3];

    public static void loadSprites() {
        try {
            sprites[0] = ImageIO.read(Enemy.class.getResourceAsStream("/images/enemy0.png"));
            sprites[1] = ImageIO.read(Enemy.class.getResourceAsStream("/images/enemy1.png"));
            sprites[2] = ImageIO.read(Enemy.class.getResourceAsStream("/images/enemy2.png"));
        } catch (Exception e) {
            System.out.println("Erro ao carregar sprites dos inimigos.");
        }
    }

    public Enemy(int x, int y, int type, int planet) {
        this.startX = x;
        this.x = x;
        this.y = y;
        this.type = type;
        this.size = 30 + type * 5;
        this.enemyHp = (planet >= 8) ? 3 : 1;
    }

    public void update(int planet) {
        y += 2 + type; // Desce horizontalmente

        // Movimento de um lado para o outro (ziguezague)
        // O valor '50' é a largura do movimento e '0.05' é a velocidade da oscilação
        x = startX + (int)(50 * Math.sin(y * 0.03));
    }

    public void draw(Graphics2D g) {
        if (sprites[type] != null) {
            g.drawImage(sprites[type], x, y, size, size, null);
        } else {
            g.setColor(Color.RED);
            g.fillOval(x, y, size, size);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public boolean hit(Bullet b) {
        return getBounds().intersects(b.getBounds());
    }

    public boolean hit(Player p) {
        return getBounds().intersects(new Rectangle(p.x, p.y, p.width, p.height));
    }
}