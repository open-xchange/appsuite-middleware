package com.openexchange.publish.microformats;

import java.util.HashMap;
import java.util.Map;


public class ItemWriterRegistry {
    
    private Map<Integer, ItemWriter<?>> writers = new HashMap<Integer, ItemWriter<?>>();
    
    public void addWriter(ItemWriter<?> writer, int type) {
        writers.put(type, writer);
    }
    
    public <T> ItemWriter<T> getWriter(Class<T> clazz, int type) {
        return (ItemWriter<T>) writers.get(type);
    }
}
