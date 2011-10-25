package com.openexchange.admin.soap.dataobjects;


public class SOAPMapEntry {

    private String key;
    
    private SOAPStringMap value;
    
    /**
     * Default constructor needed for Bean
     */
    public SOAPMapEntry() {
        
    }
    
    /**
     * @param key
     * @param value
     */
    public SOAPMapEntry(String key, SOAPStringMap value) {
        this.key = key;
        this.value = value;
    }

    public final String getKey() {
        return key;
    }

    public final SOAPStringMap getValue() {
        return value;
    }

    public final void setKey(String key) {
        this.key = key;
    }
    
    public final void setValue(SOAPStringMap value) {
        this.value = value;
    }

}
