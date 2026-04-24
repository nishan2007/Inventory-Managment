import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static final String URL_PROPERTY = "smartstock.db.url";
    private static final String USER_PROPERTY = "smartstock.db.user";
    private static final String PASSWORD_PROPERTY = "smartstock.db.password";

    private static final String URL_ENV = "SMARTSTOCK_DB_URL";
    private static final String USER_ENV = "SMARTSTOCK_DB_USER";
    private static final String PASSWORD_ENV = "SMARTSTOCK_DB_PASSWORD";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(
                    requiredSetting(URL_PROPERTY, URL_ENV),
                    requiredSetting(USER_PROPERTY, USER_ENV),
                    requiredSetting(PASSWORD_PROPERTY, PASSWORD_ENV)
            );
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static String requiredSetting(String propertyName, String envName) {
        String systemValue = System.getProperty(propertyName);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        throw new IllegalStateException(
                "Missing database configuration. Set system property '" + propertyName +
                        "' or environment variable '" + envName + "'."
        );
    }
}
