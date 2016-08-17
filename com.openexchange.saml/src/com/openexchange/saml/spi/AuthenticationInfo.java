/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.saml.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the necessary information to authenticate a certain user. The IDs of the according
 * user and his context must be provided. Additionally some properties can be defined. Those are later
 * on passed into {@link SAMLBackend#enhanceAuthenticated(com.openexchange.authentication.Authenticated, Map)}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
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