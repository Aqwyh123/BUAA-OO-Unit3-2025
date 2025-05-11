import com.oocourse.spec2.main.OfficialAccountInterface;
import com.oocourse.spec2.main.PersonInterface;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class OfficialAccount implements OfficialAccountInterface {
    private final int ownerId;
    private final int id;
    private final String name;
    private final HashSet<Integer> articles;
    private final TreeMap<Integer, TreeSet<Integer>> followers;
    private final HashMap<Integer, Integer> contributions;

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        this.articles = new HashSet<>();
        this.followers = new TreeMap<>();
        this.contributions = new HashMap<>();
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void addFollower(PersonInterface person) {
        contributions.put(person.getId(), 0);
        followers.putIfAbsent(0, new TreeSet<>());
        followers.get(0).add(person.getId());
    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        return contributions.containsKey(person.getId());
    }

    public Set<Integer> viewFollowers() {
        return Collections.unmodifiableSet(contributions.keySet());
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        articles.add(id);
        int contribution = contributions.get(person.getId());
        followers.get(contribution).remove(person.getId());
        if (followers.get(contribution).isEmpty()) {
            followers.remove(contribution);
        }
        contributions.put(person.getId(), contribution + 1);
        followers.putIfAbsent(contribution + 1, new TreeSet<>());
        followers.get(contribution + 1).add(person.getId());
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    @Override
    public void removeArticle(int id) {
        articles.remove(id);
    }

    public void decreaseContribution(PersonInterface person) {
        int contribution = contributions.get(person.getId());
        followers.get(contribution).remove(person.getId());
        if (followers.get(contribution).isEmpty()) {
            followers.remove(contribution);
        }
        contributions.put(person.getId(), contribution - 1);
        followers.putIfAbsent(contribution - 1, new TreeSet<>());
        followers.get(contribution - 1).add(person.getId());
    }

    @Override
    public int getBestContributor() {
        return followers.get(followers.lastKey()).first();
    }
}
