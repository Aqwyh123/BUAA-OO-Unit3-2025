import com.oocourse.spec1.main.PersonInterface;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestClass {
    // 测试空网络的三元组数量
    @Test
    public void testEmptyNetwork() {
        Network network = new Network();
        assertEquals(0, network.queryTripleSum());
    }

    // 测试三人形成三元组的情况
    @Test
    public void testSingleTriple() throws Exception {
        Network network = new Network();
        addPeople(network, 1, 2, 3);
        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 10);
        network.addRelation(3, 1, 10);
        assertEquals(1, network.queryTripleSum());
    }

    // 测试三人不成环的情况
    @Test
    public void testNoTriple() throws Exception {
        Network network = new Network();
        addPeople(network, 1, 2, 3);
        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 10);
        // 缺少 3-1 的关系，不成环
        assertEquals(0, network.queryTripleSum());
    }

    // 测试四人形成两个三元组的情况
    @Test
    public void testMultipleTriples() throws Exception {
        Network network = new Network();
        addPeople(network, 1, 2, 3, 4);
        network.addRelation(1, 2, 10);
        network.addRelation(2, 3, 10);
        network.addRelation(3, 1, 10); // 第一个三元组 [1,2,3]
        network.addRelation(3, 4, 10);
        network.addRelation(4, 2, 10); // 第二个三元组 [2,3,4]
        assertEquals(2, network.queryTripleSum());
        network.modifyRelation(2, 3, -10);
        assertEquals(0, network.queryTripleSum());
    }

    // 测试完全图中的三元组数量（n个节点的完全图应包含C(n,3)个三元组）
    @Test
    public void testCompleteGraph() throws Exception {
        Network network = new Network();
        addPeople(network, 1, 2, 3, 4, 5);

        for (int i = 1; i <= 5; i++) {
            for (int j = i + 1; j <= 5; j++) {
                network.addRelation(i, j, 10);
            }
        }

        // 计算 C(5,3) = 10
        assertEquals(10, network.queryTripleSum());

        network.modifyRelation(1, 2, -10);
        assertEquals(7, network.queryTripleSum());
    }

    // 验证 queryTripleSum 是 pure 方法（调用前后状态不变）
    @Test
    public void testPureMethodNoSideEffects() throws Exception {
        Network network1 = new Network();
        addPeople(network1, 1, 2, 3);
        network1.addRelation(1, 2, 10);
        network1.addRelation(2, 3, 10);
        network1.addRelation(3, 1, 10);

        Network network2 = new Network();
        addPeople(network2, 1, 2, 3);
        network2.addRelation(1, 2, 10);
        network2.addRelation(2, 3, 10);
        network2.addRelation(3, 1, 10);

        network2.queryTripleSum();

        PersonInterface[] persons1 = network1.getPersons();
        PersonInterface[] persons2 = network2.getPersons();
        for (PersonInterface person1 : persons1) {
            boolean isEqual = false;
            for (PersonInterface person2 : persons2) {
                isEqual |= ((Person) person1).strictEquals(person2);
            }
            assertTrue(isEqual);
        }
    }

    // 辅助方法：添加多个 Person 到网络
    private void addPeople(Network network, int... ids) throws Exception {
        for (int id : ids) {
            network.addPerson(new Person(id, "Person" + id, 20));
        }
    }
}