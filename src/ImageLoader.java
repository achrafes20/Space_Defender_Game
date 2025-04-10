// ImageLoader.java
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ImageLoader {
    private static final HashMap<String, Image> images = new HashMap<>();

    public static Image getImage(String filename) {
        try {
            return ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + filename);
            e.printStackTrace();
            return null;
        }
    }
}
