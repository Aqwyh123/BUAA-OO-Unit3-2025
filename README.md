# BUAA-OO-2025-Unit3总结

## 一、架构设计

本单元作业的基本架构设计由`JML`规格规定，类与接口之间一一对应。
本单元作业具体架构设计的首要目标是降低时间复杂度，多采用空间换时间、动态维护等方式。

规格与实现分离的本质，是通过抽象契约解耦需求定义与技术细节。规格保证了功能正确性的下限，而实现的质量（尤其是对时间/空间复杂度的把控）决定了系统性能的上限。但我们仍需警惕理论模型与实际运行的差距，在规格约束下做出务实的设计选择。

### （一）图模型的构建和维护

#### 1.社交关系图

```java
public class Person implements PersonInterface {
	private final HashMap<Integer, Integer> values;
	private final TreeMap<Integer, TreeSet<Integer>> acquaintances;
	...
}
```

`Person`类维护`<relatedPersonId, relationValue>`无序键值对，以及`<relationValue, relatedPersonIds>`有序键值对组，可在不超过$O(log n)$的时间复杂度下完成社交关系图的增、删、改、按`id`查询`value`以及获取最大`value`对应最小`id`。

对于`queryCoupleSum()`这一特殊边计数方法，笔者考虑到动态维护的逻辑过于复杂，因此在确保`queryBestAcquaintance()`的时间复杂度已被降低至$O(1)$的前提下，采用遍历实现，达到可接受的$O(n)$时间复杂度。

对于`queryTripleSum()`这一三元环计数方法，笔者采用动态维护的方式，在添加/删除关系时，计算新增/减少的三元环数量，以增删过程的时间复杂度上升至$O(n)$为代价，将查询方法高达$O(n^3)$的时间复杂度降至$O(1)$。

对于`isCircle()`这一连通性判断方法，以及`queryShortestPath`这一路径长度查询方法，笔者考虑到删除关系时重建并查集过于耗时，且并查集不支持查询路径长度，因此均采用时间复杂度为$O(V+E)$（实际应更小）的双向`BFS`算法。

#### 2.标签关系图

```java
public class Person implements PersonInterface {
	private final HashMap<Integer, TagInterface> ownedTags;
	private final HashMap<SimpleEntry<Integer, Integer>, TagInterface> relatedTags;
	...
}
```

`Person`类维护`<tagId, ownedTag>`键值对，以及`<<ownerId, tagId>, relatedTag>`键值对，可在不超过$O(log n)$的时间复杂度下完成所拥有/被包含标签关系图的增删查改。

`getAgeMean()`均值查询和`getAgeVar()`方差查询的动态维护优化比较简单，但需注意`JML`规格要求的“方差”因“均值”被整除的问题，导致
$$
\frac{\sum_{i = 1}^n(age_i - ageMean)^2}{n} = \frac{(\sum_{i=1}^nage_i^2) - 2\times ageMean \times(\sum_{i=1}^nage_i)+ n\times ageMean^2}{n} \neq \frac{(\sum_{i=1}^nage_i^2) - n\times ageMean^2}{n}
$$
对于`queryTagValueSum()`方法，其朴素实现的时间复杂度为$O(n^2)$，而动态维护无法仅在`Tag`内部完成。尽管动态维护的逻辑非常复杂，笔者还是选择额外维护被包含标签，以增删改过程的时间复杂度上升至$O(n)$为代价，在关系变动时通知相关Tag维护其`valueSum`。但笔者认为，这一优化的性价比不高，且很可能在一定的数据规模下因增大常数而起到反作用。

### （二）性能问题及修复情况

本单元作业中，笔者遇到的唯一性能问题与`receivedArticles`结构的维护有关。
按照`JML`规格，这个结构应存储`Integer`，要求有序，支持头插、删除、包含性查询以及转为`List`。

如果使用`ArrayList`，头插、删除和包含性查询的时间复杂度均为$O(n)$，无需多余操作即可作为`List`返回；如果使用`LinkedList`，头插的时间复杂度可降至`O(1)`；如果使用`LinkedHashSet`，转为List的时间复杂度上升为O(n)，但其余操作的时间复杂度均降至$O(logn)$，唯一缺点在于不支持头插。

第二次作业中，该结构无需支持元素重复。笔者最初考虑使用`LinkedHashSet`，其转为`List`的方法被笔者实现为先转为`ArrayList`再逆序。这个过程显然不够优雅，但笔者未能想到遍历头插`LinkedList`的实现，出于朴素的对顺序表非尾插的不信任，笔者决定换用`LinkedList`。然而，笔者未注意到该结构的删除方法被一个$O(n)$的算法调用，且未考虑到`LinkedList`对缓存不友好，更不知晓其Java实现存在的性能问题，使程序中出现时间复杂度至少为$O(n^2)$的方法，最终导致超时。

笔者换用`LinkedHashSet`解决了这一问题。经过测试，`ArrayList`也可满足需要，这说明即使在Java这类高度封装的面向对象编程语言中，时间复杂度也不是算法设计在时间方面唯一需要考虑的因素，我们依然要有常数优化的意识，考虑缓存甚至内部细节实现。

