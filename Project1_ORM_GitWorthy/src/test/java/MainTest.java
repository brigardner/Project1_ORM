import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

public class MainTest {
    public static void main(String ...args) {
        Repository<TestClassA> testRepo = new Repository<>(new TestClassA());
        System.out.println(testRepo);

        List<TestClassA> testClassAS = testRepo.readAll();
        for (TestClassA t : testClassAS) {
            System.out.println(t);
        }

        Repository<TestClassB> testRepoB = new Repository<>(new TestClassB());
        System.out.println(testRepoB);

        TestClassB t2 = new TestClassB(6);
        t2 = testRepoB.read(t2);
        System.out.println(t2);

    }
}
