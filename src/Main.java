import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;

public class Main extends Application {
//    private void moveCanvas(int x, int y) {
//        canvas.
//    }
//
    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Group root = new Group();
        primaryStage.setTitle("Tomograph2D");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0));

        final ImageView mainImageView = new ImageView();
        final ImageView sinogramImageView = new ImageView();
        Image image = new Image("file:res/test_image.png");

        //File file = new File("/home/marta/IdeaProjects/tomograph2D/output.jpg");
        //Image image = new Image(file.toURI().toString());

        mainImageView.setImage(image);
        sinogramImageView.setImage(image);

        Canvas circleCanvas = new Canvas(image.getWidth(), image.getHeight());
        circleCanvas.setTranslateX(image.getWidth());
        GraphicsContext gc1 = circleCanvas.getGraphicsContext2D();
        gc1.setStroke(Color.BLUE);
        gc1.strokeOval(0,  0, image.getHeight()-1, image.getWidth()-1);

        final HBox pictureRegion = new HBox();
        pictureRegion.getChildren().add(mainImageView);
        pictureRegion.getChildren().add(sinogramImageView);

        gridPane.add(pictureRegion,1,1);

        root.getChildren().add(gridPane);
        root.getChildren().add(circleCanvas);
        primaryStage.setScene(new Scene(root, 700 , 700));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
        System.out.println("xd");
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
