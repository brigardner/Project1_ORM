import Annotations.Entity;
import Annotations.Getter;
import Annotations.Property;
import Annotations.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.*;

public class Repository<O> {
    //java.sql.Connection object allowing data to be stored into a SQL database
    protected final Connection connection;

    private static SQLPreparedStatementScriptor scriptor;

    //Repository Table object that holds information about storing/retrieving data from SQL table
    //such as fields and respective getter/setter methods
    private Table table;

    //Boolean to check if table object is valid
    private boolean tableInitialized;

    //No-arg constructor which sets the java.sql.Connection object using the ConnectionManager class
    public Repository(O o) {
        connection = ConnectionManager.getConnection();
        tableInitialized = this.initializeTable(o);
        scriptor = SQLPreparedStatementScriptor.getScriptor();
    }

    public Table getTable() {
        return table;
    }

    public boolean initializeTable(O o) {
        //Start by checking that the class has the Entity annotation and setting table name
        String tableName;

        if (!o.getClass().isAnnotationPresent(Entity.class)) {
            System.out.println("Class not entity.");
            return false;
        }
        else {
            tableName = o.getClass().getAnnotation(Entity.class).tableName();
        }

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
                System.out.println(column.getFieldName() + " added to table: " + table.addColumn(column));
            }
        }

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

        //Return true if table initialization was successful
        return true;
    }

    public boolean isTableInitialized() {
        return tableInitialized;
    }

    public void setTableInitialized(boolean tableInitialized) {
        this.tableInitialized = tableInitialized;
    }

    //Method to return integer form of SQL type (for primitives)
    public int getSQLTypeInt(Type type) {
        //Only allow primitives & strings
        //for now
        String typeName = type.getTypeName();

        //Switch statement to return integers corresponding to SQL types
        switch (typeName) {
            case "char": return Types.CHAR;
            case "int": return Types.INTEGER;
            case "double": return Types.DOUBLE;
            case "boolean": return Types.BOOLEAN;
            default: return 0;
        }
    }

    //Method to return list of all fields, regardless of access modifier
    public Field[] getAllFields(Class c) {
        return c.getDeclaredFields();
    }

    public Property[] getFieldAnnotations(Class c) {
        Field[] fields = getAllFields(c);
        Property[] annotations = new Property[fields.length];
        for (int i = 0; i < fields.length; i++) {
            annotations[i] = fields[i].getAnnotation(Property.class);
        }
        return annotations;
    }

    //Method to perform basic create operation
    public O create(O o) {
        //Create SQL string generated by SQLStringScriptor
        String sql = SQLStringScriptor.makeCreateSQLString(this);

        //Check if SQL string creation failed and returned null
        if (sql.equals("")) {
            return null;
        }

        //Test what sql was generated
        System.out.println(sql);

        //Declare PreparedStatement object to be run
        PreparedStatement preparedStatement;

        try {
            //Attempt to create a PreparedStatement with the generated SQL string
            preparedStatement = connection.prepareStatement(sql);

            //Attempt to parameterize the statement
            preparedStatement = SQLPreparedStatementScriptor.prepareCreateStatement(this, preparedStatement, o);

            //Check if the prepared statement was set to null due to exceptions
            if (preparedStatement == null) {
                return null;
            }

            //Attempt to execute the prepared statement
            preparedStatement.executeUpdate();

            //Return inserted object if successful
            return o;
        } catch (SQLException e) {
            //Return null if unsuccessful
            e.printStackTrace();
            return null;
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
            e.printStackTrace();
            return null;
        }

        //Return entered object
        return o;

    }

     */
}
