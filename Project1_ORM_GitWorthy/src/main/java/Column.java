import Annotations.Getter;
import Annotations.PrimaryKey;
import Annotations.Property;
import Annotations.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//Class that holds information pertaining to how a given field in a Java class relates to a SQL database table
//Such as the field name as found in the database table, the hash of that name for rapid lookup/comparison, and
//public getter/setter methods that are connected to the field
public class Column {
    //Member variables
    private String fieldName;
    private int fieldHash;
    private Field property;
    private Method getter;
    private Method setter;

    //No args constructor
    public Column() {
        this.fieldName = "";
        this.setFieldHash();
    }

    //Constructor with field name and field object parameters
    public Column(String fieldName, Field property) {
        this.fieldName = fieldName;
        this.property = property;
        this.setFieldHash();
    }

    //Constructor with all settable fields as parameters
    public Column(String fieldName, Field property, Method getter, Method setter) {
        this.fieldName = fieldName;
        this.property = property;
        this.getter = getter;
        this.setter = setter;
        this.setFieldHash();
    }

    //POJO getter/setter methods
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

    //Changes to field name always also set the field hash to a new value corresponding to the name
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
        this.setFieldHash();
    }

    //FieldHash setter method that takes in no parameters but uses the current field name value to determine its value
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

    //Method to return a field's potential getter name depending on the name of the field in the java class
    private String potentialGetterName() {
        //Check if the property field is empty and return an empty string if so
        if (this.getProperty() == null) {
            return "";
        }

        //Create a String to be returned
        String potentialGetterName;

        //Create string holding the column's java field name
        String javaFieldName = this.getProperty().getName();

        //Get the first character of the field name and set to uppercase to match camel casing
        char firstLetterCapitalized = Character.toUpperCase(javaFieldName.charAt(0));

        //If the property is of type boolean, start getter name with "is"; otherwise, start with "get"
        if (this.getProperty().getType() == Boolean.class) {
            potentialGetterName = "is" + firstLetterCapitalized;
        }
        else {
            potentialGetterName = "get" + firstLetterCapitalized;
        }

        //If the size of the java field name is more than one, add the rest of the field name as normal
        if (javaFieldName.length() > 1) {
            potentialGetterName += javaFieldName.substring(1);
        }

        return potentialGetterName;
    }

    //Method to return a field's potential setter name depending on the name of the field in the java class
    private String potentialSetterName() {
        //Check if the property field is empty and return an empty string if so
        if (this.getProperty() == null) {
            return "";
        }

        //Create a String to be returned
        String potentialSetterName;

        //Create string holding the column's java field name
        String javaFieldName = this.getProperty().getName();

        //Get the first character of the field name and set to uppercase to match camel casing
        char firstLetterCapitalized = Character.toUpperCase(javaFieldName.charAt(0));

        //Start setter name with the prefix "set" and add the first letter capitalized
        potentialSetterName = "set" + firstLetterCapitalized;

        //If the size of the java field name is more than one, add the rest of the field name as normal
        if (javaFieldName.length() > 1) {
            potentialSetterName += javaFieldName.substring(1);
        }

        return potentialSetterName;
    }

    //Method to take a given list of methods and return a subset of said list containing methods that either:
    // -Have the Getter annotation
    // -Match the conventional getter name
    public List<Method> getPotentialGetter(Method[] methods) {
        //Create new list to be filled and returned
        List<Method> potentialGetters = new ArrayList<>();

        //Search for the annotation "Getter", check that the annotation's field name matches this column's field name
        //and return the list with only one method if found
        for (Method m : methods) {
            if (m.isAnnotationPresent(Getter.class) && m.getAnnotation(Getter.class).fieldName().equals(this.getFieldName())) {
                potentialGetters.add(m);
                return potentialGetters;
            }
        }

        //Create a String that holds this field's potential getter name
        String potentialGetterName = this.potentialGetterName();

        //Search through the list of methods and add any that match the conventional getter method signature
        for (Method m : methods) {
            if (m.getName().equals(potentialGetterName)) {
                potentialGetters.add(m);
            }
        }

        //Return list of potential getters, whether empty or not
        return potentialGetters;
    }

    //Method to take a given list of methods and return a subset of said list containing methods that either:
    // -Have the Setter annotation
    // -Match the conventional Setter name
    public List<Method> getPotentialSetter(Method[] methods) {
        //Create new list to be filled and returned
        List<Method> potentialSetters = new ArrayList<>();

        //Search for the annotation "Setter", check that the annotation's field name matches this column's field name
        //and return the list with only one method if found
        for (Method m : methods) {
            if (m.isAnnotationPresent(Setter.class) && m.getAnnotation(Setter.class).fieldName().equals(this.getFieldName())) {
                potentialSetters.add(m);
                return potentialSetters;
            }
        }

        //Create a String that holds this field's potential Setter name
        String potentialSetterName = this.potentialSetterName();

        //Search through the list of methods and add any that match the conventional Setter method signature
        for (Method m : methods) {
            if (m.getName().equals(potentialSetterName)) {
                potentialSetters.add(m);
            }
        }

        //Return list of potential Setters, whether empty or not
        return potentialSetters;
    }

    //Method to iterate through a list of given methods and return the first one that matches the standards to be a
    //getter for this field, i.e. having no parameters and having the same return type as this field
    public Method getValidGetter(List<Method> methods) {
        for (Method m : methods) {
            if (this.isValidGetter(m)) {
                return m;
            }
        }

        //Return null if matching getter was not found
        return null;
    }

    //Method to iterate through a list of given methods and return the first one that matches the standards to be a
    //setter for this field, i.e. having exactly one parameter that matches the type of this field
    public Method getValidSetter(List<Method> methods) {
        for (Method m : methods) {
            if (this.isValidSetter(m)) {
                return m;
            }
        }

        //Return null if matching setter was not found
        return null;
    }


    //Method to test whether a given method parameter is a valid getter method
    public boolean isValidGetter(Method m) {
        //Get array of potential getter parameters
        Class[] paramTypes = m.getParameterTypes();

        //If parameter list is NOT empty, return false
        if (paramTypes.length != 0) {
            return false;
        }

        //Return whether the field type matches potential getter return type
        return this.property.getType() == m.getReturnType();
    }

    //Method to test whether a given method parameter is a valid setter method
    public boolean isValidSetter(Method m) {
        //Get array of potential setter parameters
        Class[] paramTypes = m.getParameterTypes();

        //If parameter list is not exactly one, return false
        if (paramTypes.length != 1) {
            return false;
        }

        //If parameter list only has one parameter, return whether field type matches setter parameter type
        return this.property.getType() == paramTypes[0];
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

    //Methods that will print 'null' if methods etc are... null
    //...why is this needed? ToString() can't handle null values? Really?
    public String getPropertyString() {
        return (this.getProperty() == null) ? "null" : this.getProperty().getName();
    }

    public String getGetterString() {
        return (this.getGetter() == null) ? "null" : this.getGetter().getName();
    }

    public String getSetterString() {
        return (this.getSetter() == null) ? "null" : this.getSetter().getName();
    }

    //Method to convert Column information to string
    @Override
    public String toString() {
        return "Column: " + fieldName + ", Class field: " + this.getPropertyString() + ", Getter: " + this.getGetterString() +
                ", Setter: " + this.getSetterString() + ", is primary key: " + isValidPrimaryKey();
    }
}
