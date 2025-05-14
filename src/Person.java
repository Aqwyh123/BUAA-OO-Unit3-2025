import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
    private final HashMap<Integer, TagInterface> ownedTags;
    private final HashMap<SimpleEntry<Integer, Integer>, TagInterface> relatedTags;
    private final ReservedLinkedHashMultiSet<Integer> receivedArticles;
    private final LinkedList<MessageInterface> messages;
    private int money;
    private int socialValue;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.values = new HashMap<>();
        this.acquaintances = new TreeMap<>();
        this.ownedTags = new HashMap<>();
        this.relatedTags = new HashMap<>();
        this.receivedArticles = new ReservedLinkedHashMultiSet<>();
        this.messages = new LinkedList<>();
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
    public TagInterface getTag(int id) {
        return ownedTags.get(id);
    }

    @Override
    public void addTag(TagInterface tag) {
        ownedTags.put(tag.getId(), tag);
    }

    @Override
    public void delTag(int id) {
        ownedTags.remove(id);
    }

    public Collection<TagInterface> viewRelatedTags() {
        return Collections.unmodifiableCollection(relatedTags.values());
    }

    public void addToTag(int personId, TagInterface tag) {
        relatedTags.put(new SimpleEntry<>(personId, tag.getId()), tag);
    }

    public void delFromTag(int personId, TagInterface tag) {
        relatedTags.remove(new SimpleEntry<>(personId, tag.getId()));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PersonInterface)) {
            return false;
        }
        return ((PersonInterface) obj).getId() == id;
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
        return receivedArticles.toList();
    }

    @Override
    public List<Integer> queryReceivedArticles() {
        return receivedArticles.toList().subList(0, Math.min(5, receivedArticles.size()));
    }

    public boolean containsArticle(int articleId) {
        return receivedArticles.contains(articleId);
    }

    public void addArticle(int articleId) {
        receivedArticles.add(articleId);
    }

    public void removeArticle(int articleId) {
        receivedArticles.remove(articleId);
    }

    @Override
    public void addSocialValue(int num) {
        socialValue += num;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public List<MessageInterface> getMessages() {
        return new ArrayList<>(messages);
    }

    @Override
    public List<MessageInterface> getReceivedMessages() {
        return new ArrayList<>(messages).subList(0, Math.min(5, messages.size()));
    }

    public void addMessage(MessageInterface message) {
        messages.addFirst(message);
    }

    @Override
    public void addMoney(int num) {
        money += num;
    }

    @Override
    public int getMoney() {
        return money;
    }
}
