package com.ams;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import models.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class DataEntryController implements Initializable {
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
    @FXML
    private Label alertMessage;
    @FXML
    private Button editButton;
    @FXML
    private ImageView editImageView;
    @FXML
    private Button createStudentButton;
    @FXML
    private ImageView createStudentImageView;
    @FXML
    private ImageView refreshImageView;
    @FXML
    private Button removeCourseButton;
    @FXML
    private ImageView removeCourseImageView;
    @FXML
    private RadioButton instructorRadioButton;
    @FXML
    private RadioButton studentRadioButton;
    private ToggleGroup genderToggleGroup;
    private ToggleGroup roleToggleGroup;
    @FXML
    private ImageView logout;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Utilities.addHoverEffect(logout);
        background.setImage(Utilities.mainPage);
        underline.setImage(Utilities.underline);
        assignImageView.setImage(Utilities.greenButton);
        clearCoursesImageView.setImage(Utilities.redButton);
        createImageView.setImage(Utilities.blueButton);
        createStudentImageView.setImage(Utilities.greenButton);
        editImageView.setImage(Utilities.redButton);
        user.setImage(Utilities.user);
        Utilities.addDateTime(dateTimeLabel);
        username.setText(Login.getUsername());
        removeCourseImageView.setImage(Utilities.redButton);
        assignedCoursesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        genderToggleGroup= new ToggleGroup();
        roleToggleGroup=new ToggleGroup();
        studentRadioButton.setToggleGroup(roleToggleGroup);
        instructorRadioButton.setToggleGroup(roleToggleGroup);
        instructorRadioButton.setOnAction(event -> setInstructorImage());
        studentRadioButton.setOnAction(event -> setStudentImage());
        maleRadioButton.setToggleGroup(genderToggleGroup);
        femaleRadioButton.setToggleGroup(genderToggleGroup);
        Utilities.addSearchBar(listView,Database.getAllCourses(),searchCourses,Course::getName);
        Utilities.addHoverEffect(createImageView,createButton);
        Utilities.addHoverEffect(assignImageView,assignButton);
        Utilities.addHoverEffect(clearCoursesImageView,clearCoursesButton);
        Utilities.addHoverEffect(createStudentImageView,createStudentButton);
        Utilities.addHoverEffect(editImageView,editButton);
        refreshImageView.setImage(Utilities.refreshIcon);
        Utilities.addHoverEffect(refreshImageView);
    }
    @FXML
    private void logout(){
        SceneManager.switchToLogin();
    }
    @FXML
    private void setStudentImage(){
        System.out.println("student image selected");
        user.setImage(Utilities.user);
    }
    @FXML
    private void switchToAttendance(){
        SceneManager.switchToAttendance();
    }
    @FXML
    private void setInstructorImage(){
        user.setImage(Utilities.instructor);
        System.out.println("instructor image selected");
    }
    @FXML
    private void refresh(){
        Utilities.addSearchBar(listView,Database.getAllCourses(),searchCourses,Course::getName);
    }

    @FXML
    private void switchToMain(){
        SceneManager.switchToMainPage();
    }
    @FXML
    private void switchToCourses(){
        SceneManager.switchToCourses();
    }
    private void clear(){
        nameTextField.setText("");
        emailTextField.setText("");
        numberTextField.setText("");
        ageTextField.setText("");
        assignedCoursesListView.getItems().clear();
        roleToggleGroup.selectToggle(null);
        genderToggleGroup.selectToggle(null);
    }
    @FXML
    private void submit(){
        try{
            Entry entry=getEntry();
            Database.insertEntry(entry);
            entry.setId(Database.getEntryID(entry));
            Database.assignCourses(entry,assignedCoursesListView.getItems());
            Utilities.showAlert(Alert.AlertType.INFORMATION,"Success","Entry inserted successfully!");
            clear();
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
        if(studentRadioButton.isSelected()){
            Student student=new Student(nameTextField.getText(),Integer.parseInt(ageTextField.getText()),gender,emailTextField.getText(),numberTextField.getText());
            student.setRegisterDate(LocalDate.now());
            return student;
        }
        else{
            if(instructorRadioButton.isSelected()){
                Instructor instructor=new Instructor(nameTextField.getText(),Integer.parseInt(ageTextField.getText()),gender,emailTextField.getText(),numberTextField.getText());
                instructor.setRegisterDate(LocalDate.now());
                return instructor;
            }
        }
        throw new IllegalArgumentException("Select entry role: Student or Instructor?");
    }
    @FXML
    private void edit(){
        SceneManager.switchToEditStudents();
    }
    @FXML
    private void clearAssignedCourses(){
        assignedCoursesListView.getItems().clear();
    }
    @FXML
    private void removeSelectedCourses(){
        assignedCoursesListView.getItems().removeAll(assignedCoursesListView.getSelectionModel().getSelectedItems());
    }
    @FXML
    private void assign(){
        for(int i=0;i<listView.getSelectionModel().getSelectedItems().size();i++){
            if(!assignedCoursesListView.getItems().contains(listView.getSelectionModel().getSelectedItems().get(i))){
                assignedCoursesListView.getItems().add(listView.getSelectionModel().getSelectedItems().get(i));
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
