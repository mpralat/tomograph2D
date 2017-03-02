import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        final float alfa = 0.2f;
        final int beta = 179;
        final int detectorCount = 900;

        Sinogram sinogram = new Sinogram();
        Tomograph tomograph = new Tomograph(alfa, beta, detectorCount, sinogram.getImageSize()/2 - 1);

        for (int step = 0; step < tomograph.getSteps(); step++) {
            int emitterPosX = tomograph.getEmitterPosX(step);
            int emitterPosY = tomograph.getEmitterPosY(step);
            System.out.println("---------------- " + step + " ------------------------");
            System.out.println("emitterPosX = " + emitterPosX + "emittorPosY = " + emitterPosY);
            ArrayList<Float> row = new ArrayList<>();
            for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {
                int sensorPosX = tomograph.getDetectorsSensorPosX(step, sensorIndex);
                int sensorPosY = tomograph.getDetectorsSensorPosY(step, sensorIndex);
                System.out.println("sensorPosX = " + sensorPosX + "sensorPosY = " + sensorPosY);
                row.add(sinogram.BresenhamAlgorithm(emitterPosX, emitterPosY, sensorPosX, sensorPosY));
            }
            sinogram.insertRowToMatrix(row);
        }

        sinogram.saveSinogramAsImage();

    }
}
