package models;

public class Batch {
    private int id;
    private String name;
    private int instructor_id;
    private int course_id;
    private int noSessions;
    private int noStudents;

    public Batch(String name, int instructor_id, int course_id, int noSessions, int noStudents) {
        this.name = name;
        this.instructor_id = instructor_id;
        this.course_id = course_id;
        this.noSessions = noSessions;
        this.noStudents = noStudents;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInstructor_id() {
        return instructor_id;
    }

    public void setInstructor_id(int instructor_id) {
        this.instructor_id = instructor_id;
    }

    public int getCourse_id() {
        return course_id;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    public int getNoSessions() {
        return noSessions;
    }

    public void setNoSessions(int noSessions) {
        this.noSessions = noSessions;
    }

    public int getNoStudents() {
        return noStudents;
    }

    public void setNoStudents(int noStudents) {
        this.noStudents = noStudents;
    }
}
