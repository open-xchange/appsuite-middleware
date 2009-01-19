package com.openexchange.publish.microformats;

import java.util.HashMap;
import java.util.Map;


public class ItemLoaderRegistry {
    
    private Map<Integer, ItemLoader<?>> itemLoaders = new HashMap<Integer, ItemLoader<?>>();
    
    public void addItemLoader(ItemLoader<?> loader, int type) {
        itemLoaders.put(type, loader);
    }
    
    public <T> ItemLoader<T> getItemLoader(Class<T> clazz, int type) {
        return (ItemLoader<T>) itemLoaders.get(type);
    }
}
