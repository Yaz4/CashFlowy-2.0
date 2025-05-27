package CashFlowy;

import CashFlowy.controller.MainController;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static final String JDBC_Driver = "org.postgresql.Driver";
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/jdbc_schema?user=user&password=secret&ssl=false";


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        HikariDataSource hikariDataSource = initDataSource(JDBC_Driver, JDBC_URL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.initDataSource(hikariDataSource);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("CashFlowy");
        stage.setScene(scene);
        stage.show();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/CashFlowy Logo.png")));
    }
    private HikariDataSource initDataSource(String JDBC_Driver, String JDBC_URL) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(JDBC_Driver);
        config.setJdbcUrl(JDBC_URL);
        config.setLeakDetectionThreshold(2000);
        return new HikariDataSource(config);
    }


}

