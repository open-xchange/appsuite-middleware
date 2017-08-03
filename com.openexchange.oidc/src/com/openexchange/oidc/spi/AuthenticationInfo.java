package com.openexchange.oidc.spi;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationInfo {

    private final Map<String, String> properties = new HashMap<String, String>();

    private final int contextId;

    private final int userId;

    /**
     * Initializes a new {@link AuthenticationInfo}.
     *
     * @param contextId The context ID
     * @param userId The user ID
     */
    public AuthenticationInfo(int contextId, int userId) {
        super();
        this.contextId = contextId;
        this.userId = userId;
    }

    /**
     * Gets the context ID
     *
     * @return The context ID
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user ID
     *
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the properties as mutable map.
     *
     * @return The properties, possibly empty but not <code>null</code>
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets a property. Please note that internally some attributes are contributed to this
     * map. They will always be prefixed with <code>com.openexchange.saml</code>. You should
     * either use your own namespace for those properties or use un-qualified keys. A property
     * will be overridden if it is set more than once.
     *
     * @param key The key
     * @param value The value
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AuthenticationInfo other = (AuthenticationInfo) obj;
        if (contextId != other.contextId)
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AuthenticationInfo [contextId=" + contextId + ", userId=" + userId + ", properties=" + properties + "]";
    }

}
