package com.openexchange.publish;




public class Path {
    private int ownerId;
    private int contextId;
    private String siteName;
    
    public Path() {
        
    }
    
    public Path(int ownerId, int contextId, String siteName) {
        super();
        this.ownerId = ownerId;
        this.contextId = contextId;
        this.siteName = siteName;
    }

    public Path(String path) {
        //TODO
    }
    
    public String toString() {
        // TODO
        throw new UnsupportedOperationException(); // Nullllll Bock
    }

    public int getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    
    public int getContextId() {
        return contextId;
    }
    
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }
    
    public String getSiteName() {
        return siteName;
    }
    
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
    
    
}
