import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

//Class that manages repository connection to SQL database
public class ConnectionManager {
    //Static member holding Connection object
    private static Connection connection;

    //Empty constructor
    private ConnectionManager() { }

    //Function that instantiates connection variable if it is null and returns it
    public static Connection getConnection() {
        if (connection == null) {
            connection = connect();
        }

        return connection;
    }

    //Method that instantiates connection variable with given full connection string
    public static Connection getConnection(String connectionString) {
        connection = connect(connectionString);

        return connection;
    }

    //Method that instantiates connection variable with given parts to connection string if it is null
    public static Connection getConnection(String hostname, String port, String dbname, String username, String password) {
        connection = connect(hostname, port, dbname, username, password);

        return connection;
    }

    //Method that attempts to return a valid connection using the data found in src/main/resources/jdbc.properties
    private static Connection connect() {
        try {
            Properties props = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream input = loader.getResourceAsStream("jdbc.properties");
            props.load(input);
/*
            FileReader fr = new FileReader("src/main/resources/jdbc.properties");
            props.load(fr);
*/
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

    //Method that attempts to return a valid connection using a given full connection string
    public static Connection connect(String connectionString) {
        try {
            connection = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    //Method that attempts to return a valid connection using given parts to connection string
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

    //Method to read info from a given properties file path and return a String that can be used to get a database connection
    public static String getConnectionString() {
        //Create empty String and properties object
        String connectionString;
        Properties props = new Properties();

        //Create file reader object to read from the properties file
        //FileReader fr;
        try {
            /*
            //Attempt to open the properties file using the given file path
            fr = new FileReader("src/main/resources/jdbc.properties");

            //Read the file into the properties object
            props.load(fr);
*/
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream input = loader.getResourceAsStream("jdbc.properties");
            props.load(input);

            //Set the connection string based on info found in the file
            connectionString = "jdbc:mariadb://" +
                    props.getProperty("hostname") + ":" +
                    props.getProperty("port") + "/" +
                    props.getProperty("dbname") + "?user=" +
                    props.getProperty("username") + "&password=" +
                    props.getProperty("password");
        } catch (IOException e) {
            //Log any exceptions and set the connection string to null if an exception was caught
            e.printStackTrace();
            connectionString = null;
        }

        //Return connection string
        return connectionString;
    }
}
