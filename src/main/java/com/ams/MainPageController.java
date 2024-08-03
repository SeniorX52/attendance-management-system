package com.ams;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import models.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {
    @FXML
    private Label dateTimeLabel;
    @FXML
    private ImageView background;
    @FXML
    private ImageView underline;
    @FXML
    private Label username;
    @FXML
    private Label students;
    @FXML
    private Label instructors;
    @FXML
    private Label courses;
    @FXML
    private Label batches;
    @FXML
    private TableView<Session> todaySessions;
    @FXML
    private TableColumn<Session,String> courseNameColumn;
    @FXML
    private TableColumn<Session,String> sessionNameColumn;
    @FXML
    private TableColumn<Session, String> sessionTime;
    @FXML
    private ImageView refreshImageView;
    @FXML
    private ImageView logout;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Utilities.addHoverEffect(logout);
        background.setImage(Utilities.mainPage);
        underline.setImage(Utilities.underline);
        Utilities.addDateTime(dateTimeLabel);
        Utilities.addHoverEffect(refreshImageView);
        username.setText(Login.getUsername());
        students.setText(   "Students registered this month: "+studentsRegisteredMonthly()+", total: "+Database.getAllStudents().size());
        instructors.setText("Total number of instructors: "+Database.getAllInstructors().size());
        courses.setText(    "Total number of courses: "+Database.getAllCourses().size());
        batches.setText("Total number of batches: "+Database.getAllBatches().size());
        todaySessions.getItems().setAll(getTodaysSessions());
        todaySessions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        courseNameColumn.setCellFactory(column -> new TableCell<Session,String>() {
            @Override
            protected void updateItem(String item,boolean empty){
                super.updateItem(item, empty);
                if(empty || getTableRow()==null || getTableRow().getItem()==null){
                    setText(null);
                }
                else{
                    Optional<Integer> courseID=Database.getAllBatches().stream().filter(batch -> batch.getId()==getTableRow().getItem().getBatch_id()).map(Batch::getCourse_id).findFirst();
                    Course course=courseID.map(Database::getCourse).orElse(null);
                    if(course!=null){
                        setText(course.getName());
                    }
                    else {
                        setText(null);
                    }
                }

            }
        });
        sessionNameColumn.setCellFactory(column ->new TableCell<>(){
            @Override
            protected void updateItem(String item,boolean empty){
                super.updateItem(item, empty);
                if(empty || getTableRow()==null || getTableRow().getItem()==null){
                    setText(null);
                }
                else{
                    setText(getTableRow().getItem().getName());
                }
            }
        });
        sessionTime.setCellFactory(column -> new TableCell<>(){
            @Override
            protected void updateItem(String item,boolean empty){
                super.updateItem(item,empty);
                if(empty || getTableRow()==null || getTableRow().getItem()==null){
                    setText(null);
                }
                else{
                    setText(getTableRow().getItem().getTime().toString());
                }
            }
        });
    }
    @FXML
    private void logout(){
        SceneManager.switchToLogin();
    }
    private List<Session> getTodaysSessions(){
        LocalDate today=LocalDate.now();
        List<Session> list=new ArrayList<>();
        List<Session> sessions=Database.getAllSessions();
        for(Session session:sessions){
            if(session.getDate().equals(today)){
                list.add(session);
            }
        }
        return list;
    }
    @FXML
    private void refresh(){
        students.setText(   "Students registered this month: "+studentsRegisteredMonthly()+", total: "+Database.getAllStudents().size());
        instructors.setText("Total number of instructors: "+Database.getAllInstructors().size());
        courses.setText(    "Total number of courses: "+Database.getAllCourses().size());
        batches.setText("Total number of batches: "+Database.getAllBatches().size());
        todaySessions.getItems().setAll(getTodaysSessions());
    }
    @FXML
    private void goToAttendance(){
        Session session=todaySessions.getSelectionModel().getSelectedItem();
        if(session==null){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error","Choose a session!");
            return;
        }
        SceneManager.switchToAttendance(session);
    }
    private int studentsRegisteredMonthly(){
        List<Student> students=Database.getAllStudents();
        int count=0;

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        Month currentMonth = today.getMonth();
        LocalDate startOfMonth = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        for(Student student:students){
            LocalDate dateToCheck=student.getRegisterDate();
            if (!dateToCheck.isBefore(startOfMonth) && !dateToCheck.isAfter(endOfMonth)) {
                count++;
            }
        }
        return count;
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
    private void switchToAttendance(){
        SceneManager.switchToAttendance();
    }
}
