package com.scb.assignment.cache.impl;

import com.scb.assignment.cache.Cache;

import java.util.concurrent.*;
import java.util.function.Function;

public class CacheImpl<K, V> implements Cache<K, V> {

    private final Function<K, V> cacheFunction;
    private ConcurrentMap<K, Future<V>> cacheMap;

    public CacheImpl(Function<K, V> function)
    {
        this.cacheFunction = function;
        this.cacheMap = new ConcurrentHashMap<>();
    }

    /**
     * Method to get the value from cached map
     * Checks for the future object in the map, if present gets the value from the future object
     * If absent, creates a future task and a callable to get the value from the function
     * Future acts as a placeholder, and prevents blocking while adding to the cache
     *
     * @param key
     * @return cached value
     * @throws InterruptedException
     */
    @Override
    public V get(K key) throws InterruptedException{

        Future<V> future = cacheMap.get(key);

        if(future == null) {
            Callable<V> callable = () -> cacheFunction.apply(key);
            FutureTask<V> nextTask = new FutureTask<>(callable);
            cacheMap.putIfAbsent(key, nextTask);

            if(future == null)
            {
                future = nextTask;
                nextTask.run();
            }
        }
        try {
            return future.get();
        } catch(ExecutionException e)
        {
            throw new RuntimeException(e);
        } catch(InterruptedException e)
        {
            cacheMap.remove(key, future);
            throw e;
        }
    }
}
