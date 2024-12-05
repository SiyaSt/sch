package itmo.anastasiya;

import java.util.HashMap;
import java.util.Map;

public class MemoryManager {
    private final Map<String, ObjectEntry> memory = new HashMap<>();

    private static class ObjectEntry {
        Object value;
        int refCount;

        ObjectEntry(Object value) {
            this.value = value;
            this.refCount = 1;
        }
    }

    public void allocate(String name, Object value) {
        if (memory.containsKey(name)) {
            releaseReference(name);
        }
        memory.put(name, new ObjectEntry(value));
    }

    public void addReference(String name) {
        ObjectEntry entry = memory.get(name);
        if (entry != null) {
            entry.refCount++;
        } else {
            throw new RuntimeException("Variable " + name + " does not exist");
        }
    }

    public void releaseReference(String name) {
        ObjectEntry entry = memory.get(name);
        if (entry != null) {
            entry.refCount--;
            if (entry.refCount <= 0) {
                memory.remove(name);
                System.out.println("Object " + name + " deallocated");
            }
        } else {
            throw new RuntimeException("Variable " + name + " does not exist");
        }
    }

    public Object getValue(String name) {
        ObjectEntry entry = memory.get(name);
        return (entry != null) ? entry.value : null;
    }

    public void allocateArray(String name, int size) {
        if (memory.containsKey(name)) {
            releaseReference(name);
        }
        memory.put(name, new ObjectEntry(new Object[size]));
    }

    public Object[] getArray(String name) {
        ObjectEntry entry = memory.get(name);
        if (entry != null && entry.value instanceof Object[]) {
            return (Object[]) entry.value;
        }
        throw new RuntimeException("Variable " + name + " is not an array");
    }

    public void setArrayElement(String name, int index, Object value) {
        Object[] array = getArray(name);
        if (index < 0 || index >= array.length) {
            throw new RuntimeException("Array index out of bounds: " + index);
        }
        array[index] = value;
    }

    public Object getArrayElement(String name, int index) {
        Object[] array = getArray(name);
        if (index < 0 || index >= array.length) {
            throw new RuntimeException("Array index out of bounds: " + index);
        }
        return array[index];
    }
}
