import java.lang.reflect.Method;
import java.sql.SQLException;

public class MainTest {
    public static void main(String ...args) {
        Repository<TestClassA> testRepo = new Repository<>(new TestClassA());
        System.out.println(testRepo);

        Method[] methods = TestClassA.class.getMethods();

        for (Method m : methods) {
            System.out.println(m.getName());
        }

        Repository<TestClassB> testRepoB = new Repository<>(new TestClassB());
        System.out.println(testRepoB);

        TestClassB t2 = new TestClassB(6);
        t2 = testRepoB.read(t2);
        System.out.println(t2);

    }
}
