import ORMExceptions.NoMatchingSQLTypeException;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Class that contains methods that return PreparedStatement objects
//Using SQLStringScriptor methods to define PreparedStatement SQL statement strings
public class SQLPreparedStatementScriptor {
    //Singleton design ensures that static member sqlDataTypes is set once and only once
    private static SQLPreparedStatementScriptor scriptor;

    //Hashmap to map Java object names to designated SQL type integers
    private static Map<String, Integer> sqlDataTypeInts;

    //Private constructor for singleton
    private SQLPreparedStatementScriptor() {
        setSqlTypeInts();
    }

    public static SQLPreparedStatementScriptor getScriptor() {
        //If the singleton is currently null, set the value to a new empty scriptor
        if (scriptor == null) {
            scriptor = new SQLPreparedStatementScriptor();
        }

        //Return scriptor
        return scriptor;
    }

    //Method to fill hash map with SQL type ints
    private static void setSqlTypeInts() {
        //Initialize SQLDataTypeInts with an empty HashMap
        sqlDataTypeInts = new HashMap<>();

        //Fill map with java class name (key) and SQL data type int (value) pairs
        sqlDataTypeInts.put(Character.class.getName(), Types.CHAR);
        sqlDataTypeInts.put(Integer.class.getName(), Types.INTEGER);
        sqlDataTypeInts.put(Double.class.getName(), Types.DOUBLE);
        sqlDataTypeInts.put(String.class.getName(), Types.VARCHAR);
        sqlDataTypeInts.put(Boolean.class.getName(), Types.BOOLEAN);
    }

    //Method to return integer form of SQL type
    public static int getSQLTypeInt(Class c) {
        //Get given class name
        String className = c.getName();

        //Look through hashmap sqlTypeInts for matching class name; return value
        return sqlDataTypeInts.getOrDefault(className, 0);
    }

    //Method to set preparedStatement parameters depending on class type
    public static boolean setPreparedStatementObject(int index, Object o, PreparedStatement preparedStatement) throws SQLException {
        //Get given object class type
        Class c = o.getClass();

        //Get name of given object's class
        String className = c.getName();

        //Look through HashMap sqlTypeInts for the key matching class name
        Integer sqlTypeInt = sqlDataTypeInts.get(className);

        //Return false if the SQL type int was not found in map
        if (sqlTypeInt == null) {
            return false;
        }

        return false;

    }

    //Method to attempt parameterizing WHERE clause in PreparedStatement
    public static PreparedStatement parameterizeWhereClause(Repository repository, PreparedStatement preparedStatement, Object o, int whereClauseIndex) {
        //Get the primary key field of repository table
        int primaryKeyColumnIndex = repository.getTable().getPrimaryKeyField();

        //Check if the primary key field index is valid index
        if (primaryKeyColumnIndex < 0) {
            return null;
        }

        //Attempt to set the primary key field from object o as the parameter for WHERE clause
        try {
            //Get the column from the table with given primary key column index
            Column primaryKeyColumn = repository.getTable().get(primaryKeyColumnIndex);

            //Temporary integer variable holding the SQLTypeInt matching field type
            int fieldTypeInt = getSQLTypeInt(o.getClass());

            //Check if the SQLTypeInt wasn't found and was set to 0
            if (fieldTypeInt == 0) {
                throw new NoMatchingSQLTypeException(o.getClass().getName());
            }

            //Attempt to add the primary key field value to where clause
            preparedStatement.setObject(whereClauseIndex, primaryKeyColumn.getGetter().invoke(o), fieldTypeInt);

            //Return the parameterized prepared SQL statement
            return preparedStatement;
        } catch (SQLException | NoMatchingSQLTypeException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        //Return null if try block was not successfully completed
        return null;
    }

    //Method to generate the PreparedStatement for a create method (INSERT)
    public static PreparedStatement prepareCreateStatement(Repository repository, PreparedStatement preparedStatement, Object o) {
        //String to hold SQL statement defined by SQLStringScriptor.makeCreateSQLString()
        String sql = SQLStringScriptor.makeCreateSQLString(repository);

        //Check if the SQL statement returned by makeCreateSQLString is empty
        //return null if so
        if (sql.equals("")) {
            return null;
        }

        //Try to catch SQL exceptions
        try {
            //Create array list of valid write fields
            List<Column> columns = repository.getTable().getWriteableFields();

            //Temporary integer variable holding the SQLTypeInt matching field type
            int fieldTypeInt = 0;

            //Iterate through columns and add field values to statement
            for (int i = 0; i < columns.size(); i++) {
                Column c = columns.get(i);
                fieldTypeInt = getSQLTypeInt(c.getProperty().getType());

                //Check if the SQLTypeInt wasn't found and was set to 0
                if (fieldTypeInt == 0) {
                    throw new NoMatchingSQLTypeException(c.getProperty().getType().getName());
                }

                //Set the column value if SQLTypeInt was found
                preparedStatement.setObject(i + 1, c.getGetter().invoke(o), fieldTypeInt);
            }

            //Return the parameterized prepared SQL statement
            return preparedStatement;
        } catch (SQLException | NoMatchingSQLTypeException | InvocationTargetException | IllegalAccessException e) {
            //Edit exception handling to... handle exceptions
            e.printStackTrace();
        }

        //Return null if try block was not successfully completed
        return null;
    }

    //Method to generate the PreparedStatement for a read method
}
