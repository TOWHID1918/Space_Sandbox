package project.aoop.space_sandbox.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.springframework.stereotype.Component;
import project.aoop.space_sandbox.camera.GameCamera;
import project.aoop.space_sandbox.entity.Planet;
import project.aoop.space_sandbox.entity.Ship;
import project.aoop.space_sandbox.entity.ShipGeometry;
import project.aoop.space_sandbox.service.LevelService;
import project.aoop.space_sandbox.service.NavigationService;

import java.util.List;

@Component
public class HUDRenderer {

    private final GameCamera       camera;
    private final LevelService     levelService;
    private final NavigationService navigationService;

    public HUDRenderer(GameCamera camera, LevelService levelService,
                       NavigationService navigationService) {
        this.camera            = camera;
        this.levelService      = levelService;
        this.navigationService = navigationService;
    }

    public void render(GraphicsContext gc, Ship ship, List<Planet> planets) {
        drawLevelPanel(gc);
        drawTargetArrow(gc, ship, planets);
        drawSpeedBar(gc, ship);
    }

    private void drawLevelPanel(GraphicsContext gc) {
        double h = levelService.hasLaser() ? 95 : 78;

        gc.setFill(Color.color(0, 0, 0, 0.65));
        gc.fillRoundRect(12, 12, 200, h, 8, 8);
        gc.setStroke(Color.color(1, 1, 1, 0.08));
        gc.setLineWidth(1);
        gc.strokeRoundRect(12, 12, 200, h, 8, 8);

        gc.setFont(Font.font("Courier New", 10));
        gc.setFill(Color.color(1, 1, 1, 0.3));
        gc.fillText("── SHIP LEVEL ──", 22, 30);

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
        gc.setFill(Color.web("#4dffc8"));
        gc.fillText("LEVEL " + levelService.getLevel(), 22, 54);

        gc.setFont(Font.font("Courier New", 10));
        gc.setFill(Color.color(1, 1, 1, 0.4));
        gc.fillText(String.join("  ", levelService.getAccessiblePlanets()), 22, 70);

        if (levelService.hasLaser()) {
            gc.setFill(Color.web("#ff4444"));
            gc.fillText("⚡ LASER UNLOCKED", 22, 86);
        }
    }

    private void drawTargetArrow(GraphicsContext gc, Ship ship, List<Planet> planets) {
        String targetName = navigationService.getTarget();
        Planet target = planets.stream()
                .filter(p -> p.getName().equals(targetName))
                .findFirst().orElse(null);
        if (target == null) return;

        double W = camera.getScreenW(), H = camera.getScreenH();

        // Anchor to where the ship is actually drawn
        double cx = camera.toScreenX(ship.getX());
        double cy = camera.toScreenY(ship.getY());

        double dx    = target.getX() - ship.getX();
        double dy    = target.getY() - ship.getY();
        double dist  = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);

        double tsx = camera.toScreenX(target.getX());
        double tsy = camera.toScreenY(target.getY());
        double tr  = target.getRadius() * camera.getZoom();
        boolean visible = tsx + tr > 0 && tsx - tr < W && tsy + tr > 0 && tsy - tr < H;

        if (visible) {
            // planet is on screen: ring around it
            double pr = tr + 12;
            gc.setStroke(Color.color(0.3, 1, 0.8, 0.5));
            gc.setLineWidth(1.5);
            gc.strokeOval(tsx - pr, tsy - pr, pr * 2, pr * 2);
            return;
        }

        // off screen: arrow sits just outside the ship's wings, scaled to ship size
        double shipRadiusPx = ShipGeometry.WORLD_PER_UNIT * camera.getZoom() * 10.5;
        double ringDist = Math.min(shipRadiusPx + 44, Math.min(W, H) * 0.5 - 60);
        ringDist = Math.max(ringDist, 60);

        double ax = cx + Math.cos(angle) * ringDist;
        double ay = cy + Math.sin(angle) * ringDist;

        gc.save();
        gc.translate(ax, ay);
        gc.rotate(Math.toDegrees(angle));
        gc.setFill(Color.color(0.3, 1, 0.8, 0.9));
        gc.fillPolygon(new double[]{14, -6, -6}, new double[]{0, -5, 5}, 3);
        gc.setStroke(Color.color(0.3, 1, 0.8, 0.4));
        gc.setLineWidth(2);
        gc.strokeLine(-6, 0, -16, 0);
        gc.restore();

