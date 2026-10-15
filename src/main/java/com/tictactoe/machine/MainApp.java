package com.tictactoe.machine;

import com.tictactoe.machine.engine.GameEngine;
import com.tictactoe.machine.model.GameConfig;
import com.tictactoe.machine.model.GameState;
import com.tictactoe.machine.util.Constants;
import com.tictactoe.machine.util.ResizeHelper;
import com.tictactoe.machine.view.MainView;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class MainApp extends Application {
    private Stage primaryStage;
    private MainView mainView;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        GameConfig config = new GameConfig();
        GameState state = new GameState();
        GameEngine engine = new GameEngine(config, state);

        mainView = new MainView(engine, primaryStage);
        engine.newMatch();

        Scene scene = new Scene(mainView, 1260, 880);
        primaryStage.setTitle(Constants.APP_TITLE);
        primaryStage.setScene(scene);
        
        // Attach application window and taskbar icon
        primaryStage.getIcons().add(createAppIcon());
        
        // Window sizing boundaries (Supports both Landscape and Portrait)
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(520);

        // Restore saved dimensions
        loadWindowState(primaryStage);

        primaryStage.show();

        // Enable drag to resize borders
        ResizeHelper.addResizeListener(primaryStage);

        // Subtle, crisp fade-in without bouncy scale distortion
        mainView.setOpacity(0.0);
        FadeTransition fade = new FadeTransition(Duration.millis(160), mainView);
        fade.setToValue(1.0);
        fade.play();
    }

    @Override
    public void stop() {
        saveWindowState(primaryStage);
    }

    private File getConfigFile() {
        File dir = new File(System.getProperty("user.home"), ".tictactoe-machine");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "window.properties");
    }

    private void loadWindowState(Stage stage) {
        File file = getConfigFile();
        if (file.exists()) {
            Properties props = new Properties();
            try (FileReader reader = new FileReader(file)) {
                props.load(reader);
                double w = Double.parseDouble(props.getProperty("window.width", "1260"));
                double h = Double.parseDouble(props.getProperty("window.height", "880"));
                stage.setWidth(Math.max(w, 480));
                stage.setHeight(Math.max(h, 520));
                if (props.containsKey("window.x")) {
                    try {
                        double x = Double.parseDouble(props.getProperty("window.x", "60"));
                        stage.setX(Math.max(x, 20));
                    } catch (Exception ignored) {
                        stage.setX(60);
                    }
                }
                if (props.containsKey("window.y")) {
                    try {
                        double y = Double.parseDouble(props.getProperty("window.y", "60"));
                        stage.setY(Math.max(y, 20));
                    } catch (Exception ignored) {
                        stage.setY(60);
                    }
                }
                if (Boolean.parseBoolean(props.getProperty("window.maximized", "false"))) {
                    stage.setMaximized(true);
                }
            } catch (Exception e) {
                stage.setWidth(1260);
                stage.setHeight(880);
                stage.setX(60);
                stage.setY(60);
            }
        } else {
            stage.setWidth(1260);
            stage.setHeight(880);
            stage.setX(60);
            stage.setY(60);
        }
    }

    private void saveWindowState(Stage stage) {
        File file = getConfigFile();
        Properties props = new Properties();
        props.setProperty("window.maximized", String.valueOf(stage.isMaximized()));
        if (!stage.isMaximized()) {
            props.setProperty("window.width", String.valueOf(stage.getWidth()));
            props.setProperty("window.height", String.valueOf(stage.getHeight()));
            props.setProperty("window.x", String.valueOf(Math.max(stage.getX(), 20)));
            props.setProperty("window.y", String.valueOf(Math.max(stage.getY(), 20)));
        } else {
            props.setProperty("window.width", "1260");
            props.setProperty("window.height", "880");
        }
        try (FileWriter writer = new FileWriter(file)) {
            props.store(writer, "Window State Configuration");
        } catch (Exception e) {
            // Ignore saving issues
        }
    }

    private Image createAppIcon() {
        int size = 64;
        WritableImage img = new WritableImage(size, size);
        PixelWriter pw = img.getPixelWriter();
        Color bg = Color.web("#0F172A");
        Color grid = Color.web("#38BDF8");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (x < 2 || x >= size - 2 || y < 2 || y >= size - 2) {
                    pw.setColor(x, y, grid);
                } else if (x == 21 || x == 22 || x == 42 || x == 43 || y == 21 || y == 22 || y == 42 || y == 43) {
                    pw.setColor(x, y, grid);
                } else {
                    pw.setColor(x, y, bg);
                }
            }
        }
        return img;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
