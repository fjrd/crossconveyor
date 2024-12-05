package ru.tablehero;

import org.junit.jupiter.api.Test;
import ru.tablehero.menu.service.CrossConveyor;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class CrossConveyorTest {

    @Test
    void givenSingleConveyorWithoutIntersections_whenPuttingItems_thenInitialPutsReturns() {
        CrossConveyor crossConveyor = CrossConveyor.builder()
                .addConveyor("conveyor1", 3)
                .build();

        assertNull(crossConveyor.put("conveyor1", 1));
        assertNull(crossConveyor.put("conveyor1", 2));
        assertNull(crossConveyor.put("conveyor1", 3));

        assertEquals(Integer.valueOf(1), crossConveyor.put("conveyor1", 4));
        assertEquals(Integer.valueOf(2), crossConveyor.put("conveyor1", 5));
    }

    @Test
    void givenConveyorWithIntersections_whenPuttingItems_thenIntersectionsUpdateCorrectly() {
        int intersectionIndex = 2;
        CrossConveyor crossConveyor = CrossConveyor.builder()
                .addConveyor("conveyor1", 5)
                .addIntersection(intersectionIndex)
                .build();

        crossConveyor.put("conveyor1", 1);
        crossConveyor.put("conveyor1", 2);
        crossConveyor.put("conveyor1", 3);
        crossConveyor.put("conveyor1", 4);
        crossConveyor.put("conveyor1", 5);
        assertEquals(Integer.valueOf(1), crossConveyor.put("conveyor1", 6));

        assertEquals(Integer.valueOf(4), crossConveyor.getIntersectionsMap().get(intersectionIndex).getValue());
    }

    @Test
    void givenInvalidConveyorName_whenPutIsCalled_thenExceptionIsThrown() {
        CrossConveyor crossConveyor = CrossConveyor.builder()
                .addConveyor("conveyor1", 3)
                .build();
        assertThrows(IllegalArgumentException.class, () -> crossConveyor.put("invalidConveyor", 1));
    }

    @Test
    void givenMultipleConveyors_whenPuttingItems_thenOutputsAreAsExpected() {
        CrossConveyor crossConveyor = CrossConveyor.builder()
                .addConveyor("conveyor1", 3)
                .addConveyor("conveyor2", 3)
                .build();

        assertNull(crossConveyor.put("conveyor1", 1));
        assertNull(crossConveyor.put("conveyor2", 2));
        assertNull(crossConveyor.put("conveyor1", 3));
        assertNull(crossConveyor.put("conveyor2", 4));
        assertNull(crossConveyor.put("conveyor1", 5));
        assertNull(crossConveyor.put("conveyor2", 6));

        // Outputs after conveyor reaches its capacity
        assertEquals(Integer.valueOf(1), crossConveyor.put("conveyor1", 7));
        assertEquals(Integer.valueOf(2), crossConveyor.put("conveyor2", 8));
    }

    @Test
    void givenConcurrentPutsWithIntersections_whenExecuted_thenLocksAreManagedCorrectly() throws InterruptedException, ExecutionException {
        int intersectionIndex = 2;
        CrossConveyor crossConveyor = CrossConveyor.builder()
                .addConveyor("conveyor1", 5)
                .addConveyor("conveyor2", 5)
                .addIntersection(intersectionIndex)
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Void> task1 = () -> {
            for (int i = 1; i <= 1000000; i++) {
                crossConveyor.put("conveyor1", i);
            }
            return null;
        };
        Callable<Void> task2 = () -> {
            for (int i = 1; i <= 1000000; i++) {
                crossConveyor.put("conveyor2", i);
            }
            return null;
        };

        Future<Void> future1 = executor.submit(task1);
        Future<Void> future2 = executor.submit(task2);
        future1.get();
        future2.get();
        executor.shutdown();

        CrossConveyor.Intersection intersection = crossConveyor.getIntersectionsMap().get(intersectionIndex);
        assertNotNull(intersection.getValue());
    }

    @Test
    void givenConveyorWithMaxLengthZero_whenPuttingItems_thenItemIsImmediatelyReturned() {
        CrossConveyor crossConveyor = CrossConveyor.builder()
                .addConveyor("conveyor1", 0)
                .build();

        Integer output = crossConveyor.put("conveyor1", 1);
        assertEquals(Integer.valueOf(1), output);
    }

    @Test
    void givenConveyorWithMultipleIntersections_whenPuttingItems_thenIntersectionsAreUpdated() {
        int intersectionOneIndex = 1;
        int intersectionTwoIndex = 3;
        CrossConveyor crossConveyor = CrossConveyor.builder()
                .addConveyor("conveyor1", 5)
                .addIntersection(intersectionOneIndex)
                .addIntersection(intersectionTwoIndex)
                .build();

        crossConveyor.put("conveyor1", 1);
        crossConveyor.put("conveyor1", 2);
        crossConveyor.put("conveyor1", 3);
        crossConveyor.put("conveyor1", 4);
        crossConveyor.put("conveyor1", 5);

        CrossConveyor.Intersection intersection1 = crossConveyor.getIntersectionsMap().get(intersectionOneIndex);
        CrossConveyor.Intersection intersection2 = crossConveyor.getIntersectionsMap().get(intersectionTwoIndex);

        assertNotNull(intersection1.getValue());
        assertNotNull(intersection2.getValue());
    }
    
    @Test
    void test() {
        int intersectionOneIndex = 3;
        int intersectionTwoIndex = 7;
        String conveyorOneName = "conveyor1";
        String conveyorTwoName = "conveyor2";
        CrossConveyor crossConveyor = CrossConveyor.builder()
                .addConveyor(conveyorOneName, 11)
                .addConveyor(conveyorTwoName, 11)
                .addIntersection(intersectionOneIndex)
                .addIntersection(intersectionTwoIndex)
                .build();
        assertNull(crossConveyor.put(conveyorOneName, 1));
        assertNull(crossConveyor.put(conveyorOneName, 2));
        assertNull(crossConveyor.put(conveyorOneName, 3));
        assertNull(crossConveyor.put(conveyorOneName, 4));
        assertNull(crossConveyor.put(conveyorOneName, 5));
        assertNull(crossConveyor.put(conveyorOneName, 6));
        assertNull(crossConveyor.put(conveyorOneName, 7));
        assertNull(crossConveyor.put(conveyorOneName, 8));
        assertNull(crossConveyor.put(conveyorOneName, 9));
        assertNull(crossConveyor.put(conveyorOneName, 10));
        assertNull(crossConveyor.put(conveyorOneName, 11));
        assertEquals(1, crossConveyor.put(conveyorOneName, 12));

        assertNull(crossConveyor.put(conveyorTwoName, 1));
        assertNull(crossConveyor.put(conveyorTwoName, 2));
        assertNull(crossConveyor.put(conveyorTwoName, 3));
        assertEquals(5, crossConveyor.put(conveyorTwoName, 4)); // from conveyor1
        assertNull(crossConveyor.put(conveyorTwoName, 5));
        assertNull(crossConveyor.put(conveyorTwoName, 6));
        assertNull(crossConveyor.put(conveyorTwoName, 7));
        assertEquals(9, crossConveyor.put(conveyorTwoName, 8)); // from conveyor1
        assertNull(crossConveyor.put(conveyorTwoName, 9));
        assertNull(crossConveyor.put(conveyorTwoName, 10));
        assertNull(crossConveyor.put(conveyorTwoName, 11));
        assertEquals(1,crossConveyor.put(conveyorTwoName, 12));

    }
}