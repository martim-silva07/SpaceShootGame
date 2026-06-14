import java.awt.*;
import java.util.Random;

public class Star {
    public double x, y;
    int size;

    public Star(int x, int y, double speed, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void update() {
        // Removido o movimento para as estrelas ficarem estáticas
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 200));
        g.fillOval((int) x, (int) y, size, size);
    }
}