        double lx = ax + Math.cos(angle) * 24;
        double ly = ay + Math.sin(angle) * 24;
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.color(0.3, 1, 0.8, 0.9));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        gc.fillText("→ " + target.getName(), lx, ly - 5);
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.setFont(Font.font("Courier New", 10));
        gc.fillText((int) dist + " u", lx, ly + 9);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    public void drawLockWarnings(GraphicsContext gc, Ship ship, List<Planet> planets) {
        for (Planet p : planets) {
            if (!levelService.isLocked(p.getName())) continue;

            double dx   = ship.getX() - p.getX();
            double dy   = ship.getY() - p.getY();
            double dist = Math.sqrt(dx*dx + dy*dy);

            double zone    = p.getGravityRadius();
            boolean inside = dist < zone;
            boolean close  = dist < zone * 1.8;

            if (!close) continue;

            double cx = camera.getScreenW() / 2;
            double cy = camera.getScreenH() / 2;

            double severity = 1.0 - ((dist - zone) / (zone * 0.8));
            severity = Math.max(0, Math.min(1, severity));

            double pulse = 0.6 + 0.4 * Math.sin(
                    System.currentTimeMillis() * (0.004 + severity * 0.012));

            if (inside) {
                for (int i = 0; i < 3; i++) {
                    double margin = i * 12.0;
                    gc.setStroke(Color.color(1, 0.1, 0.1,
                            0.08 * pulse * (3 - i)));
                    gc.setLineWidth(20 - i * 6);
                    gc.strokeRect(margin, margin,
                            camera.getScreenW() - margin*2,
                            camera.getScreenH() - margin*2);
                }

                double bw = 420, bh = 52;
                double bx = cx - bw/2, by = 60;

                gc.setFill(Color.color(0.6, 0, 0, 0.75 * pulse));
                gc.fillRoundRect(bx, by, bw, bh, 6, 6);

                gc.setStroke(Color.color(1, 0.2, 0.2, pulse));
                gc.setLineWidth(1.5);
                gc.strokeRoundRect(bx, by, bw, bh, 6, 6);

                gc.setFill(Color.color(1, 1, 1, pulse));
                gc.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("⚠  LOCKED ZONE — " + p.getName().toUpperCase()
                        + "  ⚠", cx, by + 33);
                gc.setTextAlign(TextAlignment.LEFT);

                double surface = dist - p.getRadius();
                gc.setFill(Color.color(1, 0.5, 0.5, 0.85));
                gc.setFont(Font.font("Courier New", 11));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(String.format("%.0f u to surface — THRUST AWAY NOW",
                        Math.max(0, surface)), cx, by + bh + 16);
                gc.setTextAlign(TextAlignment.LEFT);
            } else {
                double angle = Math.atan2(dy, dx);
                double ww = 260, wh = 44;

                double wx, wy;
                if (Math.abs(Math.cos(angle)) > Math.abs(Math.sin(angle))) {
                    wx = Math.cos(angle) > 0
                            ? camera.getScreenW() - ww - 12
                            : 12;
                    wy = cy - wh / 2;
                } else {
                    wx = cx - ww / 2;
                    wy = Math.sin(angle) > 0
                            ? camera.getScreenH() - wh - 50
                            : 115;
                }

                gc.setFill(Color.color(0.3, 0, 0, 0.70 * pulse));
                gc.fillRoundRect(wx, wy, ww, wh, 6, 6);

                gc.setStroke(Color.color(1, 0.2, 0.2, 0.5 * pulse));
                gc.setLineWidth(1);
                gc.strokeRoundRect(wx, wy, ww, wh, 6, 6);

                gc.setFill(Color.color(1, 0.4, 0.4, pulse));
                gc.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("⚠  " + p.getName().toUpperCase() + " — LOCKED",
                        wx + ww/2, wy + 17);

                double toZone = dist - zone;
                gc.setFill(Color.color(1, 0.7, 0.7, 0.8 * pulse));
                gc.setFont(Font.font("Courier New", 10));
                gc.fillText(String.format("%.0f u to danger zone", toZone),
                        wx + ww/2, wy + 32);
                gc.setTextAlign(TextAlignment.LEFT);
            }
        }
    }

    private void drawSpeedBar(GraphicsContext gc, Ship ship) {
        double cx     = camera.getScreenW() / 2;
        double bottom = camera.getScreenH() - 14;

        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.fillRoundRect(cx - 80, bottom - 28, 160, 28, 6, 6);
        gc.setFill(Color.color(1, 1, 1, 0.55));
        gc.setFont(Font.font("Courier New", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("SPD  %.2f  u/s", ship.getSpeed()), cx, bottom - 10);
        gc.setTextAlign(TextAlignment.LEFT);
    }
}