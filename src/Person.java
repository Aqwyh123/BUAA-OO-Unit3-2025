import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.TreeMap;
import java.util.TreeSet;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    private final HashMap<Integer, Integer> values;
    private final TreeMap<Integer, TreeSet<Integer>> acquaintances;
    private final HashMap<Integer, Tag> ownedTags;
    private final HashMap<SimpleEntry<Integer, Integer>, Tag> relatedTags;
    private final LinkedHashSet<Integer> receivedArticles;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.values = new HashMap<>();
        this.acquaintances = new TreeMap<>();
        this.ownedTags = new HashMap<>();
        this.relatedTags = new HashMap<>();
        this.receivedArticles = new LinkedHashSet<>();
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
        return ownedTags.containsKey(id);
    }

    @Override
    public Tag getTag(int id) {
        return ownedTags.get(id);
    }

    @Override
    public void addTag(TagInterface tag) {
        ownedTags.put(tag.getId(), (Tag) tag);
    }

    @Override
    public void delTag(int id) {
        ownedTags.remove(id);
    }

    public Collection<Tag> viewRelatedTags() {
        return Collections.unmodifiableCollection(relatedTags.values());
    }

    public void addToTag(int personId, TagInterface tag) {
        relatedTags.put(new SimpleEntry<>(personId, tag.getId()), (Tag) tag);
    }

    public void delFromTag(int personId, TagInterface tag) {
        relatedTags.remove(new SimpleEntry<>(personId, tag.getId()));
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
        boolean objectEquals = p.ownedTags.equals(ownedTags) && p.values.equals(values);
        return basicEquals && objectEquals;
    }

    @Override
    public boolean isLinked(PersonInterface person) {
        return values.containsKey(person.getId()) || person.getId() == id;
    }

    public void addLinked(PersonInterface person, int value) {
        values.put(person.getId(), value);
        acquaintances.putIfAbsent(value, new TreeSet<>());
        acquaintances.get(value).add(person.getId());
    }

    public void replaceLink(PersonInterface person, int value) {
        Integer oldValue = values.put(person.getId(), value);
        acquaintances.get(oldValue).remove(person.getId());
        if (acquaintances.get(oldValue).isEmpty()) {
            acquaintances.remove(oldValue);
        }
        acquaintances.putIfAbsent(value, new TreeSet<>());
        acquaintances.get(value).add(person.getId());
    }

    public void removeLink(PersonInterface person) {
        Integer oldValue = values.remove(person.getId());
        acquaintances.get(oldValue).remove(person.getId());
        if (acquaintances.get(oldValue).isEmpty()) {
            acquaintances.remove(oldValue);
        }
        ownedTags.values().forEach((tag) -> {
            if (tag.hasPerson(person)) {
                tag.delPerson(person);
                ((Person) person).delFromTag(id, tag);
            }
        });
    }

    @Override
    public int queryValue(PersonInterface person) {
        return values.getOrDefault(person.getId(), 0);
    }

    public int getAcquaintanceSize() {
        return values.size();
    }

    public Set<Integer> viewAcquaintances() {
        return Collections.unmodifiableSet(values.keySet());
    }

    public int queryBestAcquaintance() {
        return acquaintances.get(acquaintances.lastKey()).first();
    }

    @Override
    public List<Integer> getReceivedArticles() {
        List<Integer> articles = new ArrayList<>(receivedArticles);
        Collections.reverse(articles);
        return articles;
    }

    @Override
    public List<Integer> queryReceivedArticles() {
        List<Integer> articles = new ArrayList<>(receivedArticles);
        Collections.reverse(articles);
        return articles.subList(0, Math.min(5, acquaintances.size()));
    }

    public void addReceivedArticle(int articleId) {
        receivedArticles.add(articleId);
    }

    public void removeReceivedArticle(int articleId) {
        receivedArticles.remove(articleId);
    }
}
