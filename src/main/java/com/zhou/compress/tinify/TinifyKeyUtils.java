package com.zhou.compress.tinify;

import com.zhou.compress.file.PropertiesUtil;

import java.util.ArrayList;
import java.util.List;

public class TinifyKeyUtils {
    private static final String KEY_Tinify = "TinifyKey";
    private   List<String> tinifyKeyList = null;
    private int position = 0 ;

    private static TinifyKeyUtils instance;
    private TinifyKeyUtils(){}

    public static TinifyKeyUtils get(){
        if (instance == null){
            instance = new TinifyKeyUtils();
        }
        return instance;
    }

    public void initTinifyKey(){
        tinifyKeyList = new ArrayList<>();
        tinifyKeyList.add("XfXgJcXTZb1LDrsXWhslWSMDJynYfvFh");
        tinifyKeyList.add("r9Sk370XxNFJxZ0lHlKlYCf9Br2KJxTw");
        tinifyKeyList.add("cVQ9b2kH5whWhL6TXcbydqlgvG1pjJvj");
    }

    public List<String> getTinifyKeyList() {
        if (tinifyKeyList == null){
            initTinifyKey();
        }
        return tinifyKeyList;
    }

    public String getKey() {
        if (tinifyKeyList != null){
            if (position >= tinifyKeyList.size()){
                throw new IllegalStateException("TinyPng has no enough key ");
            }
            return tinifyKeyList.get(position);
        }
        return null;
    }

    public String getNextKey(){
        position++;
        return getKey();
    }
}
