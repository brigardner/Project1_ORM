import java.lang.reflect.Method;

public class TestMain {
    public static void main(String ...args) {

        Method[] methods = TestClass.class.getMethods();
        Method[] declaredMethods = TestClass.class.getDeclaredMethods();

        TestClass t = new TestClass(0, "b");

        //Test below

        //Test out create function
        Repository<TestClass> testClassRepository = new Repository<>(new TestClass());

        for (Column c : testClassRepository.getTable().getColumns()) {
            System.out.println(c);
            System.out.println(c.hasValidGetter());
            System.out.println(c.hasValidSetter());
        }
        System.out.println(SQLStringScriptor.makeCreateSQLString(testClassRepository));
        System.out.println(SQLStringScriptor.makeReadSQLString(testClassRepository));
        System.out.println(SQLStringScriptor.makeUpdateSQLString(testClassRepository));
        System.out.println(SQLStringScriptor.makeDeleteSQLString(testClassRepository));
        t = testClassRepository.create(new TestClass(8, "cyan"));
        t = testClassRepository.read(t);
        System.out.println(t.getName());
        t = testClassRepository.update(new TestClass(8, "blue"));
        System.out.println(testClassRepository.read(t).getName());
        System.out.println(testClassRepository.delete(t));

/*
        //Create TestClass object to attempt storing
        TestClass testClass = new TestClass(2, "Brian");

        testClassRepository.create(testClass);
*/

    }
}
