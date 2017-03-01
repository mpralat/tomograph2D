import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

//      System.out.printf("%d ", img.getRGB(x,y)& 0xFF);
        Sinogram sinogram = new Sinogram();
        Tomograph tomograph = new Tomograph(30, 30, 5, sinogram.getImageSize()/2);

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
