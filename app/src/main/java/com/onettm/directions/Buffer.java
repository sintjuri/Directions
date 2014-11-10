package com.onettm.directions;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


abstract public class Buffer<T extends Object> {
    protected Queue<T> data;
    private int size;

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

    private T get(){
        return data.poll();
    }

    abstract T getAveragedValue();

}
