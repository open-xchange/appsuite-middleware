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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.quota;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link ResourceDescription} - A resource description.
 * <p>
 * A simple property container capable to carry arbitrary objects (<a href="http://en.wikipedia.org/wiki/Plain_Old_Java_Object">POJO</a>s
 * preferred).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ResourceDescription {

    /** The empty resource description */
    private static final ResourceDescription EMPTY_RESOURCE_DESCRIPTION = new ResourceDescription() {

        @Override
        public boolean containsProperty(final String propertyName) {
            return false;
        }

        @Override
        public ResourceDescription put(final String propertyName, final Object propertyValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResourceDescription remove(final String propertyName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResourceDescription putProperties(final Map<? extends String, ? extends Object> properties) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getProperty(final String propertyName) {
            return null;
        }

        @Override
        public java.util.Set<String> getPropertyNames() {
            return Collections.emptySet();
        }

    };

    /**
     * Gets the empty resource description.
     *
     * @return The empty resource description
     */
    public static ResourceDescription getEmptyResourceDescription() {
        return EMPTY_RESOURCE_DESCRIPTION;
    }

    /** The properties map */
    protected final Map<String, Object> properties;

    /**
     * Initializes a new {@link ResourceDescription}.
     */
    public ResourceDescription() {
        super();
        properties = new HashMap<String, Object>(8);
    }

    /**
     * Initializes a new {@link ResourceDescription}.
     *
     * @param properties The properties
     */
    protected ResourceDescription(final Map<String, Object> properties) {
        super();
        this.properties = properties;
    }

    /**
     * Gets the available property names.
     *
     * @return The names
     */
    public Set<String> getPropertyNames() {
        return new LinkedHashSet<String>(properties.keySet());
    }

    /**
     * Checks for existence of specified property.
     *
     * @param propertyName The property name
     * @return <code>true</code> if such a property exists; otherwise <code>false</code>
     */
    public boolean containsProperty(final String propertyName) {
        return properties.containsKey(propertyName);
    }

    /**
     * Puts specified property.
     *
     * @param propertyName The property name
     * @param propertyValue The property value
     * @return This description with property put
     */
    public ResourceDescription put(final String propertyName, final Object propertyValue) {
        if (null == propertyName || null == propertyValue) {
            return this;
        }
        properties.put(propertyName, propertyValue);
        return this;
    }

    /**
     * Removes specified property.
     *
     * @param propertyName The property name
     * @return This description with property removed
     */
    public ResourceDescription remove(final String propertyName) {
        properties.remove(propertyName);
        return this;
    }

    /**
     * Puts specified properties.
     *
     * @param properties The properties to put
     * @return This description with properties put
     */
    public ResourceDescription putProperties(final Map<? extends String, ? extends Object> properties) {
        if (null != properties) {
            this.properties.putAll(properties);
        }
        return this;
    }

    /**
     * Gets the named property.
     *
     * @param propertyName The property name
     * @return The property value or <code>null</code>
     */
    public Object getProperty(final String propertyName) {
        return properties.get(propertyName);
    }

}
