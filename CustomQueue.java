/**
 * Custom Generic FIFO Queue (First-In, First-Out).
 * Implemented using an efficient Circular Array to avoid element shifting.
 * 
 * ============================================================================
 * KDU IT22013 DSA VIVA EVALUATION SHEET:
 * ============================================================================
 * 1. Data Structure: Queue (FIFO - First-In, First-Out)
 * 2. Real-World Justification:
 *    - In infinite terrain scrolling, upcoming world features (hills, valleys,
 *      suspension bridges, checkpoint milestones) must be generated and loaded
 *      in chronological distance order ahead of the camera view.
 *    - The earliest generated terrain chunk is the first to be reached and passed
 *      by the vehicle, and is subsequently dequeued and evicted to prevent
 *      memory leaks and maintain 60 FPS performance.
 * 3. Time Complexities:
 *    - Enqueue: O(1) constant time
 *    - Dequeue: O(1) constant time
 *    - Peek:    O(1) constant time
 * 4. Space Complexity: O(n) where n is fixed circular buffer capacity.
 * ============================================================================
 */
public class CustomQueue<T> {
    private Object[] elements;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    /**
     * Default constructor initializes queue with default capacity of 30.
     */
    public CustomQueue() {
        this(30);
    }

    /**
     * Parameterized constructor initializes circular buffer with specified capacity.
     * @param capacity maximum number of queue slots
     */
    public CustomQueue(int capacity) {
        this.capacity = capacity;
        this.elements = new Object[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    /**
     * Checks if queue is full.
     * Time Complexity: O(1)
     */
    public boolean isFull() {
        return size == capacity;
    }

    /**
     * Checks if queue has no elements.
     * Time Complexity: O(1)
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns current number of items stored in queue.
     * Time Complexity: O(1)
     */
    public int size() {
        return size;
    }

    /**
     * Inserts an item at the rear of the queue using circular wrap-around indexing.
     * If full, automatically dequeues the oldest chunk to accommodate newest chunks.
     * Time Complexity: O(1)
     * @param item element to enqueue
     */
    public void enqueue(T item) {
        if (isFull()) {
            dequeue(); // Evict oldest segment to accommodate new procedural chunks
        }
        rear = (rear + 1) % capacity;
        elements[rear] = item;
        size++;
    }

    /**
     * Removes and returns the item from the front of the queue.
     * Throws IllegalStateException if queue is empty.
     * Time Complexity: O(1)
     * @return front element
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue Underflow: Queue is empty!");
        }
        T item = (T) elements[front];
        elements[front] = null; // Prevent memory leak
        front = (front + 1) % capacity;
        size--;
        return item;
    }

    /**
     * Inspects the front item without removing it.
     * Time Complexity: O(1)
     * @return front element, or null if empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return (T) elements[front];
    }

    /**
     * Accesses an element by relative index from front.
     * Time Complexity: O(1)
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        int actual = (front + index) % capacity;
        return (T) elements[actual];
    }

    /**
     * Resets queue pointers and nullifies stored elements.
     * Time Complexity: O(n)
     */
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            elements[i] = null;
        }
        front = 0;
        rear = -1;
        size = 0;
    }
}
