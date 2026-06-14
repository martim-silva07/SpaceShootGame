import java.awt.*;
import java.util.Random;

public class Particle {
    public double x, y;
    public double dx, dy; // Variáveis de direção que faltavam
    public int life;
    private Color color;

    public Particle(int x, int y) {
        this.x = x;
        this.y = y;
        Random rand = new Random();
        this.dx = (rand.nextDouble() - 0.5) * 4;
        this.dy = (rand.nextDouble() - 0.5) * 4;
        this.life = rand.nextInt(20) + 10;
        this.color = new Color(255, rand.nextInt(150), 0);
    }

    public void update() {
        x += dx;
        y += dy;
        life--;
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRect((int)x, (int)y, 3, 3);
    }
}