import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Created by marta on 18.03.17.
 * Class that runs the sinogram computations.
 */

public class ComputationManager {
    private final GraphicsContext mainGraphicContext;
    private final Controller controller;
    private final Sinogram sinogram;
    private final Tomograph tomograph;
    private volatile boolean shutdownTask = false;

    public ComputationManager(Controller controller) {
        this.controller = controller;
        this.mainGraphicContext = controller.getMainGraphicContext();
        this.sinogram = new Sinogram(controller);
        this.tomograph = new Tomograph(controller.getAlfa(), controller.getBeta(), controller.getDetectorCount(), sinogram.getInputImageSize()/2 - 1);
        sinogram.initializeSinogramMatrix(tomograph.getSteps(), tomograph.getDetectorsSensorsCount());
        mainGraphicContext.setFill(Color.RED);
        mainGraphicContext.setStroke(Color.GRAY);
    }


    public void startSinogramTask(int stepCount) {
        // Starts a new thread for the sinogram task. Only in automatic mode.
        Runnable task = () -> {
            try {
                    runSinogram(stepCount);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread backgroundComputationsThread = new Thread(task);
        backgroundComputationsThread.setDaemon(true);
        backgroundComputationsThread.start();
    }

    private void runSinogram(int stepCount) throws IOException {
            for (int step = stepCount; step <= tomograph.getSteps(); step++) {
                if(!shutdownTask) {
                    oneSinogramIteration(step);
                }
                else {
                    controller.getSinogramImage().setImage(null);
                    controller.getMainGraphicContext().clearRect(0, 0, 255, 255);

                    break;
                }
            }
    }

    private void saveSinogram() throws IOException {
        // save and filter singogram
        sinogram.SinogramToImage();
        System.out.println("main.Sinogram saved as image");

        for (int emitter = 0; emitter < sinogram.sinogramMatrix.length; emitter++) {
            for (int detector = 0; detector < sinogram.sinogramMatrix[0].length; detector++) {
                sinogram.BresenhamAlgorithm(emitter, detector, tomograph, false);
            }
        }
        // save result
        sinogram.saveOutputImage("output/output.jpg");
        System.out.println("Finished saving the result");
        controller.getStartButton().setDisable(false);
        controller.getStartManuallyButton().setDisable(false);
        controller.clear();
    }
    public boolean oneSinogramIteration(int step) throws IOException {
        if(step == tomograph.getSteps()) {
            System.out.println("Finished iterating. Saving the sinogram image now.");
            saveSinogram();
            return false;
        }

        float row[] = new float[tomograph.getDetectorsSensorsCount()];
        int dotPosX = (int) (Math.ceil(Math.cos((controller.getAlfa() * Math.PI)/180 * step) * 255));
        int dotPosY = (int) (Math.ceil(Math.sin((controller.getAlfa() * Math.PI)/180 * step) * 255));
        for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {

            row[sensorIndex] = sinogram.BresenhamAlgorithm(step, sensorIndex, tomograph, true);

            int sensorPosX = tomograph.getDetectorsSensorPosX(step, sensorIndex);
            int sensorPosY = tomograph.getDetectorsSensorPosY(step, sensorIndex);
            if (sensorIndex == 0 || sensorIndex == tomograph.getDetectorsSensorsCount()-1) {
                mainGraphicContext.strokeLine(((sensorPosX + 255) / 2.0), (255 - (sensorPosY + 255) / 2.0), (((dotPosX + 255) / 2.0) - 2), (255 - (dotPosY + 255) / 2.0) - 2);
            }
            if (sensorIndex == tomograph.getDetectorsSensorsCount()-1){
                mainGraphicContext.clearRect(0, 0, 255, 255);
                mainGraphicContext.strokeLine(((sensorPosX + 255) / 2.0), (255 - (sensorPosY + 255) / 2.0), (((dotPosX + 255) / 2.0) - 2), (255 - (dotPosY + 255) / 2.0) - 2);
                mainGraphicContext.strokeOval(0,  0, 255, 255);
            }
        }
        mainGraphicContext.fillOval(((dotPosX + 255)/2.0)-5,  (255-(dotPosY+255)/2.0)-5, 4, 4);
//TODO Maybe function or sth
//            Drawing the sinogram step by step
        sinogram.insertRowToMatrix(row, step);

        if(step%5==0){
            float max = 0.0f;
            float min = Float.POSITIVE_INFINITY;
            for(int i = 0; i< sinogram.sinogramMatrix.length; i++) {
                for(int j = 0; j< sinogram.sinogramMatrix[i].length; j++) {
                    max = Math.max(max, sinogram.sinogramMatrix[i][j]);
                    min = Math.min(min, sinogram.sinogramMatrix[i][j]);
                }
            }
            //System.out.println("min = " + min + " max = " + max);
            BufferedImage image = new BufferedImage(sinogram.sinogramMatrix.length, sinogram.sinogramMatrix[0].length, BufferedImage.TYPE_BYTE_GRAY );
            for(int i = 0; i< sinogram.sinogramMatrix.length; i++) {
                for(int j = 0; j< sinogram.sinogramMatrix[i].length; j++) {
                    int a = (int)(Sinogram.normalize(sinogram.sinogramMatrix[i][j], max, min) * 255);
                    java.awt.Color newColor = new java.awt.Color(a,a,a);
                    image.setRGB(i,j,newColor.getRGB());
                }
            }
            WritableImage sinImage = SwingFXUtils.toFXImage(image, null);
            controller.getSinogramImage().setImage(sinImage);
        }
        return true;
    }


    public void setShutdownTask(boolean shutdownTask) {
        this.shutdownTask = shutdownTask;
    }
}
