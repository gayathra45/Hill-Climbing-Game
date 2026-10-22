/**
 * Custom Generic LIFO Stack (Last-In, First-Out).
 * Implemented using an internal array with dynamic safety checks.
 * 
 * Real-World Justifications for Hill Climb Racing:
 * 1. Stunt Combo History: Air stunts (Backflips, Wheelies, Air Time) are pushed
 *    onto the stack in real-time. The most recent stunt pops first to calculate
 *    combo multipliers.
 * 2. Fuel Boost Revert / Emergency Undo Buffer: Stores state snapshots to roll back
 *    in case of emergency rewind mechanics.
 * 
 * IT22013: Data Structures and Algorithms Mini Game
 */
public class CustomStack<T> {
    private Object[] elements;
    private int top;
    private int capacity;

    public CustomStack() {
        this(20);
    }

    public CustomStack(int capacity) {
        this.capacity = capacity;
        this.elements = new Object[capacity];
        this.top = -1;
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public boolean isFull() {
        return top == capacity - 1;
    }

    public int size() {
        return top + 1;
    }

    public void push(T item) {
        if (isFull()) {
            // Shift elements down if stack overflows to maintain rolling recent history
            for (int i = 0; i < capacity - 1; i++) {
                elements[i] = elements[i + 1];
            }
            elements[capacity - 1] = item;
            return;
        }
        elements[++top] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack Underflow: No items in stack!");
        }
        T item = (T) elements[top];
        elements[top--] = null;
        return item;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return (T) elements[top];
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index > top) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }
        return (T) elements[index];
    }

    public void clear() {
        for (int i = 0; i <= top; i++) {
            elements[i] = null;
        }
        top = -1;
    }
}
