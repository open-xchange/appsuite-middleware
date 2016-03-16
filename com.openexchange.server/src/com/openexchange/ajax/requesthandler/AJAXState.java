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

package com.openexchange.ajax.requesthandler;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AJAXState} - Stores properties bound to a dispatcher cycle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJAXState {

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<String, Object> properties;
    private final ConcurrentMap<String, Object> initializers;
    private final Queue<AJAXStateHandler> handlers;

    /**
     * Initializes a new {@link AJAXState}.
     */
    public AJAXState() {
        super();
        properties = new ConcurrentHashMap<String, Object>();
        initializers = new ConcurrentHashMap<String, Object>();
        handlers = new ConcurrentLinkedQueue<AJAXStateHandler>();
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
     * Puts specified property (if absent).
     *
     * @param name The property name
     * @param value The property value
     * @return The previous value associated with the specified name, otherwise <code>null</code>
     */
    public <V> V putProperty(final String name, final V value) {
        return (V) properties.putIfAbsent(name, value);
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
        return (null == initializers.putIfAbsent(identifier, PRESENT));
    }

    /**
     * Closes this state orderly.
     */
    public void close() {
        for (AJAXStateHandler handler; (handler = handlers.poll()) != null;) {
            try {
                handler.cleanUp(this);
            } catch (final Exception e) {
                LoggerFactory.getLogger(AJAXState.class).error("Failed closing handler: {}", handler.getClass().getName(), e);
            }
        }
        initializers.clear();
        properties.clear();
    }

}
