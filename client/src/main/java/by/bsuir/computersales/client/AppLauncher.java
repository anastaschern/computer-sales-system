package by.bsuir.computersales.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Главный класс клиентского приложения.
 *
 * Отвечает за запуск JavaFX и отображение
 * главного окна программы.
 */
public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {

        // Создаём начальную надпись.
        Label welcomeLabel = new Label(
                "Система учёта продаж компьютерной техники"
        );

        // Создаём корневой контейнер окна.
        StackPane root = new StackPane(welcomeLabel);

        // Создаём сцену.
        Scene scene = new Scene(root, 900, 600);

        // Настраиваем главное окно.
        primaryStage.setTitle(
                "Система учёта продаж компьютерной техники"
        );

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(450);

        // Отображаем окно.
        primaryStage.show();
    }

    /**
     * Точка входа в клиентское приложение.
     */
    public static void main(String[] args) {
        launch(args);
    }
}