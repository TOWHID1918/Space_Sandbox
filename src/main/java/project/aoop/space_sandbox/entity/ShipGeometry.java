package project.aoop.space_sandbox.entity;

/**
 * Single source of truth for the ship's shape.
 * Design space: origin = ship centre, +x = right wing, -y = nose.
 * ShipRenderer draws in this space, CombatService spawns shots from it,
 * so the barrels and the shots can never drift apart.
 */
public final class ShipGeometry {

    private ShipGeometry() {}

    /** World units covered by one design unit. Change this to resize the ship everywhere. */
    public static final double WORLD_PER_UNIT = 0.08;

    // Muzzle tips in design units
    public static final double BULLET_MUZZLE_X = -6.2;   // left wing
    public static final double BULLET_MUZZLE_Y = -3.6;
    public static final double LASER_MUZZLE_X  =  6.2;   // right wing
    public static final double LASER_MUZZLE_Y  = -4.2;

    /** Converts a point on the ship (design units) to world coordinates. */
    public static double[] toWorld(Ship ship, double ux, double uy) {
        double sin = Math.sin(ship.getAngle());
        double cos = Math.cos(ship.getAngle());
        // design +x -> (-sin, cos)   |   design -y (forward) -> (cos, sin)
        double wx = ship.getX() + (-ux * sin - uy * cos) * WORLD_PER_UNIT;
        double wy = ship.getY() + ( ux * cos - uy * sin) * WORLD_PER_UNIT;
        return new double[]{wx, wy};
    }
}