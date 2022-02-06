import Annotations.Entity;
import Annotations.PrimaryKey;
import Annotations.Property;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.List;

public class Repository<O> {
    //java.sql.Connection object allowing data to be stored into a SQL database
    protected final Connection connection;

    private SQLResultSetReader<O> reader;

    //Repository Table object that holds information about storing/retrieving data from SQL table
    //such as fields and respective getter/setter methods
    private Table table;

    //Sub tables that hold columns with valid getter/setter methods
    private Table validGetterFields;
    private Table validSetterFields;

    //Sub table that holds columns with valid getter methods and are not auto-increment primary keys
    private Table writableFields;

    //Boolean to check if table object is valid
    private boolean tableInitialized;

    //Constructor taking in only a generic
    public Repository(O o) {
        connection = ConnectionManager.getConnection();

        tableInitialized = this.initializeTable(o);
        setValidGetterFields(this.table.getValidGetterFields());
        setValidSetterFields(this.table.getValidSetterFields());
        setWritableFields(this.getValidGetterFields().getWriteableFields());

        reader = new SQLResultSetReader<>();
    }

    //Constructor taking in a generic and connection string
    public Repository(O o, String connectionString) {
        connection = ConnectionManager.getConnection(connectionString);

        tableInitialized = this.initializeTable(o);
        setValidGetterFields(this.table.getValidGetterFields());
        setValidSetterFields(this.table.getValidSetterFields());

        reader = new SQLResultSetReader<>();
    }

    //Constructor taking in a generic and individual parts of a connection string
    public Repository(O o, String hostname, String port, String dbname, String username, String password) {
        connection = ConnectionManager.getConnection(hostname, port, dbname, username, password);

        tableInitialized = this.initializeTable(o);
        setValidGetterFields(this.table.getValidGetterFields());
        setValidSetterFields(this.table.getValidSetterFields());

        reader = new SQLResultSetReader<>();
    }

    public String getTableName() { return table.getTableName(); }

    public Table getTable() {
        return table;
    }

    public Table getValidGetterFields() {
        return validGetterFields;
    }

    public Table getValidSetterFields() {
        return validSetterFields;
    }

    public Table getWritableFields() {
        return writableFields;
    }

    public boolean initializeTable(O o) {
        //Start by checking that the class has the Entity annotation and setting table name
        String tableName;

        if (!o.getClass().isAnnotationPresent(Entity.class)) {
            System.out.println("Class not entity.");
            return false;
        }

        tableName = o.getClass().getAnnotation(Entity.class).tableName();

        //Get class fields (private and public)
        Field[] fields = o.getClass().getDeclaredFields();

        //Get class methods
        Method[] methods = o.getClass().getMethods();

        //Create and populate a Table object with Columns
        this.table = new Table(tableName);

        //Temporary Column object to store fieldName and Property before attempting to store into table
        Column column;

        //Temporary fieldName string & fieldHash int
        String fieldName;
        int fieldHash;

        for (Field f : fields) {
            //Check that the field has the Property annotation
            if (f.isAnnotationPresent(Property.class)) {
                column = new Column();
                column.setFieldName(f.getAnnotation(Property.class).fieldName());
                column.setProperty(f);

                //Attempt to add column to table
                table.add(column);
            }
        }

        //Iterate through the table columns and find valid getter and setter methods appropriate for a given field
        for (int index = 0; index < table.size(); index++) {
            //Get a list of potential getter and setter methods, based on either use of Getter/Setter annotation or
            // method name
            List<Method> potentialGetterMethods = table.get(index).getPotentialGetter(methods);
            List<Method> potentialSetterMethods = table.get(index).getPotentialSetter(methods);

            //Find first valid getter/setter method out of returned list
            Method getter = table.get(index).getValidGetter(potentialGetterMethods);
            Method setter = table.get(index).getValidSetter(potentialSetterMethods);

            //Set the column's getter/setter method to the appropriate valid methods, if found
            table.get(index).setGetter(getter);
            table.get(index).setSetter(setter);
        }

/*
        //Iterate through methods to find getters/setters
        //Add them to appropriate column in table if fieldNames match
        for (Method m : methods) {
            //Check that the field has the Getter annotation
            if (m.isAnnotationPresent(Getter.class)) {
                //Check that the column exists by checking against field name hash
                fieldName = m.getAnnotation(Getter.class).fieldName();
                fieldHash = fieldName.hashCode();

                for (Column c : table.getColumns()) {
                    //Add Getter method to Column if field name found
                    if (fieldHash == c.getFieldHash()) {
                        c.setGetter(m);
                    }
                }
            }
            //Check that the field has the Setter annotation
            else if (m.isAnnotationPresent(Setter.class)) {
                //Check that the column exists by checking against field name hash
                fieldName = m.getAnnotation(Setter.class).fieldName();
                fieldHash = fieldName.hashCode();

                for (Column c : table.getColumns()) {
                    //Add Setter method to Column if field name found
                    if (fieldHash == c.getFieldHash()) {
                        c.setSetter(m);
                    }
                }
            }
        }
*/
        //Return true if table initialization was successful
        return true;
    }

    public void setValidGetterFields(Table validGetterFields) {
        if (this.isTableInitialized()) {
            this.validGetterFields = validGetterFields;
        }
    }

    public void setValidSetterFields(Table validSetterFields) {
        if (this.isTableInitialized()) {
            this.validSetterFields = validSetterFields;
        }
    }

    public void setWritableFields(Table writableFields) {
        if (this.isTableInitialized()) {
            this.writableFields = writableFields;
        }
    }

    public boolean isTableInitialized() {
        return tableInitialized;
    }

