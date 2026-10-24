package project.aoop.space_sandbox.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;
import project.aoop.space_sandbox.camera.GameCamera;
import project.aoop.space_sandbox.entity.Bullet;

import java.util.List;

@Component
public class BulletRenderer {

    private final GameCamera camera;

    public BulletRenderer(GameCamera camera) {
        this.camera = camera;
    }

    public void render(GraphicsContext gc, List<Bullet> bullets) {
        for (Bullet b : bullets) {
            double sx = camera.toScreenX(b.getX());
            double sy = camera.toScreenY(b.getY());

            if (sx < -50 || sx > camera.getScreenW() + 50) continue;
            if (sy < -50 || sy > camera.getScreenH() + 50) continue;

            if (b.isLaser()) {
                drawChargedLaser(gc, sx, sy, b);
            } else {
                drawBullet(gc, sx, sy, b);
            }
        }
    }

    private void drawChargedLaser(GraphicsContext gc, double sx, double sy, Bullet b) {
        double charge = b.getChargeLevel();
        double alpha  = Math.min(1.0, b.getLifetime() * 0.4);
        double angle = Math.toDegrees(Math.atan2(b.getVy(), b.getVx()));
        double length = 180 + charge * 120;
        double width = 3 + charge * 3;

        gc.save();
        gc.translate(sx, sy);
        gc.rotate(angle);
        // A restrained glow surrounds the narrow beam without obscuring the ship.
        gc.setFill(Color.color(0.65, 0.0, 0.0, alpha * 0.18));
        gc.fillRect(0, -width * 2.2, length, width * 4.4);
        gc.setFill(Color.color(1.0, 0.02, 0.01, alpha * 0.9));
        gc.fillRect(0, -width, length, width * 2);
        gc.setFill(Color.color(1.0, 0.16, 0.08, alpha));
        gc.fillRect(0, -width / 2, length, width);
        gc.restore();
    }

    private void drawBullet(GraphicsContext gc, double sx, double sy, Bullet b) {
        double alpha = Math.min(1.0, b.getLifetime() * 1.5);

        double angle = Math.toDegrees(Math.atan2(b.getVy(), b.getVx()));
        gc.save();
        gc.translate(sx, sy);
        gc.rotate(angle);
        gc.setFill(Color.color(1, 0.05, 0.02, alpha * 0.3));
        gc.fillRect(-10, -4, 20, 8);
        gc.setFill(Color.color(1, 0.12, 0.05, alpha));
        gc.fillRect(-7, -2, 14, 4);
        gc.restore();
    }
}