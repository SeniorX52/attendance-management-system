package com.ams;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import models.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Attendance implements Initializable {
    @FXML
    private ImageView background;
    @FXML
    private ImageView underline;
    @FXML
    private Label username;
    @FXML
    private ImageView refreshImageView;
    @FXML
    private TextField courseTextField;
    @FXML
    private TextField batchTextField;
    @FXML
    private TextField sessionTextField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<Course> courseChoiceBox;
    @FXML
    private ChoiceBox<Batch> batchChoiceBox;
    @FXML
    private ChoiceBox<Session> sessionChoiceBox;
    @FXML
    private TableView<Student> studentTableView;
    @FXML
    private TableColumn<Student,String> studentName;
    @FXML
    private TableColumn<Student,String> studentPhone;
    @FXML
    private TableColumn<Student,String> studentEmail;
    @FXML
    private TableColumn<Student,Boolean> studentAttendance;
    @FXML
    private TableView<Instructor> instructorTableView;
    @FXML
    private TableColumn<Instructor,String> instructorName;
    @FXML
    private TableColumn<Instructor,String> instructorPhone;
    @FXML
    private TableColumn<Instructor,String> instructorEmail;
    @FXML
    private TableColumn<Instructor,Boolean> instructorAttendance;
    @FXML
    private Label dateTimeLabel;
    @FXML
    private ImageView logout;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Utilities.addHoverEffect(logout);
        background.setImage(Utilities.mainPage);
        underline.setImage(Utilities.underline);
        Utilities.addSearchBar(courseChoiceBox,Database.getAllCourses(),courseTextField);
        Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(),batchTextField);
        Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(),sessionTextField);
        courseChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue, oldValue, newValue) -> {
            if(newValue!=null){
                Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(newValue),batchTextField);
                Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(newValue),sessionTextField);
            }
            else{
                Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(),batchTextField);
                Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(),sessionTextField);
            }
        }));
        Utilities.addDateTime(dateTimeLabel);
        username.setText(Login.getUsername());
        batchChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue, oldValue, newValue) -> {
            if(newValue!=null){
                Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(newValue),sessionTextField);
            }
            else{
                if(courseChoiceBox.getSelectionModel().getSelectedItem()!=null){
                    Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(courseChoiceBox.getSelectionModel().getSelectedItem()),sessionTextField);
                }
                else{
                    Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(),sessionTextField);
                }
            }
        }));
        Utilities.addHoverEffect(refreshImageView);
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if(batchChoiceBox.getSelectionModel().getSelectedItem()!=null){
                    Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(newValue,batchChoiceBox.getSelectionModel().getSelectedItem()),sessionTextField);
                }
                else{
                    if(courseChoiceBox.getSelectionModel().getSelectedItem()!=null){
                        Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(newValue,courseChoiceBox.getSelectionModel().getSelectedItem()),sessionTextField);
                    }
                    else{
                        Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(newValue),sessionTextField);
                    }
                }
            }
            else{
                if(batchChoiceBox.getSelectionModel().getSelectedItem()!=null){
                    Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(batchChoiceBox.getSelectionModel().getSelectedItem()),sessionTextField);
                }
                else{
                    if(courseChoiceBox.getSelectionModel().getSelectedItem()!=null){
                        Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(courseChoiceBox.getSelectionModel().getSelectedItem()),sessionTextField);
                    }
                    else{
                        Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(),sessionTextField);
                    }
                }
            }
        });
        sessionChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue,oldValue, newValue) -> {
            if(newValue!=null){
                studentTableView.getItems().setAll(Database.getAllStudents(newValue));
                instructorTableView.getItems().setAll(Database.getInstructor(newValue));
            }
            else{
                studentTableView.getItems().setAll(new ArrayList<>());
                instructorTableView.getItems().setAll(new ArrayList<>());
            }
        }));
        studentName.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        studentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        studentAttendance.setCellFactory(column -> new TableCell<Student, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    Student student = getTableRow().getItem();
                    if (student != null) {
                        if (isSelected) {
                            onAttendanceChecked(student);
                        } else {
                            onAttendanceUnchecked(student);
                        }
                    }
                });
                setGraphic(checkBox);
            }

            @Override
            protected void updateItem(Boolean isAttending, boolean empty) {
                super.updateItem(isAttending, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Student student = getTableRow().getItem();
                    if (student != null) {
                        checkBox.setSelected(Database.isEntryAttended(student,sessionChoiceBox.getSelectionModel().getSelectedItem()));
                    }
                    setGraphic(checkBox);
                }
            }
        });

        // Initialize Instructor Table
        instructorName.setCellValueFactory(new PropertyValueFactory<>("name"));
        instructorPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        instructorEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        instructorAttendance.setCellFactory(column -> new TableCell<Instructor, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    Instructor instructor = getTableRow().getItem();
                    if (instructor != null) {
                        // Update the database based on the new state
                        if (isSelected) {
                            onAttendanceChecked(instructor);
                        } else {
                            onAttendanceUnchecked(instructor);
                        }
                    }
                });
                setGraphic(checkBox);
            }
            @Override
            protected void updateItem(Boolean isAttending, boolean empty) {
                super.updateItem(isAttending, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Instructor instructor = getTableRow().getItem();
                    if (instructor != null) {
                        checkBox.setSelected(Database.isEntryAttended(instructor,sessionChoiceBox.getSelectionModel().getSelectedItem()));
                    }
                    setGraphic(checkBox);
                }
            }
        });
    }
    @FXML
    private void logout(){
        SceneManager.switchToLogin();
    }
    public void onAttendanceChecked(Entry entry){
        Database.updateAttendance(entry,sessionChoiceBox.getSelectionModel().getSelectedItem(),true);
    }
    public void onAttendanceUnchecked(Entry entry){
        Database.updateAttendance(entry,sessionChoiceBox.getSelectionModel().getSelectedItem(),false);
    }
    public void setSession(Session session){
        sessionChoiceBox.setValue(session);
    }
    @FXML
    private void switchToStudents(){
        SceneManager.switchToDataEntry();
    }
    @FXML
    private void switchToCourses(){
        SceneManager.switchToCourses();
    }
    @FXML
    private void switchToMain(){
        SceneManager.switchToMainPage();
    }
    @FXML
    public void refresh(){
        Utilities.addSearchBar(courseChoiceBox,Database.getAllCourses(),    courseTextField);
        Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(),     batchTextField);
        Utilities.addSearchBar(sessionChoiceBox,Database.getAllSessions(),  sessionTextField);
        courseTextField.setText("");
        batchTextField.setText("");
        sessionTextField.setText("");
        datePicker.setValue(null);
        sessionChoiceBox.setValue(null);
    }
}
