package CashFlowy.controller;

import com.zaxxer.hikari.HikariDataSource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    private HikariDataSource dataSource;

    // Senza db
    private static final String USERNAME = "yaz";
    private final String PASSWORD = "123";

    public void setDataSource(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (USERNAME.equals(username) && PASSWORD.equals(password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
                Parent root = loader.load();

                MainController mainController = loader.getController();
                mainController.initDataSource(dataSource);

                Stage stage = new Stage();
                stage.setTitle("CashFlowy - Dashboard");
                stage.setScene(new Scene(root));
                stage.setFullScreen(true);
                stage.show();
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/CashFlowy Logo.png")));

                // Chiude la schermata di login
                ((Stage) usernameField.getScene().getWindow()).close();

            } catch (Exception e) {
                showAlert("Errore", "Impossibile caricare la schermata principale.");
                e.printStackTrace();
            }
        } else {
            showAlert("Accesso negato", "Username o password errati.");
        }
    }

    public static String getUsername() {
        return new String(USERNAME);
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

