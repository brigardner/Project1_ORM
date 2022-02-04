public class TestMain {
    public static void main(String ...args) {
        //Test below

        //Test out create function
        Repository<TestClass> testClassRepository = new Repository<>(new TestClass());

        for (Column c : testClassRepository.getTable().getColumns()) {
            System.out.println(c);
        }
/*
        //Create TestClass object to attempt storing
        TestClass testClass = new TestClass(1, "Brian");

        testClassRepository.create(testClass);
*/

    }
}
