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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.capabilities;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Capability} - Represents a capability.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Capability implements Serializable {

    private static final long serialVersionUID = 8389975218424678442L;

    private final String id;
    private final Map<String, String> attributes;

    /**
     * Initializes a new {@link Capability}.
     *
     * @param id The identifier of the capability
     */
    public Capability(String id) {
        super();
        this.id = id;
        attributes = new ConcurrentHashMap<String, String>();
    }

    /**
     * Gets this capability's identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets this capability's attributes
     *
     * @return The attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Sets specified attribute for this capability.
     *
     * @param key The attribute's key
     * @param value The attribute's value
     * @return This capability with attribute applied
     */
    public Capability set(String key, String value) {
        attributes.put(key, value);
        return this;
    }

    /**
     * Gets the value of the attribute associated with given key.
     *
     * @param key The attribute's key
     * @return The value or <code>null</code> if there is no such attribute
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Sets all attributes from given capability for this capability.
     *
     * @param otherCapability The other capability providing the attributes
     */
    public void learnFrom(Capability otherCapability) {
        if (null != otherCapability) {
            attributes.putAll(otherCapability.attributes);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Capability)) {
            return false;
        }
        Capability other = (Capability) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id;
    }

}
