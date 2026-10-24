package project.aoop.space_sandbox.service;

import org.springframework.stereotype.Service;
import project.aoop.space_sandbox.entity.Bullet;
import project.aoop.space_sandbox.entity.Ship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class CombatService {

    private final List<Bullet> bullets     = new ArrayList<>();
    private final LevelService levelService;

    // ── Bullet cooldown ───────────────────────────────────────────────────
    private double shootCooldown = 0;

    // ── Laser charge ──────────────────────────────────────────────────────
    private double  chargeLevel  = 0;    // 0.0 → 1.0
    private boolean charging     = false;
    private boolean laserMode    = false;

    private static final double CHARGE_TIME  = 10.0; // seconds to reach 100% charge
    private static final double MIN_CHARGE   = 0.1;  // below this charge is wasted
    private static final double BULLET_SPEED = 6.0;
    private static final double LASER_SPEED  = 10.0;

    public CombatService(LevelService levelService) {
        this.levelService = levelService;
    }

    // ── Called by GameLoop based on mouse state ───────────────────────────

    // Mouse pressed — start charging if laser, otherwise fire bullets
    public void onMousePressed(Ship ship) {
        if (shouldUseLaser()) {
            charging    = true;
            chargeLevel = 0;  // reset charge on new press
        }
    }

    // Mouse held every frame
    public void onMouseHeld(Ship ship, double dt) {
        if (shouldUseLaser()) {
            // Build charge
            if (charging) {
                chargeLevel = Math.min(1.0, chargeLevel + dt / CHARGE_TIME);
            }
        } else {
            // No laser — fire bullets continuously
            tryShootBullet(ship);
            if (shootCooldown > 0) shootCooldown -= dt;
        }
    }

    // Mouse released
    public void onMouseReleased(Ship ship) {
        if (shouldUseLaser() && charging) {
            if (chargeLevel >= MIN_CHARGE) {
                fireChargedLaser(ship);
            } else {
                System.out.println("⚡ Charge too low — hold longer");
            }
            charging    = false;
            chargeLevel = 0;
        }
    }

    // ── Internal fire methods ─────────────────────────────────────────────

    private void tryShootBullet(Ship ship) {
        if (shootCooldown > 0) return;

        double directionX = Math.cos(ship.getAngle());
        double directionY = Math.sin(ship.getAngle());
        double muzzleDistance = 1.2;
        double x = ship.getX() + directionX * muzzleDistance;
        double y = ship.getY() + directionY * muzzleDistance;
        double bvx = directionX * BULLET_SPEED + ship.getVx();
        double bvy = directionY * BULLET_SPEED + ship.getVy();

        bullets.add(new Bullet(x, y, bvx, bvy));
        shootCooldown = levelService.getFireRateCooldown();
    }

    private void fireChargedLaser(Ship ship) {
        double directionX = Math.cos(ship.getAngle());
        double directionY = Math.sin(ship.getAngle());
        double bvx   = directionX * LASER_SPEED;
        double bvy   = directionY * LASER_SPEED;

        bullets.add(new Bullet(ship.getX(), ship.getY(), bvx, bvy, chargeLevel));
        System.out.println("⚡ Laser fired at "
                + (int)(chargeLevel * 100) + "% charge");
    }

    // ── Called every frame ────────────────────────────────────────────────

    public void update(double dt, Ship ship) {
        if (shootCooldown > 0) shootCooldown -= dt;

        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update(dt, ship);
            if (b.isExpired()) it.remove();
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public List<Bullet> getBullets()    { return bullets; }
    public double       getCharge()     { return chargeLevel; }
    public boolean      isCharging()    { return charging; }
    public boolean      isLaserMode()   { return shouldUseLaser(); }

    public void toggleWeaponMode() {
        if (levelService.hasLaser()) {
            laserMode = !laserMode;
            charging = false;
            chargeLevel = 0;
            System.out.println("Weapon: " + (laserMode ? "LASER" : "BULLET"));
        }
    }

    private boolean shouldUseLaser() {
        return levelService.hasLaser() && laserMode;
    }

    // True when charge is ready to fire (visual feedback)
    public boolean isFullyCharged()     { return chargeLevel >= 1.0; }
}