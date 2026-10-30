package project.aoop.space_sandbox.engine;

import javafx.animation.AnimationTimer;
import org.springframework.stereotype.Component;
import project.aoop.space_sandbox.camera.GameCamera;
import project.aoop.space_sandbox.entity.CelestialBody;
import project.aoop.space_sandbox.entity.Moon;
import project.aoop.space_sandbox.entity.Planet;
import project.aoop.space_sandbox.entity.Ship;
import project.aoop.space_sandbox.entity.Star;
import project.aoop.space_sandbox.input.InputHandler;
import project.aoop.space_sandbox.physics.OrbitalMechanics;
import project.aoop.space_sandbox.render.GameRenderer;
import project.aoop.space_sandbox.render.MinimapRenderer;
import project.aoop.space_sandbox.render.SolarSystemMap;
import project.aoop.space_sandbox.service.CombatService;
import project.aoop.space_sandbox.service.LevelService;
import project.aoop.space_sandbox.service.ShipService;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameLoop extends AnimationTimer {

    private long    lastTime      = 0;
    private boolean levelUpHeld   = false;
    private boolean levelDownHeld = false;
    private boolean escHeld       = false;

    // Set by JavaFXLauncher after the canvas is created
    private double canvasW = 1280;
    private double canvasH = 720;

    private final GameState        gameState;
    private final ShipService      shipService;
    private final InputHandler     inputHandler;
    private final GameRenderer     renderer;
    private final GameCamera       camera;
    private final Star             star;
    private final List<Planet>     planets;
    private final OrbitalMechanics orbitalMechanics;
    private final LevelService     levelService;
    private final CombatService    combatService;
    private final SolarSystemMap   solarSystemMap;
    private final MinimapRenderer  minimapRenderer;

    public GameLoop(GameState gameState, ShipService shipService,
                    InputHandler inputHandler, GameRenderer renderer,
                    GameCamera camera, Star star, List<Planet> planets,
                    OrbitalMechanics orbitalMechanics, LevelService levelService,
                    CombatService combatService, SolarSystemMap solarSystemMap,
                    MinimapRenderer minimapRenderer) {
        this.gameState        = gameState;
        this.shipService      = shipService;
        this.inputHandler     = inputHandler;
        this.renderer         = renderer;
        this.camera           = camera;
        this.star             = star;
        this.planets          = planets;
        this.orbitalMechanics = orbitalMechanics;
        this.levelService     = levelService;
        this.combatService    = combatService;
        this.solarSystemMap   = solarSystemMap;
        this.minimapRenderer  = minimapRenderer;
    }

    public void setCanvasSize(double w, double h) {
        this.canvasW = w;
        this.canvasH = h;
    }

    @Override
    public void handle(long now) {
        if (lastTime == 0) { lastTime = now; return; }
        double dt = Math.min((now - lastTime) / 1_000_000_000.0, 0.05);
        lastTime = now;

        if (!gameState.isRunning()) return;

        Ship ship = shipService.getShip();

        // ── 1. Input ──────────────────────────────────────────────────────
        if (inputHandler.thrustForward()) shipService.thrustForward(dt);
        if (inputHandler.rotateLeft())    shipService.rotateLeft(dt);
        if (inputHandler.rotateRight())   shipService.rotateRight(dt);
        if (inputHandler.brake())         shipService.brake(dt);

        // Mouse: clicks on the minimap / open map must never fire the weapons
        double  mx          = inputHandler.getMouseX();
        double  my          = inputHandler.getMouseY();
        boolean overMinimap = minimapRenderer.isClickOnMinimap(mx, my, canvasW, canvasH);
        boolean mapOpen     = solarSystemMap.isOpen();

        if (inputHandler.isMouseJustPressed() && !overMinimap && !mapOpen) {
            combatService.onMousePressed(ship);
        }
        if (inputHandler.isMouseHeld() && !overMinimap && !mapOpen) {
            combatService.onMouseHeld(ship, dt);
        }
        if (inputHandler.isMouseJustReleased()) {
            if (mapOpen) {
                renderer.handleClick(mx, my);                 // pick a planet on the map
            } else if (overMinimap && !combatService.isCharging()) {
                solarSystemMap.open();                        // open the full map
            } else {
                combatService.onMouseReleased(ship);          // fire the charged laser
            }
        }

        // Right click: switch bullet <-> laser
        if (inputHandler.isSecondaryMouseJustPressed()) {
            combatService.toggleWeaponMode();
        }

        // L = level up, P = level down (single press each)
        if (inputHandler.levelUp()) {
            if (!levelUpHeld) { levelService.levelUp(); levelUpHeld = true; }
        } else { levelUpHeld = false; }

        if (inputHandler.levelDown()) {
            if (!levelDownHeld) { levelService.levelDown(); levelDownHeld = true; }
        } else { levelDownHeld = false; }

        // ESC closes the map
        if (inputHandler.closeMap()) {
            if (!escHeld) { solarSystemMap.close(); escHeld = true; }
        } else { escHeld = false; }

        // ── 2. Move planets ───────────────────────────────────────────────
        for (Planet p : planets) {
            if (p instanceof Moon m) {
                m.updateAroundEarth(dt);       // Moon orbits Earth
            } else {
                orbitalMechanics.update(p, dt);
            }
        }

        // ── 3. Ship physics ───────────────────────────────────────────────
        List<CelestialBody> bodies = new ArrayList<>();
        bodies.add(star);
        bodies.addAll(planets);
        shipService.update(bodies, dt);

        // ── 4. Combat — AFTER the ship moved, so shots and the beam use the
        //       same ship position that gets drawn this frame ─────────────
        combatService.update(dt, ship);

        // ── 5. Crash / landing checks ─────────────────────────────────────
        if (dist(ship, star.getX(), star.getY()) <= star.getRadius()) {
            ship.crash();
            gameState.setState(GameState.State.PAUSED);
            System.out.println("☀ DESTROYED — vaporized by the Sun");
            return;
        }

        for (Planet p : planets) {
            if (!levelService.isLocked(p.getName())) continue;
            if (dist(ship, p.getX(), p.getY()) <= p.getRadius()) {
                ship.crash();
                gameState.setState(GameState.State.PAUSED);
                System.out.println("💀 DESTROYED — pulled into " + p.getName()
                        + " (locked — reach level "
                        + levelService.getPlanetLevel(p.getName()) + " first)");
                return;
            }
        }

        // ── 6. Camera locks onto the ship ─────────────────────────────────
        camera.follow(ship.getX(), ship.getY(), dt);

        // ── 7. Render ─────────────────────────────────────────────────────
        renderer.render(ship, inputHandler.thrustForward());

        // ── 8. Clear one-frame input flags ────────────────────────────────
        inputHandler.clearFrameState();
    }

    private double dist(Ship ship, double tx, double ty) {
        double dx = ship.getX() - tx;
        double dy = ship.getY() - ty;
        return Math.sqrt(dx * dx + dy * dy);
    }
}