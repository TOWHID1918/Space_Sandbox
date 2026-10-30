package project.aoop.space_sandbox.camera;

import org.springframework.stereotype.Component;

@Component
public class GameCamera {
    private double x = 0, y = 0;
    private double zoom = 50.0;
    private double screenW = 1280;
    private double screenH = 720;

    public double toScreenX(double worldX) {
        return (worldX - x) * zoom + screenW / 2;
    }
    public double toScreenY(double worldY) {
        return (worldY - y) * zoom + screenH / 2;
    }

    public double toWorldX(double screenX) {
        return (screenX - screenW / 2) / zoom + x;
    }
    public double toWorldY(double screenY) {
        return (screenY - screenH / 2) / zoom + y;
    }

    public void follow(double targetX, double targetY, double deltaTime) {
        x = targetX;
        y = targetY;
    }

    public void adjustZoom(double factor) {
        zoom = Math.clamp(zoom * factor, 10, 200);
    }

    public void setScreenSize(double w, double h) { screenW = w; screenH = h; }

    public double getX()       { return x; }
    public double getY()       { return y; }
    public double getZoom()    { return zoom; }
    public double getScreenW() { return screenW; }
    public double getScreenH() { return screenH; }
}