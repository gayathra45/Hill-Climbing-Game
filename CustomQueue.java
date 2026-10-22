/**
 * Custom Generic FIFO Queue (First-In, First-Out).
 * Implemented using a circular array.
 * 
 * Real-World Justifications for Hill Climb Racing:
 * 1. Procedural Terrain & Obstacle Pipeline: Upcoming hills, valleys, bridges, and
 *    checkpoints are enqueued ahead of the camera view in chronological order and
 *    dequeued as the player passes them to maintain infinite smooth scrolling.
 * 2. Coin Cluster Generation: Groups of coins along flight arcs are queued
 *    sequentially to match the physics arc of jumps.
 * 
 * IT22013: Data Structures and Algorithms Mini Game
 */
public class CustomQueue<T> {
    private Object[] elements;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    public CustomQueue() {
        this(30);
    }

    public CustomQueue(int capacity) {
        this.capacity = capacity;
        this.elements = new Object[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void enqueue(T item) {
        if (isFull()) {
            dequeue(); // Evict oldest segment to accommodate new procedural chunks
        }
        rear = (rear + 1) % capacity;
        elements[rear] = item;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue Underflow: Queue is empty!");
        }
        T item = (T) elements[front];
        elements[front] = null;
        front = (front + 1) % capacity;
        size--;
        return item;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return (T) elements[front];
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        int actual = (front + index) % capacity;
        return (T) elements[actual];
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            elements[i] = null;
        }
        front = 0;
        rear = -1;
        size = 0;
    }
}
