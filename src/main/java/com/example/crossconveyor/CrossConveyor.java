package ru.tablehero.menu.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrossConveyor {

    @Getter //getter for tests
    private final Map<String, Conveyor> conveyorMap;

    @Getter //getter for tests
    private final Map<Integer, Intersection> intersectionsMap;

    public CrossConveyor(Builder builder) {
        conveyorMap = builder.conveyorMap;
        intersectionsMap = builder.intersections;
    }

    public Integer put(String conveyorName, Integer value) {
        Conveyor conveyor = Optional.ofNullable(conveyorMap.get(conveyorName))
                .orElseThrow(() -> new IllegalArgumentException("Conveyor " + conveyorName + " not found"));
        return conveyor.put(value, intersectionsMap.values());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, Conveyor> conveyorMap = new HashMap<>();
        private final Map<Integer, Intersection> intersections = new HashMap<>();

        public Builder addConveyor(String name, Integer maxLength) {
            conveyorMap.put(name, new Conveyor(maxLength));
            return this;
        }

        public Builder addIntersection(Integer index) {
            intersections.put(index, new Intersection(index));
            return this;
        }

        public CrossConveyor build() {
            return new CrossConveyor(this);
        }
    }

    @Getter
    public static class Conveyor {
        private final Integer maxLength;
        private final LinkedList<Integer> list;

        public Conveyor(Integer maxLength) {
            this.maxLength = maxLength;
            list = new LinkedList<>(Collections.nCopies(maxLength, null));
        }

        public Integer put(Integer value, Collection<Intersection> intersections) {
            intersections.stream()
                    .sorted(Comparator.comparing(Intersection::getIndex))
                    .forEach(intersection -> {
                        intersection.getIntersectionLock().lock();
                        list.set(intersection.getIndex(), intersection.getValue());
                    });
            list.addFirst(value);
            Integer result = list.pollLast();
            intersections.stream()
                    .sorted(Comparator.comparing(Intersection::getIndex).reversed())
                    .forEach(intersection -> {
                        intersection.setValue(list.get(intersection.getIndex()));
                        intersection.getIntersectionLock().unlock();
                    });
            return result;
        }
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Intersection {
        private final Integer index;
        private Integer value = null;
        private Lock intersectionLock = new ReentrantLock();
    }
}
