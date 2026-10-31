package project.aoop.space_sandbox.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineJoin;
import org.springframework.stereotype.Component;
import project.aoop.space_sandbox.camera.GameCamera;
import project.aoop.space_sandbox.entity.Asteroid;
import project.aoop.space_sandbox.entity.Debris;
import project.aoop.space_sandbox.service.AsteroidService;

@Component
public class AsteroidRenderer {

    private static final Color OUTLINE = Color.color(0.09, 0.09, 0.11, 0.90);

    private final GameCamera      camera;
    private final AsteroidService asteroidService;

    // scratch buffers so we don't allocate per asteroid per frame
    private final double[] xs = new double[Asteroid.SHAPE_POINTS];
    private final double[] ys = new double[Asteroid.SHAPE_POINTS];

    public AsteroidRenderer(GameCamera camera, AsteroidService asteroidService) {
        this.camera = camera;
        this.asteroidService = asteroidService;
    }

    public void render(GraphicsContext gc) {
        double W    = camera.getScreenW();
        double H    = camera.getScreenH();
        double zoom = camera.getZoom();
        double t    = System.nanoTime() * 1e-9;

        gc.save();
        gc.setLineJoin(StrokeLineJoin.ROUND);

        for (Asteroid a : asteroidService.getAsteroids()) {
            double sx = camera.toScreenX(a.getX());
            double sy = camera.toScreenY(a.getY());
            double r  = a.getRadius() * zoom;
            double margin = a.getKind() == Asteroid.Kind.INCOMING ? r * 2.5 + 80 : r * 1.3 + 4;
            if (sx < -margin || sx > W + margin || sy < -margin || sy > H + margin) continue;

            drawAsteroid(gc, a, sx, sy, r, t);
        }

        for (Debris d : asteroidService.getDebris()) {
            drawDebris(gc, d, zoom);
        }

        gc.restore();
    }

    // ── rock colour: light grey, slightly different per asteroid ─────────

    private static Color rockColor(double tint) {
        double g    = 0.60 + 0.22 * tint;        // 0.60 (stone grey) .. 0.82 (near white)
        double warm = (tint - 0.5) * 0.05;       // tiny warm/cool drift
        return Color.color(c01(g + warm), c01(g), c01(g - warm));
    }

    // ── asteroid ─────────────────────────────────────────────────────────

    private void drawAsteroid(GraphicsContext gc, Asteroid a, double sx, double sy,
                              double r, double t) {
        if (a.getKind() == Asteroid.Kind.INCOMING) drawIncomingGlow(gc, a, sx, sy, r, t);

        Color base = rockColor(a.getTint());

        // far away: a dot is enough
        if (r < 2.5) {
            double rr = Math.max(r, 1.1);
            gc.setFill(shade(base, 1.25));
            gc.fillOval(sx - rr, sy - rr, rr * 2, rr * 2);
            return;
        }

        // lumpy outline, rotated by hand so the lighting stays fixed while the rock tumbles
        float[] shape = a.getShape();
        int n = shape.length;
        double cos = Math.cos(a.getAngle()), sin = Math.sin(a.getAngle());
        for (int i = 0; i < n; i++) {
            double ang = i * Math.PI * 2 / n;
            double lx = Math.cos(ang) * r * shape[i];
            double ly = Math.sin(ang) * r * shape[i];
            xs[i] = sx + lx * cos - ly * sin;
            ys[i] = sy + lx * sin + ly * cos;
        }

        // body: bright top-left, dark far edge
        gc.setFill(new RadialGradient(-135, 0.45, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, shade(base, 1.50)),
                new Stop(0.50, base),
                new Stop(1.00, shade(base, 0.38))));
        gc.fillPolygon(xs, ys, n);

        // surface detail, clipped to the rock so nothing sticks out
        if (r >= 6) {
            gc.save();
            gc.beginPath();
            gc.moveTo(xs[0], ys[0]);
            for (int i = 1; i < n; i++) gc.lineTo(xs[i], ys[i]);
            gc.closePath();
            gc.clip();

            // soft bounce light on the lit side
            gc.setFill(Color.color(1, 1, 1, 0.10));
            gc.fillOval(sx - r * 0.75, sy - r * 0.80, r * 1.1, r * 0.75);

            // craters: dark hollow + lit far wall
            for (double[] c : a.getCraters()) {
                double ang = c[0] + a.getAngle();
                double cx = sx + Math.cos(ang) * c[1] * r;
                double cy = sy + Math.sin(ang) * c[1] * r;
                double cr = c[2] * r;
                gc.setFill(Color.color(0, 0, 0, 0.22));
                gc.fillOval(cx - cr, cy - cr, cr * 2, cr * 2);
                gc.setStroke(Color.color(1, 1, 1, 0.28));
                gc.setLineWidth(Math.max(0.8, r * 0.03));
                gc.strokeArc(cx - cr, cy - cr, cr * 2, cr * 2, -110, 130, ArcType.OPEN);
            }

            // speckles on big rocks
            if (r >= 10) {
                for (int i = 0; i < n; i += 2) {
                    double ang = i * 2.3 + a.getAngle();
                    double dd  = 0.25 + 0.55 * ((shape[i] - 0.72) / 0.40);
                    double px  = sx + Math.cos(ang) * dd * r;
                    double py  = sy + Math.sin(ang) * dd * r;
                    double pr  = Math.max(0.8, r * 0.035);
                    gc.setFill((i / 2) % 2 == 0 ? Color.color(0, 0, 0, 0.14)
                            : Color.color(1, 1, 1, 0.12));
                    gc.fillOval(px - pr, py - pr, pr * 2, pr * 2);
                }
            }

            // cracks once it's badly damaged
            if (a.getHealth() < a.getMaxHealth() * 0.5) {
                gc.setStroke(Color.color(0.05, 0.05, 0.06, 0.80));
                gc.setLineWidth(Math.max(1.0, r * 0.05));
                for (double[] c : a.getCraters()) {
                    double ang = c[0] + a.getAngle();
                    gc.strokeLine(sx + Math.cos(ang) * r * 0.1, sy + Math.sin(ang) * r * 0.1,
                            sx + Math.cos(ang) * r * 0.85, sy + Math.sin(ang) * r * 0.85);
                }
            }
            gc.restore();
        }

