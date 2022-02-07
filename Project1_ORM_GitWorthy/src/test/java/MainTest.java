public class MainTest {
    public static void main(String ...args) {
        ExceptionLogger exceptionLogger = ExceptionLogger.getExceptionLogger();

        Repository<TestClassA> testRepo = new Repository<>(new TestClassA());
        System.out.println(testRepo);

        Repository<TestClassB> testRepoB = new Repository<>(new TestClassB());
        System.out.println(testRepoB);

        TestClassB t2 = new TestClassB(2);
        System.out.println();

    }
}
