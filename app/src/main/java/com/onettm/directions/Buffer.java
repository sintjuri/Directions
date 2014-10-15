package com.onettm.directions;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sintyaev on 13.10.14.
 */
abstract public class Buffer<T> {
    protected Queue<T> data;
    private int size;
    private boolean rendered = false;

    public Buffer(int size){
        this.size = size;
        data = new LinkedBlockingQueue<T>(size);
    }

    public void add(T element){
        if(data.size() == size){
            get();
        }
        data.offer(element);
    }

    public T get(){
        return data.poll();
    }

    public boolean isRendered() {
        return rendered || data.size() == 0;
    }

    abstract public T getAverageValue();

}
