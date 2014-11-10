package com.onettm.directions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

public class SensorBuffer extends Buffer<Object>{
    public SensorBuffer(int size) {
        super(size);
    }

    @Override
    public float[] getAveragedValue() {
        float[] result = new float[3];
        if (!data.isEmpty()) {
            List<Object> copy = deepCopy(data);
            Collections.sort(copy, new Comparator<Object>(){
                @Override
                public int compare(Object lhs, Object rhs) {
                    int result = 0;
                    if(!Arrays.equals((float[])lhs, (float[])rhs)){
                        if((evklidLength((float[])lhs)-evklidLength((float[])rhs))<0){
                            result = 1;
                        }else{
                            result = -1;
                        }
                    }
                    return result;
                }
            });
            int mid = copy.size()/2;
            if(mid<copy.size()){
                result = (float[])copy.get(mid);
            }

            /*int count = 0;
            for (Object d : data) {
                for(int i = 0; i< result.length; i++){
                    result[i]+=((float[])d)[i];
                }
                count++;
            }
            for(int i = 0; i< result.length; i++){
                result[i]=result[i]/count;
            }
            return result;*/
        }
        return result;
    }

    private double evklidLength(float[] var){
        float result = 0;
        for(float elem : var){
            result+= elem*elem;
        }
        return Math.sqrt(result);
    }

    private List<Object> deepCopy(Queue<Object> var){
        return new ArrayList<Object>(var);
    }
}
