package ru.kernelpunik.tokenizer;

import lombok.Getter;
import ru.kernelpunik.teradactyle.models.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CollisionReport {
    @Getter
    private final String name;
    private final LinkedHashMap<Component, Long> collisions;
    @Getter
    private long totalFingerprints = 0;

    public CollisionReport() {
        this("");
    }

    public CollisionReport(String name) {
        this.name = name;
        collisions = new LinkedHashMap<>();
    }


    public void addFingerprints() {
        addFingerprints(1);
    }

    public void addFingerprints(int n) {
        totalFingerprints += n;
    }

    public void addFingerprints(long n) {
        totalFingerprints += n;
    }
    public void addCollisionWith(Component component) {
        addCollisionWith(component, 1);
    }

    public void addCollisionWith(Component component, long n) {
        long newCollisionCount = collisions.getOrDefault(component, 0L) + n;
        collisions.put(component, newCollisionCount);
    }

    public void addAllCollisions(Map<Component, Long> collisions) {
        for (Map.Entry<Component, Long> c : collisions.entrySet()) {
            addCollisionWith(c.getKey(), c.getValue());
        }
    }

    public Map<Component, Long> getCollisions() {
        return collisions;
    }

    public void removeBelow(double rate) {
        for (Map.Entry<Component, Long> entry : collisions.entrySet()) {
            if (entry.getValue() * 1.0 / totalFingerprints < rate) {
                collisions.remove(entry.getKey());
            }
        }
    }

    public void removeAll(Set<Component> toRemove) {
        for (Component c : toRemove) {
            collisions.remove(c);
        }
    }

}
