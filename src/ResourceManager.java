import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private static final Map<String, Image> images = new HashMap<>();
    private static final Map<String, Clip> sounds = new HashMap<>();

    public static void preloadResources() {
        // Preload images
        loadImage("/background.png");
        loadImage("/game_icon.png");

        for (int i = 0; i < 3; i++) {
            loadImage("/ship_" + i + ".png");
            loadImage("/enemy_" + (i == 0 ? "basic" : i == 1 ? "fast" : "tank") + ".png");
        }

        for (int i = 0; i < 8; i++) {
            loadImage("/explosion_" + i + ".png");
        }

        // Preload sounds
        loadSound("/game_start.wav");
        loadSound("/shoot.wav");
        loadSound("/explosion.wav");
        loadSound("/hit.wav");
        loadSound("/player_hit.wav");
        loadSound("/game_over.wav");
        loadSound("/level_up.wav");
        loadSound("/button_hover.wav");
        loadSound("/button_click.wav");
    }

    public static Image getImage(String filename) {
        if (!images.containsKey(filename)) {
            loadImage(filename);
        }
        return images.getOrDefault(filename, createPlaceholderImage());
    }

    public static Clip getSound(String filename) {
        if (!sounds.containsKey(filename)) {
            loadSound(filename);
        }
        return sounds.get(filename);
    }

    private static void loadImage(String filename) {
        try {
            BufferedImage image = ImageIO.read(ResourceManager.class.getResourceAsStream(filename));
            images.put(filename, image);
        } catch (Exception e) {
            System.err.println("Could not load image: " + filename);
            images.put(filename, createPlaceholderImage());
        }
    }

    private static void loadSound(String filename) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(
                    ResourceManager.class.getResourceAsStream(filename));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            sounds.put(filename, clip);
        } catch (Exception e) {
            System.err.println("Could not load sound: " + filename);
        }
    }

    private static Image createPlaceholderImage() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect(0, 0, 50, 50);
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", 20, 30);
        g2d.dispose();
        return img;
    }
}