然而在第三次作业中，该结构需要支持元素重复。笔者不愿依赖`ArrayList`的常数优势，参照`LinkedHashSet`原理自行实现了一个新容器

```java
public class ReservedLinkedHashMultiSet<E> {
    private static class Node<E> {
        private final E element;
        private Node<E> prev;
        private Node<E> next;

        public Node(E element) {
            this.element = element;
        }
    }

    private final Map<E, List<Node<E>>> elementMap = new HashMap<>();
    private final Node<E> dummyHead = new Node<>(null);
    private final Node<E> dummyTail = new Node<>(null);
    private int size = 0;

    public ReservedLinkedHashMultiSet() {
        dummyHead.next = dummyTail;
        dummyTail.prev = dummyHead;
    }

    public void add(E element) {
        Node<E> newNode = new Node<>(element);
        linkToHead(newNode);
        elementMap.computeIfAbsent(element, e -> new ArrayList<>()).add(newNode);
        size++;
    }

    public boolean contains(E element) {
        return elementMap.containsKey(element);
    }

    public int size() {
        return size;
    }

    public List<E> toList() {
        List<E> result = new ArrayList<>();
        Node<E> current = dummyHead.next;
        while (current != dummyTail) {
            result.add(current.element);
            current = current.next;
        }
        return result;
    }

    public void remove(E element) {
        List<Node<E>> nodes = elementMap.remove(element);
        if (nodes == null) {
            return;
        }
        nodes.forEach(this::unlink);
        size -= nodes.size();
    }

    private void linkToHead(Node<E> node) {
        node.next = dummyHead.next;
        node.prev = dummyHead;
        dummyHead.next.prev = node;
        dummyHead.next = node;
    }

    private void unlink(Node<E> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
    }
}
```

## 二、测试分析

### （一）测试分析

#### 1. 单元测试 (Unit Test)

- **目标**：验证代码的最小可测单元（如单个方法）的正确性  
- **特点**：隔离依赖，测试全面，但编写规模较大

#### 2. 功能测试 (Functional Test)

- **目标**：验证系统功能是否符合需求规格
- **特点**：黑盒测试，编写规模较小，但测试可能不够全面

#### 3. 集成测试 (Integration Test)

- **目标**：检测多个模块/组件协同工作时的交互问题
- **特点**：关注数据流传递，是单元测试的集合

#### 4. 压力测试 (Stress Test)

- **目标**：评估系统在极端数据下的性能
- **特点**：按需测试大数据量、高频操作场景

#### 5. 回归测试 (Regression Test)

- **目标**：确保代码修改后原有功能未被破坏  
- **特点**：复用历史测试用例，贯穿整个开发周期

### （二）Junit 测试总结

#### 1.数据构造策略

对于图结构，构造测试主要考虑图的规模问题，需构造零图及各种规模的图（稀疏图、完全图等）进行测试。
对于`pure`方法，测试数据应随机生成，避免测试不够全面。

#### 2.JML 规格作用

前置条件（`requires`）：定义测试的合法输入范围，需据此构造合法的测试数据。

后置条件（`ensures`）：作为断言的直接依据（`Junit`测试的关键逻辑），可据此逐句检查方法正确性。

异常声明（`signals`）：明确需抛出的异常类型及触发条件，应捕获异常进行检查。

`assignable`语句与`pure`声明：规定可更改的数据模型，要检查方法调用前后有无`unexpected`更改

此外，不变式作为隐含条件，需要时刻结合起来考虑。

通过`JML`规格映射的测试用例，结合通过`JML`规格映射的测试方法，且编写起来逻辑清晰，能严格验证代码实现是否满足规格中定义的所有功能性约束，但无法覆盖时间限制等非功能性需求。

## 三、大模型辅助体验

首次阅读`JML`规格时，可使用大模型辅助解释。
这可以对冲`JML`规格可读性差的影响，但需提示大模型逐句对照，避免大模型生成笼统模糊的自然语言解释，遗漏细节。

部分复杂算法，可使用大模型辅助实现。
给定`JML`规格后，再提示大模型遵守时间复杂度要求，基本可以一次编写出正确代码，必要时给出改进指示，大模型也能很好地遵守。

`Junit`测试用例，可使用大模型辅助生成。
大模型在这方面的能力比较差，表现为滥用反射、生造方法，因此不能给它过多信息。给出测试框架作为限制，可有效防止其兜兜转转回到有限的训练集中去。

## 四、学习体会

`JML`用严谨的程序设计语言代替自然语言作为规格，彻底解决了自然语言的模糊性、二义性问题，但同时提升了编写规格的难度，降低了规格的可读性。

受限于`JML`的表达能力（借用`Java`语言），功能复杂的程序很难编写出正确的`JML`规格，有时不得与实现产生一定的耦合，违背了设计与实现相分离的初衷。

更为致命的是，正确性之外还存在性能等规格无法描述的需求。`JML`规格虽然写作程序设计语言的形式，却也因此不能直接对应程序设计语言来实现，最终依然需要程序员将其转化为自然语言来全面分析。

但是，`JML`在测试环节大有用处。复杂的正确性检验可被简单地映射，大大降低了测试程序编写的难度。

