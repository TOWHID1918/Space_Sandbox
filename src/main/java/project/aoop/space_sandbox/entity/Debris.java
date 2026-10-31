package project.aoop.space_sandbox.entity;

/** Short-lived effect: rock chips, a flash when an asteroid breaks, or a hit spark. */
public class Debris {

    public static final int CHIP = 0, FLASH = 1, SPARK = 2;

    private final int    type;
    private double       x, y, vx, vy;
    private final double maxLife;
    private double       life;
    private final double size;      // world units
    private double       angle;
    private final double spin;
    private final double tint;

    private Debris(int type, double x, double y, double vx, double vy,
                   double life, double size, double spin, double tint) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.maxLife = life;
        this.life = life;
        this.size = size;
        this.spin = spin;
        this.tint = tint;
    }

    public static Debris chip(double x, double y, double vx, double vy,
                              double life, double size, double tint) {
        return new Debris(CHIP, x, y, vx, vy, life, size, (Math.random() - 0.5) * 12.0, tint);
    }

    public static Debris flash(double x, double y, double size) {
        return new Debris(FLASH, x, y, 0, 0, 0.28, size, 0, 0);
    }

    public static Debris spark(double x, double y, double vx, double vy) {
        return new Debris(SPARK, x, y, vx, vy, 0.18 + Math.random() * 0.12, 0.15, 0, 0);
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
        angle += spin * dt;
        life -= dt;
    }

    public boolean isDead()      { return life <= 0; }
    public double  getProgress() { return Math.max(0, Math.min(1, 1.0 - life / maxLife)); }

    public int    getType()  { return type; }
    public double getX()     { return x; }
    public double getY()     { return y; }
    public double getSize()  { return size; }
    public double getAngle() { return angle; }
    public double getTint()  { return tint; }
}