import java.awt.*;

public class Explosion {
    private int x, y;
    private int currentFrame = 0;
    private final int totalFrames = 8;
    private boolean active = true;
    private final Image[] frames;
    private final int width, height;
    private long lastUpdateTime;
    private final long frameDelay = 50; // ms entre chaque frame

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
        this.frames = new Image[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            frames[i] = ResourceManager.getImage("/explosion_" + i + ".png");
        }
        this.width = frames[0].getWidth(null);
        this.height = frames[0].getHeight(null);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > frameDelay) {
            currentFrame++;
            if (currentFrame >= totalFrames) {
                active = false;
            }
            lastUpdateTime = currentTime;
        }
    }

    public void draw(Graphics g) {
        if (active && currentFrame < totalFrames) {
            g.drawImage(frames[currentFrame],
                    x - width/2,
                    y - height/2,
                    width, height, null);
        }
    }

    public boolean isActive() {
        return active;
    }
}