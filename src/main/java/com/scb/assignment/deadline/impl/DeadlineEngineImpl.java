package com.scb.assignment.deadline.impl;

import com.scb.assignment.deadline.DeadlineEngine;
import com.scb.assignment.util.Pair;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;


public class DeadlineEngineImpl implements DeadlineEngine {

    private AtomicLong currCounter;
    private ConcurrentMap<Long, Pair<Long, Long>> scheduleMap;
    private Queue<Pair<Long, Long>> minHeap;
    private ExecutorService executorService;

    public DeadlineEngineImpl()
    {
        currCounter = new AtomicLong(1L);
        scheduleMap = new ConcurrentHashMap<>();
        minHeap = new PriorityBlockingQueue<>(10, Comparator.comparing(e -> e.first));
        executorService = Executors.newFixedThreadPool(5);
    }

    @Override
    public long schedule(long deadlineMs) {

        Long next = currCounter.getAndIncrement();
        Pair<Long, Long> pair = Pair.of(deadlineMs, next);
        scheduleMap.putIfAbsent(next, pair);
        minHeap.add(pair);
        return next;

    }

    @Override
    public boolean cancel(long requestId) {
        Pair<Long, Long> pair = scheduleMap.get(requestId);
        if(pair != null)
        {
            scheduleMap.remove(requestId, pair);
            minHeap.remove(pair);
            return true;
        }
        return false;
    }

    @Override
    public int poll(long nowMs, Consumer<Long> handler, int maxPoll) {

        int pollCnt = 0;

        while(!minHeap.isEmpty()
                && minHeap.peek().first <= nowMs
                && pollCnt++ < maxPoll)
        {

            Pair<Long, Long> pair = minHeap.poll();
            scheduleMap.remove(pair.second, pair);

            Runnable runnable = () -> handler.accept(pair.second);
            executorService.submit(runnable);

        }
        return pollCnt;
    }

    @Override
    public int size() {
        return scheduleMap.size();
    }
}

