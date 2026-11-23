
package by.bsuir.computersales.client;

import by.bsuir.computersales.client.controller.LoginController;
import by.bsuir.computersales.client.service.ClientAuthService;
import by.bsuir.computersales.client.view.LoginView;
import by.bsuir.computersales.client.view.MainView;
import by.bsuir.computersales.common.model.User;
import by.bsuir.computersales.client.view.MainView;
import by.bsuir.computersales.common.model.User;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Главный класс клиентского приложения.
 *
 * Отвечает за запуск JavaFX, создание сервисов
 * и отображение окон приложения.
 */
public class AppLauncher extends Application {

    private ClientAuthService authService;

    @Override
    public void start(Stage primaryStage) {

        // Создаём сервис взаимодействия с сервером.
        authService = new ClientAuthService();

        // Создаём форму авторизации.
        LoginView loginView = new LoginView();

        // Создаём сцену с формой входа.
        Scene loginScene = new Scene(loginView, 900, 600);

        // Создаём контроллер авторизации.
        new LoginController(
                loginView,
                authService,
                loginResult -> showMainScreen(primaryStage)
        );

        // Настраиваем главное окно.
        primaryStage.setTitle(
                "Система учёта продаж компьютерной техники"
        );

        primaryStage.setScene(loginScene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(450);

        // Закрываем сетевое соединение при закрытии приложения.
        primaryStage.setOnCloseRequest(event -> {
            if (authService != null) {
                authService.close();
            }
        });

        // Отображаем окно.
        primaryStage.show();
    }

    /**
     * Отображает временный экран после успешной авторизации.
     */
    private void showMainScreen(Stage primaryStage) {

        // Получаем данные авторизованного пользователя.
        User currentUser = authService.getCurrentUser();

        // Создаём главное окно.
        MainView mainView = new MainView(currentUser);

        // Настраиваем кнопку выхода.
        mainView.getLogoutButton().setOnAction(event -> {

            try {
                // Завершаем пользовательскую сессию на сервере.
                authService.logout();

                // Возвращаем пользователя к форме авторизации.
                LoginView loginView = new LoginView();

                Scene loginScene = new Scene(loginView, 900, 600);

                new LoginController(
                        loginView,
                        authService,
                        loginResult -> showMainScreen(primaryStage)
                );

                primaryStage.setScene(loginScene);

            } catch (Exception e) {

                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR,
                        "Не удалось завершить сессию: " + e.getMessage()
                ).showAndWait();
            }
        });

        // Отображаем главное окно.
        Scene mainScene = new Scene(mainView, 900, 600);

        primaryStage.setScene(mainScene);
    }

    /**
     * Точка входа в клиентское приложение.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
