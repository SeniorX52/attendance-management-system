package com.ams;

import com.mysql.cj.util.Util;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

public class Settings implements Initializable {

    @FXML
    private TextField hostNameField;

    @FXML
    private TextField dbNameField;

    @FXML
    private TextField portField;

    @FXML
    private Button saveButton;
    @FXML
    private ImageView saveImageView;
    @FXML
    private ImageView closeImageView;

    private Properties properties = new Properties();
    private String configFilePath = "src/main/resources/com/ams/database.properties";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadProperties();
        hostNameField.setText(properties.getProperty("serverName", ""));
        dbNameField.setText(properties.getProperty("databaseName", ""));
        portField.setText(properties.getProperty("port", ""));
        Utilities.addHoverEffect(saveImageView,saveButton);
        Utilities.addHoverEffect(closeImageView);
    }
    @FXML
    private void handleSave() {
        loadProperties();
        String url=null;
        try{
            url=generateJdbcUrl(hostNameField.getText(),Integer.parseInt(portField.getText()),dbNameField.getText(),"");
        }
        catch (NumberFormatException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error","Enter a valid port number");
            return;
        }
        catch (IllegalArgumentException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            return;
        }
        properties.setProperty("url",url);
        properties.setProperty("serverName", hostNameField.getText());
        properties.setProperty("databaseName", dbNameField.getText());
        properties.setProperty("port", portField.getText());
        try (FileOutputStream out = new FileOutputStream(configFilePath)) {
            properties.store(out, "Updated database configuration");
            Utilities.showAlert(Alert.AlertType.INFORMATION, "Success", "Updated database configuration successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Login loginController;
    public void setLoginController(Login loginController){
        this.loginController=loginController;
    }
    @FXML
    private void handleClose() {
        loginController.close();
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
    public static String generateJdbcUrl(String hostname, int port, String databaseName, String options) {
        if (hostname == null || hostname.isEmpty()) {
            throw new IllegalArgumentException("Hostname cannot be null or empty.");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty.");
        }
        return String.format("jdbc:mysql://%s:%d/%s%s", hostname, port, databaseName, options != null ? options : "");
    }


}
