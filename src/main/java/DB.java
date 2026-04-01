import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {
    // TODO: change if needed
    private static final String URL = "jdbc:mysql://sql5.freesqldatabase.com:3306/sql5821878";
    private static final String USER = "sql5821878";
    private static final String PASSWORD = "wWUf4wqUc3";

    private DB() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}