package com.openexchange.admin.rmi.extensions;

import java.io.Serializable;

/**
 * 
 * 
 * @author d7
 * @deprecated This interface will be removed with next version use the abstract class OXCommonExtensionInterface instead
 */
public interface OXCommonExtensionInterface extends Serializable {
    /**
     * If an error has occured you get the error text of the extension
     * here
     * @return a string containing the error text
     */
    public String getExtensionError();

    /**
     * If an error has occured you set the error text of the extension
     * here
     */
    public void setExtensionError(final String errortext);

    /**
     * Used to return a string representation of the underlying object
     * @return
     */
    public String toString();

    public boolean equals(final Object obj);

}
