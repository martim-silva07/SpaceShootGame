import java.awt.*;

public class CropPlot {
    public int x, y, width = 85, height = 65;
    public int state = 0;
    private int growthTimer = 0;

    public CropPlot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void plant() { state = 1; growthTimer = 0; }
    public void harvest() { state = 0; }

    public void update() {
        if (state > 0 && state < 3) {
            growthTimer++;
            if (growthTimer > 250) {
                state++;
                growthTimer = 0;
            }
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(36, 24, 14));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(90, 70, 50));
        g.drawRect(x, y, width, height);

        if (state == 1) {
            g.setColor(new Color(145, 90, 40));
            g.fillOval(x + 38, y + 28, 8, 8);
        } else if (state == 2) {
            g.setColor(new Color(50, 180, 70));
            g.fillRect(x + 40, y + 20, 4, 25);
        } else if (state == 3) {
            g.setColor(new Color(0, 255, 100));
            g.fillRect(x + 38, y + 12, 8, 35);
            g.setColor(new Color(0, 255, 100, 50));
            g.drawRect(x + 32, y + 6, 20, 47);
        }
    }

    public boolean isPlayerNear(Player p) {
        return new Rectangle(x - 15, y - 15, width + 30, height + 30)
                .intersects(new Rectangle(p.x, p.y, p.width, p.height));
    }
}