package com.webcrawling.util;

import java.util.Map;
import java.util.TreeMap;

public class Log {

	Map<String,String> log;
	
	public Log() {
		log=new TreeMap<>();
	}
	
	public synchronized void put(String key, String value) {
		log.put(key, value);
	}
	
	public synchronized String get(String key) {
		return log.get(key);
	}
	
	public synchronized Map<String,String> getAll() {
		return log;
	}
}
