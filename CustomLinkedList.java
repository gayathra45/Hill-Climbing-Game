/**
 * Custom Generic Singly Linked List.
 * 
 * Real-World Justifications for Hill Climb Racing:
 * 1. Active Collectibles: Coins, fuel canisters, and stars scattered across the terrain
 *    are stored in a Linked List. When the player collects a coin, it is removed in O(1)
 *    time without costly array copy operations.
 * 2. Spline Waypoints: Dynamic terrain spline control nodes continuously attach to the tail
 *    and detach from the head as the vehicle moves forward.
 * 
 * IT22013: Data Structures and Algorithms Mini Game
 */
public class CustomLinkedList<T> {
    public static class Node<T> {
        public T data;
        public Node<T> next;

        public Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head = newNode;
        }
        size++;
    }

    public T removeFirst() {
        if (isEmpty()) return null;
        T data = head.data;
        head = head.next;
        size--;
        if (head == null) {
            tail = null;
        }
        return data;
    }

    public boolean remove(T data) {
        if (isEmpty()) return false;
        if (head.data.equals(data)) {
            removeFirst();
            return true;
        }

        Node<T> current = head;
        while (current.next != null && !current.next.data.equals(data)) {
            current = current.next;
        }

        if (current.next != null) {
            if (current.next == tail) {
                tail = current;
            }
            current.next = current.next.next;
            size--;
            return true;
        }
        return false;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> curr = head;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.data;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public Node<T> getHead() {
        return head;
    }
}
