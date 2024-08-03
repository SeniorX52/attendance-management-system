package models;

import com.ams.Utilities;
import com.mysql.cj.jdbc.MysqlDataSource;
import javafx.scene.control.Alert;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Properties;


public class Database {
    private static String port="3306";
    private static String serverName = "sql7.freesqldatabase.com";
    private static String url = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7723338";
    private static String dbName="sql7723338";
    private static MysqlDataSource dataSource;
    private static String username;
    private static String password;
    private  static Properties properties = new Properties();
    public static void initDB() throws Exception{
        loadProperties();
        String serverName = properties.getProperty("serverName");
        String dbName = properties.getProperty("databaseName");
        String url = properties.getProperty("url");
        String portStr = properties.getProperty("port");
        if (serverName == null || serverName.isEmpty()) {
            throw new IllegalArgumentException("Server name cannot be empty.");
        }
        if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be empty.");
        }
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty.");
        }
        if (portStr == null || portStr.isEmpty()) {
            throw new IllegalArgumentException("Port cannot be empty.");
        }
        int port=Integer.parseInt(portStr);
        dataSource = new MysqlDataSource();
        dataSource.setServerName(serverName);
        dataSource.setPort(port);
        dataSource.setDatabaseName(dbName);
    }
    private static void loadProperties() {
        String installDir = System.getProperty("user.dir");
        String configFilePath = installDir + File.separator + "database.properties";

        properties = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        }
        catch (IOException e) {
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
    }
    public static Connection getConnection(String username, String password) throws SQLException {
        Database.username=username;
        Database.password=password;
        return dataSource.getConnection(username,password);
    }
    public static void exportToExcel() throws Exception {
        Connection connection = DriverManager.getConnection(url, username, password);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);
        String outputFile = null;
        if (file != null) {
            outputFile = file.getAbsolutePath();
        } else {
            connection.close();
            return;
        }

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = "ams";
            ResultSet rs = metaData.getTables(null, schema, null, new String[]{"TABLE"});
            List<String> tableNames = new ArrayList<>(List.of("courses","students","instructors"));
            XSSFWorkbook workbook = new XSSFWorkbook();
            for (String tableName : tableNames) {
                ResultSet tableData=null;
                tableData = connection.createStatement().executeQuery("SELECT * FROM " + schema + "." + tableName);
                Sheet sheet = workbook.createSheet(tableName);
                ResultSetMetaData metaData2 = tableData.getMetaData();
                int columnCount = metaData2.getColumnCount();

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < columnCount; i++) {
                    headerRow.createCell(i).setCellValue(metaData2.getColumnName(i + 1));
                }

                int rowNum = 1;
                while (tableData.next()) {
                    Row row = sheet.createRow(rowNum++);
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = tableData.getObject(i);
                        Cell cell = row.createCell(i - 1);
                        if (value instanceof Integer || value instanceof Long) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Double || value instanceof Float) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else {
                            cell.setCellValue(String.valueOf(value));
                        }
                    }
                }
            }
            List<Batch> batches=Database.getAllBatches();
            for(Batch batch:batches){
                String query="SELECT students.id,students.name,students.phone,students.email,sessions.name,sessions.date_time,students_attendances.status FROM students_attendances " +
                        "JOIN students ON students_attendances.student_id= students.id " +
                        "JOIN sessions ON students_attendances.session_id= sessions.id " +
                        "WHERE sessions.batch_id = ?";
                PreparedStatement ps=connection.prepareStatement(query);
                ps.setInt(1,batch.getId());
                ResultSet resultSet= ps.executeQuery();
                Sheet sheet = workbook.createSheet(batch.getName());
                ResultSetMetaData metaData2 = resultSet.getMetaData();
                int columnCount = metaData2.getColumnCount();

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < columnCount; i++) {
                    headerRow.createCell(i).setCellValue(metaData2.getColumnName(i + 1));
                }

                int rowNum = 1;
                while (resultSet.next()) {
                    Row row = sheet.createRow(rowNum++);
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = resultSet.getObject(i);
                        Cell cell = row.createCell(i - 1);
                        if (value instanceof Integer || value instanceof Long) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Double || value instanceof Float) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else {
                            cell.setCellValue(String.valueOf(value));
                        }
                    }
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                workbook.write(outputStream);
            }
        } finally {
            connection.close();
        }
    }
    public static List<Session> getAllSessions(Entry entry,Batch batch){
        String query="SELECT * FROM sessions " +
                "JOIN students_attendances ON sessions.id = students_attendances.session_id " +
                "WHERE batch_id = ? AND student_id = ?";
        List<Session> sessions=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setInt(1,batch.getId());
            ps.setInt(2,entry.getId());
            try(ResultSet resultSet=ps.executeQuery()){
                while(resultSet.next()){
                    Timestamp timestamp = resultSet.getTimestamp("date_time");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    LocalDate date = dateTime.toLocalDate();
                    LocalTime time = dateTime.toLocalTime();
                    Session session=new Session(resultSet.getString("name"),date,time);
                    session.setBatch_id(batch.getId());
                    session.setId(resultSet.getInt("id"));
                    sessions.add(session);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }
    public static void insertCourse(Course course) throws SQLException{
        if(isCourseExist(course.getName())){
            throw new SQLException("Course already exists!");
        }
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement("INSERT INTO courses(name) VALUES (?)")){
            ps.setString(1,course.getName());
            ps.executeUpdate();
        }
    }
    private static boolean isCourseExist(String name) throws SQLException {
        String query = "SELECT COUNT(*) FROM courses WHERE name = ?";
        try ( Connection connection=dataSource.getConnection(username,password);PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    public static void insertEntry(Entry entry) throws SQLException{
        if(isEntryExist(entry.getEmail())){
            throw new SQLException("Duplicated person!");
        }
        String query;
        if(entry instanceof Student){
            query = "INSERT INTO students(name,email,phone,age,gender,register_date) VALUES (?,?,?,?,?,?)";
        }
        else{
            query = "INSERT INTO instructors(name,email,phone,age,gender,register_date) VALUES (?,?,?,?,?,?)";
        }
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps= connection.prepareStatement(query)){
            ps.setString(1, entry.getName());
            ps.setString(2, entry.getEmail());
            ps.setString(3, entry.getPhoneNumber());
            ps.setInt(4,    entry.getAge());
            ps.setString(5, entry.getGender());
            ps.setDate(6,entry.getSQLDate());
            ps.execute();
        }
        System.out.println("Person inserted Successfully");
    }
    public static List<Entry> getAllEntries(){
        List<Entry> list=new ArrayList<>();
        list.addAll(getAllStudents());
        list.addAll(getAllInstructors());
        return list;
    }
    public static void deleteCourse(Course course) throws SQLException {
        for(Entry entry:Database.getAllEntries()){
            removeAssignedEntryBatch(entry,course);
            List<Course> courses=new ArrayList<>();
            courses.add(course);
            deleteAssignedCourses(entry,courses);
        }
        for(Batch batch:Database.getAllBatches(course)){
            deleteBatch(batch);
        }
        String query ="DELETE FROM courses WHERE id=? ";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query)){
            ps.setInt(1,course.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void removeAssignedEntryBatch(Entry entry, Batch batch){
        String query;
        if(entry instanceof Student){
            query ="DELETE FROM students_batches WHERE student_id=? AND batch_id=?";
        }
        else{
            query ="DELETE FROM instructors_batches WHERE instructor_id=? AND batch_id=?";
        }
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query)){
            ps.setInt(1,entry.getId());
            ps.setInt(2,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void removeAssignedEntryBatch(Entry entry){
        String query;
        if(entry instanceof Student){
            query ="DELETE FROM students_batches WHERE student_id=?";
        }
        else{
            query ="DELETE FROM instructors_batches WHERE instructor_id=?";
        }
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query)){
            ps.setInt(1,entry.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void insertInstructorForBatch(Instructor instructor,Batch batch){
        String query ="INSERT INTO instructors_batches(instructor_id,batch_id) VALUES (?,?)";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query)){
            ps.setInt(1,instructor.getId());
            ps.setInt(2,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static Instructor getInstructor(Batch batch){
        Instructor instructor = null;
        String query = "SELECT * FROM instructors " +
                "JOIN instructors_batches ON instructors.id=instructors_batches.instructor_id " +
                "WHERE batch_id=?";
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1,batch.getId());
            try(ResultSet resultSet=ps.executeQuery()){
                if(resultSet.next()){
                            instructor = new Instructor(resultSet.getString("name"),
                            resultSet.getInt("age"),
                            resultSet.getString("gender"),
                            resultSet.getString("email"),
                            resultSet.getString("phone"));
                    instructor.setId(resultSet.getInt("id"));
                    instructor.setSQLDate(resultSet.getDate("register_date"));
                }
                else{
                    System.out.println("No instructor found for this batch!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instructor;
    }
    public static void removeAssignedEntryBatch(Entry entry, Course course){
        String query;
        if(entry instanceof Student){
            query ="DELETE students_batches FROM students_batches " +
                    "JOIN batches ON students_batches.batch_id=batches.id " +
                    "WHERE students_batches.student_id=? AND batches.course_id=?";
        }
        else{
            query ="DELETE instructors_batches FROM instructors_batches " +
                    "JOIN batches ON instructors_batches.batch_id=batches.id " +
                    "WHERE instructors_batches.instructor_id=? AND batches.course_id=?";
        }
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query)){
            ps.setInt(1,entry.getId());
            ps.setInt(2,course.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void deleteBatch(Batch batch){
        String query ="DELETE FROM students_batches WHERE batch_id=?";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query)){
            ps.setInt(1,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        String query2 ="DELETE FROM instructors_batches WHERE batch_id=?";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query2)){
            ps.setInt(1,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        String query3 ="DELETE students_attendances FROM students_attendances " +
                "JOIN sessions ON students_attendances.session_id=sessions.id " +
                "WHERE sessions.batch_id=?";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query3)){
            ps.setInt(1,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        String query4 ="DELETE instructors_attendances FROM instructors_attendances " +
                "JOIN sessions ON instructors_attendances.session_id=sessions.id " +
                "WHERE sessions.batch_id=?";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query4)){
            ps.setInt(1,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        deleteSessions(batch);
        String query5 ="DELETE FROM batches WHERE id=? ";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query5)){
            ps.setInt(1,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static List<Batch> getAllBatches(Entry entry){
        String query;
        if(entry instanceof Student){
            query="SELECT * FROM batches " +
                    "JOIN students_courses ON batches.course_id=students_courses.course_id " +
                    "WHERE students_courses.student_id = ?";
        }
        else{
            query="SELECT * FROM batches " +
                    "JOIN instructors_courses ON batches.course_id=instructors_courses.course_id " +
                    "WHERE instructors_courses.instructor_id = ?";
        }
        List<Batch> batches=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setInt(1,entry.getId());
            try(ResultSet resultSet= ps.executeQuery()){
                while(resultSet.next()){
                    Batch batch=new Batch(  resultSet.getString("name"),
                            resultSet.getInt("instructor_id"),
                            resultSet.getInt("course_id"),
                            resultSet.getInt("sessions_number"),
                            resultSet.getInt("students_number"));
                    batch.setId(resultSet.getInt("id"));
                    batches.add(batch);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return batches;
    }
    public static void deleteSessions(Batch batch){
        String query ="DELETE FROM sessions WHERE batch_id=?";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query)){
            ps.setInt(1,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void assignStudent(Student student, Batch batch){
        String query ="INSERT INTO students_batches(student_id,batch_id) VALUES (?,?)";
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement ps=connection.prepareStatement(query)){
            ps.setInt(1,student.getId());
            ps.setInt(2,batch.getId());
            ps.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static List<Session> getAllSessions(){
        String query="SELECT * FROM sessions";
        List<Session> sessions=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            try(ResultSet resultSet=ps.executeQuery()){
                while(resultSet.next()){
                    Timestamp timestamp = resultSet.getTimestamp("date_time");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    LocalDate date = dateTime.toLocalDate();
                    LocalTime time = dateTime.toLocalTime();
                    Session session=new Session(resultSet.getString("name"),date,time);
                    session.setBatch_id(resultSet.getInt("batch_id"));
                    session.setId(resultSet.getInt("id"));
                    sessions.add(session);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }
    public static List<Session> getAllSessions(Course course){
        String query="SELECT * FROM sessions " +
                "JOIN batches ON sessions.batch_id = batches.id " +
                "WHERE batches.course_id = ?";
        List<Session> sessions=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setInt(1,course.getId());
            try(ResultSet resultSet=ps.executeQuery()){
                while(resultSet.next()){
                    Timestamp timestamp = resultSet.getTimestamp("date_time");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    LocalDate date = dateTime.toLocalDate();
                    LocalTime time = dateTime.toLocalTime();
                    Session session=new Session(resultSet.getString("name"),date,time);
                    session.setBatch_id(resultSet.getInt("batch_id"));
                    session.setId(resultSet.getInt("id"));
                    sessions.add(session);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }
    public static List<Session> getAllSessions(LocalDate localDate,Batch batch){
        String query="SELECT * FROM sessions " +
                "WHERE CAST(date_time AS DATE) = ? AND batch_id = ?";
        List<Session> sessions=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setDate(1, Date.valueOf(localDate));
            ps.setInt(2,batch.getId());
            try(ResultSet resultSet=ps.executeQuery()){
                while(resultSet.next()){
                    Timestamp timestamp = resultSet.getTimestamp("date_time");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    LocalDate date = dateTime.toLocalDate();
                    LocalTime time = dateTime.toLocalTime();
                    Session session=new Session(resultSet.getString("name"),date,time);
                    session.setBatch_id(resultSet.getInt("batch_id"));
                    session.setId(resultSet.getInt("id"));
                    sessions.add(session);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }
    public static List<Session> getAllSessions(LocalDate localDate){
        String query="SELECT * FROM sessions " +
                "WHERE CAST(date_time AS DATE) = ?";
        List<Session> sessions=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setDate(1, Date.valueOf(localDate));
            try(ResultSet resultSet=ps.executeQuery()){
                while(resultSet.next()){
                    Timestamp timestamp = resultSet.getTimestamp("date_time");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    LocalDate date = dateTime.toLocalDate();
                    LocalTime time = dateTime.toLocalTime();
                    Session session=new Session(resultSet.getString("name"),date,time);
                    session.setBatch_id(resultSet.getInt("batch_id"));
                    session.setId(resultSet.getInt("id"));
                    sessions.add(session);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }
    public static List<Session> getAllSessions(LocalDate localDate, Course course){
        String query="SELECT * FROM sessions " +
                "JOIN batches ON sessions.batch_id = batches.id " +
                "WHERE CAST(date_time AS DATE) = ? AND batches.course_id =?";
        List<Session> sessions=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setDate(1, Date.valueOf(localDate));
            ps.setInt(2, course.getId());
            try(ResultSet resultSet=ps.executeQuery()){
                while(resultSet.next()){
                    Timestamp timestamp = resultSet.getTimestamp("date_time");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    LocalDate date = dateTime.toLocalDate();
                    LocalTime time = dateTime.toLocalTime();
                    Session session=new Session(resultSet.getString("name"),date,time);
                    session.setBatch_id(resultSet.getInt("batch_id"));
                    session.setId(resultSet.getInt("id"));
                    sessions.add(session);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }

    public static List<Student> getAllAssignedStudents(Batch batch){
        String query = "SELECT * FROM students " +
                "JOIN students_batches ON students.id = students_batches.student_id " +
                "WHERE students_batches.batch_id = ?";
        List<Student> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1,batch.getId());
            try(ResultSet resultSet = ps.executeQuery()){
                while (resultSet.next()) {
                    Student student= new Student(resultSet.getString("name"),
                            resultSet.getInt("age"),
                            resultSet.getString("gender"),
                            resultSet.getString("email"),
                            resultSet.getString("phone"));
                    student.setId(resultSet.getInt("id"));
                    student.setSQLDate(resultSet.getDate("register_date"));
                    list.add(student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // gets all students assigned to a course but still not assigned to a batch
    public static List<Student> getAllAvailableStudents(Course course){
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM students " +
                "JOIN students_courses ON students.id = students_courses.student_id " +
                "LEFT JOIN students_batches ON students_courses.student_id = students_batches.student_id " +
                "WHERE students_courses.course_id = ? AND students_batches.batch_id IS NULL";
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, course.getId());

            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student(
                            resultSet.getString("name"),
                            resultSet.getInt("age"),
                            resultSet.getString("gender"),
                            resultSet.getString("email"),
                            resultSet.getString("phone")
                    );
                    student.setId(resultSet.getInt("id"));
                    if (resultSet.getDate("register_date") != null) {
                        student.setSQLDate(resultSet.getDate("register_date"));
                    }
                    list.add(student);
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }
    public static List<Student> getAllStudents(){
        List<Student> list=new ArrayList<>();
        try{
            Connection connection=dataSource.getConnection(username,password);
            Statement statement = connection.createStatement();
            String query="SELECT * FROM students";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                Student student=new Student(resultSet.getString("name"),
                        resultSet.getInt("age"),
                        resultSet.getString("gender"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"));
                student.setId(resultSet.getInt("id"));
                if(resultSet.getDate("register_date")!=null){
                    student.setSQLDate(resultSet.getDate("register_date"));
                }
                list.add(student);
            }
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }
    public static List<Course> getAllCourses(){
        List<Course> list=new ArrayList<>();
        try{
            Connection connection=dataSource.getConnection(username,password);
            Statement statement = connection.createStatement();
            String query="SELECT * FROM courses";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                Course course=new Course(resultSet.getString("name"));
                course.setId(resultSet.getInt("id"));
                list.add(course);
            }
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }
    public static void assignCourses(Entry entry, List<Course> courses) throws SQLException {
        String query;
        if(entry instanceof Student){
            query = "INSERT INTO students_courses(student_id, course_id) VALUES (?, ?)";
        }
        else{
            query = "INSERT INTO instructors_courses(instructor_id, course_id) VALUES (?, ?)";
        }
        try (Connection connection = dataSource.getConnection(username, password)) {
            for (Course course : courses) {
                if(!isEntryAssignedCourse(entry,course)){
                    try (PreparedStatement insertStatement = connection.prepareStatement(query)) {
                        insertStatement.setInt(1, entry.getId());
                        insertStatement.setInt(2, course.getId());
                        insertStatement.executeUpdate();
                    }
                }
            }
        }
        System.out.println("Courses assigned successfully");
    }
    private static boolean isEntryAssignedCourse(Entry entry, Course course) throws SQLException {
        String query;
        if(entry instanceof Student){
            query = "SELECT COUNT(*) FROM students_courses WHERE student_id = ? AND course_id=?";
        }
        else{
            query = "SELECT COUNT(*) FROM instructors_courses WHERE instructor_id = ? AND course_id=?";
        }
        try ( Connection connection=dataSource.getConnection(username,password);PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, entry.getId());
            ps.setInt(2, course.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    private static boolean isEntryExist(String email) throws SQLException {
        String[] queries = {
                "SELECT COUNT(*) FROM students WHERE email = ?",
                "SELECT COUNT(*) FROM instructors WHERE email = ?"
        };
        try (Connection connection = dataSource.getConnection(username, password)) {
            for (String query : queries) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return true; // Return true as soon as we find a match
                        }
                    }
                }
            }
        }
        return false;
    }
    public static void deleteEntry(Entry entry) throws SQLException {
        String query;
        if(entry instanceof Student){
            query = "DELETE FROM students WHERE email = ?";
        }
        else{
            query = "DELETE FROM instructors WHERE email = ?";
        }
        try (Connection connection=dataSource.getConnection(username,password);PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, entry.getEmail());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Person deleted successfully");
            } else {
                throw new SQLException("No person found with the given data");
            }
        }
    }

    public static List<Course> getEntryAssignedCourses(Entry entry) throws SQLException {
        String query;
        if(entry instanceof Student){
            query = "SELECT * FROM courses JOIN students_courses ON courses.id = students_courses.course_id WHERE students_courses.student_id = ?;";
        }
        else{
            query = "SELECT * FROM courses JOIN instructors_courses ON courses.id = instructors_courses.course_id WHERE instructors_courses.instructor_id = ?;";
        }
        List<Course> courses = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, entry.getId());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    int courseId = resultSet.getInt("id");
                    String courseName = resultSet.getString("name");
                    Course course = new Course(courseName);
                    course.setId(courseId);
                    courses.add(course);
                }
            }
        }
        return courses;
    }
    public static List<Instructor> getAllInstructors() {
        String query = "SELECT * FROM instructors";
        List<Instructor> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet resultSet = ps.executeQuery()) {
            while (resultSet.next()) {
                Instructor instructor = new Instructor(resultSet.getString("name"),
                        resultSet.getInt("age"),
                        resultSet.getString("gender"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"));
                instructor.setId(resultSet.getInt("id"));
                if(resultSet.getDate("register_date")!=null){
                    instructor.setSQLDate(resultSet.getDate("register_date"));
                }
                list.add(instructor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public static int getEntryID(Entry entry) throws SQLException {
        String query;
        if(entry instanceof Student){
            query = "SELECT id FROM students WHERE email = ?";
        }
        else{
            query = "SELECT id FROM instructors WHERE email = ?";
        }
        if (!isEntryExist(entry.getEmail())) {
            throw new SQLException("Person not found");
        }
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, entry.getEmail());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("No ID found for entry");
                }
            }
        }
    }
    public static void updateEntry(Entry entry) throws SQLException {
        String query;
        if(entry instanceof Student){
            query = "UPDATE students SET name = ?, email = ?, phone = ?, age = ?, gender = ? WHERE id = ?";
        }
        else{
            query = "UPDATE instructors SET name = ?, email = ?, phone = ?, age = ?, gender = ? WHERE id = ?";
        }
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, entry.getName());
            ps.setString(2, entry.getEmail());
            ps.setString(3, entry.getPhoneNumber());
            ps.setInt(4, entry.getAge());
            ps.setString(5, entry.getGender());
            ps.setInt(6, entry.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Person updated successfully");
            } else {
                System.out.println("No person found with the given ID");
            }
        }
    }

    public static void deleteAssignedCourses(Entry entry,List<Course> courses) throws SQLException {
        String query;
        if(entry instanceof Student){
            query="DELETE FROM students_courses WHERE student_id=? AND course_id=?";
        }
        else{
            query="DELETE FROM instructors_courses WHERE instructor_id=? AND course_id=?";
        }
        try(Connection connection=dataSource.getConnection(username,password)){
            for(Course course:courses){
                PreparedStatement ps=connection.prepareStatement(query);
                ps.setInt(1,entry.getId());
                ps.setInt(2,course.getId());
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Assigned course deleted successfully");
                }
                else {
                    System.out.println("No person found with the associated courses");
                }
            }
        }
    }
    public static Course getCourse(int id){
        String query = "SELECT * FROM courses WHERE id=?";
        Course course=null;
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    course = new Course(resultSet.getString("name"));
                    course.setId(resultSet.getInt("id"));

                } else {
                    System.out.println("Course not found for ID: " + id);
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return course;
    }
    public static int getBatchID(Batch batch) throws SQLException {
        if(isBatchExist(batch.getName())){
            String query="SELECT id FROM batches WHERE name=?";
            try (Connection connection = dataSource.getConnection(username, password);
                 PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, batch.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    } else {
                        throw new SQLException("No ID found for batch");
                    }
                }
            }
        }
        else{
            throw new SQLException("Batch doesn't exist");
        }
    }
    public static void insertBatch(Batch batch) throws SQLException {
        if(isBatchExist(batch.getName())){
            throw new SQLException("Batch already exists!");
        }
        String query="INSERT INTO batches(name,sessions_number,students_number,course_id,instructor_id) VALUES (?,?,?,?,?)";
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setString(1,batch.getName());
            ps.setInt(2,batch.getNoSessions());
            ps.setInt(3,batch.getNoStudents());
            ps.setInt(4,batch.getCourse_id());
            ps.setInt(5,batch.getInstructor_id());
            ps.executeUpdate();
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
    }
    public static List<Batch> getAllBatches(Course course){
        String query="SELECT * FROM batches WHERE course_id = ?";
        List<Batch> batches=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setInt(1,course.getId());
            try(ResultSet resultSet= ps.executeQuery()){
                while(resultSet.next()){
                    Batch batch=new Batch(  resultSet.getString("name"),
                            resultSet.getInt("instructor_id"),
                            resultSet.getInt("course_id"),
                            resultSet.getInt("sessions_number"),
                            resultSet.getInt("students_number"));
                    batch.setId(resultSet.getInt("id"));
                    batches.add(batch);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return batches;
    }
    public static void updateBatch(Batch batch) throws SQLException {
        String query="UPDATE batches SET name = ?, instructor_id = ?, sessions_number = ?, students_number =?  WHERE id = ?";
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setString(1,batch.getName());
            ps.setInt(2,batch.getInstructor_id());
            ps.setInt(3,batch.getNoSessions());
            ps.setInt(4,batch.getNoStudents());
            ps.setInt(5,batch.getId());
            ps.executeUpdate();
        }
    }
    public static List<Batch> getAllBatches(){
        String query="SELECT * FROM batches";
        List<Batch> batches=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query);ResultSet resultSet= ps.executeQuery()){
            while(resultSet.next()){
                Batch batch=new Batch(  resultSet.getString("name"),
                                        resultSet.getInt("instructor_id"),
                                        resultSet.getInt("course_id"),
                                        resultSet.getInt("sessions_number"),
                                        resultSet.getInt("students_number"));
                batch.setId(resultSet.getInt("id"));
                batches.add(batch);
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return batches;
    }
    public static List<Student> getAllStudents(Session session){
        List<Student> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(username, password)) {
            String query = "SELECT * FROM students " +
                    "JOIN students_attendances ON students.id = students_attendances.student_id " +
                    "WHERE students_attendances.session_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1,session.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Student student = new Student(
                        resultSet.getString("name"),
                        resultSet.getInt("age"),
                        resultSet.getString("gender"),
                        resultSet.getString("email"),
                        resultSet.getString("phone")
                );
                student.setId(resultSet.getInt("id"));
                student.setSQLDate(resultSet.getDate("register_date"));
                list.add(student);
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public static Instructor getInstructor(Session session){
        Instructor instructor=null;
        try (Connection connection = dataSource.getConnection(username, password)) {
            String query = "SELECT * FROM instructors " +
                    "JOIN instructors_attendances ON instructors.id = instructors_attendances.instructor_id " +
                    "WHERE instructors_attendances.session_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1,session.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                instructor = new Instructor(
                        resultSet.getString("name"),
                        resultSet.getInt("age"),
                        resultSet.getString("gender"),
                        resultSet.getString("email"),
                        resultSet.getString("phone")
                );
                instructor.setId(resultSet.getInt("id"));
                instructor.setSQLDate(resultSet.getDate("register_date"));
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instructor;
    }
    public static boolean isEntryAttended(Entry entry, Session session){
        String query;
        if(entry instanceof Student){
            query="SELECT status FROM students_attendances WHERE student_id = ? AND session_id = ?";
        }
        else{
            query="SELECT status FROM instructors_attendances WHERE instructor_id = ? AND session_id = ?";
        }
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement preparedStatement=connection.prepareStatement(query)){
            preparedStatement.setInt(1,entry.getId());
            preparedStatement.setInt(2,session.getId());
            ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                String status=resultSet.getString("status");
                if(status.equals("Attended")){
                    return true;
                }
                else{
                    return false;
                }
            }
            resultSet.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean isEntryAttendanceExists(Entry entry,Session session){
        String query;
        if(entry instanceof Student){
            query="SELECT COUNT(*) FROM students_attendances WHERE student_id = ? AND session_id = ?";
        }
        else{
            query="SELECT COUNT(*) FROM instructors_attendances WHERE instructor_id = ? AND session_id = ?";
        }
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, entry.getId());
            preparedStatement.setInt(2, session.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        return false; // Assuming no result means no attendance
    }
    public static void insertAttendance(Entry entry,Session session){
        if(isEntryAttendanceExists(entry,session)){
            return;
        }
        String query;
        if(entry instanceof Student){
            query="INSERT INTO students_attendances(student_id,session_id) VALUES (?,?)";
        }
        else{
            query="INSERT INTO instructors_attendances(instructor_id,session_id) VALUES (?,?)";
        }
        try(Connection connection=dataSource.getConnection(username,password);PreparedStatement preparedStatement=connection.prepareStatement(query)){
            preparedStatement.setInt(1,entry.getId());
            preparedStatement.setInt(2,session.getId());
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void updateAttendance(Entry entry,Session session,boolean isAttended){
        String query;
        if(entry instanceof Student){
            query="UPDATE students_attendances SET status = ? WHERE student_id = ? AND session_id = ?";
        }
        else{
            query="UPDATE instructors_attendances SET status = ? WHERE instructor_id = ? AND session_id = ?";
        }
        String status=isAttended?"Attended":"Not Attended";
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setString(1,status);
            ps.setInt(2,entry.getId());
            ps.setInt(3,session.getId());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean isBatchExist(String name) {
        String query = "SELECT COUNT(*) FROM batches WHERE name = ?";
        try ( Connection connection=dataSource.getConnection(username,password);PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
        }
        return false;
    }
    public static void insertSession(Session session) throws SQLException {
        String query="INSERT INTO sessions(name,batch_id,date_time) VALUES (?,?,?)";
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setString(1,session.getName());
            ps.setInt(2,session.getBatch_id());
            ps.setTimestamp(3,session.getDateTime());
            ps.executeUpdate();
        }

    }
    public static List<Session> getAllSessions(Batch batch){
        String query="SELECT * FROM sessions WHERE batch_id = ?";
        List<Session> sessions=new ArrayList<>();
        try(PreparedStatement ps=  dataSource.getConnection(username,password).prepareStatement(query)){
            ps.setInt(1,batch.getId());
            try(ResultSet resultSet=ps.executeQuery()){
                while(resultSet.next()){
                    Timestamp timestamp = resultSet.getTimestamp("date_time");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    LocalDate date = dateTime.toLocalDate();
                    LocalTime time = dateTime.toLocalTime();
                    Session session=new Session(resultSet.getString("name"),date,time);
                    session.setBatch_id(batch.getId());
                    session.setId(resultSet.getInt("id"));
                    sessions.add(session);
                }
            }
        }
        catch (SQLException e){
            Utilities.showAlert(Alert.AlertType.ERROR,"Error",e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }
    public static List<Instructor> getAllInstructors(Course course){
        String query = "SELECT * FROM instructors " +
                "JOIN instructors_courses ON instructors.id = instructors_courses.instructor_id " +
                "WHERE instructors_courses.course_id = ?";
        List<Instructor> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1,course.getId());
                try(ResultSet resultSet = ps.executeQuery()){
                    while (resultSet.next()) {
                        Instructor instructor = new Instructor(resultSet.getString("name"),
                                resultSet.getInt("age"),
                                resultSet.getString("gender"),
                                resultSet.getString("email"),
                                resultSet.getString("phone"));
                        instructor.setId(resultSet.getInt("id"));
                        instructor.setSQLDate(resultSet.getDate("register_date"));
                        list.add(instructor);
                    }
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public static void updateSession(Session session) throws SQLException {
        String query="UPDATE sessions SET name = ?, batch_id = ?, date_time = ? WHERE id = ?";
        try(Connection connection=dataSource.getConnection(username,password); PreparedStatement ps=  connection.prepareStatement(query)){
            ps.setString(1,session.getName());
            ps.setInt(2,session.getBatch_id());
            ps.setTimestamp(3,session.getDateTime());
            ps.setInt(4,session.getId());
            ps.executeUpdate();
        }

    }
    public static int getSessionId(Session session) throws SQLException{
        String query="SELECT id FROM sessions WHERE name = ?";
        try (Connection connection = dataSource.getConnection(username, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, session.getName());
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    return rs.getInt("id");
                }
                else {
                    throw new SQLException("Session ID is not found");
                }
            }
        }
    }
    public static void deleteAttendance(Entry entry,Batch batch) throws SQLException{
        String query;
        if(entry instanceof Student){
            query = "DELETE students_attendances FROM students_attendances " +
                    "JOIN sessions ON students_attendances.session_id = sessions.id " +
                    "WHERE students_attendances.student_id = ? " +
                    "AND sessions.batch_id = ?";
        }
        else{
            query = "DELETE instructors_attendances FROM instructors_attendances " +
                    "JOIN sessions ON instructors_attendances.session_id = sessions.id " +
                    "WHERE instructors_attendances.instructor_id = ? " +
                    "AND sessions.batch_id = ?";
        }
        try(Connection connection=dataSource.getConnection(username,password)){
            try(PreparedStatement ps=connection.prepareStatement(query)){
                ps.setInt(1,entry.getId());
                ps.setInt(2,batch.getId());
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Attendance deleted successfully");
                } else {
                    System.out.println("No person found with the associated batch");
                }
            }
        }
    }
    public static void deleteAttendance(Entry entry,List<Course> courses) throws SQLException {
        String query;
        if(entry instanceof Student){
            query = "DELETE students_attendances FROM students_attendances " +
                    "JOIN sessions ON students_attendances.session_id = sessions.id " +
                    "JOIN batches ON sessions.batch_id = batches.id " +
                    "JOIN courses ON batches.course_id = courses.id " +
                    "WHERE students_attendances.student_id = ? " +
                    "AND courses.id = ?";
        }
        else{
            query = "DELETE instructors_attendances FROM instructors_attendances " +
                    "JOIN sessions ON instructors_attendances.id = sessions.id " +
                    "JOIN batches ON sessions.batch_id = batches.id " +
                    "JOIN courses ON batches.course_id = courses.id " +
                    "WHERE instructors_attendances.instructor_id = ? " +
                    "AND courses.id = ?";
        }
        try(Connection connection=dataSource.getConnection(username,password)){
            for(Course course:courses){
                try(PreparedStatement ps=connection.prepareStatement(query)){
                    ps.setInt(1,entry.getId());
                    ps.setInt(2,course.getId());
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Attendance deleted successfully");
                    } else {
                        System.out.println("No person found with the associated courses");
                    }
                }
            }
        }

    }
}
