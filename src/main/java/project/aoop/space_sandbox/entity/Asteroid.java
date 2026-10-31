package project.aoop.space_sandbox.entity;

import java.util.Random;

public class Asteroid {

    public enum Kind { BELT, INCOMING }

    public static final int SHAPE_POINTS = 11;

    private final long   id;        // belt asteroids keep a stable id so destroyed ones stay destroyed
    private final long   cellKey;   // belt chunk this asteroid belongs to
    private final Kind   kind;

    private double x, y;
    private final double vx, vy;    // constant: asteroids ignore gravity

    private final double radius;    // world units
    private final double maxHealth;
    private double health;

    private double angle;           // visual tumble
    private final double spin;
    private final double tint;      // 0..1 colour variation

    private final float[]    shape;    // radial multipliers -> lumpy outline
    private final double[][] craters;  // {angle, distance 0..1, size 0..1}

    private double  hitFlash = 0;
    private double  age      = 0;
    private boolean credited = true;   // counts toward the destroyed counter

    public Asteroid(long id, long cellKey, Kind kind,
                    double x, double y, double vx, double vy,
                    double radius, double angle, double spin,
                    double tint, long shapeSeed) {
        this.id = id;
        this.cellKey = cellKey;
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.angle = angle;
        this.spin = spin;
        this.tint = tint;
        this.maxHealth = Math.max(1, Math.round(radius * 1.1));
        this.health = maxHealth;

        Random r = new Random(shapeSeed);
        shape = new float[SHAPE_POINTS];
        for (int i = 0; i < SHAPE_POINTS; i++) shape[i] = 0.72f + r.nextFloat() * 0.40f;

        craters = new double[3][3];
        for (int i = 0; i < 3; i++) {
            craters[i][0] = r.nextDouble() * Math.PI * 2;
            craters[i][1] = 0.15 + r.nextDouble() * 0.40;
            craters[i][2] = 0.14 + r.nextDouble() * 0.16;
        }
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
        angle += spin * dt;
        age += dt;
        if (hitFlash > 0) hitFlash = Math.max(0, hitFlash - dt * 6.0);
    }

    public void damage(double amount) {
        health -= amount;
        hitFlash = 1.0;
    }

    public void destroy()         { health = 0; }
    public void destroyNoCredit() { health = 0; credited = false; }

    public boolean isDead()       { return health <= 0; }
    public boolean isCredited()   { return credited; }

    public long     getId()        { return id; }
    public long     getCellKey()   { return cellKey; }
    public Kind     getKind()      { return kind; }
    public double   getX()         { return x; }
    public double   getY()         { return y; }
    public double   getVx()        { return vx; }
    public double   getVy()        { return vy; }
    public double   getRadius()    { return radius; }
    public double   getHealth()    { return health; }
    public double   getMaxHealth() { return maxHealth; }
    public double   getAngle()     { return angle; }
    public double   getTint()      { return tint; }
    public double   getHitFlash()  { return hitFlash; }
    public double   getAge()       { return age; }
    public float[]  getShape()     { return shape; }
    public double[][] getCraters() { return craters; }
}