import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        final float alfa = 0.2f;
        final int beta = 360;
        final int detectorCount = 900;

        Sinogram sinogram = new Sinogram();
        Tomograph tomograph = new Tomograph(alfa, beta, detectorCount, sinogram.getInputImageSize()/2 - 1);

        // create sinogram of object
        for (int step = 0; step < tomograph.getSteps(); step++) {                                           // emitters count
            int emitterPosX = tomograph.getEmitterPosX(step);
            int emitterPosY = tomograph.getEmitterPosY(step);
            //System.out.println("---------------- " + step + " ------------------------");
            //System.out.println("emitterPosX = " + emitterPosX + "emitterPosY = " + emitterPosY);
            ArrayList<Float> row = new ArrayList<>();
            for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {  // detectors count
                int sensorPosX = tomograph.getDetectorsSensorPosX(step, sensorIndex);
                int sensorPosY = tomograph.getDetectorsSensorPosY(step, sensorIndex);
                //System.out.println("sensorPosX = " + sensorPosX + "sensorPosY = " + sensorPosY);
                row.add(sinogram.BresenhamAlgorithm(emitterPosX, emitterPosY, sensorPosX, sensorPosY));
            }
            sinogram.insertRowToMatrix(row);
        }
        sinogram.saveSinogramAsImage();
        System.out.println("Sinogram saved as image");

        // create object from sinogram
        //sinogram.createObjectFromMatrix();
        for (int emitter = 0; emitter < sinogram.matrix.size(); emitter++) {
            for (int detector = 0; detector < sinogram.matrix.get(0).size(); detector++) {
                sinogram.BresenhamAlgorithm2(tomograph.getEmitterPosX(emitter), tomograph.getEmitterPosY(emitter),
                        tomograph.getDetectorsSensorPosX(emitter, detector), tomograph.getDetectorsSensorPosY(emitter, detector),
                        emitter, detector);
            }
        }
        System.out.println("output ready to be saved");
        sinogram.saveOutputImage();
    }
}
