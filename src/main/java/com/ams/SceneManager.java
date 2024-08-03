package com.ams;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Session;

import java.io.IOException;
import java.util.Objects;

public class SceneManager {
    public static Stage primaryStage = null;
    private static Scene loginScene;


    public static void switchToLogin(){
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource("Login.fxml")));
            Parent rootLogin = loader.load();
            loginScene = new Scene(rootLogin);
        } catch (IOException e){
            e.printStackTrace();
        }
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }
    private static Scene mainPageScene;
    public static void switchToMainPage(){
        if(mainPageScene==null){
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource("MainPage.fxml")));
                Parent root = loader.load();
               mainPageScene = new Scene(root);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        primaryStage.setScene(mainPageScene);
        primaryStage.show();
    }
    private static Scene studentsScene;
    public static void switchToDataEntry(){
        if(studentsScene==null){
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource("DataEntry.fxml")));
                Parent root = loader.load();
                studentsScene = new Scene(root);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        primaryStage.setScene(studentsScene);
        primaryStage.show();
    }
    private static Scene editStudentScene;
    public static void switchToEditStudents(){
        if(editStudentScene==null){
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource("DataEntryEdit.fxml")));
                Parent root = loader.load();
                editStudentScene = new Scene(root);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        primaryStage.setScene(editStudentScene);
        primaryStage.show();
    }
    private static Scene coursesScene;
    public static void switchToCourses(){
        if(coursesScene==null){
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource("Courses.fxml")));
                Parent root = loader.load();
                coursesScene = new Scene(root);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        primaryStage.setScene(coursesScene);
        primaryStage.show();
    }
    private static Scene assignBatch;
    public static void switchToAssignBatch(){
        if(assignBatch==null){
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource("AssignBatch.fxml")));
                Parent root = loader.load();
                assignBatch = new Scene(root);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        primaryStage.setScene(assignBatch);
        primaryStage.show();
    }
    private static Scene attendance;
    public static void switchToAttendance(){
        if(attendance==null){
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource("Attendance.fxml")));
                Parent root = loader.load();
                attendance = new Scene(root);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        primaryStage.setScene(attendance);
        primaryStage.show();
    }
    private static Attendance attendanceController;
    public static void switchToAttendance(Session session){
        if(attendance==null || attendanceController==null){
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SceneManager.class.getResource("Attendance.fxml")));
                Parent root = loader.load();
                attendanceController=loader.getController();
                attendance = new Scene(root);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        attendanceController.refresh();
        attendanceController.setSession(session);
        primaryStage.setScene(attendance);
        primaryStage.show();
    }

}
