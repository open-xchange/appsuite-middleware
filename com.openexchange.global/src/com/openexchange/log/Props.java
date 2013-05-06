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

package com.openexchange.log;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogProperties.Name;

/**
 * {@link Props} - The log properties associated with a certain {@link Thread thread}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Props {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(Props.class);
    private final EnumMap<LogProperties.Name, Object> map;
    private final Set<LogProperties.Name> keys;

    /**
     * Initializes a new {@link Props}.
     */
    protected Props() {
        super();
        map = new EnumMap<LogProperties.Name, Object>(LogProperties.Name.class);
        keys = map.keySet();
    }

    /**
     * Initializes a new {@link Props}.
     *
     * @param other The source properties
     */
    protected Props(final Props other) {
        super();
        map = new EnumMap<LogProperties.Name, Object>(other.map);
        keys = map.keySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * Gets the backing map
     *
     * @return The backing map
     */
    public Map<LogProperties.Name, Object> getMap() {
        return map;
    }

    /**
     * Gets the {@code Map} view on this {@code Props}
     *
     * @return The map
     */
    public Map<String, Object> asMap() {
        return asMap(false);
    }

    /**
     * Gets the {@code Map} view on this {@code Props}
     *
     * @param sorted Whether returned map shall be sorted.
     * @return The map
     */
    public Map<String, Object> asMap(final boolean sorted) {
        final Map<String, Object> m = sorted ? new TreeMap<String, Object>() : new HashMap<String, Object>(map.size());
        for (final Entry<Name, Object> entry : map.entrySet()) {
            m.put(entry.getKey().getName(), entry.getValue());
        }
        return m;
    }

    /**
     * Checks for presence of associated property.
     *
     * @param name The property name
     * @return <code>true</code> if present; otherwise <code>false</code>
     */
    public boolean contains(final LogProperties.Name name) {
        return map.containsKey(name);
    }

    /**
     * Gets the property associated with given name.
     *
     * @param sName The property name
     * @return The property value or <code>null</code> if absent
     */
    @SuppressWarnings("unchecked")
    public <V> V get(final String sName) {
        final LogProperties.Name name = LogProperties.Name.nameFor(sName);
        if (null == name) {
            return null;
        }
        try {
            return (V) get(name);
        } catch (final ClassCastException e) {
            LOG.warn("Type mismatch", e);
            return null;
        }
    }

    /**
     * Gets the property associated with given name.
     *
     * @param name The property name
     * @return The property value or <code>null</code> if absent
     */
    @SuppressWarnings("unchecked")
    public <V> V get(final LogProperties.Name name) {
        try {
            return (V) map.get(name);
        } catch (final ClassCastException e) {
            LOG.warn("Type mismatch", e);
            return null;
        }
    }

    /**
     * Puts specified mapping. Any existing mapping is overwritten.
     *
     * @param name The property name
     * @param value The property value
     * @return <code>true</code> if there was already a mapping for specified property name (that is now overwritten); otherwise <code>false</code>
     */
    public <V> boolean put(final Name name, final V value) {
        if (null == name) {
            return false;
        }
        if (null == value) {
            return (null != map.remove(name));
        }
        return (null != map.put(name, value));
    }

    /**
     * Puts specified mappings. Any existing mappings are replaced.
     *
     * @param props The properties to put
     */
    public <V> void putAll(final Props props) {
        if (null == props) {
            return;
        }
        this.map.putAll(props.map);
    }

    /**
     * Removes the property associated with given name.
     *
     * @param name The property name
     */
    public void remove(final Name name) {
        if (null != name) {
            map.remove(name.getName());
        }
    }

    /**
     * Removes the properties associated with given names.
     *
     * @param names The property names
     */
    public void remove(final Collection<Name> names) {
        if (null != names) {
            keys.removeAll(names);
        }
    }

	/**
	 * Creates a shallow copy of this log properties.
	 *
	 * @return The shallow copy
	 */
	public Props copy() {
		return new Props(this);
	}

}
