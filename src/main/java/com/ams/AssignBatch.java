package com.ams;

import exceptions.InvalidDataException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import models.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AssignBatch implements Initializable {
    @FXML
    private TableView<Session> tableView;
    @FXML
    private TableColumn<Session, String> sessionsName;

    @FXML
    private TableColumn<Session, LocalDate> sessionsDate;

    @FXML
    private TableColumn<Session, LocalTime> sessionsTime;
    @FXML
    private ImageView background;
    @FXML
    private ImageView underline;
    @FXML
    private Label username;
    @FXML
    private ImageView createImageView;
    @FXML
    private Button createButton;
    @FXML
    private Button editButton;
    @FXML
    private ImageView editImageView;
    @FXML
    private ImageView refreshImageView;
    @FXML
    private ChoiceBox<Course> courseChoiceBox;
    @FXML
    private ChoiceBox<Batch> batchChoiceBox;
    @FXML
    private TextField courseTextField;
    @FXML
    private TextField batchTextField;
    @FXML
    private ImageView removeImageView;
    @FXML
    private Button removeButton;
    @FXML
    private ImageView assignImageView;
    @FXML
    private Button assignButton;
    @FXML
    private ListView<Student> assignedStudents;
    @FXML
    private TableView<Student> availableStudents;
    @FXML
    private TableColumn<Student,String> studentName;
    @FXML
    private TableColumn<Student,LocalDate> studentRegisterDate;
    @FXML
    private ChoiceBox<Instructor> assignedInstructorChoiceBox;
    @FXML
    private ProgressBar batchCapacity;
    @FXML
    private Label batchCapacityLabel;
    @FXML
    private ImageView logout;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Utilities.addHoverEffect(logout);
        background.setImage(Utilities.mainPage);
        underline.setImage(Utilities.underline);
        refreshImageView.setImage(Utilities.refreshIcon);
        createImageView.setImage(Utilities.greenButton);
        Utilities.addHoverEffect(refreshImageView);
        editImageView.setImage(Utilities.redButton);
        username.setText(Login.getUsername());
        batchCapacity.setStyle("-fx-accent: #4CAF50;");
        sessionsName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        sessionsName.setCellFactory(TextFieldTableCell.forTableColumn());
        sessionsDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        sessionsTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        studentName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        assignedInstructorChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue, oldInstructor, newInstructor) -> {
            if(newInstructor!=null && batchChoiceBox.getSelectionModel().getSelectedItem()!=null && oldInstructor!=null && oldInstructor!=newInstructor){
                Batch batch=batchChoiceBox.getSelectionModel().getSelectedItem();
                Database.removeAssignedEntryBatch(oldInstructor,batch);
                batch.setInstructor_id(newInstructor.getId());
                try {
                    Database.deleteAttendance(oldInstructor,batch);
                    Database.updateBatch(batch);
                    Database.insertInstructorForBatch(newInstructor,batch);
                    for(Session session:Database.getAllSessions(batch)){
                        Database.insertAttendance(newInstructor,session);
                    }
                    Utilities.showAlert(Alert.AlertType.INFORMATION,"Success", newInstructor.getName()+" is now the instructor of this batch.");
                } catch (SQLException e) {
                    Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
                    e.printStackTrace();
                }
            }
        }));
        studentRegisterDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getRegisterDate();
            return new SimpleObjectProperty<>(date);
        });
        Utilities.addSearchBar(courseChoiceBox, Database.getAllCourses(),courseTextField);
        Utilities.addHoverEffect(editImageView,editButton);
        Utilities.addHoverEffect(assignImageView,assignButton);
        Utilities.addHoverEffect(removeImageView,removeButton);
        Utilities.addHoverEffect(createImageView,createButton);
        courseChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue, course, t1) -> {
            if(t1!=null){
                fillAvailableStudents(t1);
                Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(t1),batchTextField);
            }
            else{
                Utilities.addSearchBar(batchChoiceBox,new ArrayList<>(),batchTextField);
                availableStudents.getItems().setAll(new ArrayList<>());
            }
        }));
        Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(),batchTextField);
        batchChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue, batch, t1) -> {
            if(t1!=null){
                tableView.getItems().setAll(Database.getAllSessions(t1));
                assignedStudents.getItems().setAll(Database.getAllAssignedStudents(t1));
                assignedInstructorChoiceBox.setValue(Database.getInstructor(t1));
                assignedInstructorChoiceBox.getItems().setAll(Database.getAllInstructors(courseChoiceBox.getSelectionModel().getSelectedItem()));
                batchCapacityLabel.setText("Batch Capacity: "+t1.getNoStudents());
                batchCapacity.setProgress((double) assignedStudents.getItems().size() / batchChoiceBox.getSelectionModel().getSelectedItem().getNoStudents());
            }
            else{
                tableView.getItems().setAll(new ArrayList<>());
                batchCapacityLabel.setText("Batch Capacity: ");
                batchCapacity.setProgress(0);
                assignedStudents.getItems().setAll(new ArrayList<>());
                assignedInstructorChoiceBox.setValue(null);
                assignedInstructorChoiceBox.getItems().setAll(new ArrayList<>());
            }
        }));
        availableStudents.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        assignedStudents.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    public void fillAvailableStudents(Course course){
        List<Student> students=Database.getAllAvailableStudents(course);
        availableStudents.getItems().setAll(students);
    }
    @FXML
    private void refresh(){
        Utilities.addSearchBar(courseChoiceBox, Database.getAllCourses(),courseTextField);
        assignedStudents.getItems().setAll(new ArrayList<>());
        availableStudents.getItems().setAll(new ArrayList<>());
        assignedInstructorChoiceBox.setValue(null);
        assignedInstructorChoiceBox.getItems().setAll(new ArrayList<>());
    }
    @FXML
    private void logout(){
        SceneManager.switchToLogin();
    }

    @FXML
    private void assign(){
        try{
            if(assignedStudents.getItems().size()==batchChoiceBox.getSelectionModel().getSelectedItem().getNoStudents()){
                Utilities.showAlert(Alert.AlertType.INFORMATION,"Information","Max number of students in batch = " +batchChoiceBox.getSelectionModel().getSelectedItem().getNoStudents());
                return;
            }
            validateFields();

            List<Student> students=availableStudents.getSelectionModel().getSelectedItems();

            for(int i=0;i<students.size() && assignedStudents.getItems().size()<batchChoiceBox.getSelectionModel().getSelectedItem().getNoStudents();i++){
                Database.assignStudent(students.get(i),batchChoiceBox.getSelectionModel().getSelectedItem());
                for(Session session:Database.getAllSessions(batchChoiceBox.getSelectionModel().getSelectedItem())){
                    Database.insertAttendance(students.get(i),session);
                }
            }
            availableStudents.getItems().setAll(Database.getAllAvailableStudents(courseChoiceBox.getSelectionModel().getSelectedItem()));
            assignedStudents.getItems().setAll(Database.getAllAssignedStudents(batchChoiceBox.getSelectionModel().getSelectedItem()));
            batchCapacity.setProgress((double) assignedStudents.getItems().size() / batchChoiceBox.getSelectionModel().getSelectedItem().getNoStudents());
        }
        catch (InvalidDataException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }

    }
    @FXML
    private void remove(){
        try{
            validateFields();
            List<Student> students=assignedStudents.getSelectionModel().getSelectedItems();
            for(Student student:students){
                Database.removeAssignedEntryBatch(student,batchChoiceBox.getSelectionModel().getSelectedItem());
                List<Course> courses=new ArrayList<>();
                courses.add(courseChoiceBox.getSelectionModel().getSelectedItem());
                Database.deleteAttendance(student,courses);
            }
            availableStudents.getItems().setAll(Database.getAllAvailableStudents(courseChoiceBox.getSelectionModel().getSelectedItem()));
            assignedStudents.getItems().setAll(Database.getAllAssignedStudents(batchChoiceBox.getSelectionModel().getSelectedItem()));
            batchCapacity.setProgress((double) assignedStudents.getItems().size() / batchChoiceBox.getSelectionModel().getSelectedItem().getNoStudents());
        }
        catch (SQLException | InvalidDataException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }
    }
    private void validateFields() throws InvalidDataException {
        if(courseChoiceBox.getSelectionModel().getSelectedItem()==null){
            throw new InvalidDataException("Choose a course!");
        }
        if(batchChoiceBox.getSelectionModel().getSelectedItem()==null){
            throw new InvalidDataException("Choose a batch to assign!");
        }
    }

    @FXML
    private void create(){
        SceneManager.switchToCourses();
    }
    @FXML
    private void switchToMain(){
        SceneManager.switchToMainPage();
    }
    @FXML
    private void switchToDataEntry(){
        SceneManager.switchToDataEntry();
    }
    @FXML
    private void switchToAttendance(){
        SceneManager.switchToAttendance();
    }
}
