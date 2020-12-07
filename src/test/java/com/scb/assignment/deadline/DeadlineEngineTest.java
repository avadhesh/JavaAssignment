package com.scb.assignment.deadline;

import com.scb.assignment.deadline.impl.DeadlineEngineImpl;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Instant;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class DeadlineEngineTest {

    @Test
    public void smokeTest() throws InterruptedException {
        Consumer<Long> consumer = t -> {
            try {
                Thread.sleep(1000);
                System.out.println(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        DeadlineEngine engine = new DeadlineEngineImpl();

        long first = engine.schedule(Instant.now().toEpochMilli());
        assertEquals(1, engine.size());

        long second = engine.schedule(Instant.now().toEpochMilli());
        assertEquals(2, engine.size());

        engine.cancel(1);
        assertEquals(1, engine.size());

        engine.poll(Instant.now().toEpochMilli(), consumer , 3);
        assertEquals(0, engine.size());

        engine.schedule(Instant.now().toEpochMilli());
        engine.schedule(Instant.now().toEpochMilli());
        assertEquals(2, engine.size());

        engine.poll(Instant.now().toEpochMilli(), consumer, 2);
        assertEquals(0, engine.size());

        Thread.sleep(5000);

    }

    @Test
    public void negativeTest() throws InterruptedException {
        Consumer<Long> consumer = t -> {
            try {
                Thread.sleep(1000);
                System.out.println(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        DeadlineEngine engine = new DeadlineEngineImpl();

        engine.schedule(Instant.now().toEpochMilli());
        assertEquals(1, engine.size());

        engine.poll(Instant.now().toEpochMilli(), consumer, -1);
        assertEquals(1, engine.size());

        engine.poll(Instant.now().toEpochMilli(), consumer, 0);
        assertEquals(1, engine.size());

        engine.poll(Instant.now().toEpochMilli(), consumer, 1);
        assertEquals(0, engine.size());

        engine.poll(Instant.now().toEpochMilli(), consumer, 1);
        assertEquals(0, engine.size());

        boolean result = engine.cancel(1L);
        assertFalse(result);
        assertEquals(0, engine.size());

        Thread.sleep(5000);

    }

    @Test
    public void maxPollTest() throws InterruptedException {
        Consumer<Long> consumer = t -> {
            try {
                Thread.sleep(1000);
                System.out.println(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        DeadlineEngine engine = new DeadlineEngineImpl();

        IntStream.range(0,10).parallel().unordered().forEach( e -> engine.schedule(Instant.now().toEpochMilli()));
        assertEquals(10, engine.size());

        engine.poll(Instant.now().toEpochMilli(), consumer, 4);
        assertEquals(6, engine.size());

        engine.poll(Instant.now().toEpochMilli(), consumer, 6);
        assertEquals(0, engine.size());

        Thread.sleep(5000);

    }
}
