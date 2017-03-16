import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import org.dcm4che2.data.*;
import org.dcm4che2.io.*;
import org.dcm4che2.media.*;
import org.dcm4che2.data.DicomObject;
//import org.dcm4che2.imageio.ImageReaderFactory;
public class Controller implements Initializable{
    private static final float ALPHA = 0.2f;
    private static final int BETA = 360;
    private static final int DETECTOR_COUNT = 900;

    @FXML private ImageView mainImage;
    @FXML private ImageView detectorsImage;
    @FXML private ImageView sinogramImage;
    @FXML private ImageView finalImage;
    @FXML private Canvas detectorsCanvas;
    @FXML private GraphicsContext mainGraphicContext;
    @FXML private Button startButton;
    @FXML private TextField alphaTextEdit;
    @FXML private TextField betaTextEdit;
    @FXML private TextField detectorsTextEdit;
    private float alfa = 0.2f;
    private int beta = 360;
    private int detectorCount = 900;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        DicomObject obj;

        Image image = new Image("file:res/test_image.png");
        mainGraphicContext = detectorsCanvas.getGraphicsContext2D();
        mainImage.setImage(image);
        detectorsImage.setImage(image);
        // initialize your logic here: all @FXML variables will have been injected
        mainGraphicContext.strokeOval(0,  0, 255, 255);
        startButton.setOnAction(actionEvent -> {
            mainGraphicContext.setStroke(Color.GRAY);
            mainGraphicContext.clearRect(0, 0, mainGraphicContext.getCanvas().getWidth(), mainGraphicContext.getCanvas().getHeight());
            mainGraphicContext.strokeOval(0,  0, 255, 255);
            System.out.println("alpha " + alfa + " beta " + beta + " detectors " + detectorCount);
            startButton.setDisable(true);
            // Run the Sinogram computations
            startSinogramTask();
        });
        alphaTextEdit.textProperty().addListener((observable, newValue, oldValue) -> {
            alphaTextEdit.setText(validate(alphaTextEdit.getText()));
            if(alphaTextEdit.getLength() > 0)
                alfa = Float.parseFloat(alphaTextEdit.getText());
            else
                alfa = ALPHA;
            System.out.println(alfa);
        });
        betaTextEdit.textProperty().addListener((observable, newValue, oldValue) -> {
            if(betaTextEdit.getLength() > 0)
                beta = Integer.parseInt(betaTextEdit.getText()) * 2;
            else
                beta = BETA;
            betaTextEdit.setText(validate(betaTextEdit.getText()));
        });
        detectorsTextEdit.textProperty().addListener((observable, newValue, oldValue) -> {
            if (detectorsTextEdit.getLength() > 0)
                detectorCount = Integer.parseInt(detectorsTextEdit.getText());
            else
                detectorCount = DETECTOR_COUNT;
            detectorsTextEdit.setText(validate(detectorsTextEdit.getText()));
        });
    }

    private void startSinogramTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    runSinogram();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread backgroundComputationsThread = new Thread(task);
        backgroundComputationsThread.setDaemon(true);
        backgroundComputationsThread.start();
    }

    private void runSinogram() throws IOException {

        mainGraphicContext.setFill(Color.RED);
        mainGraphicContext.setStroke(Color.GRAY);
        Sinogram sinogram = new Sinogram(this);
        Tomograph tomograph = new Tomograph(alfa, beta, detectorCount, sinogram.getInputImageSize()/2 - 1);
        sinogram.initializeSinogramMatrix(tomograph.getSteps(), tomograph.getDetectorsSensorsCount());

        for (int step = 0; step < tomograph.getSteps(); step++) {
            //mainGraphicContext.clearRect(0, 0, mainGraphicContext.getCanvas().getWidth(), mainGraphicContext.getCanvas().getHeight());
            float row[] = new float[tomograph.getDetectorsSensorsCount()];
            int emitterPosX = tomograph.getEmitterPosX(step);
            int emitterPosY = tomograph.getEmitterPosY(step);
            for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {
                row[sensorIndex] = sinogram.BresenhamAlgorithm(step, sensorIndex, tomograph, true);

                int sensorPosX = tomograph.getDetectorsSensorPosX(step, sensorIndex);
                int sensorPosY = tomograph.getDetectorsSensorPosY(step, sensorIndex);

                if (sensorIndex == 0 || sensorIndex == tomograph.getDetectorsSensorsCount()-1) {
                    mainGraphicContext.strokeLine(((sensorPosX + 255) / 2.0), (255 - (sensorPosY + 255) / 2.0), (((emitterPosX + 255) / 2.0) - 2), (255 - (emitterPosY + 255) / 2.0) - 2);
                }
                if (sensorIndex == tomograph.getDetectorsSensorsCount()-1){
                    mainGraphicContext.clearRect(0, 0, mainGraphicContext.getCanvas().getWidth(), mainGraphicContext.getCanvas().getHeight());
                    mainGraphicContext.strokeLine(((sensorPosX + 255) / 2.0), (255 - (sensorPosY + 255) / 2.0), (((emitterPosX + 255) / 2.0) - 2), (255 - (emitterPosY + 255) / 2.0) - 2);
                    mainGraphicContext.strokeOval(0,  0, 255, 255);

                }
            }
            mainGraphicContext.fillOval(((emitterPosX + 255)/2.0)-5,  (255-(emitterPosY+255)/2.0)-5, 4, 4);

            sinogram.insertRowToMatrix(row, step);
        }

        // save and filter singoram
        sinogram.SinogramToImage();
        System.out.println("main.Sinogram saved as image");

        for (int emitter = 0; emitter < sinogram.sinogramMatrix.length; emitter++) {
            for (int detector = 0; detector < sinogram.sinogramMatrix[0].length; detector++) {
                sinogram.BresenhamAlgorithm(emitter, detector, tomograph, false);
            }
        }
        // save result
        sinogram.saveOutputImage("output.jpg");
        System.out.println("Finished saving the result");
        startButton.setDisable(false);
    }
    private String validate(String text)
    {
        if (text.matches("[0-9]*")) {
            return text;
        } else if (text.length() > 1){
            System.out.println('x');
            return text.substring(0, text.length()-1);
        } else return "";
    }

    public ImageView getSinogramImage() {
        return sinogramImage;
    }

    public ImageView getFinalImage() {
        return finalImage;
    }
}
