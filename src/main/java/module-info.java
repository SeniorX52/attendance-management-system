module com.ams {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.sql;
    requires mysql.connector.j;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    opens com.ams to javafx.fxml;
    exports com.ams;
    exports models;
    opens models to javafx.base, javafx.fxml;

}