package project.aoop.space_sandbox.render;

import jakarta.annotation.PostConstruct;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;
import project.aoop.space_sandbox.camera.GameCamera;
import project.aoop.space_sandbox.entity.CelestialBody;
import project.aoop.space_sandbox.entity.Planet;
import project.aoop.space_sandbox.entity.Ship;
import project.aoop.space_sandbox.entity.Star;
import project.aoop.space_sandbox.service.CombatService;
import project.aoop.space_sandbox.service.LevelService;
import project.aoop.space_sandbox.service.NavigationService;

import java.io.InputStream;
import java.util.List;

@Component
public class GameRenderer {

    private Canvas          canvas;
    private GraphicsContext gc;
    private Image           sunImage;

    // Last mouse position — stored for map click handling
    private double lastMouseX, lastMouseY;

    private final GameCamera               camera;
    private final StarfieldRenderer        starfieldRenderer;
    private final ShipRenderer             shipRenderer;
    private final PlanetRenderer           planetRenderer;
    private final MinimapRenderer          minimapRenderer;
    private final HUDRenderer             hudRenderer;
    private final SolarSystemMap          solarSystemMap;
    private final ProceduralPlanetRenderer proceduralRenderer;
    private final BulletRenderer          bulletRenderer;
    private final NavigationService        navigationService;
    private final LevelService             levelService;
    private final CombatService            combatService;
    private final Star                     star;
    private final List<Planet>             planets;

    public GameRenderer(GameCamera camera, StarfieldRenderer starfieldRenderer,
                        ShipRenderer shipRenderer, PlanetRenderer planetRenderer,
                        MinimapRenderer minimapRenderer, HUDRenderer hudRenderer,
                        SolarSystemMap solarSystemMap,
                        ProceduralPlanetRenderer proceduralRenderer,
                        BulletRenderer bulletRenderer,
                        NavigationService navigationService,
                        LevelService levelService,
                        CombatService combatService,
                        Star star, List<Planet> planets) {
        this.camera             = camera;
        this.starfieldRenderer  = starfieldRenderer;
        this.shipRenderer       = shipRenderer;
        this.planetRenderer     = planetRenderer;
        this.minimapRenderer    = minimapRenderer;
        this.hudRenderer        = hudRenderer;
        this.solarSystemMap     = solarSystemMap;
        this.proceduralRenderer = proceduralRenderer;
        this.bulletRenderer     = bulletRenderer;
        this.navigationService  = navigationService;
        this.levelService       = levelService;
        this.combatService      = combatService;
        this.star               = star;
        this.planets            = planets;
    }

    @PostConstruct
    public void init() {
        InputStream is = getClass().getResourceAsStream("/sprites/sun.jpg");
        if (is != null) sunImage = new Image(is);
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.gc     = canvas.getGraphicsContext2D();
    }

    // Called by JavaFXLauncher on mouse release
    // Only handles map/minimap — shooting is handled by InputHandler
    public void handleClick(double x, double y) {
        if (canvas == null) return;
        double w = canvas.getWidth(), h = canvas.getHeight();

        if (solarSystemMap.isOpen()) {
            solarSystemMap.handleClick(x, y, w, h, planets, navigationService);
        } else if (minimapRenderer.isClickOnMinimap(x, y, w, h)) {
            solarSystemMap.open();
        }
    }

    public void render(Ship ship, boolean thrusting) {
        if (gc == null) return;
        double w = canvas.getWidth(), h = canvas.getHeight();

        // World layers — back to front
        starfieldRenderer.render(gc, camera, w, h);
        planetRenderer.drawOrbitRings(gc, planets);
        // Draw ALL gravity zones — color tells the player exactly what each planet is
        planets.forEach(p -> drawPlanetGravityZone(gc, p, ship));
        drawSunGravityZone(gc, ship);
        proceduralRenderer.drawSun(gc, star);
        planets.forEach(p -> proceduralRenderer.drawPlanet(gc, p));
        bulletRenderer.render(gc, combatService.getBullets()); // bullets behind ship
        shipRenderer.render(gc, camera, ship, thrusting);

        // HUD — always on top
        minimapRenderer.render(gc, w, h, ship, star, planets);
        hudRenderer.render(gc, ship, planets);
        hudRenderer.drawLockWarnings(gc, ship, planets);
        solarSystemMap.render(gc, w, h, ship, star, planets, navigationService);
    }

