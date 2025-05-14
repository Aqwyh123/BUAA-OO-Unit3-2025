import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

public class NetworkTest {
    HashSet<Integer> personIds = new HashSet<>();
    HashSet<Integer> messageIds = new HashSet<>();

    @Test
    public void testAllEmojisExactlyAtLimit() throws Exception {
        NetworkInterface[] networks = createNetworks();
        int limit = 5;

        // 添加3个emoji，每个热度正好等于limit
        for (int i = 0; i < 3; i++) {
            // 发送5次使热度等于limit
            for (int j = 0; j < limit; j++) {
                sendMessage(networks, addEmojiMessage(networks, i));
            }
        }

        Assert.assertTrue(checkDeleteColdEmoji(networks, limit));
    }

    @Test
    public void testLargeScaleMixedData() throws Exception {
        NetworkInterface[] networks = createNetworks();
        int limit = 10;
        Random rand = new Random();

        // 生成50个emoji（其中20个热度>=limit）
        for (int i = 0; i < 50; i++) {
            int heat = i < 20 ? limit + rand.nextInt(10) : rand.nextInt(limit);
            // 发送消息累积热度
            for (int j = 0; j < heat; j++) {
                sendMessage(networks, addEmojiMessage(networks, i));
            }
            // 添加未发送的emoji消息（热度不计）
            if (rand.nextBoolean()) {
                addEmojiMessage(networks, i);
            }
            // 穿插普通消息
            if (i % 5 == 0) {
                addOtherMessage(networks);
            }
        }

        Assert.assertTrue(checkDeleteColdEmoji(networks, limit));
    }

    @Test
    public void testBoundaryZeroLimit() throws Exception {
        NetworkInterface[] networks = createNetworks();
        // 添加10个未发送的emoji（热度0）
        for (int i = 0; i < 10; i++) {
            addEmojiMessage(networks, i);
        }
        // 添加5个发送过的emoji（热度>=1）
        for (int i = 10; i < 15; i++) {
            sendMessage(networks, addEmojiMessage(networks, i));
        }
        // limit=0应删除所有热度<0的emoji（即全部保留）
        Assert.assertTrue(checkDeleteColdEmoji(networks, 0));
    }

    @Test
    public void testAllEmojisRemoved() throws Exception {
        NetworkInterface[] networks = createNetworks();
        int limit = 1;
        // 添加5个未发送的emoji（热度0）
        for (int i = 0; i < 5; i++) {
            addEmojiMessage(networks, i);
        }
        // 添加普通消息
        for (int i = 0; i < 3; i++) {
            addOtherMessage(networks);
        }
        // 所有emoji热度均<limit，应全部删除
        Assert.assertTrue(checkDeleteColdEmoji(networks, limit));
    }

    private int getRandomPersonId() {
        Random random = new Random();
        int id = random.nextInt();
        while (personIds.contains(id)) {
            id = random.nextInt();
        }
        personIds.add(id);
        return id;
    }

    private int getRandomMessageId() {
        Random random = new Random();
        int id = random.nextInt();
        while (messageIds.contains(id)) {
            id = random.nextInt();
        }
        messageIds.add(id);
        return id;
    }

    private NetworkInterface[] createNetworks() {
        NetworkInterface[] networks = new NetworkInterface[2];
        networks[0] = new Network();
        networks[1] = new Network();
        return networks;
    }

    private EmojiMessageInterface[] addEmojiMessage(NetworkInterface[] networks, int id) throws EqualPersonIdException, PersonIdNotFoundException, EqualRelationException, EqualEmojiIdException, EmojiIdNotFoundException, EqualMessageIdException, ArticleIdNotFoundException {
        int person1Id = getRandomPersonId();
        int person2Id = getRandomPersonId();
        int person1Age = new Random().nextInt(200) + 1;
        int person2Age = new Random().nextInt(200) + 1;
        int relationValue = new Random().nextInt(200) + 1;
        PersonInterface network1Person1 = new Person(person1Id, "Person " + person1Id, person1Age);
        PersonInterface network1Person2 = new Person(person2Id, "Person " + person2Id, person2Age);
        PersonInterface network2Person1 = new Person(person1Id, "Person " + person1Id, person1Age);
        PersonInterface network2Person2 = new Person(person2Id, "Person " + person2Id, person2Age);
        networks[0].addPerson(network1Person1);
        networks[0].addPerson(network1Person2);
        networks[0].addRelation(person1Id, person2Id, relationValue);
        networks[1].addPerson(network2Person1);
        networks[1].addPerson(network2Person2);
        networks[1].addRelation(person1Id, person2Id, relationValue);
        int messageId = getRandomMessageId();
        EmojiMessageInterface network1EmojiMessage = new EmojiMessage(messageId, id, network1Person1, network1Person2);
        EmojiMessageInterface network2EmojiMessage = new EmojiMessage(messageId, id, network2Person1, network2Person2);
        if (!networks[0].containsEmojiId(id)) {
            networks[0].storeEmojiId(id);
        }
        if (!networks[1].containsEmojiId(id)) {
            networks[1].storeEmojiId(id);
        }
        networks[0].addMessage(network1EmojiMessage);
        networks[1].addMessage(network2EmojiMessage);
        return new EmojiMessageInterface[]{network1EmojiMessage, network2EmojiMessage};
    }

