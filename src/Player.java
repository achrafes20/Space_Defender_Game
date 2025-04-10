import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class Player {
    private int x, y;
    private int speed;
    private final Image[] sprites = new Image[3];
    private int currentSprite = 0;
    private final Set<Integer> activeKeys = new HashSet<>();
    private long lastShotTime = 0;
    private final long shotCooldown;
    private final int shipType;
    private int health;
    private boolean invincible = false;
    private long invincibleEndTime = 0;

    public int getX() {
        return x;
    }

    public Player(int startX, int startY, int shipType) {
        this.x = startX;
        this.y = startY;
        this.shipType = shipType;
        this.health = 3;

        switch(shipType) {
            case 0: // Standard
                sprites[0] = ResourceManager.getImage("/ship_0.png");
                sprites[1] = ResourceManager.getImage("/ship_0.png");
                sprites[2] = ResourceManager.getImage("/ship_0.png");
                speed = 5;
                shotCooldown = 300;
                break;
            case 1: // Fast
                sprites[0] = ResourceManager.getImage("/ship_1.png");
                sprites[1] = ResourceManager.getImage("/ship_1.png");
                sprites[2] = ResourceManager.getImage("/ship_1.png");
                speed = 7;
                shotCooldown = 500;
                break;
            case 2: // Heavy
                sprites[0] = ResourceManager.getImage("/ship_2.png");
                sprites[1] = ResourceManager.getImage("/ship_2.png");
                sprites[2] = ResourceManager.getImage("/ship_2.png");
                speed = 3;
                shotCooldown = 150;
                break;
            default:
                sprites[0] = ResourceManager.getImage("/ship_0.png");
                sprites[1] = ResourceManager.getImage("/ship_0.png");
                sprites[2] = ResourceManager.getImage("/ship_0.png");
                speed = 5;
                shotCooldown = 300;
        }
    }

    public void handleKeyPress(int keyCode) {
        activeKeys.add(keyCode);
        updateSprite();
    }

    public void handleKeyRelease(int keyCode) {
        activeKeys.remove(keyCode);
        updateSprite();
    }

    private void updateSprite() {
        if (activeKeys.contains(KeyEvent.VK_LEFT)) {
            currentSprite = 2;
        } else if (activeKeys.contains(KeyEvent.VK_RIGHT)) {
            currentSprite = 1;
        } else {
            currentSprite = 0;
        }
    }

    public void update() {
        if (invincible && System.currentTimeMillis() > invincibleEndTime) {
            invincible = false;
        }

        activeKeys.forEach(key -> {
            switch (key) {
                case KeyEvent.VK_LEFT:
                    x = Math.max(0, x - speed);
                    break;
                case KeyEvent.VK_RIGHT:
                    x = Math.min(750, x + speed);
                    break;
                case KeyEvent.VK_UP:
                    y = Math.max(0, y - speed);
                    break;
                case KeyEvent.VK_DOWN:
                    y = Math.min(550, y + speed);
                    break;
            }
        });
    }

    public boolean canShoot() {
        return System.currentTimeMillis() - lastShotTime > shotCooldown;
    }

    public void shoot() {
        lastShotTime = System.currentTimeMillis();
    }

    public void takeDamage() {
        if (!invincible) {
            health--;
            activateInvincibility(2000);
        }
    }

    public void activateInvincibility(long duration) {
        invincible = true;
        invincibleEndTime = System.currentTimeMillis() + duration;
    }

    public void draw(Graphics g) {
        if (!invincible || (System.currentTimeMillis() / 100) % 2 == 0) {
            g.drawImage(sprites[currentSprite], x, y, 50, 60, null);
        }
        drawHealthBar(g);
    }

    private void drawHealthBar(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y - 15, 50, 5);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 15, (int)(50 * ((double)health / 3)), 5);
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, 50, 60);
    }

    public int getCenterX() {
        return x + 25;
    }

    public int getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }

    public int getShipType() {
        return shipType;
    }

    public boolean isInvincible() {
        return invincible;
    }
}