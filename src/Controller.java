import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

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
    @FXML private Button chooseFileButton;
    @FXML private TextField alphaTextEdit;
    @FXML private TextField betaTextEdit;
    @FXML private TextField detectorsTextEdit;
    private Image imageToProcess;
    private BufferedImage bufferedImage;
    private float alfa = 0.2f;
    private int beta = 360;
    private int detectorCount = 900;
    private Desktop desktop = Desktop.getDesktop();

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        try {
            setBufferedImage(ImageIO.read(new File("res/test_image.png")));
        } catch (IOException e) {
            System.out.println("Error reading file!");
        }

        Image image = new Image("file:src/test_image.png");
        mainGraphicContext = detectorsCanvas.getGraphicsContext2D();
        mainImage.setImage(image);
        detectorsImage.setImage(image);
        // initialize your logic here: all @FXML variables will have been injected
        mainGraphicContext.strokeOval(0,  0, 255, 255);
        textEditSetup();
        buttonsSetup();
    }

    private void startSinogramTask() {
        Runnable task = () -> {
            try {
                runSinogram();
            } catch (IOException e) {
                e.printStackTrace();
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
                    mainGraphicContext.clearRect(0, 0, 255, 255);
                    mainGraphicContext.strokeLine(((sensorPosX + 255) / 2.0), (255 - (sensorPosY + 255) / 2.0), (((emitterPosX + 255) / 2.0) - 2), (255 - (emitterPosY + 255) / 2.0) - 2);
                    mainGraphicContext.strokeOval(0,  0, 255, 255);
                }
            }
            mainGraphicContext.fillOval(((emitterPosX + 255)/2.0)-5,  (255-(emitterPosY+255)/2.0)-5, 4, 4);
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
                            int a = (int)(sinogram.normalize(sinogram.sinogramMatrix[i][j], max, min) * 255);
                            java.awt.Color newColor = new java.awt.Color(a,a,a);
                            image.setRGB(i,j,newColor.getRGB());
                        }
                    }
                    WritableImage sinImage = SwingFXUtils.toFXImage(image, null);
                    getSinogramImage().setImage(sinImage);
            }
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
        sinogram.saveOutputImage("output/output.jpg");
        System.out.println("Finished saving the result");
        startButton.setDisable(false);
    }

//************ GUI SETUP
    private void textEditSetup(){
        alphaTextEdit.textProperty().addListener((observable, newValue, oldValue) -> {
            alphaTextEdit.setText(validate(alphaTextEdit.getText()));
            if(alphaTextEdit.getLength() > 0)
                alfa = Float.valueOf(alphaTextEdit.getText());
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
    private String validate(String text) {
        if (text.matches("[0-9,.]*")){
            return text;
        } else if (text.length() > 1){
            System.out.println('x');
            return text.substring(0, text.length()-1);
        } else return "";
    }

    private void buttonsSetup() {
        startButton.setOnAction(actionEvent -> {
            mainGraphicContext.setStroke(Color.GRAY);
            mainGraphicContext.clearRect(0, 0, mainGraphicContext.getCanvas().getWidth(), mainGraphicContext.getCanvas().getHeight());
            mainGraphicContext.strokeOval(0,  0, 255, 255);
            System.out.println("alpha " + alfa + " beta " + beta + " detectors " + detectorCount);
            startButton.setDisable(true);
            // Run the Sinogram computations
            startSinogramTask();
        });

        chooseFileButton.setOnAction((ActionEvent actionEvent) -> {
            FileChooser fileChooser = new FileChooser();
            configureFileChooser(fileChooser);
            //fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog((Stage) startButton.getScene().getWindow());
            if (file != null) {
                //openFile(file);
                //BufferedImage bufferedImage = null;
                try {
                    setBufferedImage(ImageIO.read(file));
                    imageToProcess = SwingFXUtils.toFXImage(bufferedImage, null);
                    mainImage.setImage(imageToProcess);
                    detectorsImage.setImage(imageToProcess);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
    }

//    GETTERS
    public ImageView getSinogramImage() { return sinogramImage;}

    public ImageView getFinalImage() {
        return finalImage;
    }
    public BufferedImage getBufferedImage() {return bufferedImage;}

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
}
