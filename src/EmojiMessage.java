import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class EmojiMessage extends Message implements EmojiMessageInterface {
    public EmojiMessage(int messageId, int emojiNumber, PersonInterface messagePerson1,
        PersonInterface messagePerson2) {
        super(messageId, emojiNumber, messagePerson1, messagePerson2);
    }

    public EmojiMessage(int messageId, int emojiNumber, PersonInterface messagePerson1,
        TagInterface messageTag) {
        super(messageId, emojiNumber, messagePerson1, messageTag);
    }

    @Override
    public int getEmojiId() {
        return getSocialValue();
    }
}
