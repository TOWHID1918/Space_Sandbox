# Space Sandbox

A 2D top-down space exploration game built with Java, Spring Boot, and JavaFX. Pilot a ship through the solar system, land on planets, collect resources, and upgrade your ship to reach the next destination.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 — dependency injection only, no web server |
| UI / Rendering | JavaFX 21 — Canvas + AnimationTimer |
| Build | Gradle (Groovy) |

---

## Controls

| Key / Input | Action |
|---|---|
| W / ↑ | Thrust forward |
| A / ← | Rotate left |
| D / → | Rotate right |
| Space | Brake (gradual deceleration) |
| Left Click (hold) | Shoot bullets / charge laser |
| Right Click | Switch weapon mode (bullet ↔ laser) |
| L | Level up (debug) |
| P | Level down (debug) |
| + / - | Zoom in / out |
| Scroll Wheel | Zoom |
| Click Minimap | Open solar system map |
| Click Planet on Map | Track that planet with arrow |
| ESC | Close map |

---

## Planet Order & Level System

```
Level 1 → Earth, Moon     (safe)
Level 2 → Venus           (enemies)
Level 3 → Mercury         (safe)
Level 4 → Mars            (enemies)
Level 5 → Jupiter         (enemies)  ← laser unlocked
Level 6 → Saturn          (safe)
Level 7 → Uranus          (enemies)
Level 8 → Neptune         (enemies)  ← final
```

### Gravity Rules Per Level

| Planet Status | Gravity | Zone Color |
|---|---|---|
| Current level | Orbit assist — ship naturally circles | Blue |
| Mastered (below level) | None — fly past freely | White (faint) |
| Locked (above level) | Strong unavoidable pull — crashes ship | Red (pulsing) |
| Sun | Raw gravity always — escapable with thrust | Orange |

---

## Speed & Thrust Per Level

| Level | Speed Limit | Thrust | Brake |
|---|---|---|---|
| 1 — Earth, Moon | 8.0 u/s | 0.08 | 0.17 |
| 2 — Venus | 10.0 u/s | 0.17 | 0.33 |
| 3 — Mercury | 12.0 u/s | 0.30 | 0.60 |
| 4 — Mars | 28.0 u/s | 0.50 | 1.00 |
| 5 — Jupiter | 45.0 u/s | 0.83 | 1.67 |
| 6 — Saturn | 60.0 u/s | 1.33 | 2.67 |
| 7 — Uranus | 75.0 u/s | 2.00 | 4.00 |
| 8 — Neptune | 90.0 u/s | 3.00 | 6.00 |

---

## Weapons

### Bullets
- Hold left click to fire continuously
- Fires from alternating gun barrels (gun 1 → gun 2 → gun 1 → ...)
- Speed: 6 u/s + ship velocity
- Fire rate increases each level

### Laser (unlocked at Level 5 — Jupiter)
- Switch to laser mode with right click
- Hold left click to charge (0% → 100% over 10 seconds)
- Release to fire — minimum 10% charge required
- Charge level determines size, speed, and lifetime
- Full charge: white pulsing ring appears around ship — "READY"

---

## Files By Feature

### Ship
| File | Role |
|---|---|
| `entity/Ship.java` | Stores x, y, vx, vy, angle, health. Methods: thrust, rotate, brake, update |
| `service/ShipService.java` | Coordinates input → ship. Applies speed cap per level |
| `input/InputHandler.java` | Tracks held keys and mouse state (W A D Space, clicks) |
| `render/ShipRenderer.java` | Draws ship.png at screen center, scales with zoom, shows charge ring |
| `camera/GameCamera.java` | Converts world coordinates → screen pixels. Smooth follow + zoom |

### Gravity
| File | Role |
|---|---|
| `physics/GravitySystem.java` | Applies orbit assist (planets) or raw pull (Sun) |
| `entity/CelestialBody.java` | Holds gravityRadius field — zone where gravity activates |
| `service/LevelService.java` | Decides which planets have active gravity per level |
| `config/PhysicsConfig.java` | G constant, ORBIT_ASSIST_RATE, RADIAL_DAMPEN_RATE, LOCKED_GRAVITY_MULT |