    // ── Planet zones — color by status ───────────────────────────────────────
    private void drawPlanetGravityZone(GraphicsContext gc, Planet p, Ship ship) {
        double sx = camera.toScreenX(p.getX());
        double sy = camera.toScreenY(p.getY());
        double r  = p.getGravityRadius() * camera.getZoom();

        double dx      = ship.getX() - p.getX();
        double dy      = ship.getY() - p.getY();
        double dist    = Math.sqrt(dx*dx + dy*dy);
        boolean inside = dist < p.getGravityRadius();

        String name = p.getName();

        if (levelService.isMastered(name)) {
            // Mastered — white, very faint, dashed
            // Shows history without being distracting
            gc.setStroke(Color.color(1, 1, 1, inside ? 0.35 : 0.08));
            gc.setLineWidth(1.0);
            gc.setLineDashes(4);
            gc.strokeOval(sx - r, sy - r, r*2, r*2);
            gc.setLineDashes(0);

        } else if (levelService.isGravityActive(name)) {
            // Current level — blue, solid when inside
            gc.setStroke(inside
                    ? Color.color(0.3, 0.7, 1.0, 0.85)
                    : Color.color(0.3, 0.7, 1.0, 0.25));
            gc.setLineWidth(inside ? 2.0 : 1.0);
            gc.setLineDashes(inside ? 0 : 5);
            gc.strokeOval(sx - r, sy - r, r*2, r*2);
            gc.setLineDashes(0);

            // Inner pulse ring when inside
            if (inside) {
                gc.setStroke(Color.color(0.3, 0.7, 1.0, 0.12));
                gc.setLineWidth(6);
                gc.strokeOval(sx - r, sy - r, r*2, r*2);
            }

        } else if (levelService.isLocked(name)) {
            // Locked — red, warning, pulsing when inside
            if (inside) {
                // Pulsing danger effect
                double pulse = 0.5 + 0.5 * Math.sin(
                        System.currentTimeMillis() * 0.008);

                // Thick pulsing outer ring
                gc.setStroke(Color.color(1, 0.1, 0.1, 0.15 + pulse * 0.3));
                gc.setLineWidth(10);
                gc.strokeOval(sx - r, sy - r, r*2, r*2);

                // Sharp inner ring
                gc.setStroke(Color.color(1, 0.2, 0.2, 0.8));
                gc.setLineWidth(2);
                gc.strokeOval(sx - r, sy - r, r*2, r*2);

                // DANGER label near the planet
                double labelX = camera.toScreenX(p.getX());
                double labelY = camera.toScreenY(p.getY())
                        - p.getRadius() * camera.getZoom() - 18;
                gc.setFill(Color.color(1, 0.2, 0.2, 0.85));
                gc.setFont(javafx.scene.text.Font.font(
                        "Courier New", javafx.scene.text.FontWeight.BOLD, 11));
                gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
                gc.fillText("DANGER — LOCKED", labelX, labelY);
                gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);

            } else {
                // Outside — subtle red dashed ring, visible as warning
                gc.setStroke(Color.color(1, 0.2, 0.2, 0.18));
                gc.setLineWidth(1.0);
                gc.setLineDashes(5);
                gc.strokeOval(sx - r, sy - r, r*2, r*2);
                gc.setLineDashes(0);
            }
        }
    }

    private void drawSunGravityZone(GraphicsContext gc, Ship ship) {
        double sx = camera.toScreenX(star.getX());
        double sy = camera.toScreenY(star.getY());
        double r  = star.getGravityRadius() * camera.getZoom();

        double dx      = ship.getX() - star.getX();
        double dy      = ship.getY() - star.getY();
        boolean inside = Math.sqrt(dx*dx + dy*dy) < star.getGravityRadius();

        gc.setStroke(inside
                ? Color.color(1, 0.4, 0.0, 0.8)
                : Color.color(1, 0.5, 0.1, 0.12));
        gc.setLineWidth(inside ? 2.0 : 1.0);
        gc.setLineDashes(inside ? 0 : 6);
        gc.strokeOval(sx - r, sy - r, r*2, r*2);
        gc.setLineDashes(0);
    }

    private void drawGravityZone(CelestialBody body, Color baseColor, Ship ship) {
        double sx = camera.toScreenX(body.getX());
        double sy = camera.toScreenY(body.getY());
        double r  = body.getGravityRadius() * camera.getZoom();

        double dx      = ship.getX() - body.getX();
        double dy      = ship.getY() - body.getY();
        boolean inside = Math.sqrt(dx*dx + dy*dy) < body.getGravityRadius();

        Color ring = Color.color(baseColor.getRed(), baseColor.getGreen(),
                baseColor.getBlue(), inside ? 0.6 : 0.10);

        gc.setStroke(ring);
        gc.setLineWidth(inside ? 1.5 : 1.0);
        gc.setLineDashes(inside ? 0 : 6);
        gc.strokeOval(sx - r, sy - r, r*2, r*2);
        gc.setLineDashes(0);
    }

    private void drawSun() {
        double sx = camera.toScreenX(star.getX());
        double sy = camera.toScreenY(star.getY());
        double r  = star.getRadius() * camera.getZoom();

        gc.setFill(Color.color(1, 0.7, 0.1, 0.15));
        gc.fillOval(sx - r*2, sy - r*2, r*4, r*4);
        gc.setFill(Color.color(1, 0.8, 0.2, 0.25));
        gc.fillOval(sx - r*1.4, sy - r*1.4, r*2.8, r*2.8);

        if (sunImage != null) {
            gc.save();
            gc.beginPath();
            gc.arc(sx, sy, r, r, 0, 360);
            gc.clip();
            gc.drawImage(sunImage, sx - r, sy - r, r*2, r*2);
            gc.restore();
        } else {
            gc.setFill(Color.web("#ffe066"));
            gc.fillOval(sx - r, sy - r, r*2, r*2);
        }
    }
}