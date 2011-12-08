package com.openexchange.admin.soap.dataobjects;

public class Entry {

    private String key;
    
    private String value;
    
    /**
     * Default constructor needed for Bean
     */
    public Entry() {
        
    }
    
    /**
     * @param key
     * @param value
     */
    public Entry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public final String getKey() {
        return key;
    }

    public final String getValue() {
        return value;
    }

    public final void setKey(String key) {
        this.key = key;
    }
    
    public final void setValue(String value) {
        this.value = value;
    }

}
