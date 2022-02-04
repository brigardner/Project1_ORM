import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {
    private static Connection connection;

    private ConnectionManager() { }

    public static Connection getConnection() {
        /*
        if (connection == null) {
            connection = connect();
        }

         */
        return connection;
    }

    private static Connection connect() {
        try {
            Properties props = new Properties();

            FileReader fr = new FileReader("src/main/resources/jdbc.properties");
            props.load(fr);

            String connectionString = "jdbc:mariadb://" +
                    props.getProperty("hostname") + ":" +
                    props.getProperty("port") + "/" +
                    props.getProperty("dbname") + "?user=" +
                    props.getProperty("username") + "&password=" +
                    props.getProperty("password");

            connection = DriverManager.getConnection(connectionString);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }
    public static String getConnectionString() {
        String connectionString;
        Properties props = new Properties();

        FileReader fr = null;
        try {
            fr = new FileReader("src/main/resources/jdbc.properties");

            props.load(fr);

            connectionString = "jdbc:mariadb://" +
                    props.getProperty("hostname") + ":" +
                    props.getProperty("port") + "/" +
                    props.getProperty("dbname") + "?user=" +
                    props.getProperty("username") + "&password=" +
                    props.getProperty("password");
        } catch (IOException e) {
            e.printStackTrace();
            connectionString = null;
        }

        return connectionString;
    }

    public static Connection connect(String hostname, String port, String dbname, String username, String password) {
        try {
            String connectionString = "jdbc:mariadb://" + hostname + ":" + port + "/" + dbname +
                    "?user=" + username + "&password=" + password;

            connection = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static Connection connect(String connectionString) {
        try {
            connection = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }
}
