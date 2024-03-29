
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
        primaryStage.setTitle("Tomograph");
        primaryStage.setScene(new Scene(root, 1250, 480));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop(){
        System.out.println("Stage is closing");
        System.out.println("Killing all threads");
        System.exit(0);
    }


    public static void main(String[] args) {
        launch(args);
    }
}