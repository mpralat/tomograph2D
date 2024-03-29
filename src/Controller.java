import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

public class Controller implements Initializable {
    private static final float ALPHA = 0.5f;
    private static final int BETA = 360;
    private static final int DETECTOR_COUNT = 800;
    private int currentStep = 0;

    @FXML private GraphicsContext mainGraphicContext;
    @FXML private Canvas detectorsCanvas;
    @FXML private ImageView detectorsImage;
    @FXML private ImageView sinogramImage;
    @FXML private ImageView finalImage;
    @FXML private ImageView squareErrorImage;
    @FXML private Button startButton;
    @FXML private Button chooseFileButton;
    @FXML private Button nextIterButton;
    @FXML private Button stopButton;
    @FXML private Button startManuallyButton;
    @FXML private TextField alphaTextEdit;
    @FXML private TextField betaTextEdit;
    @FXML private TextField detectorsTextEdit;
    @FXML private TextField nameTextEdit;
    @FXML private TextField ageTextEdit;
    @FXML private TextArea commentsTextEdit;
    @FXML private ChoiceBox sexChoiceBox;
    @FXML private Label infoLabel;

    private Image imageToProcess;
    private BufferedImage bufferedImage;
    private ComputationManager computationManager;
    private float alfa = ALPHA;
    private int beta = BETA;
    private int detectorCount = DETECTOR_COUNT;
    private boolean started = false;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        try {
            setBufferedImage(ImageIO.read(new File("src/test_image.png")));
        } catch (IOException e) {
            System.out.println("Error reading file!");
        }
        Image image = new Image("file:src/test_image.png");
        mainGraphicContext = detectorsCanvas.getGraphicsContext2D();
        detectorsImage.setImage(image);
        // initialize your logic here: all @FXML variables will have been injected
        mainGraphicContext.strokeOval(0, 0, 255, 255);
        computationManager = new ComputationManager(this);
        sexChoiceBox.getSelectionModel().selectFirst();
        textEditSetup();
        buttonsSetup();
    }

    //************ GUI SETUP
    private void textEditSetup() {
        alphaTextEdit.setText(String.valueOf(alfa));
        betaTextEdit.setText(String.valueOf(beta/2));
        detectorsTextEdit.setText(String.valueOf(detectorCount));

        alphaTextEdit.textProperty().addListener((observable, newValue, oldValue) -> {
            alphaTextEdit.setText(validate(alphaTextEdit.getText()));
            if (alphaTextEdit.getLength() > 0) {
                alfa = Float.valueOf(alphaTextEdit.getText());
            }
            else
                alfa = ALPHA;
            System.out.println(alfa);
        });
        betaTextEdit.textProperty().addListener((observable, newValue, oldValue) -> {
            if (betaTextEdit.getLength() > 0)
                beta = Integer.parseInt(betaTextEdit.getText()) * 2;
            else
                beta = BETA;
            if (beta > 361){
                beta = BETA;
            }
            betaTextEdit.setText(validate(betaTextEdit.getText()));
        });
        detectorsTextEdit.textProperty().addListener((observable, newValue, oldValue) -> {
            if (detectorsTextEdit.getLength() > 0)
                detectorCount = Integer.parseInt(detectorsTextEdit.getText());
            else
                detectorCount = DETECTOR_COUNT;
            if(detectorCount == 1)
                detectorCount=DETECTOR_COUNT;
            detectorsTextEdit.setText(validate(detectorsTextEdit.getText()));

        });
        ageTextEdit.textProperty().addListener(((observableValue, s, t1) -> ageTextEdit.setText(validate(ageTextEdit.getText()))));
    }

    private String validate(String text) {
        // Checking the correctness for the alpha, beta and detectors count text edits.
        // Validates the string. If the recently typed letter is neither a dot nor a number, it is erased.
        if (text.matches("[0-9,.]*")) {
            return text;
        } else if (text.length() > 1) {
            return text.substring(0, text.length() - 1);
        } else return "";
    }

    private void disableTextEdits(boolean option) {
        alphaTextEdit.setDisable(option);
        betaTextEdit.setDisable(option);
        detectorsTextEdit.setDisable(option);
    }

    private void disableButtons(boolean option){
        startButton.setDisable(option);
        startManuallyButton.setDisable(option);
        nextIterButton.setDisable(option);
    }

    private void setTextEdits(){
        alphaTextEdit.setText(String.valueOf(alfa));
        betaTextEdit.setText(String.valueOf(beta/2));
        detectorsTextEdit.setText(String.valueOf(detectorCount));
    }

    private void buttonsSetup() {
        nextIterButton.setDisable(true);
        startButton.setOnAction(actionEvent -> {
            getFinalImage().setImage(null);
            getSquareErrorImage().setImage(null);
            if(!started) clear();
            started = true;
            setTextEdits();
            disableTextEdits(true);
            disableButtons(true);
            prepareForDrawing();
            // Run the Sinogram computations
            computationManager.startSinogramTask(getCurrentStep());
            infoLabel.setText("Processing...");
        });
        startManuallyButton.setOnAction(actionEvent -> {
            if (!started) clear();
            started = true;
            prepareForDrawing();
            disableTextEdits(true);
            nextIterButton.setDisable(false);
            setCurrentStep(0);
        });
        nextIterButton.setOnAction(actionEvent -> {
            for (int i = 0; i < 20; i++) {
                try {
                    if (!computationManager.oneSinogramIteration(getCurrentStep())) {
                        nextIterButton.setDisable(true);
                        startButton.setDisable(false);
                        startManuallyButton.setDisable(false);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                setCurrentStep(getCurrentStep() + 1);
            }
        });
        stopButton.setOnAction((ActionEvent actionEvent) -> {
            computationManager.setShutdownTask();
            disableButtons(false);
            clear();
            getSinogramImage().setImage(null);
            getMainGraphicContext().clearRect(0, 0, 255, 255);
            infoLabel.setText(" ");
        });
        // FILE CHOOSER SETUP
        chooseFileButton.setOnAction((ActionEvent actionEvent) -> {
            FileChooser fileChooser = new FileChooser();
            configureFileChooser(fileChooser);
            File file = fileChooser.showOpenDialog(startButton.getScene().getWindow());
            if (file != null) {
                try {
                    setBufferedImage(ImageIO.read(file));
                    imageToProcess = SwingFXUtils.toFXImage(bufferedImage, null);
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
                new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.png", "*.JPG", "*.PNG")
        );
    }

    void clear(){
        System.out.println("clear");
        computationManager = new ComputationManager(this);
        currentStep = 0;
        disableTextEdits(false);
        setStarted(false);
    }

    void saveDicom(){
        try {
            DicomFile dicomFile = new DicomFile("output/output", this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareForDrawing() {
        // Preparing the graphic context: setting up the colours and clearing the canvas.
        mainGraphicContext.setStroke(Color.GRAY);
        mainGraphicContext.clearRect(0, 0, 255, 255);
        mainGraphicContext.strokeOval(0, 0, 255, 255);
        System.out.println("alpha " + alfa + " beta " + beta + " detectors " + detectorCount);
    }

    //    GETTERS
    ImageView getSinogramImage() { return sinogramImage; }
    ImageView getFinalImage() {
        return finalImage;
    }
    ImageView getSquareErrorImage() {
        return squareErrorImage;
    }
    BufferedImage getBufferedImage() { return bufferedImage;}
    GraphicsContext getMainGraphicContext() {
        return mainGraphicContext;
    }
    Button getStartButton() {
        return startButton;
    }
    Button getStartManuallyButton() { return startManuallyButton; }
    TextField getNameTextEdit() {return nameTextEdit;}
    TextField getAgeTextEdit() {return ageTextEdit;}
    TextArea getCommentsTextEdit() {return commentsTextEdit;}
    ChoiceBox getSexChoiceBox() {return sexChoiceBox;}
    Label getInfoLabel() {return infoLabel;}

    // SETTERS
    float getAlfa() {
        return alfa;
    }
    int getBeta() {
        return beta;
    }
    int getDetectorCount() { return detectorCount; }
    private int getCurrentStep() { return currentStep;}
    private void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
    private void setStarted(boolean started) {
        this.started = started;
    }
    private void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
}
