import java.awt.*;

public class NetworkProjectile {
    private int x, y;
    private String owner;
    private boolean active = true;

    public int getY() {
        return y;
    }

    public NetworkProjectile(String owner, int x, int y) {
        this.owner = owner;
        this.x = x;
        this.y = y;
    }

    public void update() {
        y -= 10;
    }

    public void draw(Graphics g) {
        if (active) {
            g.setColor(owner.equals(GamePanel.networkManager.getPlayerName()) ? Color.YELLOW : Color.RED);
            g.fillRect(x, y, 5, 15);
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, 5, 15);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getOwner() { return owner; }
}