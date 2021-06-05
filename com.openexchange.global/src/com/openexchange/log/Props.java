/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.log;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.MDC;
import com.openexchange.log.LogProperties.Name;

/**
 * {@link Props} - The log properties associated with a certain {@link Thread thread}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated Please use slf4j MDC to manage log properties
 */
@Deprecated
public final class Props {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Props.class);

    /**
     * Initializes a new {@link Props}.
     */
    protected Props() {
        super();
    }

    @Override
    public String toString() {
        return MDC.getCopyOfContextMap().toString();
    }

    /**
     * Gets the backing map
     *
     * @return The backing map
     */
    public Map<LogProperties.Name, Object> getMap() {
        final Map<String, String> map = MDC.getCopyOfContextMap();

        final Map<LogProperties.Name, Object> retval = new EnumMap<LogProperties.Name, Object>(LogProperties.Name.class);
        for (Entry<String, String> entry : map.entrySet()) {
            final LogProperties.Name name = LogProperties.Name.nameFor(entry.getKey());
            if (null != name) {
                retval.put(name, entry.getValue());
            }
        }

        return retval;
    }

    /**
     * Gets the {@code Map} view on this {@code Props}
     *
     * @return The map
     */
    public Map<String, String> asMap() {
        return asMap(false);
    }

    /**
     * Gets the {@code Map} view on this {@code Props}
     *
     * @param sorted Whether returned map shall be sorted.
     * @return The map
     */
    public Map<String, String> asMap(final boolean sorted) {
        final Map<String, String> map = MDC.getCopyOfContextMap();
        final Map<String, String> m = sorted ? new TreeMap<String, String>() : new HashMap<String, String>(map.size());
        for (final Entry<String, String> entry : map.entrySet()) {
            m.put(entry.getKey(), entry.getValue());
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
        return null == name ? false : null != MDC.get(name.getName());
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
            return (V) MDC.get(name.toString());
        } catch (ClassCastException e) {
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
        if (null == name) {
            return null;
        }
        try {
            return (V) MDC.get(name.toString());
        } catch (ClassCastException e) {
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
        final String prev = MDC.get(name.getName());
        if (null == value) {
            MDC.remove(name.getName());
            return (null != prev);
        }

        MDC.put(name.getName(), value.toString());
        return (null != prev);
    }

    /**
     * Throws an UnsupportedOperationException!!!
     */
    @SuppressWarnings("unused")
    public <V> void putAll(final Props props) {
        throw new UnsupportedOperationException("Props.putAll()");
    }

    /**
     * Removes the property associated with given name.
     *
     * @param name The property name
     */
    public void remove(final Name name) {
        if (null != name) {
            MDC.remove(name.getName());
        }
    }

    /**
     * Removes the properties associated with given names.
     *
     * @param names The property names
     */
    public void remove(final Collection<Name> names) {
        if (null != names) {
            for (final LogProperties.Name name : names) {
                MDC.remove(name.getName());
            }
        }
    }

	/**
	 * Throws an UnsupportedOperationException!!!
	 */
	public Props copy() {
	    throw new UnsupportedOperationException("Props.copy()");
	}

}
