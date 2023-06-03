module com.fahzycoding.windowsmediaplayer_enhanced {
    requires javafx.controls;
    requires javafx.fxml;
//    requires javafx.web;
//
//    requires org.controlsfx.controls;
//    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
//    requires eu.hansolo.tilesfx;
    requires okhttp3;
    requires java.desktop;
    requires com.google.gson;
    requires java.sql;
    requires javafx.media;

    opens com.fahzycoding.windowsmediaplayer_enhanced to javafx.fxml;
    exports com.fahzycoding.windowsmediaplayer_enhanced;
}