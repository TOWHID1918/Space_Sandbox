package project.aoop.space_sandbox.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class InputHandler {

    private final Set<KeyCode> held        = new HashSet<>();
    private boolean            mouseHeld   = false;
    private boolean            mouseJustPressed  = false;
    private boolean            mouseJustReleased = false;
    private boolean            secondaryMouseJustPressed = false;
    private double             mouseX = 0, mouseY = 0;

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e  -> held.add(e.getCode()));
        scene.setOnKeyReleased(e -> held.remove(e.getCode()));
    }

    public void attachMouse(Scene scene) {
        scene.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        scene.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mouseHeld        = true;
                mouseJustPressed = true;
            } else if (e.getButton() == MouseButton.SECONDARY) {
                secondaryMouseJustPressed = true;
            }
        });
        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mouseHeld         = false;
                mouseJustReleased = true;
            }
        });
    }

    // Call once per frame AFTER checking justPressed/justReleased
    public void clearFrameState() {
        mouseJustPressed  = false;
        mouseJustReleased = false;
        secondaryMouseJustPressed = false;
    }

    private boolean is(KeyCode k) { return held.contains(k); }

    public boolean thrustForward()     { return is(KeyCode.W) || is(KeyCode.UP);   }
    public boolean rotateLeft()        { return is(KeyCode.A) || is(KeyCode.LEFT); }
    public boolean rotateRight()       { return is(KeyCode.D) || is(KeyCode.RIGHT);}
    public boolean brake()             { return is(KeyCode.SPACE);                 }
    public boolean levelUp()           { return is(KeyCode.L);                     }
    public boolean levelDown()         { return is(KeyCode.P);                     }
    public boolean zoomIn()            { return is(KeyCode.EQUALS);                }
    public boolean zoomOut()           { return is(KeyCode.MINUS);                 }
    public boolean closeMap()          { return is(KeyCode.ESCAPE);                }

    public boolean isMouseHeld()        { return mouseHeld;         }
    public boolean isMouseJustPressed() { return mouseJustPressed;  }
    public boolean isMouseJustReleased(){ return mouseJustReleased; }
    public boolean isSecondaryMouseJustPressed() { return secondaryMouseJustPressed; }
    public double  getMouseX()          { return mouseX;            }
    public double  getMouseY()          { return mouseY;            }
}