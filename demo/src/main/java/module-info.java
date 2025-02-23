module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires java.naming;
    requires com.fazecast.jSerialComm; // Add Logback as the SLF4J implementation

    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
}