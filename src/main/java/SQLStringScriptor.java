import java.util.List;

//Class that contains methods to create Strings to define PreparedStatements with
public class SQLStringScriptor {
    //Method to create field name list
    public static String getFieldNameList(Table validGetterFields) {
        //Create return string
        String fieldNames = "";

        //Fill string with field names if given columns list not empty
        if (validGetterFields.size() > 0) {
            //Add first field name to string
            fieldNames += validGetterFields.get(0).getFieldName();

            //Iterate through remaining columns and add field names
            for (int i = 1; i < validGetterFields.size(); i++) {
                fieldNames += ", " + validGetterFields.get(i).getFieldName();
            }
        }

        //Return field name list string, whether empty or not
        return fieldNames;
    }

    //Method to create placeholder list ('?' to be replaced by jdbc logic) based on given Property list size
    public static String getPlaceholderList(int listSize) {
        //Create return string
        String placeholderList = "";

        //Fill string with placeholders if list size > 0
        if (listSize > 0) {
            //Add first placeholder
            placeholderList += "?";

            //Iterate through list size and add ", ?" each time
            for (int i = 1; i < listSize; i++) {
                placeholderList += ", ?";
            }
        }

        //Return placeholder list string, empty or not
        return placeholderList;
    }

    //Method to create WHERE clause for searches based on primary key
    public static String getWhereClause(Table validGetterFields) {
        //Create return string
        String whereClause = "";

        //Variable to store index of primary key in table
        int index = validGetterFields.getPrimaryKeyField();

        //Return with empty string if no valid primary key field found
        if (index < 0) {
            return "";
        }

        //Add "WHERE " to return string
        whereClause += " WHERE ";

        //Add field name to return string
        whereClause += validGetterFields.get(index).getFieldName();

        //Add " = ?" to end of clause
        whereClause += " = ?";

        //Return where clause
        return whereClause;
    }

    //Method to create SET clause for update functions
    public static String getSetClause(Table validGetterFields) {
        //Create return string
        String setClause = "";

        //Fill string with field names if validGetterFields not empty
        if (validGetterFields.size() > 0) {
            //Add "SET" and first field name to string
            setClause += " SET " + validGetterFields.get(0).getFieldName() + " = ?";

            //Iterate through remaining validGetterFields and add field names
            for (int i = 1; i < validGetterFields.size(); i++) {
                setClause += ", " + validGetterFields.get(i).getFieldName() + " = ?";
            }
        }

        //Return set clause string, whether empty or not
        return setClause;
    }

    //Method to generate the SQL for a create method (INSERT)
    public static String makeCreateSQLString(Repository repository) {
        //Create fieldNameList string to be added to SQL statement
        String fieldNameList = getFieldNameList(repository.getValidGetterFields());

        //Create placeholderList string to be added to SQL statement
        String placeholderList = getPlaceholderList(repository.getValidGetterFields().size());

        //Begin building SQL statement with table name
        String sql = "INSERT INTO " + repository.getTableName();

        //Add the field names
        sql += " (" + fieldNameList + ") ";

        //Add placeholders
        sql += " VALUES (" + placeholderList + ")";

        //Return completed String
        return sql;
    }

    //Method to generate the SQL for a read method (SELECT)
    public static String makeReadSQLString(Repository repository) {
        //Check if the table has a valid primary key to query with
        //Return empty string if not
        if (!repository.getValidGetterFields().hasValidPrimaryKey()) {
            return "";
        }

        //Create array list of valid read columns
        List<Column> columns = repository.getTable().getReadableFieldsList();

        //Return an empty string if table has no valid read columns
        if (columns.size() == 0) {
            return "";
        }

        //Create fieldNameList string to be added to SQL statement
        String fieldNameList = getFieldNameList(repository.getValidSetterFields());

        //Create where clause to be added to SQL statement
        String whereClause = getWhereClause(repository.getValidGetterFields());

        //Begin building SQL statement with fieldNameList
        String sql = "SELECT " + fieldNameList;

        //Add table to read from
        sql += " FROM " + repository.getTableName();

        //Add where clause
        sql += whereClause;

        //Return built SQL statement
        return sql;
    }

    //Method to generate the SQL for an update method
    public static String makeUpdateString(Repository repository) {
        //Check if the table has a valid primary key to query with
        //Return empty string if not
        if (!repository.getValidGetterFields().hasValidPrimaryKey()) {
            return "";
        }

        //Create array list of valid write columns
        List<Column> columns = repository.getTable().getWriteableFieldsList();

        //Return an empty string if table has no valid write columns
        if (repository.getValidGetterFields().size() == 0) {
            return "";
        }

        //Create fieldNameList to be added to SQL statement
        String fieldNameList = getFieldNameList(repository.getValidGetterFields());

        //Create setClause to be added to SQL statement
        String setClause = getSetClause(repository.getValidGetterFields());

        //Create whereClause to be added to SQL statement
        String whereClause = getWhereClause(repository.getValidGetterFields());

        //Begin building SQL statement
        String sql = "UPDATE " + repository.getTableName();

        //Add setClause
        sql += setClause;

        //Add whereClause
        sql += whereClause;

        //Return built sql statement
        return sql;
    }

    //Method to generate the SQL for a delete method
    public static String makeDeleteString(Repository repository) {
        //Check if the table has a valid primary key to query with
        //Return empty string if not
        if (!repository.getValidGetterFields().hasValidPrimaryKey()) {
            return "";
        }

        //Create whereClause to be added to SQL statement
        String whereClause = getWhereClause(repository.getValidGetterFields());

        //Begin building SQL statement
        String sql = "DELETE FROM " + repository.getTableName();

        //Add whereClause
        sql += whereClause;

        //Return built sql statement
        return sql;
    }
}
