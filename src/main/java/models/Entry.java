package models;

import java.sql.Date;
import java.time.LocalDate;

public interface Entry {
    int getId();

    // Setter for ID
    void setId(int id);

    // Getter for Name
    String getName();

    // Setter for Name
    void setName(String name);

    // Getter for Age
    int getAge();

    // Setter for Age
    void setAge(int age);

    // Getter for Gender
    String getGender();
    Date getSQLDate();
    void setSQLDate(Date date);

    // Setter for Gender
    void setGender(String gender);

    // Getter for Email
    String getEmail();

    // Setter for Email
    void setEmail(String email);

    // Getter for Phone Number
    String getPhoneNumber();

    // Setter for Phone Number
    void setPhoneNumber(String phoneNumber);

    LocalDate getRegisterDate();

    void setRegisterDate(LocalDate registerDate);
}
