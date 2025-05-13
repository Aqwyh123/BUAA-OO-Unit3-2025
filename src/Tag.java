import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Tag implements TagInterface {
    private final int id;
    private final HashSet<PersonInterface> persons;
    private int valueSum;
    private int ageSum;
    private int ageSquareSum;

    public Tag(int id) {
        this.id = id;
        this.valueSum = 0;
        this.ageSum = 0;
        this.ageSquareSum = 0;
        this.persons = new HashSet<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TagInterface)) {
            return false;
        }
        return ((TagInterface) obj).getId() == id;
    }

    @Override
    public void addPerson(PersonInterface person) {
        for (PersonInterface p : persons) {
            if (p.isLinked(person)) {
                valueSum += 2 * p.queryValue(person);
            }
        }
        persons.add(person);
        ageSum += person.getAge();
        ageSquareSum += person.getAge() * person.getAge();
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        return persons.contains(person);
    }

    @Override
    public int getValueSum() {
        return valueSum;
    }

    public void modifyPerson(int oldValue, int newValue) {
        valueSum -= 2 * oldValue;
        valueSum += 2 * newValue;
    }

    @Override
    public int getAgeMean() {
        return persons.isEmpty() ? 0 : ageSum / persons.size();
    }

    @Override
    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        } else {
            int mean = getAgeMean();
            int size = persons.size();
            return (ageSquareSum - 2 * mean * ageSum + size * mean * mean) / size;
        }
    }

    @Override
    public void delPerson(PersonInterface person) {
        persons.remove(person);
        for (PersonInterface p : persons) {
            if (p.isLinked(person)) {
                valueSum -= 2 * p.queryValue(person);
            }
        }
        ageSum -= person.getAge();
        ageSquareSum -= person.getAge() * person.getAge();
    }

    @Override
    public int getSize() {
        return persons.size();
    }

    public Set<PersonInterface> viewPersons() {
        return Collections.unmodifiableSet(persons);
    }
}
