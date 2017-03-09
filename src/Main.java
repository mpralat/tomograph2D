import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        final float alfa = 0.2f;
        final int beta = 360;
        final int detectorCount = 900;

        Sinogram sinogram = new Sinogram();
        Tomograph tomograph = new Tomograph(alfa, beta, detectorCount, sinogram.getInputImageSize()/2 - 1);
        sinogram.initializeSinogramMatrix(tomograph.getSteps(), tomograph.getDetectorsSensorsCount());

        // create sinogram of object
        for (int step = 0; step < tomograph.getSteps(); step++) {                                           // emitters count
            int emitterPosX = tomograph.getEmitterPosX(step);
            int emitterPosY = tomograph.getEmitterPosY(step);
            //System.out.println("---------------- " + step + " ------------------------");
            //System.out.println("emitterPosX = " + emitterPosX + "emitterPosY = " + emitterPosY);
            float row[] = new float[tomograph.getDetectorsSensorsCount()];
            for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {  // detectors count
                int sensorPosX = tomograph.getDetectorsSensorPosX(step, sensorIndex);
                int sensorPosY = tomograph.getDetectorsSensorPosY(step, sensorIndex);
                //System.out.println("sensorPosX = " + sensorPosX + "sensorPosY = " + sensorPosY);
                row[sensorIndex] = sinogram.BresenhamAlgorithm(emitterPosX, emitterPosY, sensorPosX, sensorPosY);
            }
            sinogram.insertRowToMatrix(row, step);
        }

        sinogram.SinogramToImage();

        System.out.println("Sinogram saved as image");

        for (int emitter = 0; emitter < sinogram.sinogramMatrix.length; emitter++) {
            for (int detector = 0; detector < sinogram.sinogramMatrix[0].length; detector++) {
                sinogram.BresenhamAlgorithm2(tomograph.getEmitterPosX(emitter), tomograph.getEmitterPosY(emitter),
                        tomograph.getDetectorsSensorPosX(emitter, detector), tomograph.getDetectorsSensorPosY(emitter, detector),
                        emitter, detector);
            }
        }
        System.out.println("output ready to be saved");
        sinogram.saveOutputImage("output.jpg");
    }
}
