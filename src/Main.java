import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // READING IMAGE
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("test_image.png"));
        } catch (IOException e) {
            System.out.println("Error reading file!");
        }

//      System.out.printf("%d ", img.getRGB(x,y)& 0xFF);

        Tomograph tomograph = new Tomograph(30, 30, 5, img.getWidth()/2);

        for (int step = 0; step < tomograph.getSteps(); step++) {
            int emitterPosX = tomograph.getEmitterPosX(step);
            int emitterPosY = tomograph.getEmitterPosY(step);
            for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {
                int sensorPosX = tomograph.getDetectorsSensorPosX(sensorIndex);
                int sensorPosY = tomograph.getDetectorsSensorPosY(sensorIndex);
            }

        }

    }
}
