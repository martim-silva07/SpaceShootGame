import java.awt.*;

public class HeartItem {
    public int x, y;
    public boolean collected = false;
    private double bobTimer = 0;

    public HeartItem(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        // Agora o coração desce na vertical a uma velocidade constante
        y += 2;

        // Mantém uma oscilação suave para os lados para ficar com um efeito bonito
        bobTimer += 0.1;
        x += Math.sin(bobTimer) * 0.5;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        int[] xPoints = {x, x + 10, x + 20, x + 10};
        int[] yPoints = {y + 6, y, y + 6, y + 18};
        g.fillPolygon(xPoints, yPoints, 4);
        g.fillOval(x, y, 10, 10);
        g.fillOval(x + 10, y, 10, 10);
    }
}