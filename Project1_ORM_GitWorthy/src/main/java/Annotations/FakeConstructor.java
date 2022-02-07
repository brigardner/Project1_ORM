package Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Annotation to place on methods that take in no parameters and return an object of the calling object's type
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FakeConstructor {
}
