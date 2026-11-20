
package by.bsuir.computersales.client.controller;

import by.bsuir.computersales.client.service.ClientAuthService;
import by.bsuir.computersales.client.view.LoginView;
import by.bsuir.computersales.common.dto.LoginResult;
import javafx.concurrent.Task;

import java.util.function.Consumer;

/**
 * Контроллер формы авторизации.
 *
 * Обрабатывает действия пользователя и связывает
 * представление LoginView с сервисом ClientAuthService.
 */
public class LoginController {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private final LoginView view;
    private final ClientAuthService authService;
    private final Consumer<LoginResult> onLoginSuccess;

    public LoginController(
            LoginView view,
            ClientAuthService authService,
            Consumer<LoginResult> onLoginSuccess
    ) {
        this.view = view;
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        initialize();
    }

    /**
     * Настраивает обработчики элементов формы.
     */
    private void initialize() {
        view.getLoginButton().setOnAction(
                event -> handleLogin()
        );
    }

    /**
     * Обрабатывает попытку входа пользователя.
     */
    private void handleLogin() {

        view.clearMessage();

        String login = view.getLogin().trim();
        String password = view.getPassword();

        // Проверяем заполнение полей.
        if (login.isEmpty()) {
            view.showMessage("Введите логин.");
            return;
        }

        if (password == null || password.isBlank()) {
            view.showMessage("Введите пароль.");
            return;
        }

        // Блокируем форму на время выполнения запроса.
        view.setLoginDisabled(true);

        // Выполняем сетевой запрос в отдельном потоке.
        Task<LoginResult> loginTask = new Task<>() {

            @Override
            protected LoginResult call() throws Exception {
                return authService.login(
                        SERVER_HOST,
                        SERVER_PORT,
                        login,
                        password
                );
            }
        };

        // Обработка успешной авторизации.
        loginTask.setOnSucceeded(event -> {
            view.setLoginDisabled(false);

            LoginResult result = loginTask.getValue();

            onLoginSuccess.accept(result);
        });

        // Обработка ошибки авторизации или подключения.
        loginTask.setOnFailed(event -> {
            view.setLoginDisabled(false);

            Throwable exception = loginTask.getException();

            String errorMessage = exception != null
                    && exception.getMessage() != null
                    ? exception.getMessage()
                    : "Не удалось выполнить вход.";

            view.showMessage(errorMessage);
        });

        // Запускаем задачу в фоновом потоке.
        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }
}
