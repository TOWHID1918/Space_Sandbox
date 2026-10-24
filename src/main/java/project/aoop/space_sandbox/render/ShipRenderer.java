package project.aoop.space_sandbox.render;

import jakarta.annotation.PostConstruct;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;
import project.aoop.space_sandbox.camera.GameCamera;
import project.aoop.space_sandbox.entity.Ship;
import project.aoop.space_sandbox.service.CombatService;

import java.io.InputStream;

@Component
public class ShipRenderer {

    private Image shipImage;

    private final GameCamera   camera;
    private final CombatService combatService;

    public ShipRenderer(GameCamera camera, CombatService combatService) {
        this.camera        = camera;
        this.combatService = combatService;
    }

    @PostConstruct
    public void init() {
        InputStream is = getClass().getResourceAsStream("/sprites/ship.png");
        if (is != null) shipImage = new Image(is);
    }

    public void render(GraphicsContext gc, GameCamera camera, Ship ship, boolean thrusting) {
        double cx = camera.getScreenW() / 2;
        double cy = camera.getScreenH() / 2;

        // Draw charge ring BEFORE ship so it appears behind it
        if (combatService.isCharging()) {
            drawChargeRing(gc, cx, cy);
        }

        // Ship
        gc.save();
        gc.translate(cx, cy);
        // The sprite points right, matching the ship's angle of zero.
        gc.rotate(Math.toDegrees(ship.getAngle()));
        double scale = camera.getZoom() / 50.0;
        gc.scale(scale, scale);

        if (thrusting) {
            drawThrustFlame(gc);
        }

        if (shipImage != null && !shipImage.isError()) {
            // Keep the wide fighter silhouette and preserve the sprite ratio.
            double w = 90, h = 60;
            gc.drawImage(shipImage, -w/2, -h/2, w, h);
        } else {
            gc.setFill(Color.web("#2d8f9a"));
            gc.beginPath();
            gc.moveTo(42, 0); gc.lineTo(8, -18);
            gc.lineTo(-34, -12); gc.lineTo(-42, 0);
            gc.lineTo(-34, 12); gc.lineTo(8, 18);
            gc.closePath();
            gc.fill();
            gc.setFill(Color.web("#35447c"));
            gc.fillRect(-22, -12, 24, 24);
        }

        gc.restore();
    }

    private void drawThrustFlame(GraphicsContext gc) {
        double flicker = 0.85 + Math.random() * 0.3;
        double length = 26 * flicker;

        gc.setFill(Color.color(1.0, 0.35, 0.05, 0.9));
        gc.beginPath();
        gc.moveTo(-34, -14);
        gc.lineTo(-34 - length, -9);
        gc.lineTo(-34, -3);
        gc.moveTo(-34, 3);
        gc.lineTo(-34 - length, 9);
        gc.lineTo(-34, 14);
        gc.closePath();
        gc.fill();

        gc.setFill(Color.color(1.0, 0.9, 0.2, 0.95));
        gc.beginPath();
        gc.moveTo(-35, -10);
        gc.lineTo(-35 - length * 0.7, -6);
        gc.lineTo(-35, -4);
        gc.moveTo(-35, 4);
        gc.lineTo(-35 - length * 0.7, 6);
        gc.lineTo(-35, 10);
        gc.closePath();
        gc.fill();
    }

    private void drawChargeRing(GraphicsContext gc, double cx, double cy) {
        double charge  = combatService.getCharge();
        boolean full   = combatService.isFullyCharged();

        // Ring pulses when fully charged
        double pulse   = full
                ? 1.0 + Math.sin(System.currentTimeMillis() * 0.01) * 0.15
                : 1.0;
        double radius  = (30 + charge * 20) * pulse;

        // Color: blue when charging, white when full
        double r = full ? 1.0 : 0.3 + charge * 0.4;
        double g = full ? 1.0 : 0.5 + charge * 0.3;
        double b = full ? 1.0 : 1.0;

        // Outer soft ring
        gc.setStroke(Color.color(r, g, b, 0.15));
        gc.setLineWidth(8);
        gc.strokeOval(cx - radius, cy - radius, radius*2, radius*2);

        // Arc showing charge progress (0 → 360 degrees)
        double arcAngle = charge * 360;
        gc.setStroke(Color.color(r, g, b, full ? 0.9 : 0.7));
        gc.setLineWidth(2);
        gc.strokeArc(cx - radius, cy - radius, radius*2, radius*2,
                90, -arcAngle,         // starts at top, goes clockwise
                javafx.scene.shape.ArcType.OPEN);

        // Label when charging
        gc.setFill(Color.color(r, g, b, 0.8));
        gc.setFont(javafx.scene.text.Font.font("Courier New", 10));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        if (full) {
            gc.fillText("READY", cx, cy - radius - 6);
        } else {
            gc.fillText((int)(charge * 100) + "%", cx, cy - radius - 6);
        }
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }
}