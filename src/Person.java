import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    private final HashMap<Integer, Integer> acquaintance;
    private final HashMap<Integer, Tag> tags;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.acquaintance = new HashMap<>();
        this.tags = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    @Override
    public Tag getTag(int id) {
        return tags.get(id);
    }

    @Override
    public void addTag(TagInterface tag) {
        tags.put(tag.getId(), (Tag) tag);
    }

    @Override
    public void delTag(int id) {
        tags.remove(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Person)) {
            return false;
        }
        return ((Person) obj).getId() == id;
    }

    public boolean strictEquals(PersonInterface person) {
        if (!(person instanceof Person)) {
            return false;
        }
        Person p = (Person) person;
        boolean basicEquals = p.getId() == id && p.getName().equals(name) && p.getAge() == age;
        boolean objectEquals = p.tags.equals(tags) && p.acquaintance.equals(acquaintance);
        return basicEquals && objectEquals;
    }

    @Override
    public boolean isLinked(PersonInterface person) {
        return acquaintance.containsKey(person.getId()) || person.getId() == id;
    }

    public void setLinked(PersonInterface person, int value) {
        acquaintance.put(person.getId(), value);
    }

    public void setUnlinked(PersonInterface person) {
        acquaintance.remove(person.getId());
        tags.values().forEach((tag) -> {
            if (tag.hasPerson(person)) {
                tag.delPerson(person);
            }
        });
    }

    @Override
    public int queryValue(PersonInterface person) {
        return acquaintance.getOrDefault(person.getId(), 0);
    }

    public int getAcquaintanceSize() {
        return acquaintance.size();
    }

    public Map<Integer, Integer> viewAcquaintances() {
        return Collections.unmodifiableMap(acquaintance);
    }
}
