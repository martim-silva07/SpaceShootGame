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

    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, 5, 12);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 5, 12);
    }
}