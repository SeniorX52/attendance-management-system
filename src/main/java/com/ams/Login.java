package com.ams;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import models.Database;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;


public class Login implements Initializable {


    private static String username;
    @FXML
    private TextField textField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messsage;
    @FXML
    private ImageView background;
    @FXML
    private ImageView button;
    @FXML
    private Button transparentButton;
    @FXML
    private StackPane buttonStackpane;
    @FXML
    private ImageView openSettings;
    @FXML
    private AnchorPane anchorPane;
    public static String getUsername(){
        return "sql7723338";
    }
    private Properties properties = new Properties();
    private String configFilePath = "src/main/resources/com/ams/database.properties";
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        background.setImage(Utilities.loginPage);
        loadProperties();
        textField.setText(properties.getProperty("username"));
        passwordField.setText(properties.getProperty("password"));
        button.setImage(Utilities.blueButton);
        Utilities.addHoverEffect(button,transparentButton);
        Utilities.addHoverEffect(openSettings);
    }
    private void loadProperties() {
        String installDir = System.getProperty("user.dir");
        configFilePath = installDir + File.separator + "database.properties";
        properties = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        }
        catch (IOException e) {
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
    }
    private AnchorPane settingsPane;

    @FXML
    private void openSettings() {
        if(!anchorPane.getChildren().isEmpty()){
            close();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
            settingsPane = loader.load();
            Settings settings = loader.getController();
            settings.setLoginController(this);
            anchorPane.getChildren().add(settingsPane);
            settingsPane.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), settingsPane);
            fadeIn.setFromValue(0); // Start opacity
            fadeIn.setToValue(1);   // End opacity
            fadeIn.setCycleCount(1);
            fadeIn.setAutoReverse(false);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void close(){
        if (anchorPane != null && settingsPane!=null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), settingsPane);
            fadeOut.setFromValue(1); // Start opacity
            fadeOut.setToValue(0);   // End opacity
            fadeOut.setCycleCount(1);
            fadeOut.setAutoReverse(false);
            fadeOut.setOnFinished(event -> anchorPane.getChildren().remove(settingsPane));
            // Play the transition
            fadeOut.play();
        }
    }
    @FXML
    public void submit() {
        loadProperties();
        try{
            Database.initDB();
        }
        catch (NumberFormatException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error","Port must be a valid number");
            return;
        }
        catch (Exception e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            return;
        }

        try (
                var _ = Database.getConnection(textField.getText(), passwordField.getText())
        ) {
            username=textField.getText();
            properties.setProperty("username", username);
            properties.setProperty("password", passwordField.getText());
//            properties.setProperty("url",properties.getProperty("url"));
//            properties.setProperty("databaseName",properties.getProperty("databaseName"));
//            properties.setProperty("port",properties.getProperty("port"));
//            properties.setProperty("serverName",properties.getProperty("serverName"));
            try (FileOutputStream out = new FileOutputStream(configFilePath)) {
                properties.store(out, "Updated user configuration");
            } catch (IOException e) {
                e.printStackTrace();
            }
            messsage.setText("Success! Connection made to the AMS Database");
            SceneManager.switchToMainPage();

        } catch (SQLException e) {
            messsage.setText("Failed to connect to the AMS Database!");
        }
    }

    @FXML
    private Hyperlink emailLink;

    @FXML
    private void handleEmailLinkAction() {
        String email = "mailto:mostafa.abdelazizx52@gmail.com"; // Replace with your Gmail address
        try {
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI(email);
            desktop.mail(uri);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
