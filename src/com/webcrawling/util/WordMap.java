package com.webcrawling.util;

import java.net.URL;
import java.util.*;

public class WordMap {
    private Map<String, Set<URL>> wordMap;

    public WordMap(){
        this.wordMap = new HashMap<>();
    }

    public synchronized void put(String word, URL url){
        if(!this.wordMap.containsKey(word)){
            this.wordMap.put(word, new HashSet<>());
        }
        this.wordMap.get(word).add(url);
    }

    public synchronized void put(URL url, List<String> words){
       for (String word : words){
           put(word, url);
       }
    }

    public Set<URL> get(String word){
        try{
            return this.wordMap.get(word);
        }
        catch (NoSuchElementException e){
            return null;
        }
    }
}
