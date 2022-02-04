import Annotations.PrimaryKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Column {
    private String fieldName;
    private int fieldHash;
    private Field property;
    private Method getter;
    private Method setter;

    public Column() {
        this.fieldName = "";
        this.setFieldHash();
    }

    public Column(String fieldName, Field property) {
        this.fieldName = fieldName;
        this.property = property;
        this.setFieldHash();
    }

    public Column(String fieldName, Field property, Method getter, Method setter) {
        this.fieldName = fieldName;
        this.property = property;
        this.getter = getter;
        this.setter = setter;
        this.setFieldHash();
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getFieldHash() {
        return fieldHash;
    }

    public Field getProperty() {
        return property;
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
        this.setFieldHash();
    }

    public void setFieldHash() {
        this.fieldHash = this.fieldName.hashCode();
    }

    public void setProperty(Field property) {
        this.property = property;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    //Method to test whether a field has a valid getter method
    public boolean hasValidGetter() {
        //Test if getter field is null - return false if so
        if (this.getter == null) {
            return false;
        }

        //If not null, return whether the field type matches getter return type
        Class returnType = this.getter.getReturnType();
        return this.property.getType() == returnType;
    }

    //Method to test whether a field has a valid setter method
    public boolean hasValidSetter() {
        //Test if setter field is null - return false if so
        if (this.setter == null) {
            return false;
        }

        //If not null, get array of setter parameters
        Class[] paramTypes = this.setter.getParameterTypes();
        //If parameter list is less than or greater than 1, return false
        if (paramTypes.length != 1) {
            return false;
        }

        //If parameter list only has one parameter, return whether field type matches setter parameter type
        return this.property.getType() == paramTypes[0];
    }

    //Method to test whether a given field has the PrimaryKey annotation
    public boolean isValidPrimaryKey() {
        return this.property.isAnnotationPresent(PrimaryKey.class) && this.hasValidGetter();
    }

    //Method to convert Column information to string
    @Override
    public String toString() {
        return "Column: " + fieldName + ", Class field: " + property.getName() + ", Getter: " + getter.getName() +
                ", Setter: " + setter.getName() + ", is primary key: " + isValidPrimaryKey();
    }
}