        gc.setStroke(OUTLINE);
        gc.setLineWidth(Math.max(1.0, r * 0.06));
        gc.strokePolygon(xs, ys, n);

        // white flash when hit
        if (a.getHitFlash() > 0) {
            gc.setFill(Color.color(1, 1, 1, 0.55 * a.getHitFlash()));
            gc.fillPolygon(xs, ys, n);
        }
    }

    // pale streak behind + a faint red pulse, so incoming rocks still read as a threat
    private void drawIncomingGlow(GraphicsContext gc, Asteroid a, double sx, double sy,
                                  double r, double t) {
        double speed = Math.hypot(a.getVx(), a.getVy());
        if (speed < 1e-6) return;

        double dx = a.getVx() / speed, dy = a.getVy() / speed;
        double px = -dy, py = dx;
        double len = Math.min(r * 7.0 + 18.0, 110.0);
        double w   = Math.max(r * 0.7, 2.0);
        double bx  = sx - dx * len, by = sy - dy * len;

        gc.setFill(new LinearGradient(sx, sy, bx, by, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.color(0.88, 0.92, 1.0, 0.45)),
                new Stop(1.0, Color.color(0.70, 0.80, 1.0, 0.0))));
        gc.fillPolygon(new double[]{sx + px * w, sx - px * w, bx},
                new double[]{sy + py * w, sy - py * w, by}, 3);

        double pulse = 0.75 + 0.25 * Math.sin(t * 6.0 + a.getTint() * 10);
        double gr = Math.max(r, 4) * 2.3 * pulse;
        gc.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.color(1.0, 0.30, 0.25, 0.18)),
                new Stop(1.0, Color.color(1.0, 0.30, 0.25, 0.0))));
        gc.fillOval(sx - gr, sy - gr, gr * 2, gr * 2);
    }

    // ── debris, flashes, sparks ──────────────────────────────────────────

    private void drawDebris(GraphicsContext gc, Debris d, double zoom) {
        double sx = camera.toScreenX(d.getX());
        double sy = camera.toScreenY(d.getY());
        if (sx < -60 || sx > camera.getScreenW() + 60) return;
        if (sy < -60 || sy > camera.getScreenH() + 60) return;

        double p    = d.getProgress();
        double fade = 1.0 - p;

        switch (d.getType()) {
            case Debris.FLASH -> {   // white-grey dust puff
                double fr = Math.max(d.getSize() * zoom * (0.6 + 1.4 * p), 4) + 2;
                gc.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.color(1.0, 1.0, 1.0, 0.85 * fade)),
                        new Stop(0.45, Color.color(0.82, 0.84, 0.90, 0.45 * fade)),
                        new Stop(1.0, Color.color(0.70, 0.72, 0.80, 0.0))));
                gc.fillOval(sx - fr, sy - fr, fr * 2, fr * 2);
            }
            case Debris.CHIP -> {
                double s = Math.max(d.getSize() * zoom, 1.4);
                double cos = Math.cos(d.getAngle()), sin = Math.sin(d.getAngle());
                double[] ox = {-s, s * 0.8, s, -s * 0.6};
                double[] oy = {-s * 0.7, -s, s * 0.6, s};
                double[] px = new double[4], py = new double[4];
                for (int i = 0; i < 4; i++) {
                    px[i] = sx + ox[i] * cos - oy[i] * sin;
                    py[i] = sy + ox[i] * sin + oy[i] * cos;
                }
                double g = 0.60 + 0.22 * d.getTint();
                gc.setFill(Color.color(c01(g), c01(g), c01(g + 0.02), fade));
                gc.fillPolygon(px, py, 4);
            }
            default -> {   // SPARK (bullet hit): stays warm so impacts read clearly
                double s = Math.max(1.4, d.getSize() * zoom);
                gc.setFill(Color.color(1.0, 0.85, 0.45, fade));
                gc.fillOval(sx - s, sy - s, s * 2, s * 2);
            }
        }
    }

    // ── colour helpers ───────────────────────────────────────────────────

    private static double c01(double v) { return Math.max(0, Math.min(1, v)); }

    private static Color shade(Color c, double f) {
        return Color.color(c01(c.getRed() * f), c01(c.getGreen() * f), c01(c.getBlue() * f), c.getOpacity());
    }
}