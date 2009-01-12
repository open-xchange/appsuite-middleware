package com.openexchange.publish;


public class Publication {

    private int objectID;
    private int contextID;
    private int type;
    private Site site;
    
    public int getObjectID() {
        return objectID;
    }
    
    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }
    
    public int getContextID() {
        return contextID;
    }
    
    public void setContextID(int contextID) {
        this.contextID = contextID;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }

    
    public Site getSite() {
        return site;
    }

    
    public void setSite(Site site) {
        this.site = site;
    }
    
}
