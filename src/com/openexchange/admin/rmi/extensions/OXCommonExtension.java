package com.openexchange.admin.rmi.extensions;

/**
 * Extend all extensions from this class
 * 
 * @author d7
 *
 */
public abstract class OXCommonExtension implements OXCommonExtensionInterface {
    
    private String errortext;
    
    /**
     * This method is used to get the errors which appear while processing an extension. This is especially used
     * for getData methods
     */
    public String getExtensionError() {
        return this.errortext;
    }

    /**
     * This method is used to set the errors which appear while processing an extension. This is especially used
     * for getData methods
     */
    public void setExtensionError(final String errortext) {
        this.errortext = errortext;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(this.errortext);
        return sb.toString();
    }

    public boolean equals(final Object obj) {
        return false;
    }
}
