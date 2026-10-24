# Space Sandbox

Space Sandbox is a JavaFX space-exploration game built with Java and Spring Boot. Pilot a ship through a moving solar system, use gravity to enter planetary orbits, land safely, fight asteroids, and explore planets to progress through the game.

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![Build](https://img.shields.io/badge/build-Gradle-green)

## What is in the project?

- A real-time space flight view rendered on a JavaFX canvas
- A controllable ship with thrust, rotation, braking, health, and speed limits
- The Sun, planets, and a Moon with orbital movement
- Gravity zones that behave differently for active, locked, and mastered planets
- A minimap and full-screen solar-system map with target tracking
- Asteroids, bullets, and a chargeable laser
- Planet landing and a platformer-style planet surface
- Resource collection and level-based planet access

The current application opens directly into the space view. The game window is fixed at `1280 x 720`.

## Requirements

Install the following before running the project:

1. **Java Development Kit 21**
2. **Git**
3. A desktop environment capable of running JavaFX

Gradle is included through the Gradle Wrapper, so a separate Gradle installation is not required. The JavaFX dependencies are downloaded by Gradle from Maven Central.

Check Java:

```text
java -version
```

The output should show Java 21.

## Download and run

Clone the repository and enter the project directory:

```bash
git clone https://github.com/nafeenusair/Space_Sandbox.git
cd Space_Sandbox
```

### Windows

```powershell
.\gradlew.bat bootRun
```

### macOS or Linux

```bash
./gradlew bootRun
```

The first run may take longer because Gradle downloads the project dependencies. A JavaFX window titled **Space Sandbox** should open after startup.

## Controls

| Input | Action |
|---|---|
| `W` or `Up Arrow` | Thrust forward |
| `A` or `Left Arrow` | Rotate left |
| `D` or `Right Arrow` | Rotate right |
| `Space` | Brake |
| Primary mouse button | Fire bullets; in laser mode, hold to charge and release to fire |
| Secondary mouse button | Switch between bullet and laser mode after the laser is unlocked |
| Mouse wheel | Zoom in or out |
| Click the minimap | Open the full solar-system map |
| Click a planet on the full map | Track that planet |
| `Escape` | Close the full map |
| `L` / `P` | Increase/decrease the development level |

The `L` and `P` keys are development/testing shortcuts for changing the current level.

## How to play

1. Start the application and locate the ship in the space view.
2. Use `W`/`Up` to accelerate and `A`/`D` to turn.
3. Use `Space` to reduce speed before approaching a planet.
4. Enter the blue gravity zone around the current planet and slow down.
5. Touch the planet at a safe speed to land.
6. Explore the planet surface and collect resources where the surface game is available.
7. Progress through the accessible planets and improve the ship as new levels become available.

Planet gravity zones are shown visually:

- **Blue**: current active planet and orbit assistance
- **Red**: locked planet; entering the zone can pull the ship into a crash
- **Faint/white**: mastered planet
- **Orange**: Sun gravity zone

Avoid colliding with the Sun or entering a locked planet at an unsafe trajectory. The ship can be destroyed and the game will pause.

## Game progression

The intended progression is:

```text
Earth → Moon → Venus → Mercury → Mars → Jupiter → Saturn → Uranus → Neptune
```

The current level controls planet access, ship speed/thrust limits, gravity behavior, and weapon availability. The laser becomes available at level 5.

## Project layout

```text
src/main/java/project/aoop/space_sandbox/
├── config/       # Game and physics constants, Spring bean setup
├── engine/       # Per-frame game loop and game state
├── entity/       # Ship, planets, star, asteroids, bullets
├── physics/      # Gravity, orbit, and collision calculations
├── service/      # Ship, combat, navigation, level, and solar-system logic
├── render/       # JavaFX canvas rendering
├── camera/       # Space camera and coordinate conversion
├── input/        # Keyboard and mouse state
├── landing/      # Landing detection and transitions
├── animation/    # Landing animation
├── platformer/   # Planet surface mini-game
└── resource/     # Resources, deposits, and inventory

src/main/resources/
├── sprites/      # Game images
├── tiles/        # Planet surface tiles
└── application.properties
```

## Runtime flow

```text
JavaFXLauncher
  ├── starts the Spring context
  ├── creates the canvas and scene
  ├── attaches keyboard/mouse input
  └── starts GameLoop

GameLoop
  ├── reads input
  ├── updates ship, planets, bullets, and gravity
  ├── checks landing and crashes
  └── asks GameRenderer to draw the frame
```

For the class-by-class connections behind the ship, planets, stars, gravity, controls, rendering, and game loop, see [`DEVELOPER.md`](DEVELOPER.md).

## Build and test

Build the project:

```bash
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

Run the automated tests:

```bash
./gradlew test
```

## For contributors and judges

The main implementation is under `src/main/java`. Gameplay rules are mainly separated into entities, physics, services, and renderers. The project uses Spring for dependency injection and JavaFX for the desktop game window; it is not a web application and does not require a database or server.

The detailed architecture and extension guide is kept separately so this README remains useful to visitors and evaluators:

- [`DEVELOPER.md`](DEVELOPER.md) — class responsibilities, connections, controls, gravity, frame order, and where to make changes
- [`build.gradle`](build.gradle) — Java, Spring Boot, JavaFX, and test configuration
- [`src/test`](src/test) — automated tests

## License

No license has been specified yet.
