import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        Object tmpObj;

        for (int i = 0; i < table.size(); i++) {
            column = table.get(i);

            try {
                //Read next result field into a temporary object
                tmpObj = readIndividualResultField(column, rs);

                //Call invoke function on setter method to attempt using the setter method on given object
                column.getSetter().invoke(t, tmpObj);
            } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
                ExceptionLogger.getExceptionLogger().log(e);
            }
        }

        //Return the object passed in with fields successfully read from the SQL query result
        return t;
    }

    //Method to read all primary keys from a result set
    public List<Object> readAllPrimaryKeys(Table table, ResultSet rs, T t) {
        //Column holding information about this table's primary key field
        Column primaryKeyColumn = table.getPrimaryKeyField();

        //List of objects to be returned
        List<Object> primaryKeys = new ArrayList<>();

        try {
            while (rs.next()) {
                //Read result field and add into list of primary keys
                primaryKeys.add(readIndividualResultField(primaryKeyColumn, rs));
            }
        } catch (SQLException e) {
            ExceptionLogger.getExceptionLogger().log(e);
        }

        //Return the list of primary keys
        return primaryKeys;
    }

    //Method to read all fields from a result set
    public List<T> readAll(Repository repository, ResultSet rs, T t) {
        List<T> results = new ArrayList<>();
        T tmp;

        try {
            while (rs.next()) {
                tmp = (T) repository.getFakeConstructor().invoke(t);

                tmp = readIndividualResultRow(repository.getValidSetterFields(), rs, tmp);

                results.add(tmp);
            }
        } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
            ExceptionLogger.getExceptionLogger().log(e);
        }

        return results;
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
