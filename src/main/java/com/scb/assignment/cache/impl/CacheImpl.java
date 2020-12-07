package com.scb.assignment.cache.impl;

import com.scb.assignment.cache.Cache;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;

public class CacheImpl<K, V> implements Cache<K, V> {

    private final Function<K, V> cacheFunction;
    private final ConcurrentMap<K, Optional<V>> cacheMap;
    private final Set<K> lockedKeys;

    public CacheImpl(Function<K, V> function)
    {
        this.cacheFunction = function;
        this.cacheMap = new ConcurrentHashMap<>();
        this.lockedKeys = new HashSet<>();
    }

    @Override
    public V get(K key) throws InterruptedException, NoSuchElementException {

        Optional<V> result = cacheMap.get(key);

        if(result == null) {
            try{
                lock(key);
                if(cacheMap.get(key) == null) {
                    result = Optional.ofNullable(cacheFunction.apply(key));
                    cacheMap.putIfAbsent(key, result);
                }
            } finally {
                unlock(key);
            }
        }
        return cacheMap.getOrDefault(key, Optional.empty()).get();

    }

    private void lock(K key) throws InterruptedException {
        synchronized (lockedKeys) {
            while (!lockedKeys.add(key)) {
                lockedKeys.wait();
            }
        }

    }

    private void unlock(K key) {
        synchronized (lockedKeys) {
            lockedKeys.remove(key);
            lockedKeys.notifyAll();
        }
    }

}