import java.io.IOException;
import ij.plugin.DICOM;
// DICOM:
// http://www.apteryx.fr/
// https://imagej.nih.gov/ij/developer/api/ij/plugin/DICOM.html
// https://imagej.nih.gov/ij/developer/source/ij/plugin/DICOM.java.html
// http://www.java2s.com/Code/Jar/i/Downloadijjar.htm
// tutorial:
// http://www.saravanansubramanian.com/Saravanan/Articles_On_Software/Entries/2014/9/29_DICOM_Basics_-_Creating_a_DICOM_File.html

public class Main {
    public static void main(String[] args) throws IOException {
        final float alfa = 0.2f;
        final int beta = 360;
        final int detectorCount = 900;

        Sinogram sinogram = new Sinogram();
        Tomograph tomograph = new Tomograph(alfa, beta, detectorCount, sinogram.getInputImageSize()/2 - 1);
        sinogram.initializeSinogramMatrix(tomograph.getSteps(), tomograph.getDetectorsSensorsCount());

        for (int step = 0; step < tomograph.getSteps(); step++) {
            float row[] = new float[tomograph.getDetectorsSensorsCount()];
            for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {

                row[sensorIndex] = sinogram.BresenhamAlgorithm(step, sensorIndex, tomograph, true);
            }
            sinogram.insertRowToMatrix(row, step);
        }

        // save and filter singoram
        sinogram.SinogramToImage();
        System.out.println("Sinogram saved as image");

        for (int emitter = 0; emitter < sinogram.sinogramMatrix.length; emitter++) {
            for (int detector = 0; detector < sinogram.sinogramMatrix[0].length; detector++) {
                sinogram.BresenhamAlgorithm(emitter, detector, tomograph, false);
            }
        }

        // save and filter result
        sinogram.ResultAsImage();
    }
}
