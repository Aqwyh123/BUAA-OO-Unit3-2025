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

    public Set<Integer> getFollowers() {
        return Collections.unmodifiableSet(contributions.keySet());
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        articles.add(id);
        followers.get(contributions.get(person.getId())).remove(person.getId());
        if (followers.get(contributions.get(person.getId())).isEmpty()) {
            followers.remove(contributions.get(person.getId()));
        }
        contributions.put(person.getId(), contributions.get(person.getId()) + 1);
        followers.putIfAbsent(contributions.get(person.getId()), new TreeSet<>());
        followers.get(contributions.get(person.getId())).add(person.getId());
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    @Override
    public void removeArticle(int id) {
        articles.remove(id);
    }

    @Override
    public int getBestContributor() {
        return followers.get(followers.lastKey()).first();
    }
}
