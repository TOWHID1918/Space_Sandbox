package project.aoop.space_sandbox.engine;

import org.springframework.stereotype.Component;

@Component
public class GameState {
    public enum State { MENU, RUNNING, PAUSED }

    private State current = State.MENU;

    public boolean isRunning() { return current == State.RUNNING; }
    public void setState(State s) { this.current = s; }
    public State getState() { return current; }
}
