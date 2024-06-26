package ru.kernelpunik.tokenizer;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class CollisionReport {
    private final LinkedHashMap<Long, Integer> collisions;
    @Getter
    private int totalFingerprints = 0;


    public CollisionReport() {
        collisions = new LinkedHashMap<>();
    }

    public void addFingerprint() {
        totalFingerprints += 1;
    }

    public void addCollisionWith(long solutionId) {
        int newCollisionCount = collisions.getOrDefault(solutionId, 0) + 1;
        collisions.put(solutionId, newCollisionCount);
    }

    public Map<Long, Integer> getCollisions() {
        return collisions;
    }

}