### Planets
| File | Role |
|---|---|
| `entity/CelestialBody.java` | Abstract base: name, x, y, mass, radius, gravityRadius |
| `entity/Planet.java` | Adds orbital data: semiMajorAxis, semiMinorAxis, period, orbitalAngle |
| `entity/Star.java` | The Sun — fixed at world (0, 0), always has gravity |
| `entity/Earth.java` | @Component so Moon can inject it as a dependency |
| `entity/Moon.java` | Orbits Earth (not Sun) — position = Earth + offset each frame |
| `physics/OrbitalMechanics.java` | Moves each planet along its ellipse every frame |
| `config/GameConfig.java` | Creates each planet as a Spring @Bean |
| `config/PhysicsConfig.java` | All planet constants: mass, radius, orbit size, gravity zone, start angle |
| `render/ProceduralPlanetRenderer.java` | Draws all planets with code — no image files needed |

### Shooting
| File | Role |
|---|---|
| `entity/Bullet.java` | Position, velocity, lifetime, charge level, laser flag |
| `service/CombatService.java` | Fires bullets, builds laser charge, moves projectiles, toggles weapon mode |
| `render/BulletRenderer.java` | Red rectangles for bullets, long red beam for laser (scales with charge) |
| `input/InputHandler.java` | Left click = shoot/charge, right click = switch weapon |

### Rendering
| File | Role |
|---|---|
| `render/GameRenderer.java` | Master renderer — calls all sub-renderers in correct order |
| `render/StarfieldRenderer.java` | 3-layer parallax procedural stars (infinite, hash-based, wrapping) |
| `render/PlanetRenderer.java` | Faint orbit rings for each planet |
| `render/ShipRenderer.java` | Ship image at screen center + charge ring overlay |
| `render/MinimapRenderer.java` | Log-scale minimap top-right. Click to expand |
| `render/HUDRenderer.java` | Level panel, navigation arrow, speed bar |
| `render/BulletRenderer.java` | Bullets and laser beams |
| `render/SolarSystemMap.java` | Full-screen map overlay with all planets. Click planet to track |
| `render/ProceduralPlanetRenderer.java` | Per-planet drawing: Earth has continents, Jupiter has bands, Saturn has rings |

### Level & Navigation
| File | Role |
|---|---|
| `service/LevelService.java` | Tracks level 1–8, which planets are accessible, speed/thrust/fire rate per level |
| `service/NavigationService.java` | Tracks arrow target — custom (player clicked) or default (progression) |

### Game Loop
| File | Role |
|---|---|
| `engine/GameLoop.java` | AnimationTimer ~60fps: input → planets → bullets → physics → crash → render |
| `engine/GameState.java` | MENU / RUNNING / PAUSED |
| `SpaceSandboxApplication.java` | Spring Boot entry point |
| `JavaFXLauncher.java` | Opens window, wires Spring beans, attaches keyboard/mouse |

---

## Package Structure

```
src/main/java/project/aoop/space_sandbox/
│
├── SpaceSandboxApplication.java
├── JavaFXLauncher.java
│
├── config/
│   ├── PhysicsConfig.java       ← ALL constants live here
│   └── GameConfig.java          ← planet @Bean factory
│
├── engine/
│   ├── GameLoop.java
│   └── GameState.java
│
├── entity/
│   ├── CelestialBody.java
│   ├── Star.java
│   ├── Planet.java
│   ├── Earth.java
│   ├── Moon.java
│   ├── Ship.java
│   └── Bullet.java
│
├── physics/
│   ├── GravitySystem.java
│   └── OrbitalMechanics.java
│
├── service/
│   ├── LevelService.java
│   ├── ShipService.java
│   ├── CombatService.java
│   └── NavigationService.java
│
├── render/
│   ├── GameRenderer.java
│   ├── StarfieldRenderer.java
│   ├── PlanetRenderer.java
│   ├── ProceduralPlanetRenderer.java
│   ├── ShipRenderer.java
│   ├── BulletRenderer.java
│   ├── MinimapRenderer.java
│   ├── HUDRenderer.java
│   └── SolarSystemMap.java
│
├── camera/
│   └── GameCamera.java
│
├── input/
│   └── InputHandler.java
│
└── controller/
    ├── GameSceneController.java
    ├── MainMenuController.java
    ├── LandingSceneController.java
    └── PlatformerSceneController.java

src/main/resources/
├── application.properties        ← spring.main.web-application-type=none
├── fxml/                         ← JavaFX scene layouts
├── sprites/
│   └── ship.png                  ← player ship image
└── tiles/                        ← planet surface tiles (platformer, future)
```

