import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("test_image.png"));
        } catch (IOException e) {
            System.out.println("Error reading file!");
        }
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                System.out.printf("%d ", img.getRGB(x,y)& 0xFF);
            }
            System.out.println();
        }
    }
}
