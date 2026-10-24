package project.aoop.space_sandbox.entity;

import javafx.scene.paint.Color;

public class Planet extends CelestialBody {

    private final double semiMajorAxis;  // longest radius of ellipse
    private final double semiMinorAxis;  // shortest radius of ellipse
    private final double orbitalPeriod;  // seconds for one full orbit
    private final Color  color;          // fallback if no texture
    private double orbitalAngle = 0;     // current position on orbit

    public Planet(String name, double mass, double radius,
                  double semiMajorAxis, double semiMinorAxis,
                  double orbitalPeriod, Color color) {
        super(name, mass, radius);
        this.semiMajorAxis = semiMajorAxis;
        this.semiMinorAxis = semiMinorAxis;
        this.orbitalPeriod = orbitalPeriod;
        this.color         = color;
    }

    public double getSemiMajorAxis()       { return semiMajorAxis; }
    public double getSemiMinorAxis()       { return semiMinorAxis; }
    public double getOrbitalPeriod()       { return orbitalPeriod; }
    public Color  getColor()               { return color; }
    public double getOrbitalAngle()        { return orbitalAngle; }
    public void   setOrbitalAngle(double a) { this.orbitalAngle = a; }
}