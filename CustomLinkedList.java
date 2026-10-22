/**
 * Custom Generic Singly Linked List with Tail Pointer.
 * 
 * ============================================================================
 * KDU IT22013 DSA VIVA EVALUATION SHEET:
 * ============================================================================
 * 1. Data Structure: Singly Linked List with Head and Tail Pointers.
 * 2. Real-World Justification:
 *    - In Hill Climb Racing, hundreds of dynamic pickups (Gold Coins, Fuel Cans,
 *      Bonus Stars) exist across the hills.
 *    - In an ArrayList or Array, removing a collected coin requires shifting all
 *      subsequent elements, causing costly O(n) array copies at 60 FPS.
 *    - In this Custom Linked List, nodes are dynamically linked via pointers.
 *      Collecting an item simply relinks neighboring node pointers, achieving
 *      instant O(1) removal without memory reallocation or element shifting.
 * 3. Time Complexities:
 *    - Insert at Tail (addLast):   O(1)
 *    - Insert at Head (addFirst):  O(1)
 *    - Delete Head (removeFirst):  O(1)
 *    - Search & Delete by Data:    O(n) search + O(1) pointer relink
 * 4. Space Complexity: O(n) where each node dynamically allocates memory.
 * ============================================================================
 */
public class CustomLinkedList<T> {

    /**
     * Internal Node structure holding data and forward pointer.
     */
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

    /**
     * Checks if the linked list contains no nodes.
     * Time Complexity: O(1)
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns total number of active nodes.
     * Time Complexity: O(1)
     */
    public int size() {
        return size;
    }

    /**
     * Returns the head node for efficient iteration in game loops.
     * Time Complexity: O(1)
     */
    public Node<T> getHead() {
        return head;
    }

    /**
     * Appends a new item to the end of the list.
     * Time Complexity: O(1) using tail pointer.
     */
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

    /**
     * Prepends a new item to the beginning of the list.
     * Time Complexity: O(1).
     */
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

    /**
     * Removes and returns the first item from the list.
     * Time Complexity: O(1).
     */
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

    /**
     * Finds and deletes a specific item by reference/value.
     * Time Complexity: O(n) traversal, O(1) pointer adjustment.
     * @return true if found and removed, false otherwise
     */
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

    /**
     * Retrieves data at a zero-based index by linear traversal.
     * Time Complexity: O(n).
     */
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

    /**
     * Clears all nodes to allow garbage collection.
     * Time Complexity: O(1).
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}