    private void addOtherMessage(NetworkInterface[] networks) throws EqualPersonIdException, PersonIdNotFoundException, EqualRelationException, EmojiIdNotFoundException, EqualMessageIdException, ArticleIdNotFoundException {
        int person1Id = getRandomPersonId();
        int person2Id = getRandomPersonId();
        int person1Age = new Random().nextInt(200) + 1;
        int person2Age = new Random().nextInt(200) + 1;
        int relationValue = new Random().nextInt(200) + 1;
        PersonInterface network1Person1 = new Person(person1Id, "Person " + person1Id, person1Age);
        PersonInterface network1Person2 = new Person(person2Id, "Person " + person2Id, person2Age);
        PersonInterface network2Person1 = new Person(person1Id, "Person " + person1Id, person1Age);
        PersonInterface network2Person2 = new Person(person2Id, "Person " + person2Id, person2Age);
        networks[0].addPerson(network1Person1);
        networks[0].addPerson(network1Person2);
        networks[0].addRelation(person1Id, person2Id, relationValue);
        networks[1].addPerson(network2Person1);
        networks[1].addPerson(network2Person2);
        networks[1].addRelation(person1Id, person2Id, relationValue);
        int messageId = getRandomMessageId();
        int socialValue = new Random().nextInt(200) + 1;
        MessageInterface network1Message = new Message(messageId, socialValue, network1Person1, network1Person2);
        MessageInterface network2Message = new Message(messageId, socialValue, network2Person1, network2Person2);
        networks[0].addMessage(network1Message);
        networks[1].addMessage(network2Message);
    }

    private void sendMessage(NetworkInterface[] networks, MessageInterface[] messages) throws RelationNotFoundException, TagIdNotFoundException, MessageIdNotFoundException {
        networks[0].sendMessage(messages[0].getId());
        networks[1].sendMessage(messages[1].getId());
    }

    private boolean checkDeleteColdEmoji(NetworkInterface[] networks, int limit) {
        int[] oldEmojiIds = ((Network) networks[1]).getEmojiIdList();
        int[] oldEmojiHeats = ((Network) networks[1]).getEmojiHeatList();
        MessageInterface[] oldMessages = ((Network) networks[1]).getMessages();

        int result = networks[0].deleteColdEmoji(limit);

        int[] newEmojiIds = ((Network) networks[0]).getEmojiIdList();
        int[] newEmojiHeats = ((Network) networks[0]).getEmojiHeatList();
        MessageInterface[] newMessages = ((Network) networks[0]).getMessages();

        for (int i = 0; i < oldEmojiIds.length; i++) {
            if (oldEmojiHeats[i] >= limit) {
                boolean found = false;
                for (int newEmojiId : newEmojiIds) {
                    if (oldEmojiIds[i] == newEmojiId) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }

        for (int i = 0; i < newEmojiIds.length; i++) {
            boolean found = false;
            for (int j = 0; j < oldEmojiIds.length; j++) {
                if (newEmojiIds[i] == oldEmojiIds[j] && newEmojiHeats[i] == oldEmojiHeats[j]) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        int newEmojiIdsLength = 0;
        for (int i = 0; i < oldEmojiIds.length; i++) {
            if (oldEmojiHeats[i] >= limit) {
                newEmojiIdsLength++;
            }
        }
        if (newEmojiIdsLength != newEmojiIds.length) {
            return false;
        }

        if (newEmojiIds.length != newEmojiHeats.length) {
            return false;
        }

        for (MessageInterface oldMessage : oldMessages) {
            if (oldMessage instanceof EmojiMessageInterface) {
                int emojiId = ((EmojiMessageInterface) oldMessage).getEmojiId();
                boolean valid = false;
                for (int newEmojiId : newEmojiIds) {
                    if (emojiId == newEmojiId) {
                        valid = true;
                        break;
                    }
                }
                if (!valid) {
                    continue;
                }
            }
            boolean found = false;
            for (MessageInterface newMessage : newMessages) {
                if (isMessageEqual(oldMessage, newMessage)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        int newMessagesLength = 0;
        for (MessageInterface oldMessage : oldMessages) {
            if (oldMessage instanceof EmojiMessageInterface) {
                int emojiId = ((EmojiMessageInterface) oldMessage).getEmojiId();
                for (int newEmojiId : newEmojiIds) {
                    if (emojiId == newEmojiId) {
                        newMessagesLength++;
                        break;
                    }
                }
            } else {
                newMessagesLength++;
            }
        }
        if (newMessagesLength != newMessages.length) {
            return false;
        }

        return result == newEmojiIds.length;
    }

    private boolean isMessageEqual(MessageInterface message1, MessageInterface message2) {
        return message1.getId() == message2.getId() && message1.getSocialValue() == message2.getSocialValue()
                && message1.getType() == message2.getType() && Objects.equals(message1.getPerson1(), message2.getPerson1())
                && Objects.equals(message1.getPerson2(), message2.getPerson2()) && Objects.equals(message1.getTag(), message2.getTag());
    }
}