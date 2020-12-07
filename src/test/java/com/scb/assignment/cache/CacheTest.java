package com.scb.assignment.cache;

import com.scb.assignment.cache.impl.CacheImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.Assert.*;


public class CacheTest {


    @Test
    public void smokeTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException, ExecutionException {

        Function<Integer, String> function = e -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            return String.valueOf(e);
        };
        Cache<Integer, String> cache = new CacheImpl<>(function);

        assertEquals(String.valueOf(1), cache.get(1));

        Field map = cache.getClass().
                getDeclaredField("cacheMap");
        map.setAccessible(true);

        ConcurrentHashMap<Integer, Optional<String>> privateMap = (ConcurrentHashMap<Integer, Optional<String>>) map.get(cache);

        assertEquals(privateMap.size(), 1);
        assertEquals(privateMap.get(1).get(), cache.get(1));


    }

    @Test
    public void mapSanityCheckTest() throws InterruptedException {
        AtomicInteger i = new AtomicInteger(0);
        Function<Integer, String> function = e -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.out.println("I am here");
            return String.valueOf(e + i.getAndIncrement());
        };

        Cache<Integer, String> cache = new CacheImpl<>(function);

        IntStream.range(0,20).unordered().parallel().forEach( e -> {
            String val = null;
            try {
                val = cache.get(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.out.println(val);
            assertEquals("1", val);
        });

    }

    @Test(expected = NoSuchElementException.class)
    public void nullFunctionTest() throws InterruptedException {
        AtomicInteger i = new AtomicInteger(0);
        Function<Integer, String> function = e -> {

                return null;

        };

        Cache<Integer, String> cache = new CacheImpl<>(function);
        cache.get(1);
    }




    @Test
    public void singleThreadTest() throws InterruptedException {

        Function<Integer, String> function = e -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            return String.valueOf(e);
        };
        Cache<Integer, String> cache = new CacheImpl<>(function);

        for(int i = 0; i < 10; i++)
        {
            System.out.println(cache.get(i));
            assertEquals(String.valueOf(i), cache.get(i));
        }
    }

    @Test
    public void parallelExecutionTest()
    {
        Function<String, Integer> function = e -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            return Integer.valueOf(e);
        };

        Cache<String, Integer> cache = new CacheImpl<>(function);

        // Performs expensive operation for the first iteration
        // Second and third iteration should get the cached value
        for(int i = 0; i < 3; i++)
        {
            long startTime = Instant.now().toEpochMilli();
            IntStream.range(0,20).unordered().parallel().forEach( e -> {
                Integer val = Integer.MIN_VALUE;
                try {
                    val = cache.get(String.valueOf(e));
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.out.println(val);
                assertEquals(Integer.valueOf(e), val);
            });

            long endTime = Instant.now().toEpochMilli();
            System.out.printf("Time taken for iteration %d -> %d%n", i+1, endTime - startTime);

        }
    }

}
