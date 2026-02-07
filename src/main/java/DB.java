import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {
    // TODO: change if needed
    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/smartstock_dev?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "smartstock_user";
    private static final String PASSWORD = "password123";

    private DB() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}