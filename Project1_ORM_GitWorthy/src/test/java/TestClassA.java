import Annotations.Entity;
import Annotations.PrimaryKey;
import Annotations.Property;

@Entity(tableName = "TestTable")
public class TestClassA {
    @PrimaryKey(autoIncrement = false)
    @Property(fieldName = "id")
    private Integer id;

    @Property(fieldName = "name")
    private String name;

    public TestClassA() {
    }

    public TestClassA(Integer id) {
        this.id = id;
    }

    public TestClassA(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