    public void setTableInitialized(boolean tableInitialized) {
        this.tableInitialized = tableInitialized;
    }

    //Method to perform basic create operation
    public O create(O o) {
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            return null;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeCreateSQLString(this);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            return null;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Store the primary key field
            Column primaryKeyColumn = this.getValidSetterFields().getPrimaryKeyField();

            //Check if the primary key column was in valid setter list and if primary key field has autoIncrement set
            // true; set statement to return generated keys if so
            if (primaryKeyColumn != null && primaryKeyColumn.getProperty().getAnnotation(PrimaryKey.class).autoIncrement()) {
                preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }
            else {
                //Attempt to create a PreparedStatement with the generated SQL string
                preparedStatement = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
            }

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareCreateStatement(this, preparedStatement, o);

            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                return null;
            }

            //Attempt to execute the prepared statement
            preparedStatement.executeUpdate();

            //Check if primary key column was in valid setter list
            if (primaryKeyColumn != null) {
                //Check if primary key field has autoIncrement set true; attempt to set primary key field to generated
                // key if so
                if (primaryKeyColumn.getProperty().getAnnotation(PrimaryKey.class).autoIncrement()) {
                    ResultSet keys = preparedStatement.getGeneratedKeys();

                    //Check if there are any generated keys
                    if (keys.next()) {
                        reader.readGeneratedKeys(primaryKeyColumn, keys, o);
                    }
                }
            }

            //Return inserted object if successful
            return o;
        } catch (SQLException e) {
            //Return null if unsuccessful
            ExceptionLogger.getExceptionLogger().log(e);
            return null;
        }
    }

    public O read(O o, Object obj) {
        //Check if the given object matches the type of the main table's primary key field
        Column primaryKeyField  = this.table.getPrimaryKeyField();
        if (primaryKeyField.getProperty().getType() != obj.getClass()) {
            System.out.println("Value type did not match primary key type");
            return null;
        }

        try {
            this.table.getPrimaryKeyField().getSetter().invoke(o, obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            ExceptionLogger.getExceptionLogger().log(e);
            return null;
        }

        return read(o);
    }

    //Method to perform basic read operation, taking in a valid primary key value
    public O read(O o) {
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            return null;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeReadSQLString(this);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            return null;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Attempt to create a PreparedStatement with the generated SQL string
            preparedStatement = connection.prepareStatement(sql);

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareReadStatement(this, preparedStatement, o);

            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                return null;
            }

            //Attempt to execute prepared statement and save results to result set
            ResultSet rs = preparedStatement.executeQuery();

            //Check if the result set is empty and return null if so
            if (!rs.next()) {
                return null;
            }

            //Attempt to read data from result set into generic passed in
            o = reader.readIndividualResultRow(this.getValidSetterFields(), rs, o);

            //Return the filled in generic if successful
            return o;
        } catch (SQLException e) {
            ExceptionLogger.getExceptionLogger().log(e);
            return null;
        }
    }

    //Method to perform basic update operation on a given object
    public O update(O o) {
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            return null;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeUpdateSQLString(this);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            return null;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Attempt to create a PreparedStatement with the generated SQL string
            preparedStatement = connection.prepareStatement(sql);

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareUpdateStatement(this, preparedStatement, o);

            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                return null;
            }

            //Attempt to execute the prepared statement
            preparedStatement.executeUpdate();

            //Return inserted object if successful
            return o;
        } catch (SQLException e) {
            ExceptionLogger.getExceptionLogger().log(e);
            return null;
        }
    }

    //Method to perform basic delete operation on a given object
    public boolean delete(O o) {
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            return false;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeDeleteSQLString(this);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            return false;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Attempt to create a PreparedStatement with the generated SQL string
            preparedStatement = connection.prepareStatement(sql);

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareDeleteStatement(this, preparedStatement, o);

            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                return false;
            }

            //Attempt to execute the prepared statement
            preparedStatement.executeUpdate();

            //return true if successful
            return true;
        } catch (SQLException e) {
            ExceptionLogger.getExceptionLogger().log(e);
            return false;
        }
    }

    //Old create method, left for reference
    /*

    public O create(O o) {

        //Start the SQL insert statement
        String sql = "INSERT INTO ";


        //Add table name to sql string
        sql += tableName + " (";


        //Create string containing all columns to be filled from o
        String columnsInserted;

        //Check if any fields exist that are not auto-generated
        //...not sure how to do this yet


        //Make array of field names and fill from fields
        String[] fieldNames = new String[fields.length];

        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }

        if (fieldNames.length > 0) {
            //Add the first field name to the sql statement if not empty
            String fieldNamesSQL = fieldNames[0];

            //Add additional field names
            for (int i = 1; i < fieldNames.length; i++) {
                fieldNamesSQL += ", " + fieldNames[i];
            }

            //Add fieldNamesSQL to sql statement
            sql += fieldNamesSQL;
        }
        else {
            return null;
        }

        sql += ") VALUES (";

        //Add ?'s to be replaced by prepared statement methods
        //Add one for first field
        sql += "?";

        //Add more for additional fields
        for (int i = 1; i < fields.length; i++) {
            sql += ", ?";
        }

        //Finish sql statement with closing parentheses
        sql += ")";

        //Attempt to prepare statement
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            for (int i = 0; i < fields.length; i++) {
                preparedStatement.setObject(i + 1, null, getSQLTypeInt(fields[i].getType()));
            }
        } catch (SQLException e) {
            ExceptionLogger.getExceptionLogger().log(e);
            return null;
        }

        //Return entered object
        return o;

    }

     */
}
