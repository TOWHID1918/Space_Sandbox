package project.aoop.space_sandbox.entity;

public abstract class CelestialBody {

    protected String name;
    protected double x, y;
    protected double mass;
    protected double radius;
    protected double gravityRadius;

    public CelestialBody(String name, double mass, double radius) {
        this.name   = name;
        this.mass   = mass;
        this.radius = radius;
    }

    public String getName()                 { return name; }
    public double getX()                    { return x; }
    public double getY()                    { return y; }
    public double getMass()                 { return mass; }
    public double getRadius()               { return radius; }
    public double getGravityRadius()        { return gravityRadius; }
    public void setX(double x)              { this.x = x; }
    public void setY(double y)              { this.y = y; }
    public void setGravityRadius(double r)  { this.gravityRadius = r; }
}