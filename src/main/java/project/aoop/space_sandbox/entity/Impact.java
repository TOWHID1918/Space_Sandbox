package project.aoop.space_sandbox.entity;

/** A short-lived bullet-hit effect (flash + ring + sparks). */
public class Impact {

    public static final double LIFE = 0.45;

    private final double x, y;
    private final double angle;   // direction the bullet was travelling
    private double age = 0;

    public Impact(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void update(double dt)  { age += dt; }
    public boolean isDead()        { return age >= LIFE; }
    public double getProgress()    { return Math.min(1.0, age / LIFE); }
    public double getX()           { return x; }
    public double getY()           { return y; }
    public double getAngle()       { return angle; }
}