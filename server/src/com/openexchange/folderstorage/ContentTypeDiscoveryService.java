
package com.openexchange.folderstorage;


public interface ContentTypeDiscoveryService {

    /**
     * Gets the content type for specified string.
     * 
     * @param contentTypeString The content type string
     * @return The content type for specified string or <code>null</code> if none matches
     */
    public ContentType getByString(final String contentTypeString);

}
