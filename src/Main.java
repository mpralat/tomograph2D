
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1250, 520));
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    @Override
    public void stop(){
        System.out.println("Stage is closing");
        try {
            DicomFile dicomFile = new DicomFile("output/output");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Killing all threads");
        System.exit(0);
    }


    public static void main(String[] args) {
        launch(args);
    }
}