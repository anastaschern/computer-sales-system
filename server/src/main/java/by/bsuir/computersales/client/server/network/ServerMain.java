
package by.bsuir.computersales.client.server.network;

import by.bsuir.computersales.client.server.config.ServerConfig;
import by.bsuir.computersales.client.server.dao.UserDaoImpl;
import by.bsuir.computersales.client.server.database.DatabaseManager;
import by.bsuir.computersales.client.server.security.PasswordHasher;
import by.bsuir.computersales.client.server.security.SessionManager;
import by.bsuir.computersales.client.server.service.AuthService;
import by.bsuir.computersales.client.server.dao.ProductDaoImpl;
import by.bsuir.computersales.client.server.dao.ProductDao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Главный класс многопоточного сервера.
 *
 * Отвечает за загрузку конфигурации,
 * подключение к базе данных, авторизацию
 * пользователей и приём клиентских подключений.
 */
public class ServerMain {

    private final ServerConfig config;
    private final DatabaseManager databaseManager;
    private final ExecutorService clientPool;
    private final AuthService authService;
    private final ProductDao productDao;

    private ServerSocket serverSocket;
    private volatile boolean running;

    /**
     * Создаёт сервер и необходимые компоненты приложения.
     *
     * @param config настройки сервера
     */
    public ServerMain(ServerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Конфигурация сервера не должна быть null"
            );
        }

        this.config = config;

        // Создаём менеджер подключений к базе данных.
        this.databaseManager = new DatabaseManager(config);

        // Создаём DAO для работы с пользователями.
        UserDaoImpl userDao = new UserDaoImpl(databaseManager);

        // Создаём DAO для работы с каталогом товаров.
        // Создаём DAO для работы с каталогом товаров.
        this.productDao = new ProductDaoImpl(databaseManager);

        // Создаём средство проверки паролей.
        PasswordHasher passwordHasher = new PasswordHasher();

        // Создаём менеджер пользовательских сессий.
        SessionManager sessionManager = new SessionManager(
                config.getSessionTimeoutSeconds()
        );

        // Создаём сервис авторизации.
        this.authService = new AuthService(
                userDao,
                passwordHasher,
                sessionManager
        );

        // Создаём пул потоков для обработки клиентов.
        this.clientPool = Executors.newFixedThreadPool(
                config.getThreadPoolSize()
        );
    }

    /**
     * Запускает сервер и начинает принимать подключения.
     */
    public void start() throws IOException {
        if (running) {
            throw new IllegalStateException(
                    "Сервер уже запущен"
            );
        }

        serverSocket = new ServerSocket();

        serverSocket.bind(new InetSocketAddress(
                config.getServerHost(),
                config.getServerPort()
        ));

        running = true;

        System.out.println("Сервер запущен.");
        System.out.println(
                "Адрес: " + config.getServerHost()
        );
        System.out.println(
                "Порт: " + config.getServerPort()
        );
        System.out.println(
                "Размер пула потоков: "
                        + config.getThreadPoolSize()
        );

        try {
            while (running) {
                Socket clientSocket = serverSocket.accept();

                System.out.println(
                        "Подключился клиент: "
                                + clientSocket.getRemoteSocketAddress()
                );

                // Передаём клиенту общий сервис авторизации.
                // Каждый ClientHandler работает в отдельном потоке.
                clientPool.execute(
                        new ClientHandler(
                                clientSocket,
                                authService,
                                productDao
                        )
                );
            }

        } catch (IOException e) {
            if (running) {
                throw e;
            }

        } finally {
            stop();
        }
    }

    /**
     * Останавливает сервер и освобождает ресурсы.
     */
    public void stop() {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println(
                        "Ошибка закрытия серверного сокета: "
                                + e.getMessage()
                );
            }
        }

        clientPool.shutdown();

        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        databaseManager.close();

        System.out.println("Сервер остановлен.");
    }

    /**
     * Точка входа в приложение.
     *
     * Первый аргумент командной строки позволяет
     * указать путь к файлу конфигурации.
     */
    public static void main(String[] args) {
        Path configPath = args.length > 0
                ? Path.of(args[0])
                : Path.of("server.properties");

        try {
            ServerConfig config = ServerConfig.load(configPath);

            ServerMain server = new ServerMain(config);

            Runtime.getRuntime().addShutdownHook(
                    new Thread(server::stop)
            );

            server.start();

        } catch (Exception e) {
            System.err.println(
                    "Не удалось запустить сервер: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
