import com.oocourse.spec3.main.PersonInterface;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class NetworkTest {
    @Test
    public void testRingRelation() throws Exception {
        final int SIZE = 10;
        Network network = new Network();
        for (int i = 1; i <= SIZE; i++) {
            network.addPerson(new Person(i, "P" + i, 20));
        }
        for (int i = 1; i < SIZE; i++) {
            network.addRelation(i, i + 1, 200);
        }
        network.addRelation(SIZE, 1, 200);
        assertEquals(1, network.queryCoupleSum());
    }

    @Test
    public void testRelationModify() throws Exception {
        Network network = new Network();
        assertEquals(0, network.queryCoupleSum());
        network.addPerson(new Person(1, "A", 20));
        network.addPerson(new Person(2, "B", 25));
        network.addPerson(new Person(3, "C", 30));
        assertEquals(0, network.queryCoupleSum());
        network.addRelation(1, 2, 200);
        assertEquals(1, network.queryCoupleSum());
        network.addRelation(1, 3, 150);
        assertEquals(1, network.queryCoupleSum());
        network.modifyRelation(1, 3, 100);
        assertEquals(1, network.queryCoupleSum());
        network.addRelation(2, 3, 300);
        assertEquals(1, network.queryCoupleSum());
    }

    @Test
    public void testRelationRebuild() throws Exception {
        Network network = new Network();
        network.addPerson(new Person(1, "A", 20));
        network.addPerson(new Person(2, "B", 25));
        network.addRelation(1, 2, 300);
        assertEquals(1, network.queryCoupleSum());
        network.modifyRelation(1, 2, -300);
        assertEquals(0, network.queryCoupleSum());
        network.addRelation(1, 2, 500);
        assertEquals(1, network.queryCoupleSum());
    }

    @Test
    public void testMixedOperations() throws Exception {
        Network network = new Network();
        for (int i = 1; i <= 5; i++) {
            network.addPerson(new Person(i, "P" + i, 20));
        }
        network.addRelation(1, 2, 200);
        network.addRelation(3, 4, 200);
        assertEquals(2, network.queryCoupleSum());
        network.modifyRelation(1, 2, -200);
        assertEquals(1, network.queryCoupleSum());
        network.addRelation(2, 5, 300);
        assertEquals(2, network.queryCoupleSum());
        network.addRelation(4, 5, 300);
        assertEquals(1, network.queryCoupleSum());
    }

    @Test
    public void testPure() throws Exception {
        Network network1 = new Network();
        Network network2 = new Network();
        Random random = new Random();
        for (int i = 1; i <= 4; i++) {
            int age = random.nextInt(200) + 1;
            network1.addPerson(new Person(i, "P" + i, age));
            network2.addPerson(new Person(i, "P" + i, age));
        }
        for (int i = 1; i <= 4; i++) {
            for (int j = i + 1; j <= 4; j++) {
                network1.addRelation(i, j, 200);
                network2.addRelation(i, j, 200);
            }
        }
        network1.queryCoupleSum();
        for (PersonInterface person1 : network1.getPersons()) {
            boolean isEqual = false;
            for (PersonInterface person2 : network2.getPersons()) {
                isEqual |= ((Person) person1).strictEquals(person2);
            }
            assertTrue(isEqual);
        }
    }
}