
package by.bsuir.computersales.client.view;

import by.bsuir.computersales.common.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Главное окно системы учёта продаж компьютерной техники.
 *
 * Отображает информацию об авторизованном пользователе
 * и предоставляет кнопку выхода из учётной записи.
 */
public class MainView extends BorderPane {

    private final Label welcomeLabel;
    private final Label roleLabel;
    private final Button logoutButton;

    public MainView(User user) {

        // Заголовок главного окна.
        Label titleLabel = new Label(
                "Система учёта продаж компьютерной техники"
        );

        titleLabel.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
        );

        // Приветствие пользователя.
        welcomeLabel = new Label(
                "Добро пожаловать, " + user.getLogin() + "!"
        );

        welcomeLabel.setStyle(
                "-fx-font-size: 20px;"
        );

        // Отображение роли пользователя.
        String roleDescription = user.getRole() == null
                ? "Не определена"
                : user.getRole().getDescription();

        roleLabel = new Label(
                "Ваша роль: " + roleDescription
        );

        roleLabel.setStyle(
                "-fx-font-size: 15px;"
        );

        // Кнопка выхода.
        logoutButton = new Button("Выйти из системы");

        logoutButton.setPrefWidth(180);
        logoutButton.setPrefHeight(35);

        logoutButton.setStyle(
                "-fx-font-size: 14px;"
        );

        // Центральная область главного окна.
        VBox content = new VBox(
                15,
                titleLabel,
                welcomeLabel,
                roleLabel,
                logoutButton
        );

        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        setCenter(content);
    }

    public Button getLogoutButton() {
        return logoutButton;
    }
}
