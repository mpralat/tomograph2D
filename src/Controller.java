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
import java.util.ArrayList;
import java.util.ResourceBundle;


public class Controller implements Initializable{
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
    private int beta = 180;
    private int detectorCount = 500;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        Image image = new Image("file:res/image.PNG");
        mainGraphicContext = detectorsCanvas.getGraphicsContext2D();
        mainGraphicContext.setFill(Color.WHITE);
        mainImage.setImage(image);
        detectorsImage.setImage(image);
        // initialize your logic here: all @FXML variables will have been injected
        mainGraphicContext.strokeOval(0,  0, 255, 255);
        startButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent actionEvent) {
                // Run the Sinogram computations
                startSinogramTask();
            }
        });
        alphaTextEdit.textProperty().addListener((observable, newValue, oldValue) -> {
            alphaTextEdit.setText(validate(alphaTextEdit.getText()));
            alfa = Float.parseFloat(alphaTextEdit.getText());
            System.out.println(alfa);
        });
        betaTextEdit.textProperty().addListener(observable -> {
            betaTextEdit.setText(validate(betaTextEdit.getText()));
            beta = Integer.parseInt(betaTextEdit.getText());
        });
        detectorsTextEdit.textProperty().addListener(observable -> {
            detectorsTextEdit.setText(validate(detectorsTextEdit.getText()));
            detectorCount = Integer.parseInt(detectorsTextEdit.getText());
        });

        System.out.println("sdh");

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


        Sinogram sinogram = new Sinogram(this);
        Tomograph tomograph = new Tomograph(alfa, beta, detectorCount, sinogram.getInputImageSize()/2 - 1);
        sinogram.initializeSinogramMatrix(tomograph.getSteps(), tomograph.getDetectorsSensorsCount());

        for (int step = 0; step < tomograph.getSteps(); step++) {
            float row[] = new float[tomograph.getDetectorsSensorsCount()];
            int emitterPosX = tomograph.getEmitterPosX(step);
            int emitterPosY = tomograph.getEmitterPosY(step);
            for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {
                row[sensorIndex] = sinogram.BresenhamAlgorithm(step, sensorIndex, tomograph, true);

                int sensorPosX = tomograph.getDetectorsSensorPosX(step, sensorIndex);
                int sensorPosY = tomograph.getDetectorsSensorPosY(step, sensorIndex);
                mainGraphicContext.strokeOval(((sensorPosX + 255)/2.0)-3,  (255-(sensorPosY+255)/2.0)-3, 6, 6);
            }
            mainGraphicContext.fillOval(((emitterPosX + 255)/2.0)-5,  (255-(emitterPosY+255)/2.0)-5, 10, 10);
            System.out.println(emitterPosX + " " + emitterPosY);
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

    }

    private String validate(String text)
    {
        if (text.matches("[0-9]*")) {
            return text;
        } else return text.substring(0, text.length()-1);
    }

    public ImageView getMainImage() {
        return mainImage;
    }

    public ImageView getDetectorsImage() {
        return detectorsImage;
    }

    public Canvas getDetectorsCanvas() {
        return detectorsCanvas;
    }

    public ImageView getSinogramImage() {
        return sinogramImage;
    }

    public ImageView getFinalImage() {
        return finalImage;
    }
}
