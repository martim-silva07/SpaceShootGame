import java.awt.*;

public class Coin {
    public int x, y;
    public boolean collected = false;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {}

    public void draw(Graphics2D g) {
        // Brilho exterior (Néon)
        g.setColor(new Color(255, 215, 0, 100));
        g.fillOval(x - 2, y - 2, 16, 16);

        // Centro da moeda
        g.setColor(new Color(255, 200, 0));
        g.fillOval(x, y, 12, 12);

        // Detalhe interno
        g.setColor(Color.WHITE);
        g.drawOval(x + 3, y + 3, 6, 6);
    }

    public boolean hit(Player p) {
        return new Rectangle(x, y, 12, 12).intersects(new Rectangle(p.x, p.y, p.width, p.height));
    }
}