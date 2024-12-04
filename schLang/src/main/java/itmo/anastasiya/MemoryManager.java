package itmo.anastasiya;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MemoryManager {
    private final Map<String, ObjectEntry> memory = new ConcurrentHashMap<>();

    private static class ObjectEntry {
        Object value;
        int refCount;

        ObjectEntry(Object value) {
            this.value = value;
            this.refCount = 1;
        }
    }


    public synchronized void allocate(String name, Object value) {
        if (memory.containsKey(name)) {
            releaseReference(name);
        }
        memory.put(name, new ObjectEntry(value));
    }


    public synchronized void addReference(String name) {
        ObjectEntry entry = memory.get(name);
        if (entry != null) {
            entry.refCount++;
        }
    }

    public synchronized void releaseReference(String name) {
        deallocate(name);
    }


    public synchronized Object getValue(String name) {
        ObjectEntry entry = memory.get(name);
        return (entry != null) ? entry.value : null;
    }

    private synchronized void deallocate(String name) {
        ObjectEntry entry = memory.get(name);
        if (entry != null) {
            entry.refCount--;
            if (entry.refCount <= 0) {
                memory.remove(name);
                System.out.println("Object " + name + " deallocated");
            }
        }
    }
}
