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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AJAXState} - Stores properties bound to a dispatcher cycle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJAXState {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJAXState.class));

    private final Map<String, Object> properties;

    private final Set<String> initializers;

    private final List<AJAXStateHandler> handlers;

    /**
     * Initializes a new {@link AJAXState}.
     */
    public AJAXState() {
        super();
        properties = new ConcurrentHashMap<String, Object>();
        initializers = new HashSet<String>();
        handlers = new LinkedList<AJAXStateHandler>();
    }

    /**
     * Check for presence of named property.
     *
     * @param name The property name
     * @return <code>true</code> if property is present; otherwise <code>false</code>
     */
    public boolean containsProperty(final String name) {
        return properties.containsKey(name);
    }

    /**
     * Gets (optionally) the named property.
     *
     * @param name The property name
     * @return The property or <code>null</code> if absent
     */
    @SuppressWarnings("unchecked")
    public <V> V optProperty(final String name) {
        return (V) properties.get(name);
    }

    /**
     * Gets the named property.
     *
     * @param name The property name
     * @return The property
     * @throws OXException If property is absent
     */
    public <V> V getProperty(final String name) throws OXException {
        @SuppressWarnings("unchecked")
        final V value = (V) properties.get(name);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return value;
    }

    /**
     * Puts specified property.
     *
     * @param name The property name
     * @param value The property value
     * @return The value previously associated with given name or <code>null</code> if no such property existed before
     */
    public Object putProperty(final String name, final Object value) {
        return properties.put(name, value);
    }

    /**
     * Removes the property associated with specified name.
     *
     * @param name The property name
     * @return The removed property value or <code>null</code> if absent
     */
    @SuppressWarnings("unchecked")
    public <V> V removeProperty(final String name) {
        return (V) properties.remove(name);
    }

    /**
     * Clears the properties.
     */
    public void clearProperties() {
        properties.clear();
    }

    /**
     * Gets the property names.
     *
     * @return The property names.
     */
    public Set<String> propertyNames() {
        return new HashSet<String>(properties.keySet());
    }

    /**
     * Adds specified initializer identifier.
     *
     * @param identifier The initializer identifier
     * @param handler The associated handler
     * @return <code>true</code> if no such initializer identifier was present before (successful insertion); otherwise <code>false</code>
     */
    public boolean addInitializer(final String identifier, final AJAXStateHandler handler) {
        handlers.add(handler);
        return initializers.add(identifier);
    }

    /**
     * Closes this state orderly.
     */
    public void close() {
        while (!handlers.isEmpty()) {
            final AJAXStateHandler handler = handlers.remove(0);
            try {
                handler.cleanUp(this);
            } catch (final OXException e) {
                LOG.error("Failed closeing handler: " + handler.getClass().getName(), e);
            }
        }
        initializers.clear();
        properties.clear();
    }

}
