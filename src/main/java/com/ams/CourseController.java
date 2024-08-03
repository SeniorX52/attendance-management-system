package com.ams;

import exceptions.InvalidDataException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import models.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CourseController implements Initializable {
    @FXML
    private TextField courseNameTextField;
    @FXML
    private TextField noSessionsTextField;
    @FXML
    private TextField noStudentsTextField;
    @FXML
    private TextField courseSearchTextField;
    @FXML
    private Label dateTimeLabel;
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
    private Button createBatchButton;
    @FXML
    private ImageView createBatchImageViewer;
    @FXML
    private Button createCourseButton;
    @FXML
    private ImageView createCourseImageViewer;
    @FXML
    private Button editButton;
    @FXML
    private ImageView editImageView;
    @FXML
    private ImageView refreshImageView;
    @FXML
    private ChoiceBox<Course> courseChoiceBox;
    @FXML
    private ImageView book;
    @FXML
    private ImageView group;
    @FXML
    private TableView<Session> tableView;
    @FXML
    private TableColumn<Session, String> sessionsName;

    @FXML
    private TableColumn<Session, LocalDate> sessionsDate;

    @FXML
    private TableColumn<Session, LocalTime> sessionsTime;
    @FXML
    private ChoiceBox<Instructor> instructorChoiceBox;
    @FXML
    private ChoiceBox<Course> courseSessionsChoiceBox;
    @FXML
    private ChoiceBox<Batch> batchChoiceBox;
    @FXML
    private TextField courseSessionsTextField;
    @FXML
    private TextField batchTextField;
    @FXML
    private ImageView confirmSessionsImageView;
    @FXML
    private ImageView showCalendarImageView;
    @FXML
    private Button confirmSessionsButton;
    @FXML
    private Button showCalendarButton;
    @FXML
    private TextField instructorSearchTextField;
    @FXML
    private ImageView deleteImageView;
    @FXML
    private Button deleteButton;
    @FXML
    private ImageView exportImageView;
    @FXML
    private Button exportButton;
    @FXML
    private ImageView deleteCourseImageView;
    @FXML
    private Button deleteCourseButton;
    @FXML
    private ImageView logout;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Utilities.addHoverEffect(logout);
        background.setImage(Utilities.mainPage);
        underline.setImage(Utilities.underline);
        book.setImage(Utilities.book);
        deleteCourseImageView.setImage(Utilities.redButton);
        Utilities.addHoverEffect(deleteCourseImageView,deleteCourseButton);
        exportImageView.setImage(Utilities.blueButton);
        deleteImageView.setImage(Utilities.redButton);
        createCourseImageViewer.setImage(Utilities.greenButton);
        refreshImageView.setImage(Utilities.refreshIcon);
        group.setImage(Utilities.group);
        createBatchImageViewer.setImage(Utilities.blueButton);
        createImageView.setImage(Utilities.greenButton);
        editImageView.setImage(Utilities.redButton);
        showCalendarImageView.setImage(Utilities.greenButton);
        confirmSessionsImageView.setImage(Utilities.blueButton);
        username.setText(Login.getUsername());
        Utilities.addDateTime(dateTimeLabel);
        Utilities.addSearchBar(courseChoiceBox, Database.getAllCourses(),courseSearchTextField);
        Utilities.addHoverEffect(createImageView,createButton);
        Utilities.addHoverEffect(createBatchImageViewer,createBatchButton);
        Utilities.addHoverEffect(editImageView,editButton);
        Utilities.addHoverEffect(createCourseImageViewer,createCourseButton);
        sessionsName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        sessionsName.setCellFactory(TextFieldTableCell.forTableColumn());
        sessionsDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        Utilities.addHoverEffect(confirmSessionsImageView,confirmSessionsButton);
        Utilities.addHoverEffect(showCalendarImageView,showCalendarButton);
        Utilities.addSearchBar(courseSessionsChoiceBox,Database.getAllCourses(),courseSessionsTextField);
        Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(),batchTextField);
        Utilities.addSearchBar(instructorChoiceBox,Database.getAllInstructors(),instructorSearchTextField);
        Utilities.addHoverEffect(deleteImageView,deleteButton);
        Utilities.addHoverEffect(exportImageView,exportButton);
        courseChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Utilities.addSearchBar(instructorChoiceBox,Database.getAllInstructors(newValue),instructorSearchTextField);
            }
        });
        courseSessionsChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue, course, t1) -> {
            if (t1 != null) {
                Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(t1),batchTextField);
            }
        }));
        sessionsDate.setCellFactory(column -> {
            TableCell<Session, LocalDate> cell = new TableCell<>() {
                private final DatePicker datePicker = new DatePicker();
                {
                    datePicker.setOnAction(e -> commitEdit(datePicker.getValue()));
                    datePicker.setConverter(new StringConverter<>() {
                        @Override
                        public String toString(LocalDate date) {
                            return date != null ? date.toString() : "";
                        }

                        @Override
                        public LocalDate fromString(String string) {
                            return LocalDate.parse(string);
                        }
                    });
                }

                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setGraphic(null);
                    } else {
                        datePicker.setValue(date);
                        setGraphic(datePicker);
                    }
                }

                @Override
                public void commitEdit(LocalDate newValue) {
                    super.commitEdit(newValue);
                    getTableView().getItems().get(getIndex()).setDate(newValue);
                }
            };
            return cell;
        });
        Utilities.addSearchBar(deleteCourseChoiceBox,Database.getAllCourses(),deleteCourseSearch);

        // Configure sessionsTime column
        sessionsTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        sessionsTime.setCellFactory(column -> {
            TableCell<Session, LocalTime> cell = new TableCell<Session, LocalTime>() {
                private final TextField timeField = new TextField();
                private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                {
                    timeField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            try {
                                commitEdit(LocalTime.parse(timeField.getText(), timeFormatter));
                            } catch (DateTimeParseException exception) {
                                Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Time format should be in HH:MM in 24Hr format");
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(LocalTime time, boolean empty) {
                    super.updateItem(time, empty);
                    if (empty || time == null) {
                        setGraphic(null);
                    } else {
                        timeField.setText(time.format(timeFormatter));
                        setGraphic(timeField);
                    }
                }

                @Override
                public void commitEdit(LocalTime newValue) {
                    super.commitEdit(newValue);
                    getTableView().getItems().get(getIndex()).setTime(newValue);
                }
            };
            return cell;
        });
        batchChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observableValue, batch, t1) -> {
            if(t1!=null){
                tableView.getItems().setAll(Database.getAllSessions(t1));
            }
            else{
                tableView.getItems().setAll(new ArrayList<>());
            }

        }));
        Utilities.addHoverEffect(refreshImageView);
    }
    @FXML
    private void logout(){
        SceneManager.switchToLogin();
    }
    public void refresh(){
        courseSearchTextField.setText("");
        courseNameTextField.setText("");
        courseSessionsTextField.setText("");
        instructorSearchTextField.setText("");
        deleteCourseSearch.setText("");
        noStudentsTextField.setText("");
        noSessionsTextField.setText("");
        batchTextField.setText("");

        Utilities.addSearchBar(courseChoiceBox, Database.getAllCourses(),courseSearchTextField);
        Utilities.addSearchBar(courseSessionsChoiceBox,Database.getAllCourses(),courseSessionsTextField);
        Utilities.addSearchBar(batchChoiceBox,Database.getAllBatches(),courseSessionsTextField);
        Utilities.addSearchBar(instructorChoiceBox,Database.getAllInstructors(),instructorSearchTextField);
        Utilities.addSearchBar(deleteCourseChoiceBox,Database.getAllCourses(),deleteCourseSearch);
    }
    public void createCourse(){
        try{
            if(courseNameTextField.getText().isEmpty()){
                throw new InvalidDataException("Course name cannot be empty!");
            }
            Database.insertCourse(new Course(courseNameTextField.getText()));
            courseNameTextField.setText("");
            Utilities.showAlert(Alert.AlertType.INFORMATION,"Success","Course created successfully!");
            refresh();
        }
        catch (SQLException | InvalidDataException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }
    }
    public String getBatchName(Batch batch){
        String name = null;
        name="%s-B%d-%d".formatted(Database.getCourse(batch.getCourse_id()).getName(),batch.getId(),LocalDate.now().getYear());
        return name;
    }
    @FXML
    public void createBatch(){
        try{
            validateBatchData();
            Batch batch=new Batch(courseChoiceBox.getSelectionModel().getSelectedItem().getName(),instructorChoiceBox.getSelectionModel().getSelectedItem().getId(),
                    courseChoiceBox.getSelectionModel().getSelectedItem().getId(),Integer.parseInt(noSessionsTextField.getText()),
                    Integer.parseInt(noStudentsTextField.getText()));
            Database.insertBatch(batch);
            batch.setId(Database.getBatchID(batch));
            String name=getBatchName(batch);
            batch.setName(name);
            Database.updateBatch(batch);
            Database.insertInstructorForBatch(instructorChoiceBox.getSelectionModel().getSelectedItem(),batch);
            List<Session> sessions = new ArrayList<>();
            for(int i=0;i<batch.getNoSessions();i++){
                Session session=new Session(batch.getName()+ " Session "+(i+1), LocalDate.now().plusDays(i+1), LocalTime.of(10, 0));
                session.setBatch_id(batch.getId());
                Database.insertSession(session);
                session.setId(Database.getSessionId(session));
                Database.insertAttendance(instructorChoiceBox.getSelectionModel().getSelectedItem(),session);
                sessions.add(session);
            }
            Utilities.addSearchBar(deleteCourseChoiceBox,Database.getAllCourses(),deleteCourseSearch);
            tableView.getItems().setAll(sessions);
            Utilities.showAlert(Alert.AlertType.INFORMATION,"Success","Batch created successfully");
        }
        catch (SQLException | InvalidDataException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }

    }
    public void validateBatchData() throws InvalidDataException {
        if (noStudentsTextField.getText().isEmpty()) {
            throw new InvalidDataException("Number of students can't be empty");
        }
        if (noSessionsTextField.getText().isEmpty()) {
            throw new InvalidDataException("Number of sessions can't be empty");
        }
        if(courseChoiceBox.getSelectionModel().getSelectedItem()==null){
            throw new InvalidDataException("Choose a course for the batch");
        }
        try {
            if (Integer.parseInt(noSessionsTextField.getText().trim()) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Sessions number must be a positive number, not a character");
        }
        try {
            if (Integer.parseInt(noStudentsTextField.getText().trim()) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Students number must be a positive number, not a character");
        }
    }
    @FXML
    private void switchToMain(){
        SceneManager.switchToMainPage();
    }
    @FXML
    private void switchToStudents(){
        SceneManager.switchToDataEntry();
    }
    @FXML
    private void switchToAttendance(){
        SceneManager.switchToAttendance();
    }
    @FXML
    private void confirmSessions(){
        try{
            List<Session> sessions=tableView.getItems();
            if(sessions.isEmpty()){
                Utilities.showAlert(Alert.AlertType.ERROR,"Error","No sessions selected, please choose a batch to update sessions.");
                return;
            }
            for(Session session:sessions){
                Database.updateSession(session);
            }
            refresh();
            Utilities.showAlert(Alert.AlertType.INFORMATION,"Success","Sessions updated successfully");
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void assign(){
        SceneManager.switchToAssignBatch();
    }
    @FXML
    private void showSchedule(){

    }
    @FXML
    private void deleteBatch(){
        if(batchChoiceBox.getSelectionModel().getSelectedItem()!=null){
            Database.deleteBatch(batchChoiceBox.getSelectionModel().getSelectedItem());
            refresh();
            Utilities.showAlert(Alert.AlertType.INFORMATION,"Success","Batch deleted successfully!");
        }
        else {
            Utilities.showAlert(Alert.AlertType.ERROR,"Error","Choose a batch to delete!");
        }
    }
    @FXML
    private void exportExcel(){
        try{
            Database.exportToExcel();
        }
        catch (Exception e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private ChoiceBox<Course> deleteCourseChoiceBox;
    @FXML
    private TextField deleteCourseSearch;
    @FXML
    private void deleteCourse(){
        try{
            if(deleteCourseChoiceBox.getSelectionModel().getSelectedItem()!=null){
                Database.deleteCourse(deleteCourseChoiceBox.getSelectionModel().getSelectedItem());
                refresh();
                Utilities.showAlert(Alert.AlertType.INFORMATION,"Success","Course deleted successfully!");
            }
            else {
                Utilities.showAlert(Alert.AlertType.ERROR,"Error","Choose a course to delete");
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }

    }
}