---

## One Frame — What Happens at 60fps

```
GameLoop.handle(now):
  dt = nanoseconds elapsed / 1,000,000,000

  Input:
    W held       → ShipService.thrustForward(dt)
                    → ship.thrust(power, dt) → vx += cos(angle)×power×dt
                    → cap at levelService.getSpeedLimit()
    A / D held   → ship.rotateLeft/Right(dt)
    Space held   → ship.brake(dt) → gradual deceleration
    Mouse held   → combatService.onMouseHeld(ship, dt)
                    → bullet: fire if cooldown done
                    → laser: build charge toward 100%
    Mouse release → combatService.onMouseReleased(ship)
                    → laser: fire if charge >= 10%

  Planets:
    OrbitalMechanics.update(planet, dt) × 8 planets
      → angle += 2π / period × dt
      → x = semiMajor × cos(angle)
      → y = semiMinor × sin(angle)
    Moon.updateAroundEarth(dt)
      → x = earth.x + orbitRadius × cos(angle)
      → y = earth.y + orbitRadius × sin(angle)

  Bullets:
    CombatService.update(dt, ship)
      → move each bullet: x += vx×dt
      → laser attached to ship: stays at ship position
      → remove expired bullets

  Physics:
    GravitySystem.applyGravity(ship, bodies, dt)
      → locked planet  → strong raw pull toward center
      → active planet  → orbit assist: blend tangential, dampen radial
      → mastered       → skip entirely
      → Sun            → raw pull always
    ship.update(dt) → x += vx×dt, y += vy×dt

  Crash checks:
    Sun surface touched       → DESTROYED
    Locked planet touched     → DESTROYED (pulled in)
    Active planet slow touch  → LAND (visitPlanet, push out, stop)
    Active planet fast touch  → BOUNCE (reflect velocity × 0.4)

  Camera:
    GameCamera.follow(ship.x, ship.y, dt)
      → cameraX += (ship.x - cameraX) × 5 × dt   (smooth lerp)

  Render (back to front):
    1. StarfieldRenderer    → black background + 3 parallax star layers
    2. PlanetRenderer       → faint orbit ring ellipses
    3. GameRenderer         → gravity zone rings (color by status)
    4. ProceduralRenderer   → Sun + all 8 planets drawn with code
    5. BulletRenderer       → bullets and laser beams
    6. ShipRenderer         → ship.png at screen center
    7. MinimapRenderer      → log-scale minimap top-right
    8. HUDRenderer          → level panel + navigation arrow + speed bar
    9. SolarSystemMap       → full map overlay (when open)
```

---

## Game Flow

```
Launch
  └── Spring boots → creates all @Component / @Service beans
        └── JavaFXLauncher opens window
              └── GameState = RUNNING → GameLoop starts
                    │
                    ├── Open space → free flight, no gravity
                    │
                    ├── Enter active planet zone (blue ring)
                    │     └── Orbit assist → ship circles naturally
                    │           └── Thrust inward → approach surface
                    │                 ├── Speed ≤ safe → LAND
                    │                 └── Speed > safe → BOUNCE
                    │
                    ├── Enter locked planet zone (red ring)
                    │     └── Strong gravity → ship spirals in → CRASH
                    │
                    ├── Enter Sun zone (orange ring)
                    │     └── Raw gravity → thrust to escape
                    │           └── Touch surface → DESTROYED
                    │
                    └── Press L → levelUp()
                          └── New speed / thrust limits
                                New planet gravity zone activates
                                Previous planet zones go white (mastered)
```

---

## What Still Needs Building

| Feature | Package |
|---|---|
| Main menu — start screen, settings, quit | `controller/MainMenuController`, `fxml/menu.fxml` |
| Story — ship mission briefing, planet goal descriptions | `service/StoryService`, `render/StoryRenderer` |
| Planet surface mini-game (Mario-style) | `platformer/` |
| Landing cinematic animation | `animation/`, `landing/` |
| Resource collection system | `resource/` |
| Enemy ships on hostile planets | `service/EnemyService` |
| Scene transitions (space ↔ platformer) | `controller/` |
| Asteroid belt — between Mars and Jupiter, shoot or dodge | `entity/Asteroid`, `entity/AsteroidBelt`, `service/AsteroidService` |
| Kuiper belt — display only, Pluto visible but not interactive | `entity/KuiperBelt`, `render/KuiperRenderer` |
