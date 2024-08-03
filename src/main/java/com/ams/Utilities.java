package com.ams;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Utilities {
    public static final Image user=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/User.png")).toExternalForm());
    public static final Image instructor=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/Instructor.png")).toExternalForm());
    public static final Image blueButton=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/blue button.png")).toExternalForm());
    public static final Image redButton=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/red button.png")).toExternalForm());
    public static final Image greenButton=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/green button.png")).toExternalForm());
    public static final Image underline=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/underline.png")).toExternalForm());
    public static final Image mainPage=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/mainPage.jpg")).toExternalForm());
    public static final Image loginPage=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/login.jpg")).toExternalForm());
    public static final Image refreshIcon=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/Refresh_icon.png")).toExternalForm());
    public static final Image group=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/batch.png")).toExternalForm());
    public static final Image book=new Image(Objects.requireNonNull(Utilities.class.getResource("/images/Book.png")).toExternalForm());
    public static void addHoverEffect(ImageView imageView, Button button) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.GRAY);
        shadow.setRadius(20);
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> imageView.setEffect(shadow));
        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> imageView.setEffect(null));
    }
    public static void addHoverEffect(ImageView imageView) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.GRAY);
        shadow.setRadius(20);
        imageView.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> imageView.setEffect(shadow));
        imageView.addEventHandler(MouseEvent.MOUSE_EXITED, e -> imageView.setEffect(null));
    }
    public static void addHoverEffectPostion(ImageView imageView, Button targetButton) {
        // Get the target button's position and size
        double targetX = imageView.getLayoutX();
        double targetY = imageView.getLayoutY();
        double targetWidth = targetButton.getWidth();
        double targetHeight = targetButton.getHeight();

        // Create TranslateTransition to move the ImageView
        TranslateTransition moveTransition = new TranslateTransition(Duration.millis(300), imageView);
        moveTransition.setToX(targetX - imageView.getLayoutX());
        moveTransition.setToY(targetY - imageView.getLayoutY());

        // Create ScaleTransition to scale the ImageView slightly when hovered
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), imageView);
        scaleTransition.setFromX(1);
        scaleTransition.setFromY(1);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);

        // Setup mouse enter and exit events
        imageView.setOnMouseEntered(event -> {
            moveTransition.play();
            scaleTransition.play();
        });

        imageView.setOnMouseExited(event -> {
            // Reverse the transitions for mouse exit
            moveTransition.setRate(-1);
            scaleTransition.setRate(-1);
            moveTransition.play();
            scaleTransition.play();
        });
    }
    public static void addDateTime(Label dateTimeLabel){
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), _ ->updateDateTime(dateTimeLabel)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    public static void updateDateTime(Label dateTimeLabel) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateTime = sdf.format(new Date());
        dateTimeLabel.setText(currentDateTime);
    }
    public static <T> void addSearchBar(ListView<T> listView, List<T> list, TextField searchField, Function<T, String> stringExtractor) {
        ObservableList<T> itemList = FXCollections.observableArrayList(list);
        FilteredList<T> filteredItems = new FilteredList<>(itemList, _ -> true);
        listView.setItems(filteredItems);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredItems.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                String itemString = stringExtractor.apply(item).toLowerCase();
                return itemString.contains(lowerCaseFilter);
            });
        });
    }

    public static <T> void addSearchBar(ChoiceBox<T> choiceBox, List<T> items, TextField searchField) {
        ObservableList<T> itemList = FXCollections.observableArrayList(items);
        FilteredList<T> filteredItems = new FilteredList<>(itemList, p -> true);
        choiceBox.setItems(filteredItems);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredItems.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return item.toString().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        alert.setGraphic(new ImageView(book));
    }
}
