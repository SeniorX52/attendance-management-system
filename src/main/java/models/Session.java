package models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Session {
    private int id;
    private int batch_id;

    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> time = new SimpleObjectProperty<>();
    public Session(String name, LocalDate date, LocalTime time) {
        this.name.set(name);
        this.date.set(date);
        this.time.set(time);
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBatch_id(int batch_id) {
        this.batch_id = batch_id;
    }

    @Override
    public String toString() {
        return name.get();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public LocalTime getTime() {
        return time.get();
    }
    public void setTime(LocalTime time) {
        this.time.set(time);
    }

    public ObjectProperty<LocalTime> timeProperty() {
        return time;
    }
    public int getBatch_id(){
        return batch_id;
    }
    public Timestamp getDateTime(){
        return Timestamp.valueOf(LocalDateTime.of(date.get(),time.get()));
    }
}