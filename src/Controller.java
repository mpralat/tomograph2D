import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public ImageView mainImageView;
    public ImageView sinogramImageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File file = new File("test_image.png");
        Image image = new Image(file.toURI().toString());
        mainImageView.setImage(image);
        sinogramImageView.setImage(image);

        final float alfa = 0.2f;
        final int beta = 180;
        final int detectorCount = 500;

        Sinogram sinogram = new Sinogram();
        Tomograph tomograph = new Tomograph(alfa, beta, detectorCount, sinogram.getImageSize()/2 - 1);

        for (int step = 0; step < tomograph.getSteps(); step++) {
            int emitterPosX = tomograph.getEmitterPosX(step);
            int emitterPosY = tomograph.getEmitterPosY(step);
            ArrayList<Float> row = new ArrayList<>();
            for (int sensorIndex = 0; sensorIndex < tomograph.getDetectorsSensorsCount(); sensorIndex++) {
                int sensorPosX = tomograph.getDetectorsSensorPosX(step, sensorIndex);
                int sensorPosY = tomograph.getDetectorsSensorPosY(step, sensorIndex);
                row.add(sinogram.BresenhamAlgorithm(emitterPosX, emitterPosY, sensorPosX, sensorPosY));
                Circle circle = new Circle();
                circle.setCenterX(emitterPosX);
                circle.setCenterY(emitterPosY);
                circle.setRadius(50.0f);
            }
            sinogram.insertRowToMatrix(row);
        }

        sinogram.saveSinogramAsImage();
    }
}
