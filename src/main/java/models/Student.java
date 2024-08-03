package models;


import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

public class Student implements Entry {
    private int id;
    private String name;
    private int age;
    private String gender;
    private String email;
    private String phoneNumber;


    public Student(String name, int age, String gender, String email, String phoneNumber) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    private LocalDate registerDate;
    public LocalDate getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDate registerDate) {
        this.registerDate = registerDate;
    }
    public Date getSQLDate() {
        return Date.valueOf(registerDate);
    }
    public void setSQLDate(Date date){
        this.registerDate=date.toLocalDate();
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public boolean equals(Student o) {
        return name.equals(o.name) && email.equals(o.email) && age==o.getAge() && phoneNumber.equals(o.phoneNumber) && gender.equals(o.gender);
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
