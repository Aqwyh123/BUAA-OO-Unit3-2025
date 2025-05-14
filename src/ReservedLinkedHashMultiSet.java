import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        elementMap.computeIfAbsent(element, e -> new LinkedList<>()).add(newNode);
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