package com.scb.assignment.cache;

@FunctionalInterface
public interface Cache<K, V> {
    V get(K key) throws InterruptedException;
}