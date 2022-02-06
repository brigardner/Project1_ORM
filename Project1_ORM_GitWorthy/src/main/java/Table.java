import Annotations.PrimaryKey;

import java.util.ArrayList;

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
    public boolean add(Column column) {
        for (Column c : columns) {
            if (c.getFieldHash() == column.getFieldHash()) {
                return false;
            }
        }

        this.columns.add(column);
        return true;
    }

    //Method to remove a column from the table
    public void remove(int index) {
        this.columns.remove(index);
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
                writeableTable.add(this.get(i));
            }
        }

        //Return the sub table
        return writeableTable;
    }

    //Method to return a sub table of fields with valid setter methods
    public Table getValidSetterFields() {
        //Create a new table to be returned
        Table readableTable = new Table(this.getTableName());

        //Iterate through this table and add valid readable columns to array list
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).hasValidSetter()) {
                readableTable.add(this.get(i));
            }
        }

        //Return the sub table
        return readableTable;
    }

    //Method to return a sub table of fields that are not auto-increment
    public Table getWriteableFields() {
        //Create a new table to be returned
        Table writeableTable = new Table(this.getTableName());

        //Iterate through this table and add any fields that are not auto-increment
        for (int index = 0; index < this.size(); index++) {
            if (this.get(index).getProperty().isAnnotationPresent(PrimaryKey.class)) {
                if (this.get(index).getProperty().getAnnotation(PrimaryKey.class).autoIncrement()) {
                    continue;
                }
            }

            writeableTable.add(this.get(index));
        }

        //Return the sub table
        return writeableTable;
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

    //Override of toString method that converts table data into a String
    @Override
    public String toString() {
        //Create string to be returned
        StringBuilder tableString = new StringBuilder("Table \"" + this.getTableName() + "\"");

        //Iterate through the list of columns in this table and add field information to string
        for (Column c : this.columns) {
            tableString.append("\n\t").append(c);
        }

        return tableString.toString();
    }
}
