/**
 * Custom Generic LIFO Stack (Last-In, First-Out).
 * Implemented using an internal array with dynamic safety checks.
 * 
 * ============================================================================
 * KDU IT22013 DSA VIVA EVALUATION SHEET:
 * ============================================================================
 * 1. Data Structure: Stack (LIFO - Last-In, First-Out)
 * 2. Real-World Justification:
 *    - In Hill Climb Racing, vehicle aerial stunts (e.g., Backflip, Wheelie, Air-Time)
 *      are performed in rapid succession.
 *    - When the driver lands safely, stunts must be evaluated in REVERSE CHRONOLOGICAL
 *      ORDER so the most recent stunt chains with the prior stunts to calculate
 *      exponential combo multiplier bonuses.
 * 3. Time Complexities:
 *    - Push: O(1) constant time
 *    - Pop:  O(1) constant time
 *    - Peek: O(1) constant time
 * 4. Space Complexity: O(n) where n is fixed stack capacity.
 * ============================================================================
 */
public class CustomStack<T> {
    private Object[] elements;
    private int top;
    private int capacity;

    /**
     * Default constructor initializes stack with default capacity of 20 items.
     */
    public CustomStack() {
        this(20);
    }

    /**
     * Parameterized constructor initializes stack with specified capacity.
     * @param capacity maximum number of elements the stack can hold.
     */
    public CustomStack(int capacity) {
        this.capacity = capacity;
        this.elements = new Object[capacity];
        this.top = -1;
    }

    /**
     * Checks if the stack has no elements.
     * Time Complexity: O(1)
     */
    public boolean isEmpty() {
        return top == -1;
    }

    /**
     * Checks if the stack has reached maximum capacity.
     * Time Complexity: O(1)
     */
    public boolean isFull() {
        return top == capacity - 1;
    }

    /**
     * Returns the current number of elements in the stack.
     * Time Complexity: O(1)
     */
    public int size() {
        return top + 1;
    }

    /**
     * Pushes a new item onto the top of the stack.
     * If the stack is full, it evicts the oldest item (shifts down) to maintain
     * a rolling window of recent stunts without crashing.
     * Time Complexity: O(1) amortized
     * @param item element to push
     */
    public void push(T item) {
        if (isFull()) {
            // Shift elements down to accommodate newest stunt
            for (int i = 0; i < capacity - 1; i++) {
                elements[i] = elements[i + 1];
            }
            elements[capacity - 1] = item;
            return;
        }
        elements[++top] = item;
    }

    /**
     * Removes and returns the top element from the stack.
     * Throws IllegalStateException if stack is empty.
     * Time Complexity: O(1)
     * @return top element
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack Underflow: No items in stack!");
        }
        T item = (T) elements[top];
        elements[top--] = null; // Prevent memory leak
        return item;
    }

    /**
     * Retrieves the top element without removing it.
     * Time Complexity: O(1)
     * @return top element, or null if empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return (T) elements[top];
    }

    /**
     * Retrieves an element at a specific zero-indexed position in stack.
     * Time Complexity: O(1)
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index > top) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }
        return (T) elements[index];
    }

    /**
     * Clears all elements from the stack.
     * Time Complexity: O(n)
     */
    public void clear() {
        for (int i = 0; i <= top; i++) {
            elements[i] = null;
        }
        top = -1;
    }
}
