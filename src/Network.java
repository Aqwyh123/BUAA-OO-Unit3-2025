import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons;
    private final HashMap<Integer, OfficialAccount> accounts;
    private final HashMap<Integer, Integer> contributors;
    private int tripleSum;

    public Network() {
        persons = new HashMap<>();
        accounts = new HashMap<>();
        contributors = new HashMap<>();
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
            persons.get(id1).addLinked(persons.get(id2), value);
            persons.get(id2).addLinked(persons.get(id1), value);
            tripleSum += queryTripleSum(id1, id2);
            updateTagValueSum(id1, id2, 0, value);
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
            int oldValue = persons.get(id1).queryValue(persons.get(id2));
            int newValue = Math.max(oldValue + value, 0);
            if (newValue > 0) {
                persons.get(id1).replaceLink(persons.get(id2), newValue);
                persons.get(id2).replaceLink(persons.get(id1), newValue);
            } else {
                persons.get(id1).removeLink(persons.get(id2));
                persons.get(id2).removeLink(persons.get(id1));
                tripleSum -= queryTripleSum(id1, id2);
            }
            updateTagValueSum(id1, id2, oldValue, newValue);
        }
    }

    private void updateTagValueSum(int id1, int id2, int oldValue, int newValue) {
        Collection<Tag> relatedTags1 = persons.get(id1).viewRelatedTags();
        Collection<Tag> relatedTags2 = persons.get(id2).viewRelatedTags();
        int relatedPerson2;
        if (relatedTags1.size() > relatedTags2.size()) {
            relatedTags1 = relatedTags2;
            relatedPerson2 = id1;
        } else {
            relatedPerson2 = id2;
        }
        relatedTags1.forEach(tag -> {
            if (tag.hasPerson(persons.get(relatedPerson2))) {
                tag.modifyPerson(oldValue, newValue);
            }
        });
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    private int queryTripleSum(int id1, int id2) {
        int sum = 0;
        Set<Integer> neighbors1 = persons.get(id1).viewAcquaintances();
        Set<Integer> neighbors2 = persons.get(id2).viewAcquaintances();
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
            persons.get(personId1).addToTag(personId2, persons.get(personId2).getTag(tagId));
        }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId) throws PersonIdNotFoundException,
        TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else {
            return persons.get(personId).getTag(tagId).getValueSum();
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
            persons.get(personId1).delFromTag(personId2, persons.get(personId2).getTag(tagId));
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
            return persons.get(id).queryBestAcquaintance();
        }
    }

    @Override
    public int queryCoupleSum() {
        HashSet<Person> couples = new HashSet<>();
        for (Person person : persons.values()) {
            if (!couples.contains(person) && person.getAcquaintanceSize() != 0) {
                Person bestAcquaintance = persons.get(person.queryBestAcquaintance());
                if (bestAcquaintance.queryBestAcquaintance() == person.getId()) {
                    couples.add(person);
                    couples.add(bestAcquaintance);
                }
            }
        }
        return couples.size() / 2;
    }

    @Override
    public int queryShortestPath(int id1,int id2) throws PersonIdNotFoundException,
        PathNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            return 0;
        } else {
            final Queue<Integer> queue1 = new LinkedList<>();
            final Queue<Integer> queue2 = new LinkedList<>();
            final HashMap<Integer, Integer> dist1 = new HashMap<>();
            final HashMap<Integer, Integer> dist2 = new HashMap<>();
            final Set<Integer> visited1 = new HashSet<>();
            final Set<Integer> visited2 = new HashSet<>();
            queue1.offer(id1);
            queue2.offer(id2);
            dist1.put(id1, 0);
            dist2.put(id2, 0);
            visited1.add(id1);
            visited2.add(id2);
            while (!queue1.isEmpty() && !queue2.isEmpty()) {
                int res;
                if (queue1.size() <= queue2.size()) {
                    res = search(queue1, visited1, visited2, dist1, dist2);
                } else {
                    res = search(queue2, visited2, visited1, dist2, dist1);
                }
                if (res != -1) {
                    return res;
                }
            }
            throw new PathNotFoundException(id1, id2);
        }
    }

    @Override
    public boolean containsAccount(int id) {
        return accounts.containsKey(id);
    }

    @Override
    public void createOfficialAccount(int personId, int accountId, String name) throws
        PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (accounts.containsKey(accountId)) {
            throw new EqualOfficialAccountIdException(accountId);
        } else {
            OfficialAccount account = new OfficialAccount(personId, accountId, name);
            account.addFollower(persons.get(personId));
            accounts.put(accountId, account);
        }
    }

    @Override
    public void deleteOfficialAccount(int personId, int accountId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        DeleteOfficialAccountPermissionDeniedException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!accounts.containsKey(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        } else {
            accounts.remove(accountId);
        }
    }

    @Override
    public boolean containsArticle(int id) {
        return contributors.containsKey(id);
    }

    @Override
    public void contributeArticle(int personId,int accountId,int articleId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualArticleIdException, ContributePermissionDeniedException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!accounts.containsKey(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (contributors.containsKey(articleId)) {
            throw new EqualArticleIdException(articleId);
        } else if (!accounts.get(accountId).containsFollower(persons.get(personId))) {
            throw new ContributePermissionDeniedException(personId, articleId);
        } else {
            contributors.put(articleId, personId);
            accounts.get(accountId).addArticle(persons.get(personId), articleId);
            for (int follower : accounts.get(accountId).viewFollowers()) {
                persons.get(follower).addReceivedArticle(articleId);
            }
        }
    }

    @Override
    public void deleteArticle(int personId,int accountId,int articleId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!accounts.containsKey(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (!accounts.get(accountId).containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        } else if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        } else {
            accounts.get(accountId).removeArticle(articleId);
            accounts.get(accountId).decreaseContribution(persons.get(contributors.get(articleId)));
            for (int follower : accounts.get(accountId).viewFollowers()) {
                persons.get(follower).removeReceivedArticle(articleId);
            }
        }
    }

    @Override
    public void followOfficialAccount(int personId,int accountId) throws PersonIdNotFoundException,
        OfficialAccountIdNotFoundException, EqualPersonIdException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!accounts.containsKey(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (accounts.get(accountId).containsFollower(persons.get(personId))) {
            throw new EqualPersonIdException(personId);
        } else {
            accounts.get(accountId).addFollower(persons.get(personId));
        }
    }

    @Override
    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (!accounts.containsKey(id)) {
            throw new OfficialAccountIdNotFoundException(id);
        } else {
            return accounts.get(id).getBestContributor();
        }
    }

    @Override
    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!persons.containsKey(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return persons.get(id).queryReceivedArticles();
        }
    }

    private boolean search(Queue<Integer> queue, Set<Integer> visited, Set<Integer> otherVisited) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            for (int neighborId : persons.get(queue.poll()).viewAcquaintances()) {
                if (otherVisited.contains(neighborId)) {
                    return true;
                }
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    queue.offer(neighborId);
                }
            }
        }
        return false;
    }

    private int search(Queue<Integer> queue, Set<Integer> visited, Set<Integer> otherVisited,
        HashMap<Integer, Integer> dist, HashMap<Integer, Integer> otherDist) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            Integer current = queue.poll();
            Integer currentDist = dist.get(current);
            for (int neighborId : persons.get(current).viewAcquaintances()) {
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    dist.put(neighborId, currentDist + 1);
                    queue.offer(neighborId);
                    if (otherVisited.contains(neighborId)) {
                        return currentDist + 1 + otherDist.get(neighborId);
                    }
                }
            }
        }
        return -1;
    }
}
