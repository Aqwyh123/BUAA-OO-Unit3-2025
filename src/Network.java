import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons;
    private int tripleSum;

    public Network() {
        persons = new HashMap<>();
        tripleSum = 0;
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return persons.get(id);
    }

    public PersonInterface[] getPersons() {
        return persons.values().toArray(new Person[0]);
    }

    @Override
    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        if (persons.containsKey(person.getId())) {
            throw new EqualPersonIdException(person.getId());
        } else {
            persons.put(person.getId(), (Person) person);
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value) throws EqualRelationException,
        PersonIdNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (persons.get(id1).isLinked(persons.get(id2))) {
            throw new EqualRelationException(id1, id2);
        } else {
            persons.get(id1).setLinked(persons.get(id2), value);
            persons.get(id2).setLinked(persons.get(id1), value);
            tripleSum += queryTripleSum(id1, id2);
        }
    }

    @Override
    public void modifyRelation(int id1, int id2, int value) throws PersonIdNotFoundException,
        EqualPersonIdException, RelationNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new EqualPersonIdException(id1);
        } else if (!persons.get(id1).isLinked(persons.get(id2))) {
            throw new RelationNotFoundException(id1, id2);
        } else {
            int newValue = persons.get(id1).queryValue(persons.get(id2)) + value;
            if (newValue > 0) {
                persons.get(id1).setLinked(persons.get(id2), newValue);
                persons.get(id2).setLinked(persons.get(id1), newValue);
            } else {
                persons.get(id1).setUnlinked(persons.get(id2));
                persons.get(id2).setUnlinked(persons.get(id1));
                tripleSum -= queryTripleSum(id1, id2);
            }
        }
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    private int queryTripleSum(int id1, int id2) {
        int sum = 0;
        Set<Integer> neighbors1 = persons.get(id1).viewAcquaintances().keySet();
        Set<Integer> neighbors2 = persons.get(id2).viewAcquaintances().keySet();
        if (neighbors1.size() > neighbors2.size()) {
            Set<Integer> temp = neighbors1;
            neighbors1 = neighbors2;
            neighbors2 = temp;
        }
        for (int neighborId : neighbors1) {
            if (neighbors2.contains(neighborId)) {
                sum++;
            }
        }
        return sum;
    }

    @Override
    public int queryValue(int id1, int id2) throws PersonIdNotFoundException,
        RelationNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (!persons.get(id1).isLinked(persons.get(id2))) {
            throw new RelationNotFoundException(id1, id2);
        } else {
            return persons.get(id1).queryValue(persons.get(id2));
        }
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (persons.get(id1).isLinked(persons.get(id2))) {
            return true;
        } else {
            final Queue<Integer> queue1 = new LinkedList<>();
            final Queue<Integer> queue2 = new LinkedList<>();
            final Set<Integer> visit1 = new HashSet<>();
            final Set<Integer> visit2 = new HashSet<>();
            queue1.add(id1);
            visit1.add(id1);
            queue2.add(id2);
            visit2.add(id2);
            while (!queue1.isEmpty() && !queue2.isEmpty()) {
                if (queue1.size() <= queue2.size()) {
                    if (search(queue1, visit1, visit2)) {
                        return true;
                    }
                } else {
                    if (search(queue2, visit2, visit1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean search(Queue<Integer> queue, Set<Integer> visit, Set<Integer> otherVisit) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            for (int neighborId : persons.get(queue.poll()).viewAcquaintances().keySet()) {
                if (otherVisit.contains(neighborId)) {
                    return true;
                }
                if (!visit.contains(neighborId)) {
                    visit.add(neighborId);
                    queue.offer(neighborId);
                }
            }
        }
        return false;
    }

    @Override
    public void addTag(int personId, TagInterface tag) throws PersonIdNotFoundException,
        EqualTagIdException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (persons.get(personId).containsTag(tag.getId())) {
            throw new EqualTagIdException(tag.getId());
        } else {
            persons.get(personId).addTag(tag);
        }
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId) throws
        PersonIdNotFoundException, RelationNotFoundException, TagIdNotFoundException,
        EqualPersonIdException {
        if (!persons.containsKey(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        } else if (!persons.containsKey(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new EqualPersonIdException(personId1);
        } else if (!persons.get(personId2).isLinked(persons.get(personId1))) {
            throw new RelationNotFoundException(personId1, personId2);
        } else if (!persons.get(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else if (persons.get(personId2).getTag(tagId).hasPerson(persons.get(personId1))) {
            throw new EqualPersonIdException(personId1);
        } else if (persons.get(personId2).getTag(tagId).getSize() <= 999) {
            persons.get(personId2).getTag(tagId).addPerson(persons.get(personId1));
        }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId) throws PersonIdNotFoundException,
        TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else {
            return persons.get(personId).getTag(tagId).getAgeVar();
        }
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId) throws
        PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        } else if (!persons.containsKey(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        } else if (!persons.get(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else if (!persons.get(personId2).getTag(tagId).hasPerson(persons.get(personId1))) {
            throw new PersonIdNotFoundException(personId1);
        } else {
            persons.get(personId2).getTag(tagId).delPerson(persons.get(personId1));
        }
    }

    @Override
    public void delTag(int personId, int tagId) throws PersonIdNotFoundException,
        TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else {
            persons.get(personId).delTag(tagId);
        }
    }

    @Override
    public int queryBestAcquaintance(int id) throws PersonIdNotFoundException,
        AcquaintanceNotFoundException {
        if (!persons.containsKey(id)) {
            throw new PersonIdNotFoundException(id);
        } else if (persons.get(id).getAcquaintanceSize() == 0) {
            throw new AcquaintanceNotFoundException(id);
        } else {
            return persons.get(id).viewAcquaintances().entrySet().stream().max((entry1, entry2) -> {
                if (!Objects.equals(entry1.getValue(), entry2.getValue())) {
                    return entry1.getValue() - entry2.getValue(); // biggest value first
                } else {
                    return entry2.getKey() - entry1.getKey(); // smallest id first
                }
            }).orElseThrow(() -> new AcquaintanceNotFoundException(id)).getKey();
        }
    }
}
