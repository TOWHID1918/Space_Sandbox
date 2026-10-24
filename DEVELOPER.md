# Space Sandbox

A JavaFX + Spring Boot space exploration game where players pilot a ship through the solar system, land on planets, collect resources, and upgrade their ship to reach the next destination.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 (DI only — no web server) |
| UI / Rendering | JavaFX 21 (Canvas + AnimationTimer) |
| Build | Gradle (Groovy) |

## Planet Accessibility Order

```
Earth → Moon → Mars → Venus → Mercury → Jupiter → Saturn → Uranus → Neptune
```

You can visit the other planet, without completing any level but your ship will crash if it's engine isn't strong enough

---

## Project Structure

```
src/main/java/project/aoop/space_sandbox/
├── SpaceSandboxApplication.java
├── JavaFXLauncher.java
├── config/
├── engine/
├── entity/
├── physics/
├── service/
├── render/
├── camera/
├── input/
├── controller/
├── landing/
├── animation/
├── platformer/
├── resource/
└── progression/

src/main/resources/
├── application.properties
├── fxml/
├── sprites/
└── tiles/
```

---

## Package Descriptions

### Root
| File | Purpose |
|---|---|
| `SpaceSandboxApplication.java` | Spring Boot entry point. Launches JavaFX via `Application.launch()` |
| `JavaFXLauncher.java` | JavaFX `Application` class. Starts Spring context in `init()`, sets up the primary stage in `start()` |

---

### `config/`
Spring `@Configuration` classes. Global constants and bean setup.

| File | Purpose |
|---|---|
| `GameConfig.java` | Canvas size (1280×720), target FPS (60), general game constants |
| `PhysicsConfig.java` | Gravity constant, distance scale (Moon=1 unit, Mars=15, Jupiter=40, Neptune=130), time compression (6×) |

---

### `engine/`
Core game loop and state management.

| File | Purpose |
|---|---|
| `GameLoop.java` | Extends `AnimationTimer`. Called every frame — processes input, calls service updates, triggers render |
| `GameState.java` | Tracks current state: `MENU`, `SPACE`, `LANDING_ANIMATION`, `PLATFORMER`, `PAUSED` |

---

### `entity/`
Plain Java game objects. No Spring annotations. These are the things that exist in the game world.

| File | Purpose |
|---|---|
| `CelestialBody.java` | Abstract base class for all space objects. Holds `name`, `x`, `y`, `mass`, `radius` |
| `Star.java` | The Sun. Extends `CelestialBody`. Fixed at origin (0, 0) |
| `Planet.java` | Extends `CelestialBody`. Adds `semiMajorAxis`, `semiMinorAxis`, `orbitalPeriod`, `color` for elliptical orbit |
| `Ship.java` | The player's spacecraft. Holds position, velocity, angle, engine tier, health. Has `thrust()` and `rotate()` methods |
| `Asteroid.java` | Individual asteroid. Has position, velocity, radius, and `destroyed` flag |
| `AsteroidBelt.java` | Generates and holds the list of asteroids between Mars (15 units) and Jupiter (40 units) |

---

### `physics/`
All math and physics calculations. No JavaFX, no Spring services — pure logic.

| File | Purpose |
|---|---|
| `ScaleConstants.java` | Converts between real km, game units, and screen pixels. Also handles time compression |
| `GravitySystem.java` | Applies gravitational pull from the Sun and planets onto the ship each frame |
| `OrbitalMechanics.java` | Updates each planet's (x, y) position along its elliptical orbit using elapsed game time |
| `CollisionSystem.java` | Checks ship-asteroid and bullet-asteroid collisions using radius overlap |

---

### `service/`
Spring `@Service` beans. Contains game logic. These are called every frame by `GameLoop`.

| File | Purpose |
|---|---|
| `SolarSystemService.java` | Owns the `Sun` and all `Planet` objects. Calls `OrbitalMechanics` to update planet positions each frame |
| `ShipService.java` | Handles ship movement — thrust, rotate, applies gravity via `GravitySystem`, updates position |
| `TravelService.java` | Calculates and formats estimated travel time to any destination based on distance and engine tier |
| `UpgradeService.java` | Manages engine upgrade tiers (1–5). Checks resource requirements and applies upgrades to `Ship` |
| `CombatService.java` | Manages shooting — spawns bullets, moves them, checks bullet-asteroid collisions |

---

### `render/`
All JavaFX drawing code. Each class takes a `GraphicsContext` and draws one layer of the game.

| File | Purpose |
|---|---|
| `GameRenderer.java` | Master renderer. Calls all sub-renderers in the correct order each frame |
| `StarfieldRenderer.java` | Draws 3-layer procedural parallax starfield using hash-based infinite generation (no texture needed) |
| `PlanetRenderer.java` | Draws the Sun and all planets as colored circles with name labels |
| `ShipRenderer.java` | Draws the player ship as a triangle pointing in the direction of travel |
| `MinimapRenderer.java` | Draws the minimap panel (top-right). Shows Sun, planets, and ship position at solar system scale |
| `HUDRenderer.java` | Draws HUD overlay — speed, position, engine tier, health, travel time estimate |
| `LandingRenderer.java` | Draws the landing animation — ship descending toward planet surface |
| `ResourceHUDRenderer.java` | Draws resource inventory panel when on a planet surface |
| `PlatformerRenderer.java` | Master renderer for the 2D platformer scene — calls TileRenderer and draws player/pickups |
| `TileRenderer.java` | Draws the tile grid of the planet surface |

