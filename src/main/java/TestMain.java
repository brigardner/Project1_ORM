import Annotations.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestMain {
    public static void main(String ...args) {

        Method[] methods = TestClass.class.getMethods();
        Method[] declaredMethods = TestClass.class.getDeclaredMethods();

        TestClass t = new TestClass(0, "b");

        for (Method m : methods) {
            System.out.println(m.getName());
        }
        System.out.println();
        for (Method m : declaredMethods) {
            System.out.println(m.getName());
            if (m.isAnnotationPresent(Setter.class)) {
                try {
                    if (m.getParameterTypes()[0] == Integer.class) {
                        m.invoke(t, 1);
                    }
                    else if (m.getParameterTypes()[0] == String.class) {
                        m.invoke(t, "c");
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(t.getId() + ": " + t.getName());
        int i;
        Integer I;
        System.out.println(int.class.hashCode());
        System.out.println(Integer.class.hashCode());

        //Test below
/*
        //Test out create function
        Repository<TestClass> testClassRepository = new Repository<>(new TestClass());

        for (Column c : testClassRepository.getTable().getColumns()) {
            System.out.println(c);
            System.out.println(c.hasValidGetter());
            System.out.println(c.hasValidSetter());
        }
*/

/*
        //Create TestClass object to attempt storing
        TestClass testClass = new TestClass(1, "Brian");

        testClassRepository.create(testClass);
*/

    }
}
