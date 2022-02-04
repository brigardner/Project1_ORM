import java.util.ArrayList;
import java.util.List;

public class Table {
    private String tableName;
    private ArrayList<Column> columns;

    public Table(String tableName) {
        this.tableName = tableName;
        this.columns = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    private void setColumns(ArrayList<Column> columns) {
        this.columns = columns;
    }

    //Method to return a Column object from the columns list with given index
    public Column get(int index) {
        return this.columns.get(index);
    }

    //Method to add column to table - returns false if fieldName already exists
    public boolean addColumn(Column column) {
        for (Column c : columns) {
            if (c.getFieldHash() == column.getFieldHash()) {
                return false;
            }
        }

        this.columns.add(column);
        return true;
    }

    //Method to return size of columns array list
    public int size() {
        return this.columns.size();
    }

    //Method to return a sub table of fields with valid getters
    public Table getValidGetterFields() {
        //Create new table to be returned
        Table writeableTable = new Table(this.getTableName());

        //Iterate through this table and add valid writeable columns to array list
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).hasValidGetter()) {
                writeableTable.addColumn(this.get(i));
            }
        }

        //Return the sub table
        return writeableTable;
    }

    //Probably will remove; getValidGetterFields returns more useful table and has clearer name
    //Method to return a list of valid writeable fields
    public List<Column> getWriteableFieldsList() {
        //Create array list of columns
        ArrayList<Column> columns = new ArrayList<>();

        //Iterate through table and add valid writeable columns to array list
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).hasValidGetter()) {
                columns.add(this.get(i));
            }
        }

        //Return the array list
        return columns;
    }

    //Method to return a sub table of fields with valid setter methods
    public Table getValidSetterFields() {
        //Create a new table to be returned
        Table readableTable = new Table(this.getTableName());

        //Iterate through this table and add valid readable columns to array list
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).hasValidSetter()) {
                readableTable.addColumn(this.get(i));
            }
        }

        //Return the sub table
        return readableTable;
    }

    //Probably will remove; getValidSetterFields returns more useful table and has clearer name
    //Method to return a list of valid readable fields
    public List<Column> getReadableFieldsList() {
        //Create array list of columns
        ArrayList<Column> columns = new ArrayList<>();

        //Iterate through table and add valid readable columns to array list
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).hasValidSetter()) {
                columns.add(this.get(i));
            }
        }

        //Return the array list
        return columns;
    }

    //Method to return whether the table holds a valid primary key field
    public boolean hasValidPrimaryKey() {
        return (this.getPrimaryKeyFieldIndex() >= 0);
    }

    //Method to retrieve index of single primary key field
    //Returns -1 if not found
    public int getPrimaryKeyFieldIndex() {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).isValidPrimaryKey()) {
                return i;
            }
        }

        return -1;
    }

    //Method to retrieve Column containing field with primary key annotation
    public Column getPrimaryKeyField() {
        return this.get(getPrimaryKeyFieldIndex());
    }

    //Method to set columns to empty arraylist
    public void emptyColumns() {
        this.columns = new ArrayList<>();
    }
}
