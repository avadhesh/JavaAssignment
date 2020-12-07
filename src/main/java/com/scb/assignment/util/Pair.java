package com.scb.assignment.util;

public class Pair<L, R>{

    public final L first;
    public final R second;

    private Pair(L first, R second)
    {
        this.first = first;
        this.second = second;
    }

    public static <L, R> Pair<L, R> of(L first, R second)
    {
        return new Pair<>(first, second);
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == this)
            return true;

        if(!(o instanceof Pair))
            return false;

        Pair<L, R> p = (Pair<L, R>) o;

        return first.equals(p.first) &&
                second.equals(p.second);
    }
}
