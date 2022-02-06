import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//Class that contains methods that return PreparedStatement objects
//Using SQLStringScriptor methods to define PreparedStatement SQL statement strings
public class SQLPreparedStatementScriptor {
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

            //Use setIndividualParameter to parameterize the where clause
            preparedStatement = setIndividualParameter(whereClauseIndex, primaryKeyColumn.getGetter().invoke(o), preparedStatement);
            //Return the parameterized prepared SQL statement
            return preparedStatement;
        } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
            ExceptionLogger.getExceptionLogger().log(e);
        }

        //Return null if try block was not successfully completed
        return null;
    }

    //Method to parameterize columns in SQL statements that write to a database (INSERT/UPDATE)
    public static PreparedStatement parameterizeColumns(Table table, PreparedStatement preparedStatement, Object o) {
        //Attempt to set column values in prepared statement to values in object passed in
        try {
            //Iterate through columns and add field values to statement
            for (int i = 0; i < table.size(); i++) {
                preparedStatement = setIndividualParameter(i + 1, table.get(i).getGetter().invoke(o), preparedStatement);
            }

            //Return the parameterized SQL statement
            return preparedStatement;
        } catch (IllegalAccessException | InvocationTargetException | SQLException e) {
            ExceptionLogger.getExceptionLogger().log(e);
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
