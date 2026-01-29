package CashFlowy.controller;

import CashFlowy.service.auth.AuthenticationStrategy;
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
    private AuthenticationStrategy authenticationStrategy;

    // Senza db (kept for UI welcome message behavior)
    private static final String USERNAME = "yaz";

    public void setDataSource(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setAuthenticationStrategy(AuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean authenticated = authenticationStrategy != null && authenticationStrategy.authenticate(username, password);
        if (authenticated) {
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
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/CashFlowy Logo.png")));

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

