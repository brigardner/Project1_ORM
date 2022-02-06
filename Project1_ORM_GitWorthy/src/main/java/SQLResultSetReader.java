import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

//Class that contains methods that return objects
//Read from a SQL table
public class SQLResultSetReader<T> {

    //Method to read a single field of a result set and return a reference to a wrapper or string if successful
    //or null if not
    public Object readIndividualResultField(Column column, ResultSet rs) throws SQLException {
        //Get given object class type
        Class<?> c = column.getProperty().getType();

        //Attempt to read result into a field of
        if (c == Byte.class) return rs.getByte(column.getFieldName());
        else if (c == Short.class) return rs.getShort(column.getFieldName());
        else if (c == Integer.class) return rs.getInt(column.getFieldName());
        else if (c == Long.class) return rs.getLong(column.getFieldName());
        else if (c == Float.class) return rs.getFloat(column.getFieldName());
        else if (c == Double.class) return rs.getDouble(column.getFieldName());
        else if (c == Boolean.class) return rs.getBoolean(column.getFieldName());
        else if (c == String.class) return rs.getString(column.getFieldName());
        //Return null if retrieving from result set was unsuccessful
        return null;
    }

    //Method to read a single row in a result set
    public T readIndividualResultRow(Table table, ResultSet rs, T t) {
        //Temporary column object to hold information for each column in table
        Column column;

        for (int i = 0; i < table.size(); i++) {
            column = table.get(i);

            try {
                //Call invoke function on setter method to attempt using the setter method on given object
                column.getSetter().invoke(t, readIndividualResultField(column, rs));
            } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
                ExceptionLogger.getExceptionLogger().log(e);
            }
        }

        //Return the object passed in with fields successfully read from the SQL query result
        return t;
    }

    //Method to read generated keys from a result set
    public T readGeneratedKeys(Column primaryKeyColumn, ResultSet rs, T t) {
        try {
            //Call invoke function on setter method to attempt using it on a given object
            primaryKeyColumn.getSetter().invoke(t, readIndividualResultField(primaryKeyColumn, rs));
        } catch (IllegalAccessException | InvocationTargetException | SQLException e) {
            ExceptionLogger.getExceptionLogger().log(e);
        }

        //Return the object passed in with generated keys read from statement
        return t;
    }
}
