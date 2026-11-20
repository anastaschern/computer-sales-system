
package by.bsuir.computersales.client.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Представление формы авторизации пользователя.
 *
 * Содержит поля ввода логина и пароля,
 * кнопку входа и область для сообщений.
 */
public class LoginView extends VBox {

    private final TextField loginField;
    private final PasswordField passwordField;
    private final Button loginButton;
    private final Label messageLabel;

    public LoginView() {

        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPadding(new Insets(30));

        setMaxWidth(380);

        Label titleLabel = new Label(
                "Авторизация"
        );

        titleLabel.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold;"
        );

        Label subtitleLabel = new Label(
                "Система учёта продаж компьютерной техники"
        );

        loginField = new TextField();
        loginField.setPromptText("Введите логин");
        loginField.setMaxWidth(Double.MAX_VALUE);

        passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        loginButton = new Button("Войти");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setDefaultButton(true);

        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setStyle(
                "-fx-text-fill: #c62828;"
        );

        getChildren().addAll(
                titleLabel,
                subtitleLabel,
                loginField,
                passwordField,
                loginButton,
                messageLabel
        );
    }

    public String getLogin() {
        return loginField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public void showMessage(String message) {
        messageLabel.setText(message);
    }

    public void clearMessage() {
        messageLabel.setText("");
    }

    public void setLoginDisabled(boolean disabled) {
        loginField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
    }
}
