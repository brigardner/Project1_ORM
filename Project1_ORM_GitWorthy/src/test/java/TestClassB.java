import Annotations.Entity;
import Annotations.PrimaryKey;
import Annotations.Property;

@Entity(tableName = "TestTableB")
public class TestClassB {
    @PrimaryKey(autoIncrement = true)
    @Property(fieldName = "id")
    private Integer id;

    @Property(fieldName = "first_name")
    private String firstName;

    @Property(fieldName = "last_name")
    private String lastName;

    @Property(fieldName = "weight")
    private Float weight;

    @Property(fieldName = "age")
    private Byte age;

    public TestClassB() {
    }

    public TestClassB(Integer id) {
        this.id = id;
    }

    public TestClassB(Integer id, String firstName) {
        this.id = id;
        this.firstName = firstName;
    }

    public TestClassB(Integer id, String firstName, String lastName, Float weight, byte age) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.weight = weight;
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Float getWeight() {
        return weight;
    }

    public Byte getAge() {
        return age;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public void setAge(Byte age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "TestClassB{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", weight=" + weight +
                ", age=" + age +
                '}';
    }
}
