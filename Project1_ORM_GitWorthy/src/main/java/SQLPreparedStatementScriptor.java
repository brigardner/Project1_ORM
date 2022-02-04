import java.lang.reflect.InvocationTargetException;
import java.security.spec.RSAOtherPrimeInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
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

    //Method to set a statement parameter based on its type
    //Should work with any primitive wrapper (except char) and string
    public static PreparedStatement setIndividualParameter(int index, Object o, PreparedStatement preparedStatement) throws SQLException {
        //Get given object class type
        Class c = o.getClass();

        //Attempt to set the parameter to its respective type
        if (c == Byte.class) preparedStatement.setByte(index, (Byte) o);
        else if (c == Short.class) preparedStatement.setShort(index, (Short) o);
        else if (c == Integer.class) preparedStatement.setInt(index, (Integer) o);
        else if (c == Long.class) preparedStatement.setLong(index, (Long) o);
        else if (c == Float.class) preparedStatement.setFloat(index, (Float) o);
        else if (c == Double.class) preparedStatement.setDouble(index, (Double) o);
        else if (c == Boolean.class) preparedStatement.setBoolean(index, (Boolean) o);
        else if (c == String.class) preparedStatement.setString(index, (String) o);
        //Set the prepared statement to null if parameterizing was unsuccessful
        else {
            preparedStatement = null;
        }

        //Return prepared statement if successful or null if not
        return preparedStatement;
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
    public static PreparedStatement parameterizeWhereClause(Table writeableFieldsTable, PreparedStatement preparedStatement, Object o, int whereClauseIndex) {
        //Get the primary key field of repository table
        int primaryKeyColumnIndex = writeableFieldsTable.getPrimaryKeyFieldIndex();

        //Check if the primary key field index is valid index
        if (primaryKeyColumnIndex < 0) {
            return null;
        }

        //Attempt to set the primary key field from object o as the parameter for WHERE clause
        try {
            //Get the column from the table with given primary key column index
            Column primaryKeyColumn = writeableFieldsTable.get(primaryKeyColumnIndex);
/*
            //Temporary integer variable holding the SQLTypeInt matching field type
            int fieldTypeInt = getSQLTypeInt(o.getClass());

            //Check if the SQLTypeInt wasn't found and was set to 0
            if (fieldTypeInt == 0) {
                throw new NoMatchingSQLTypeException(o.getClass().getName());
            }

            //Attempt to add the primary key field value to where clause
            preparedStatement.setObject(whereClauseIndex, primaryKeyColumn.getGetter().invoke(o), fieldTypeInt);
*/
            //Use setIndividualParameter to parameterize the where clause
            preparedStatement = setIndividualParameter(whereClauseIndex, primaryKeyColumn.getGetter().invoke(o), preparedStatement);
            //Return the parameterized prepared SQL statement
            return preparedStatement;
        } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        //Return null if try block was not successfully completed
        return null;
    }

    //Method to parameterize columns in SQL statements that write to a database (INSERT/UPDATE)
    public static PreparedStatement parameterizeColumns(Table table, PreparedStatement preparedStatement, Object o) {
        //Attempt to set column values in prepared statement to values in object passed in
        try {
            //Temporary integer variable holding the SQLTypeInt matching field type
            int fieldTypeInt = 0;

            //Iterate through columns and add field values to statement
            for (int i = 0; i < table.size(); i++) {
                /*
                //Attempt to set field type int to matching SQL data type
                fieldTypeInt = getSQLTypeInt(table.get(i).getProperty().getType());

                //Check if SQLTypeInt wasn't found and was set to 0
                if (fieldTypeInt == 0) {
                    throw new NoMatchingSQLTypeException(table.get(i).getProperty().getType().getName());
                }

                //Set the column value if SQLTypeInt was found
                preparedStatement.setObject(i + 1, table.get(i).getGetter().invoke(o), fieldTypeInt);

                 */

                //Use setIndividualParameter to parameterize column value
                preparedStatement = setIndividualParameter(i + 1, table.get(i).getGetter().invoke(o), preparedStatement);
            }

            //Return the parameterized SQL statement
            return preparedStatement;
        } catch (IllegalAccessException | InvocationTargetException | SQLException e) {
            e.printStackTrace();
        }

        //Return null if exception was caught
        return null;
    }

    //Method to generate the PreparedStatement for a create method (INSERT)
    public static PreparedStatement prepareCreateStatement(Repository repository, PreparedStatement preparedStatement, Object o) {
        //Try parameterizing sql statement
        preparedStatement = parameterizeColumns(repository.getValidGetterFields(), preparedStatement, o);
        System.out.println(preparedStatement);

        //Return null if try block was not successfully completed
        return preparedStatement;
    }

    //Method to generate the PreparedStatement for a read method (SELECT)
    public static PreparedStatement prepareReadStatement(Repository repository, PreparedStatement preparedStatement, Object o) {
        //Try parameterizing sql statement
        //Parameterize the where clause with primary key
        //May change this to allow query by custom field
        preparedStatement = parameterizeWhereClause(repository.getValidSetterFields(), preparedStatement, o, 1);
        System.out.println(preparedStatement);
        //Return parameterized prepared statement if successful
        return  preparedStatement;
    }

    //Method to generate PreparedStatement for an update method
    public static PreparedStatement prepareUpdateStatement(Repository repository, PreparedStatement preparedStatement, Object o) {
        //Try parameterizing SQL statement
        preparedStatement = parameterizeColumns(repository.getValidGetterFields(), preparedStatement, o);

        preparedStatement = parameterizeWhereClause(repository.getValidGetterFields(), preparedStatement, o, repository.getValidGetterFields().size() + 1);
        System.out.println(preparedStatement);
        return preparedStatement;
    }

    //Method to generate PreparedStatement for a delete method
    public static PreparedStatement prepareDeleteStatement(Repository repository, PreparedStatement preparedStatement, Object o) {
        preparedStatement = parameterizeWhereClause(repository.getValidGetterFields(), preparedStatement, o, 1);

        return preparedStatement;
    }
}
