package com.openexchange.admin.rmi.extensions;

import java.io.Serializable;

public interface OXCommonExtensionInterface extends Serializable {
    /**
     * @return the extensionName
     */
    public String getExtensionName();

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
