package com.tictactoe.machine.util;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ResizeHelper {

    public static void addResizeListener(Stage stage) {
        ResizeListener resizeListener = new ResizeListener(stage);
        Scene scene = stage.getScene();
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, resizeListener);
    }

    private static class ResizeListener implements EventHandler<MouseEvent> {
        private final Stage stage;
        private Cursor cursorEvent = Cursor.DEFAULT;
        private final int border = 8;
        private double startX = 0;
        private double startY = 0;

        public ResizeListener(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
            Scene scene = stage.getScene();

            double mouseEventX = mouseEvent.getSceneX();
            double mouseEventY = mouseEvent.getSceneY();
            double sceneWidth = scene.getWidth();
            double sceneHeight = scene.getHeight();

            if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
                if (mouseEventX < border && mouseEventY < border) {
                    cursorEvent = Cursor.NW_RESIZE;
                } else if (mouseEventX < border && mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.SW_RESIZE;
                } else if (mouseEventX > sceneWidth - border && mouseEventY < border) {
                    cursorEvent = Cursor.NE_RESIZE;
                } else if (mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.SE_RESIZE;
                } else if (mouseEventX < border) {
                    cursorEvent = Cursor.W_RESIZE;
                } else if (mouseEventX > sceneWidth - border) {
                    cursorEvent = Cursor.E_RESIZE;
                } else if (mouseEventY < border) {
                    cursorEvent = Cursor.N_RESIZE;
                } else if (mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.S_RESIZE;
                } else {
                    cursorEvent = Cursor.DEFAULT;
                }
                scene.setCursor(cursorEvent);
            } else if (MouseEvent.MOUSE_EXITED.equals(mouseEventType) || MouseEvent.MOUSE_RELEASED.equals(mouseEventType)) {
                scene.setCursor(Cursor.DEFAULT);
            } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
                startX = stage.getWidth() - mouseEventX;
                startY = stage.getHeight() - mouseEventY;
            } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
                if (!Cursor.DEFAULT.equals(cursorEvent)) {
                    if (!Cursor.W_RESIZE.equals(cursorEvent) && !Cursor.E_RESIZE.equals(cursorEvent)) {
                        double minHeight = stage.getMinHeight() > 0 ? stage.getMinHeight() : 520;
                        if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.N_RESIZE.equals(cursorEvent) || Cursor.NE_RESIZE.equals(cursorEvent)) {
                            double newHeight = stage.getHeight() - mouseEvent.getScreenY() + stage.getY();
                            if (newHeight > minHeight) {
                                stage.setY(mouseEvent.getScreenY());
                                stage.setHeight(newHeight);
                            }
                        } else {
                            double newHeight = mouseEventY + startY;
                            if (newHeight > minHeight) {
                                stage.setHeight(newHeight);
                            }
                        }
                    }

                    if (!Cursor.N_RESIZE.equals(cursorEvent) && !Cursor.S_RESIZE.equals(cursorEvent)) {
                        double minWidth = stage.getMinWidth() > 0 ? stage.getMinWidth() : 480;
                        if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.W_RESIZE.equals(cursorEvent) || Cursor.SW_RESIZE.equals(cursorEvent)) {
                            double newWidth = stage.getWidth() - mouseEvent.getScreenX() + stage.getX();
                            if (newWidth > minWidth) {
                                stage.setX(mouseEvent.getScreenX());
                                stage.setWidth(newWidth);
                            }
                        } else {
                            double newWidth = mouseEventX + startX;
                            if (newWidth > minWidth) {
                                stage.setWidth(newWidth);
                            }
                        }
                    }
                }
            }
        }
    }
}
