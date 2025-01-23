package itmo.anastasiya;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class MemoryManager {

    private final Map<String, ObjectEntry> globalMemory = new HashMap<>();

    private final Stack<Map<String, ObjectEntry>> callStack = new Stack<>();

    private Object returnValue;

    private static final long MAX_STACK_DEPTH = 1000;
    private static class ObjectEntry {
        Object value;
        long refCount;

        ObjectEntry(Object value) {
            this.value = value;
            this.refCount = 1;
        }
    }

    public void allocate(String name, Object value) {
        if (isInFunction()) {
            allocateLocal(name, value);
        } else {
            if (globalMemory.containsKey(name)) {
                releaseReference(name);
            }
            globalMemory.put(name, new ObjectEntry(value));
        }
    }

    public void addReference(String name) {
        ObjectEntry entry = getMemoryEntry(name);
        if (entry != null) {
            entry.refCount++;
        } else {
            throw new RuntimeException("Variable " + name + " does not exist");
        }
    }

    public void exitFunction() {
        Map<String, ObjectEntry> localMemory = callStack.pop(); // Pop before iterating
        for (String name : localMemory.keySet()) {
            if (!globalMemory.containsKey(name)) {
                ObjectEntry entry = localMemory.get(name);
                if (entry != null) {
                    entry.refCount--;
                }
            }
        }
    }


    public void releaseReference(String name) {
        ObjectEntry entry = getMemoryEntry(name);
        if (entry != null) {
            entry.refCount--;
            if (entry.refCount <= 0) {
                //Determine which scope to remove from.
                if (isInFunction() && callStack.peek().containsKey(name)) {
                    callStack.peek().remove(name);
                } else {
                    globalMemory.remove(name);
                }
            }
        } else {
            throw new RuntimeException("Variable " + name + " does not exist");
        }
    }

    public Object getValue(String name) {
        ObjectEntry entry = getMemoryEntry(name);
        return (entry != null) ? entry.value : null;
    }

    public void allocateArray(String name, long size) {
        allocate(name, new Object[Math.toIntExact(size)]);
    }

    public Object[] getArray(String name) {
        Object value = getValue(name);
        if (value instanceof Object[]) {
            return (Object[]) value;
        }
        throw new RuntimeException("Variable " + name + " is not an array");
    }

    public void setArrayElement(String name, long index, Object value) {
        Object[] array = getArray(name);
        if (index < 0 || index >= array.length) {
            throw new RuntimeException("Array index out of bounds: " + index);
        }
        array[Math.toIntExact(index)] = value;
    }

    public Object getArrayElement(String name, long index) {
        Object[] array = getArray(name);
        if (index < 0 || index >= array.length) {
            throw new RuntimeException("Array index out of bounds: " + index);
        }
        return array[Math.toIntExact(index)];
    }

    public void enterFunction() {
        if (callStack.size() >= MAX_STACK_DEPTH) {
            throw new StackOverflowError("Maximum recursion depth exceeded");
        }
        callStack.push(new HashMap<>());
    }

    public void allocateLocal(String name, Object value) {
        if (!isInFunction()) {
            throw new RuntimeException("Local allocation can only be done inside a function");
        }

        Map<String, ObjectEntry> currentMemory = callStack.peek();
        if (currentMemory.containsKey(name)) {
            releaseReference(name);
        }
        currentMemory.put(name, new ObjectEntry(value));
    }

    public void setReturnValue(Object value) {
        this.returnValue = value;
    }

    public Object getReturnValue() {
        return this.returnValue;
    }

    private boolean isInFunction() {
        return !callStack.isEmpty();
    }

    private Map<String, ObjectEntry> getCurrentMemory() {
        return isInFunction() ? callStack.peek() : globalMemory;
    }

    private ObjectEntry getMemoryEntry(String name) {
        if (isInFunction() && callStack.peek().containsKey(name)) {
            return callStack.peek().get(name);
        }
        return globalMemory.get(name);
    }
    public void allocateArray(String name, Object[] array) {
        allocate(name, array);
    }
}