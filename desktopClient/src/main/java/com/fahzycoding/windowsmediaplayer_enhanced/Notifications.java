package com.fahzycoding.windowsmediaplayer_enhanced;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import javafx.util.Duration;
//import javafx.stage.Window.AnchorLocation;

public class Notifications {

    private static final double WINDOW_WIDTH = 300;
    private static final double WINDOW_HEIGHT = 80;
    private static final double WINDOW_PADDING = 10;
    private static final double FADE_IN_DURATION = 500;
    private static final double FADE_OUT_DURATION = 500;
    private static final double DISPLAY_DURATION = 5000;

    private final Stage ownerStage;
    private final Popup popup;

    public Notifications(Stage ownerStage) {
        this.ownerStage = ownerStage;
        this.popup = createPopup();
    }

    public void showNotification(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px;");
        VBox container = new VBox(messageLabel);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new javafx.geometry.Insets(WINDOW_PADDING));

        Scene scene = new Scene(container, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(null);
        popup.getContent().add(scene.getRoot());

        popup.show(ownerStage);
        fadeInNotification();
        scheduleFadeOut();
    }

    private Popup createPopup() {
        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_RIGHT);
//        popup.setAnchorLocation(Window.AnchorLocation.WINDOW_TOP_RIGHT);
        return popup;
    }

    private void fadeInNotification() {
        popup.getScene().getRoot().setOpacity(0);
        javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.millis(FADE_IN_DURATION), popup.getScene().getRoot());
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void scheduleFadeOut() {
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(DISPLAY_DURATION));
        pause.setOnFinished(event -> fadeOutNotification());
        pause.play();
    }

    private void fadeOutNotification() {
        javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.millis(FADE_OUT_DURATION), popup.getScene().getRoot());
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(event -> popup.hide());
        fadeTransition.play();
    }
}
