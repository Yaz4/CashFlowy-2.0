package CashFlowy;

import CashFlowy.controller.LoginController;
import CashFlowy.service.DataSourceProvider;
import CashFlowy.service.auth.AuthenticationStrategy;
import CashFlowy.service.auth.HardcodedAuthenticationStrategy;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        HikariDataSource hikariDataSource = DataSourceProvider.getInstance().getDataSource();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Login - CashFlowy");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();


        LoginController loginController = loader.getController();
        loginController.setDataSource(hikariDataSource);
        // Inject Strategy for authentication (Strategy Pattern)
        AuthenticationStrategy authStrategy = new HardcodedAuthenticationStrategy("yaz", "123");
        loginController.setAuthenticationStrategy(authStrategy);

        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

    }
}

