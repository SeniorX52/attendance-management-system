package com.ams;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import models.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class DataEntryEditController implements Initializable {
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField ageTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField numberTextField;
    @FXML
    private Label dateTimeLabel;
    @FXML
    private ImageView background;
    @FXML
    private ImageView underline;
    @FXML
    private Label username;
    @FXML
    private RadioButton maleRadioButton;
    @FXML
    private RadioButton femaleRadioButton;
    @FXML
    private ImageView createImageView;
    @FXML
    private Button createButton;
    @FXML
    private ImageView deleteImageView;
    @FXML
    private Button deleteButton;
    @FXML
    private ImageView assignImageView;
    @FXML
    private Button assignButton;
    @FXML
    private ImageView clearCoursesImageView;
    @FXML
    private Button clearCoursesButton;
    @FXML
    private ImageView user;
    @FXML
    private ListView<Course> listView;
    @FXML
    private ListView<Course> assignedCoursesListView;
    @FXML
    private TextField searchCourses;
    private FilteredList<String> filteredCourses;
    @FXML
    private Label alertMessage;
    @FXML
    private ChoiceBox<Entry> studentsChoiceBox;
    @FXML
    private ChoiceBox<Batch> assignedBatchesChoiceBox;
    @FXML
    private Button editButton;
    @FXML
    private Button createStudentButton;
    @FXML
    private ImageView createStudentImageView;
    @FXML
    private Button removeCourseButton;
    @FXML
    private ImageView removeCourseImageView;
    @FXML
    private ImageView editImageView;
    @FXML
    private TextField searchStudents;
    @FXML
    private ImageView refreshImageView;
    @FXML
    private Label entryRoleLabel;
    private ToggleGroup toggleGroup;
    @FXML
    private TableView<Session> sessions;
    @FXML
    private TableColumn<Session, LocalDate> dateColumn;
    @FXML
    private TableColumn<Session, String> nameColumn;
    @FXML
    private TableColumn<Session, Boolean> attendanceColumn;
    @FXML
    private ImageView logout;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Utilities.addHoverEffect(logout);
        background.setImage(Utilities.mainPage);
        underline.setImage(Utilities.underline);
        deleteImageView.setImage(Utilities.redButton);
        assignImageView.setImage(Utilities.greenButton);
        clearCoursesImageView.setImage(Utilities.redButton);
        createImageView.setImage(Utilities.blueButton);
        createStudentImageView.setImage(Utilities.greenButton);
        removeCourseImageView.setImage(Utilities.redButton);
        assignedCoursesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        editImageView.setImage(Utilities.redButton);
        refreshImageView.setImage(Utilities.refreshIcon);
        user.setImage(Utilities.user);
        Utilities.addDateTime(dateTimeLabel);
        username.setText(Login.getUsername());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        toggleGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(toggleGroup);
        femaleRadioButton.setToggleGroup(toggleGroup);
        Utilities.addSearchBar(listView,Database.getAllCourses(),searchCourses,Course::getName);
        Utilities.addHoverEffect(createImageView,createButton);
        Utilities.addHoverEffect(deleteImageView,deleteButton);
        Utilities.addHoverEffect(assignImageView,assignButton);
        Utilities.addHoverEffect(clearCoursesImageView,clearCoursesButton);
        Utilities.addHoverEffect(createStudentImageView,createStudentButton);
        Utilities.addHoverEffect(editImageView,editButton);
        Utilities.addHoverEffect(refreshImageView);

        Utilities.addHoverEffect(removeCourseImageView,removeCourseButton);
        studentsChoiceBox.getItems().addAll(Database.getAllStudents());
        assignedBatchesChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue, course, t1) -> {
            if(t1!=null){
                Entry entry=studentsChoiceBox.getSelectionModel().getSelectedItem();
                if(entry instanceof Student){
                    sessions.getItems().setAll(Database.getAllSessions(entry,t1));
                }
                else{
                    sessions.getItems().setAll(Database.getAllSessions(t1));
                }
            }
            else{
                sessions.getItems().setAll(new ArrayList<>());
            }
        }));
        studentsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                onStudentSelected(newValue);
            }
        });
        Utilities.addSearchBar(studentsChoiceBox,Database.getAllEntries(),searchStudents);
        // Initialize Date Column
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty()); // Use a property if you have one

        // Initialize Name Column
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty()); // Use a property if you have one

        // Initialize Attendance Column
        attendanceColumn.setCellFactory(column -> new TableCell<Session, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    Session session = getTableRow().getItem();
                    if (session != null) {
                        Database.updateAttendance(studentsChoiceBox.getSelectionModel().getSelectedItem(),session,isSelected);
                    }
                });
                setGraphic(checkBox);
            }

            @Override
            protected void updateItem(Boolean isAttended, boolean empty) {
                super.updateItem(isAttended, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Session session = getTableRow().getItem();
                    if (session != null) {
                        checkBox.setSelected(Database.isEntryAttended(studentsChoiceBox.getSelectionModel().getSelectedItem(),session));
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
    @FXML
    private void switchToCourses(){
        SceneManager.switchToCourses();
    }
    @FXML
    private void removeSelectedCourses(){
        try{
            Database.deleteAttendance(getEntry(),assignedCoursesListView.getSelectionModel().getSelectedItems());
            Database.deleteAssignedCourses(getEntry(),assignedCoursesListView.getSelectionModel().getSelectedItems());
            for(Course course:assignedCoursesListView.getSelectionModel().getSelectedItems()){
                Database.removeAssignedEntryBatch(getEntry(),course);
            }
            assignedCoursesListView.getItems().removeAll(assignedCoursesListView.getSelectionModel().getSelectedItems());
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }


    }
    @FXML
    private void switchToAttendance(){
        SceneManager.switchToAttendance();
    }
    @FXML
    private void refresh(){
        Utilities.addSearchBar(studentsChoiceBox,Database.getAllEntries(),searchStudents);
        Utilities.addSearchBar(listView,Database.getAllCourses(),searchCourses,Course::getName);
        assignedBatchesChoiceBox.setValue(null);
        assignedBatchesChoiceBox.getItems().setAll(new ArrayList<>());
        sessions.getItems().setAll(new ArrayList<>());
    }
    private void clear(){
        nameTextField.setText("");
        emailTextField.setText("");
        numberTextField.setText("");
        ageTextField.setText("");
        assignedCoursesListView.getItems().clear();
        assignedBatchesChoiceBox.getItems().clear();
        toggleGroup.selectToggle(null);
    }
    private void onStudentSelected(Entry entry){
        nameTextField.setText(entry.getName());
        emailTextField.setText(entry.getEmail());
        ageTextField.setText(String.valueOf(entry.getAge()));
        numberTextField.setText(entry.getPhoneNumber());

        if(entry.getGender().equals("Male")){
            maleRadioButton.fire();
        }
        else{
            femaleRadioButton.fire();
        }
        if(entry instanceof Student){
            entryRoleLabel.setText("Student");
            user.setImage(Utilities.user);
        }
        else{
            entryRoleLabel.setText("Instructor");
            user.setImage(Utilities.instructor);
        }
        try {
            assignedBatchesChoiceBox.getItems().setAll(Database.getAllBatches(entry));
            assignedCoursesListView.getItems().clear();
            assignedCoursesListView.getItems().addAll(Database.getEntryAssignedCourses(entry));
            assignedBatchesChoiceBox.setValue(null);
            sessions.getItems().setAll(new ArrayList<>());
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }

    }
    @FXML
    private void switchToMain(){
        SceneManager.switchToMainPage();
    }
    @FXML
    private void submit(){
        try{
            Entry entry= getEntry();
            if(studentsChoiceBox.getSelectionModel().getSelectedItem()!=null){
                entry.setId(studentsChoiceBox.getSelectionModel().getSelectedItem().getId());
            }
            else{
                Utilities.showAlert(Alert.AlertType.ERROR,"Error","Choose a student or instructor to edit!");
                return;
            }
            Database.updateEntry(entry);
            Database.assignCourses(entry,assignedCoursesListView.getItems());
            refresh();
            clear();
            Utilities.showAlert(Alert.AlertType.INFORMATION,"Success","Person updated Successfully!");
        } catch (SQLException | IllegalArgumentException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }
    }
    private Entry getEntry() throws IllegalArgumentException{
        String gender=null;
        if (maleRadioButton.isSelected() || femaleRadioButton.isSelected()) {
            gender = (maleRadioButton.isSelected() ? "Male": "Female");
        }
        validateStudentFields(nameTextField.getText(),emailTextField.getText(),numberTextField.getText(),ageTextField.getText(),gender);
        if(studentsChoiceBox.getSelectionModel().getSelectedItem() instanceof Student){
            Student student= new Student(nameTextField.getText(),Integer.parseInt(ageTextField.getText()),gender,emailTextField.getText(),numberTextField.getText());
            student.setId(studentsChoiceBox.getSelectionModel().getSelectedItem().getId());
            return student;
        }
        else{
            Instructor instructor= new Instructor(nameTextField.getText(),Integer.parseInt(ageTextField.getText()),gender,emailTextField.getText(),numberTextField.getText());
            instructor.setId(studentsChoiceBox.getSelectionModel().getSelectedItem().getId());
            return instructor;
        }
    }
    @FXML
    private void create(){
        SceneManager.switchToDataEntry();
    }
    @FXML
    private void clearAssignedCourses(){
        try{
            Entry entry= getEntry();
            Database.deleteAssignedCourses(entry,Database.getEntryAssignedCourses(entry));
            assignedCoursesListView.getItems().clear();
        } catch (SQLException | IllegalArgumentException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }
    }
    @FXML
    private void delete(){
        try{
            Entry entry=studentsChoiceBox.getSelectionModel().getSelectedItem();
            Database.deleteAttendance(entry,Database.getEntryAssignedCourses(entry));
            Database.deleteAssignedCourses(entry,Database.getEntryAssignedCourses(entry));
            Database.removeAssignedEntryBatch(entry);
            Database.deleteEntry(entry);
            assignedCoursesListView.getItems().clear();
            Utilities.showAlert(Alert.AlertType.INFORMATION,"Success","Person deleted Successfully!");
            refresh();
            clear();
        } catch (SQLException | IllegalArgumentException e){
            e.printStackTrace();
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }
    }

    @FXML
    private void assign(){
        ObservableList<Course> selectedItems = listView.getSelectionModel().getSelectedItems();
        ObservableList<Course> assignedItems = assignedCoursesListView.getItems();
        Set<String> assignedCourseNames = assignedItems.stream()
                .map(Course::getName)
                .collect(Collectors.toSet());
        for (Course item : selectedItems) {
            if (!assignedCourseNames.contains(item.getName())) {
                assignedItems.add(item);
                assignedCourseNames.add(item.getName()); // Add the course name to the set
            }
        }
    }
    public static void validateStudentFields(String name, String email, String phone, String ageStr, String gender) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (ageStr == null || ageStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Age cannot be empty");
        }
        try {
            int age = Integer.parseInt(ageStr);
            if (age <= 0) {
                throw new IllegalArgumentException("Age must be a positive number");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a valid number");
        }
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be empty");
        }
    }
}
