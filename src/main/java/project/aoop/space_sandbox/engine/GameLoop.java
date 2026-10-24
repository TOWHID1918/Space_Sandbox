package project.aoop.space_sandbox.engine;

import javafx.animation.AnimationTimer;
import org.springframework.stereotype.Component;
import project.aoop.space_sandbox.camera.GameCamera;
import project.aoop.space_sandbox.config.PhysicsConfig;
import project.aoop.space_sandbox.entity.CelestialBody;
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

    private long    lastTime    = 0;
    private boolean levelUpHeld = false;
    private boolean levelDownHeld = false;
    private boolean escHeld     = false;
    private boolean mousePressOnMinimap = false;

    // Set by JavaFXLauncher after canvas is created
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

    // Called from JavaFXLauncher right after canvas is created
    public void setCanvasSize(double w, double h) {
        this.canvasW = w;
        this.canvasH = h;
    }

    @Override
    public void handle(long now) {
        if (lastTime == 0) {
            lastTime = now;
            inputHandler.clearFrameState();
            return;
        }
        double dt = Math.min((now - lastTime) / 1_000_000_000.0, 0.05);
        lastTime = now;

        if (!gameState.isRunning()) {
            inputHandler.clearFrameState();
            return;
        }

        // ── 1. Input ──────────────────────────────────────────────────────
        if (inputHandler.thrustForward()) shipService.thrustForward(dt);
        if (inputHandler.rotateLeft())    shipService.rotateLeft(dt);
        if (inputHandler.rotateRight())   shipService.rotateRight(dt);
        if (inputHandler.brake())         shipService.brake(dt);

        if (inputHandler.isSecondaryMouseJustPressed()) {
            combatService.toggleWeaponMode();
        }

        // Mouse just pressed — remember whether this interaction started on the minimap.
        if (inputHandler.isMouseJustPressed()) {
            mousePressOnMinimap = !solarSystemMap.isOpen()
                    && minimapRenderer.isClickOnMinimap(
                    inputHandler.getMouseX(), inputHandler.getMouseY(),
                    canvasW, canvasH);

            if (!solarSystemMap.isOpen() && !mousePressOnMinimap) {
                combatService.onMousePressed(shipService.getShip());
            }
        }

        // Mouse held — build charge or fire bullets continuously
        if (inputHandler.isMouseHeld()
                && !solarSystemMap.isOpen()
                && !mousePressOnMinimap) {
            combatService.onMouseHeld(shipService.getShip(), dt);
        }

        // Mouse just released — fire laser or handle map click
        if (inputHandler.isMouseJustReleased()) {
            if (solarSystemMap.isOpen()) {
                // Map open — treat release as planet selection click
                renderer.handleClick(
                        inputHandler.getMouseX(),
                        inputHandler.getMouseY()
                );
            } else if (mousePressOnMinimap || minimapRenderer.isClickOnMinimap(
                    inputHandler.getMouseX(),
                    inputHandler.getMouseY(),
                    canvasW, canvasH)) {
                // Click on minimap — open full map
                solarSystemMap.open();
            } else {
                // Shooting — fire charged laser or do nothing for bullets
                combatService.onMouseReleased(shipService.getShip());
            }
            mousePressOnMinimap = false;
        }

        // These flags describe one frame only; do not replay the click next frame.
        inputHandler.clearFrameState();

        // Level up — single press only
        if (inputHandler.levelUp()) {
            if (!levelUpHeld) { levelService.levelUp(); levelUpHeld = true; }
        } else { levelUpHeld = false; }

        if (inputHandler.levelDown()) {
            if (!levelDownHeld) { levelService.levelDown(); levelDownHeld = true; }
        } else { levelDownHeld = false; }

        // ESC — close map
        if (inputHandler.closeMap()) {
            if (!escHeld) { solarSystemMap.close(); escHeld = true; }
        } else { escHeld = false; }

        // ── 2. Move planets ───────────────────────────────────────────────
        for (Planet p : planets) {
            if (p instanceof project.aoop.space_sandbox.entity.Moon m) {
                m.updateAroundEarth(dt);
            } else {
                orbitalMechanics.update(p, dt);
            }
        }

        // ── 3. Update bullets ─────────────────────────────────────────────
        combatService.update(dt, shipService.getShip());

        // ── 4. Physics ────────────────────────────────────────────────────
        List<CelestialBody> bodies = new ArrayList<>();
        bodies.add(star);
        bodies.addAll(planets);
        shipService.update(bodies, dt);

        // ── 5. Crash checks ───────────────────────────────────────────────
        Ship ship = shipService.getShip();

        // Sun
        if (dist(ship, star.getX(), star.getY()) <= star.getRadius()) {
            ship.crash();
            gameState.setState(GameState.State.PAUSED);
            System.out.println("☀ DESTROYED — vaporized by the Sun");
            return;
        }

        // Locked planet — ship crashes on contact
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

        // Current/mastered planet — normal landing check
        for (Planet p : planets) {
            if (!levelService.isAccessible(p.getName())) continue;
            if (dist(ship, p.getX(), p.getY()) <= p.getRadius()) {
                handleLanding(ship, p);
                break;
            }
        }

        // ── 6. Camera + render ────────────────────────────────────────────
        camera.follow(ship.getX(), ship.getY(), dt);
        renderer.render(ship, inputHandler.thrustForward());
    }

    private double dist(Ship ship, double tx, double ty) {
        double dx = ship.getX() - tx;
        double dy = ship.getY() - ty;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void handleLanding(Ship ship, Planet planet) {
        if (ship.getSpeed() <= PhysicsConfig.SAFE_LAND_SPEED) {
            levelService.visitPlanet(planet.getName());
        }
    }
}