---

### `camera/`
Viewport management for both the space view and the platformer.

| File | Purpose |
|---|---|
| `GameCamera.java` | Space view camera. Converts world coordinates to screen pixels. Supports pan, zoom, and smooth follow |

---

### `input/`
Keyboard input tracking.

| File | Purpose |
|---|---|
| `InputHandler.java` | Tracks which keys are currently held down. Provides named methods: `thrustForward()`, `rotateLeft()`, `shoot()`, `zoomIn()`, etc. Attach to the JavaFX `Scene` |

---

### `controller/`
JavaFX FXML controllers — manage scene transitions and UI events.

| File | Purpose |
|---|---|
| `MainMenuController.java` | Handles main menu buttons (Start, Quit) |
| `GameSceneController.java` | Wires up the main space game canvas to `GameRenderer` and `GameLoop` |
| `LandingSceneController.java` | Plays the `LandingAnimation` then transitions to the platformer |
| `PlatformerSceneController.java` | Manages the 2D platformer scene — starts `PlatformerEngine`, handles exit back to space |

---

### `landing/`
Detects and triggers the landing transition.

| File | Purpose |
|---|---|
| `LandingService.java` | Detects when the ship is close enough to a planet to land. Pauses `GameLoop` and signals `GameState` to switch to `LANDING_ANIMATION` |

---

### `animation/`
The cinematic landing sequence shown between space view and the platformer.

| File | Purpose |
|---|---|
| `LandingAnimation.java` | Controls the animation timeline — ship shrinks toward planet, planet grows to fill screen |
| `AnimationFrame.java` | A single frame of the landing animation (ship scale, planet scale, progress 0.0–1.0) |
| `LandingAnimationRenderer.java` | Draws each `AnimationFrame` onto the canvas |

---

### `platformer/`
The Mario-style 2D side-scrolling mini-game inside each planet.

| File | Purpose |
|---|---|
| `PlatformerEngine.java` | Has its own game loop (separate `AnimationTimer`). Calls `PlatformerPhysics`, handles pickups, checks win condition (all resources collected) |
| `PlatformerPlayer.java` | The astronaut character. Has position, velocity, jump state, and collected resources |
| `PlatformerPhysics.java` | 2D platformer physics — gravity, tile collision, wall/floor detection |
| `PlatformerCamera.java` | Side-scrolling camera that follows the player horizontally |
| `TileMap.java` | The planet surface as a 2D grid of `Tile` objects. Each planet has its own tile map |
| `Tile.java` | A single tile — has a `TileType` and screen position |
| `TileType.java` | Enum: `AIR`, `GROUND`, `PLATFORM`, `WALL`, `RESOURCE_BLOCK` |
| `ResourcePickup.java` | A collectible item on the planet surface. Has a `ResourceType` and collected flag |

---

### `resource/`
Everything about resources — what they are, where they are, and what the player has.

| File | Purpose |
|---|---|
| `ResourceType.java` | Enum of all resource types (e.g. `IRON`, `CRYSTAL`, `FUEL`, `RARE_MINERAL`) |
| `Resource.java` | A resource instance — has a `ResourceType` and quantity |
| `ResourceDeposit.java` | A deposit on a planet — which planet, which resource type, how much is available |
| `ResourceInventory.java` | The player's current held resources — add, remove, check quantities |
| `ResourceService.java` | Manages deposits across all planets, handles collection, and checks if a planet's resources are fully gathered |

---

### `progression/`
Controls which planet is next and what's needed to unlock it.

| File | Purpose |
|---|---|
| `PlanetOrder.java` | Defines the unlock sequence: `Earth → Moon → Mars → Venus → Mercury → Jupiter → Saturn → Uranus → Neptune` |
| `UnlockCondition.java` | Holds what's required to move to the next planet (all resources collected + engine tier) |
| `ProgressionService.java` | Checks if the player meets the `UnlockCondition` for the current planet, triggers `UpgradeService`, advances to next planet |

---

### `resources/` (assets folder)

| Folder | Purpose |
|---|---|
| `fxml/` | JavaFX scene layout files (`.fxml`) for each screen |
| `sprites/` | Images for ship, astronaut, planets, pickups |
| `tiles/` | Tile images for each planet's surface |
| `application.properties` | `spring.main.web-application-type=none` |

---

## Game Flow

```
Launch
  └── Main Menu
        └── Start
              └── Space View (GameLoop running)
                    └── Ship near planet → LandingService triggers
                          └── Landing Animation
                                └── Platformer (PlatformerEngine running)
                                      └── All resources collected
                                            └── ProgressionService checks UnlockCondition
                                                  └── UpgradeService upgrades ship
                                                        └── Next planet unlocked
                                                              └── Back to Space View
```

---

## Who Works On What

| Area | Package(s) |
|---|---|
| Space flight, gravity, orbits | `entity`, `physics`, `service`, `render`, `camera` |
| Planet surface mini-game | `platformer`, `render/PlatformerRenderer`, `render/TileRenderer` |
| Resources & inventory | `resource` |
| Planet unlock progression | `progression` |
| Landing animation | `animation`, `landing` |
| UI scenes & transitions | `controller`, `fxml/` |
