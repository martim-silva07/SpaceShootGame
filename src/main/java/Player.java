import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Player {
    public int x, y, width = 44, height = 40;
    public boolean left, right, up, down;
    private BufferedImage sprite;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/images/player.png"));
        } catch (Exception e) {
            System.out.println("Erro ao carregar player.png");
        }
    }

    public void update(int speedLevel) {
        int speed = 5 + speedLevel;
        int dx = 0, dy = 0;

        if (left)  dx -= speed;
        if (right) dx += speed;
        if (up)    dy -= speed;
        if (down)  dy += speed;

        // FIX: normaliza a velocidade diagonal para não ser ~41% mais rápida
        if (dx != 0 && dy != 0) {
            dx = (int)(dx * 0.707);
            dy = (int)(dy * 0.707);
        }

        x = Math.max(0, Math.min(850, x + dx));
        y = Math.max(10, Math.min(530, y + dy));
    }

    public void draw(Graphics2D g, boolean shield) {
        if (sprite != null) {
            g.drawImage(sprite, x, y, width, height, null);
        } else {
            g.setColor(Color.CYAN);
            g.fillRect(x, y, width, height);
        }
        if (shield) {
            g.setColor(Color.BLUE);
            g.drawOval(x - 5, y - 5, width + 10, height + 10);
        }
    }
}