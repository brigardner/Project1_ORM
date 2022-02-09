import Annotations.Entity;
import Annotations.FakeConstructor;
import Annotations.PrimaryKey;
import Annotations.Property;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Repository<O> {
    //java.sql.Connection object allowing data to be stored into a SQL database
    protected Connection connection;

    //Instance of generic O used for many methods
    private O object;

    private SQLResultSetReader<O> reader;

    private Method fakeConstructor;

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

    //Boolean that tells whether the repository will leave console breadcrumbs
    private boolean breadCrumbsOn;

    //Constructor taking in only a generic
    public Repository(O o) {
        connection = ORMConnectionManager.getConnection();

        this.setObject(o);

        tableInitialized = this.initializeTable();
        setValidGetterFields(this.table.getValidGetterFields());
        setValidSetterFields(this.table.getValidSetterFields());
        setWritableFields(this.getValidGetterFields().getWriteableFields());

        reader = new SQLResultSetReader<>();

        breadCrumbsOn = false;
    }

    //Constructor taking in a generic and connection string
    public Repository(O o, String connectionString) {
        connection = ORMConnectionManager.getConnection(connectionString);

        this.setObject(o);

        tableInitialized = this.initializeTable();
        setValidGetterFields(this.table.getValidGetterFields());
        setValidSetterFields(this.table.getValidSetterFields());

        reader = new SQLResultSetReader<>();

        breadCrumbsOn = false;
    }

    //Constructor taking in a generic and individual parts of a connection string
    public Repository(O o, String hostname, String port, String dbname, String username, String password) {
        connection = ORMConnectionManager.getConnection(hostname, port, dbname, username, password);

        this.setObject(o);

        tableInitialized = this.initializeTable();
        setValidGetterFields(this.table.getValidGetterFields());
        setValidSetterFields(this.table.getValidSetterFields());

        reader = new SQLResultSetReader<>();

        breadCrumbsOn = false;
    }

    public void connect(String connectionString) {
        connection = ORMConnectionManager.getConnection(connectionString);
    }

    public void connect(String hostname, String port, String dbname, String username, String password) {
        connection = ORMConnectionManager.getConnection(hostname, port, dbname, username, password);
    }

    public O getObject() {
        return object;
    }

    public Method getFakeConstructor() {
        return fakeConstructor;
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

    public boolean isBreadCrumbsOn() {
        return breadCrumbsOn;
    }

    @Override
    public String toString() {
        return this.getTable().toString() + "\nFake constructor: " +
                ((this.getFakeConstructor() == null) ? "null" : this.getFakeConstructor().getName());
    }

    public void setObject(O object) {
        this.object = object;
    }

    public void setFakeConstructor(Method fakeConstructor) {
        this.fakeConstructor = fakeConstructor;
    }

    //Method to fill the table object with fields and methods - returns false if a major step fails
    public boolean initializeTable() {
        //Start by checking that the class has the Entity annotation and setting table name
        String tableName;

        if (!this.object.getClass().isAnnotationPresent(Entity.class)) {
            System.out.println("Class not entity.");
            return false;
        }

        tableName = this.object.getClass().getAnnotation(Entity.class).tableName();

        //Get class fields (private and public)
        Field[] fields = this.object.getClass().getDeclaredFields();

        //Get class methods
        Method[] methods = this.object.getClass().getMethods();

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

        //Iterate through the class methods and find one with the annotation FakeConstructor to add to the repository
        for (Method m : methods) {
            if (m.isAnnotationPresent(FakeConstructor.class)) {
                if (this.isValidFakeConstructor(this.object, m)) {
                    this.setFakeConstructor(m);
                    break;
                }
            }
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

    public void setBreadCrumbsOn(boolean breadCrumbsOn) {
        this.breadCrumbsOn = breadCrumbsOn;
    }

    public boolean isTableInitialized() {
        return tableInitialized;
    }

    public void setTableInitialized(boolean tableInitialized) {
        this.tableInitialized = tableInitialized;
    }

    //Method to test whether a given Method is a valid fake constructor - i.e. it returns the generic type and
    // has no parameters
    public boolean isValidFakeConstructor(O o, Method m) {
        //Get array of potential fake constructor parameters
        Class[] paramTypes = m.getParameterTypes();

        //If parameter list is NOT empty, return false
        if (paramTypes.length != 0) {
            return false;
        }

        //Return whether the generic type matches the potential fake constructor return type
        return o.getClass() == m.getReturnType();
    }

    //Method to test whether this repository has a valid fake constructor method that instantiates a new object of
    // the generic type
    public boolean hasValidFakeConstructor(O o) {
        //Test if fake constructor field is null - return false if so
        if (this.fakeConstructor == null) {
            return false;
        }

        //If not null, return the result of isValidFakeConstructor
        return isValidFakeConstructor(o, this.fakeConstructor);
    }

    //Method that prints out given string to console if the breadCrumbs variable is true
    public void breadCrumb(String s) {
        if (this.isBreadCrumbsOn()) System.out.println(s);
    }

    //Method to perform basic create operation
    public O create(O o) {
        this.breadCrumb("");
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            this.breadCrumb("Create failed: table not initialized");
            return null;
        }

        //Check if the object passed in is null
        if (o == null) {
            this.breadCrumb("Create failed: object passed in null");
            return null;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeCreateSQLString(this);
        this.breadCrumb("Generated SQL String: " + sql);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            this.breadCrumb("Create failed: generated SQL string empty");
            return null;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Store the primary key field
            Column primaryKeyColumn = this.getValidSetterFields().getPrimaryKeyField();
            this.breadCrumb("Primary key field: " + primaryKeyColumn);
            //Check if the primary key column was in valid setter list and if primary key field has autoIncrement set
            // true; set statement to return generated keys if so
            if (primaryKeyColumn != null && primaryKeyColumn.getProperty().getAnnotation(PrimaryKey.class).autoIncrement()) {
                preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                this.breadCrumb("Auto-increment - generating keys");
            }
            else {
                //Attempt to create a PreparedStatement with the generated SQL string
                preparedStatement = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
                this.breadCrumb("Non-auto-increment - not generating keys");
            }

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareCreateStatement(this, preparedStatement, o);
            this.breadCrumb("Prepared statement generated: " + preparedStatement);

            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                this.breadCrumb("Create failed: prepared statement null");
                return null;
            }

            //Attempt to execute the prepared statement
            preparedStatement.executeUpdate();
            this.breadCrumb("Create executed");

            //Check if primary key column was in valid setter list
            if (primaryKeyColumn != null) {
                //Check if primary key field has autoIncrement set true; attempt to set primary key field to generated
                // key if so
                if (primaryKeyColumn.getProperty().getAnnotation(PrimaryKey.class).autoIncrement()) {
                    ResultSet keys = preparedStatement.getGeneratedKeys();
                    this.breadCrumb("Generated keys");

                    //Check if there are any generated keys
                    if (keys.next()) {
                        o = reader.readGeneratedKeys(primaryKeyColumn, keys, o);
                    }
                }
            }

            //Return inserted object if successful
            return o;
        } catch (SQLException e) {
            //Return null if unsuccessful
            ORMExceptionLogger.getExceptionLogger().log(e);
            this.breadCrumb("SQL exception thrown");
            return null;
        }
    }

    public O read(O o, Object obj) {
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            this.breadCrumb("Read failed: table not initialized");
            return null;
        }

        //Check if the object passed in is null
        if (o == null) {
            this.breadCrumb("Read failed: object passed in null");
            return null;
        }

        //Check if the given object matches the type of the main table's primary key field
        Column primaryKeyField  = this.table.getPrimaryKeyField();
        if (primaryKeyField.getProperty().getType() != obj.getClass()) {
            this.breadCrumb("Value type did not match primary key type");
            return null;
        }

        try {
            this.table.getPrimaryKeyField().getSetter().invoke(o, obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            ORMExceptionLogger.getExceptionLogger().log(e);
            return null;
        }

        return read(o);
    }

    //Method to perform basic read operation, taking in a valid primary key value
    public O read(O o) {
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            this.breadCrumb("Read failed: table not initialized");
            return null;
        }

        //Check if the object passed in is null
        if (o == null) {
            this.breadCrumb("Read failed: object passed in null");
            return null;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeReadSQLString(this);
        this.breadCrumb("Generated SQL String: " + sql);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            this.breadCrumb("Read failed: generated SQL string empty");
            return null;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Attempt to create a PreparedStatement with the generated SQL string
            preparedStatement = connection.prepareStatement(sql);

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareReadStatement(this, preparedStatement, o);
            this.breadCrumb("Prepared statement generated: " + preparedStatement);

            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                this.breadCrumb("Read failed: prepared statement null");
                return null;
            }

            //Attempt to execute prepared statement and save results to result set
            ResultSet rs = preparedStatement.executeQuery();
            this.breadCrumb("Read executed");

            //Check if the result set is empty and return null if so
            if (!rs.next()) {
                this.breadCrumb("Result set empty");
                return null;
            }

            //Attempt to read data from result set into generic passed in
            o = reader.readIndividualResultRow(this.getValidSetterFields(), rs, o);

            this.breadCrumb("Object read in: " + o);
            //Return the filled in generic if successful
            return o;
        } catch (SQLException e) {
            ORMExceptionLogger.getExceptionLogger().log(e);
            this.breadCrumb("SQL exception thrown");
            return null;
        }
    }

    //Method to read all rows from a table and return a list of type generic
    public List<O> readAll() {
        //Create list to be returned
        List<O> results = new ArrayList<>();

        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            this.breadCrumb("Read failed: table not initialized");
            return null;
        }

        //Check if repository has a fake constructor
        if (!this.hasValidFakeConstructor(this.getObject())) {
            return null;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeReadAllSQLString(this);
        this.breadCrumb("Generated SQL String: " + sql);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            this.breadCrumb("Read failed: generated SQL string empty");
            return null;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Attempt to create a PreparedStatement with the generated SQL string
            preparedStatement = connection.prepareStatement(sql);

            //Attempt to execute prepared statement and save results to result set
            ResultSet rs = preparedStatement.executeQuery();

            //Check if the result set is empty and return null if so
            if (!rs.next()) {
                this.breadCrumb("Result set empty");
                return null;
            }

            //Attempt to read data from result set into a list of type generic
            results = reader.readAll(this, rs, this.object);
        } catch (SQLException e) {
            this.breadCrumb("SQL exception thrown");
            ORMExceptionLogger.getExceptionLogger().log(e);
        }

        return results;
    }

    //Method to perform basic update operation on a given object
    public O update(O o) {
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            this.breadCrumb("Update failed: table not initialized");
            return null;
        }

        //Check if the object passed in is null
        if (o == null) {
            this.breadCrumb("Update failed: object passed in null");
            return null;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeUpdateSQLString(this);
        this.breadCrumb("Generated SQL String: " + sql);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            this.breadCrumb("Update failed: generated SQL string empty");
            return null;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Attempt to create a PreparedStatement with the generated SQL string
            preparedStatement = connection.prepareStatement(sql);

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareUpdateStatement(this, preparedStatement, o);
            this.breadCrumb("Prepared statement generated: " + preparedStatement);
            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                this.breadCrumb("Update failed: prepared statement null");
                return null;
            }

            //Attempt to execute the prepared statement
            preparedStatement.executeUpdate();
            this.breadCrumb("Create executed");

            //Return inserted object if successful
            return o;
        } catch (SQLException e) {
            ORMExceptionLogger.getExceptionLogger().log(e);
            this.breadCrumb("SQL exception thrown");
            return null;
        }
    }

    //Method to perform basic delete operation on a given object
    public boolean delete(O o) {
        //Check if main table is initialized
        if (!this.isTableInitialized()) {
            this.breadCrumb("Delete failed: table not initialized");
            return false;
        }

        //Check if the object passed in is null
        if (o == null) {
            this.breadCrumb("Delete failed: object passed in null");
            return false;
        }

        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeDeleteSQLString(this);
        this.breadCrumb("Generated SQL String: " + sql);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            this.breadCrumb("Delete failed: generated SQL string empty");
            return false;
        }

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Attempt to create a PreparedStatement with the generated SQL string
            preparedStatement = connection.prepareStatement(sql);

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareDeleteStatement(this, preparedStatement, o);
            this.breadCrumb("Prepared statement generated: " + preparedStatement);

            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                this.breadCrumb("Delete failed: prepared statement null");
                return false;
            }

            //Attempt to execute the prepared statement
            preparedStatement.executeUpdate();
            this.breadCrumb("Delete executed");

            //return true if successful
            return true;
        } catch (SQLException e) {
            ORMExceptionLogger.getExceptionLogger().log(e);
            this.breadCrumb("SQL exception thrown");
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
            ORMExceptionLogger.getExceptionLogger().log(e);
            return null;
        }

        //Return entered object
        return o;

    }

     */
}
