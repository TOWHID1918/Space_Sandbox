package project.aoop.space_sandbox.service;

import org.springframework.stereotype.Service;
import project.aoop.space_sandbox.config.PhysicsConfig;

import java.util.*;

@Service
public class LevelService {

    private int level = 1;
    private final Set<String> visitedPlanets = new HashSet<>();

    // Planet order: Earth > Venus > Mercury > Mars > Jupiter > Saturn > Uranus > Neptune
    // Safe planets: Earth, Mercury, Saturn
    // Enemy planets: Venus, Mars, Jupiter, Uranus, Neptune
    public int getPlanetLevel(String name) {
        return switch (name) {
            case "Earth", "Moon" -> 1;
            case "Venus"         -> 2;
            case "Mercury"       -> 3;
            case "Mars"          -> 4;
            case "Jupiter"       -> 5;
            case "Saturn"        -> 6;
            case "Uranus"        -> 7;
            case "Neptune"       -> 8;
            default              -> 0;
        };
    }

    // Safe = no enemies, just land and collect
    public boolean isSafePlanet(String name) {
        return switch (name) {
            case "Earth", "Moon", "Mercury", "Saturn" -> true;
            default -> false;
        };
    }

    // ── Planet access ─────────────────────────────────────────────────────
    public boolean isGravityActive(String name) { return getPlanetLevel(name) == level; }
    public boolean isAccessible(String name)    { return getPlanetLevel(name) <= level; }
    public boolean isMastered(String name)      { return getPlanetLevel(name) < level;  }
    public boolean isLocked(String name)        { return getPlanetLevel(name) > level;  }

    // ── Level-based ship stats ─────────────────────────────────────────────
    public double getSpeedLimit()        { return PhysicsConfig.SPEED_LIMITS[level];    }
    public double getThrustPower()       { return PhysicsConfig.THRUST_POWERS[level];   }
    public double getBrakePower()        { return getSpeedLimit() / PhysicsConfig.BRAKE_STOP_TIMES[level]; }
    public double getFireRateCooldown()  { return PhysicsConfig.FIRE_RATES[level];      }

    public List<String> getAccessiblePlanets() {
        List<String> list = new ArrayList<>(Arrays.asList("Earth", "Moon"));
        if (level >= 2) list.add("Venus");
        if (level >= 3) list.add("Mercury");
        if (level >= 4) list.add("Mars");
        if (level >= 5) list.add("Jupiter");
        if (level >= 6) list.add("Saturn");
        if (level >= 7) list.add("Uranus");
        if (level >= 8) list.add("Neptune");
        return list;
    }

    public String getTargetPlanet() {
        return switch (level) {
            case 1 -> visitedPlanets.contains("Earth") ? "Moon" : "Earth";
            case 2 -> "Venus";
            case 3 -> "Mercury";
            case 4 -> "Mars";
            case 5 -> "Jupiter";
            case 6 -> "Saturn";
            case 7 -> "Uranus";
            case 8 -> "Neptune";
            default -> "Neptune";
        };
    }

    public void visitPlanet(String name) {
        if (visitedPlanets.contains(name)) return;
        visitedPlanets.add(name);
        System.out.println("✓ Visited: " + name);
    }

    public void levelUp() {
        if (level < 8) {
            level++;
            System.out.println("★ LEVEL " + level
                    + " — target: " + getTargetPlanet()
                    + " | speed: " + getSpeedLimit() + " u/s"
                    + " | " + (isSafePlanet(getTargetPlanet()) ? "SAFE" : "ENEMIES"));
        }
    }

    public void levelDown() {
        if (level > 1) {
            level--;
            System.out.println("★ LEVEL " + level
                    + " — target: " + getTargetPlanet()
                    + " | speed: " + getSpeedLimit() + " u/s");
        }
    }

    // Laser unlocked at level 5 (Jupiter)
    public boolean hasLaser()        { return level >= 5; }
    public int     getLevel()        { return level; }
    public Set<String> getVisited()  { return visitedPlanets; }
}