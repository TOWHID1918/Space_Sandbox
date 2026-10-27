package project.aoop.space_sandbox.entity;
//test
public class Bullet {

    private double x, y;
    private double vx, vy;
    private double lifetime;
    private final boolean laser;
    private final boolean attachedToShip;
    private final double  chargeLevel; // 0.0–1.0, only used for laser

    // Regular bullet — no charge
    public Bullet(double x, double y, double vx, double vy) {
        this.x           = x;
        this.y           = y;
        this.vx          = vx;
        this.vy          = vy;
        this.laser       = false;
        this.attachedToShip = false;
        this.chargeLevel = 0;
        this.lifetime    = 2.0;
    }

    // Charged laser — lifetime and size scale with charge
    public Bullet(double x, double y, double vx, double vy, double chargeLevel) {
        this.x           = x;
        this.y           = y;
        this.vx          = vx;
        this.vy          = vy;
        this.laser       = true;
        this.attachedToShip = true;
        this.chargeLevel = chargeLevel;
        // Charge percentage maps directly to seconds: 30% = 3s, 100% = 10s.
        this.lifetime    = chargeLevel * 10.0;
    }

    public void update(double dt) {
        x        += vx * dt;
        y        += vy * dt;
        lifetime -= dt;
    }

    public void update(double dt, Ship ship) {
        if (attachedToShip) {
            x = ship.getX();
            y = ship.getY();
            vx = Math.cos(ship.getAngle());
            vy = Math.sin(ship.getAngle());
        } else {
            x += vx * dt;
            y += vy * dt;
        }
        lifetime -= dt;
    }

    public boolean isExpired()      { return lifetime <= 0; }
    public boolean isLaser()        { return laser; }
    public double  getChargeLevel() { return chargeLevel; }
    public double  getX()           { return x; }
    public double  getY()           { return y; }
    public double  getVx()          { return vx; }
    public double  getVy()          { return vy; }
    public double  getLifetime()    { return lifetime; }
}