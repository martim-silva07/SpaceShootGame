import java.awt.*;

public class Bullet {
    public int x, y;
    public boolean active = true;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y -= 11;
        if (y < 40) active = false;
    }

    // FIX: draw() agora inclui o efeito de glow (estava duplicado no GamePanel)
    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 60, 0, 150));
        g.fillRect(x - 1, y + 4, 7, 12);
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, 5, 12);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 5, 12);
    }
}