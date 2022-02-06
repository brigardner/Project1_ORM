package Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Annotation for class fields that are the primary key of a related table
//Has the "auto-increment" boolean that determines whether to attempt to store the primary key into the SQL database
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {
    boolean autoIncrement();
}
