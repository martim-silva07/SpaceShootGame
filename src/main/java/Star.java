import java.awt.*;

public class Star {
    public double x, y;
    int size;

    // FIX: removido parâmetro 'speed' que era recebido mas nunca usado
    public Star(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void update() {
        // Estrelas estáticas por design
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 200));
        g.fillOval((int) x, (int) y, size, size);
    }
}