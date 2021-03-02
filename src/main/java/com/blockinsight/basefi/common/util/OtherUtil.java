package com.blockinsight.basefi.common.util;

public class OtherUtil {
    public static int lastIndex(int[] levels){
        int index = 0;
        for(int i=0;i<levels.length;i++){
            if(1 == levels[i]){
                index = i;
            }
        }
        return index;
    }

    public static void main(String[] args) {
        int[] levels = new int[]{0,1,0,1,0,0};
        int index = lastIndex(levels);
        System.out.println(index);
    }



}
