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

package com.openexchange.groupware.container;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link SystemObject} - The system object.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class SystemObject implements Serializable {

    private static final long serialVersionUID = 5804496798486123299L;

    /**
     * The map with arbitrary properties.
     */
    protected transient Map<String, Object> map;
    protected boolean b_map;

    private Map<String, Serializable> serializableMap;

    /**
     * Initializes a new {@link SystemObject}.
     */
    protected SystemObject() {
        super();
        serializableMap = null;
    }

    /**
     * Sets specified property. Existing mapping will be replaced.
     *
     * @param name The property name
     * @param value The property value
     */
    public void setProperty(final String name, final Object value) {
        if (null == name || null == value) {
            return;
        }
        Map<String, Object> map = this.map;
        if (null == map) {
            map = new LinkedHashMap<String, Object>(12);
            this.map = map;
            b_map = true;
        }
        map.put(name, value);
    }

    /**
     * Gets the denoted property.
     *
     * @param name The property name
     * @return The property value or <code>null</code>
     */
    public <V> V getProperty(final String name) {
        if (null == name) {
            return null;
        }
        Map<String, Object> map = this.map;
        if (null == map) {
            return null;
        }
        try {
            return (V) map.get(name);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Removes the denoted property.
     *
     * @param name The property name
     * @return The removed property value or <code>null</code>
     */
    public <V> V removeProperty(final String name) {
        if (null == name) {
            return null;
        }
        Map<String, Object> map = this.map;
        if (null == map) {
            return null;
        }
        try {
            return (V) map.remove(name);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Sets the map with arbitrary properties.
     *
     * @param map The properties map
     */
    public void setMap(final Map<String, ? extends Object> map) {
        this.map = null == map ? null : new LinkedHashMap<String, Object>(map);
        b_map = true;
    }

    /**
     * Removes the map.
     */
    public void removeMap() {
        this.map = null;
        b_map = false;
    }

    /**
     * Checks if this object contains a map.
     *
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsMap() {
        return b_map;
    }

    /**
     * Gets (optionally) the map with arbitrary properties.
     *
     * @return The map or <code>null</code>
     */
    public Map<String, Object> getMap() {
        return null == map ? null : Collections.unmodifiableMap(map);
    }

    // -------------------------------------------------------------------------------------------------- //

    /**
     * The <tt>writeObject</tt> method is responsible for writing the state of the object for its particular class so that the corresponding
     * <tt>readObject</tt> method can restore it. The default mechanism for saving the <tt>Object</tt>'s fields can be invoked by calling
     * <tt>out.defaultWriteObject</tt>. The method does not need to concern itself with the state belonging to its superclasses or
     * subclasses. State is saved by writing the individual fields to the <tt>ObjectOutputStream</tt> using the <tt>writeObject</tt> method
     * or by using the methods for primitive data types supported by <tt>DataOutput</tt>.
     *
     * @param out The object output stream
     * @throws java.io.IOException If serialization fails
     */
    private void writeObject(final java.io.ObjectOutputStream out) throws java.io.IOException {
        final Map<String, Object> map = this.map;
        if (null == map) {
            this.serializableMap = null;
        } else {
            final Map<String, Serializable> serializableMap = new LinkedHashMap<String, Serializable>(map.size());
            for (final Map.Entry<String, Object> entry : map.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if ((value instanceof Serializable)) {
                    serializableMap.put(key, (Serializable) value);
                }
            }
            this.serializableMap = serializableMap;
        }
        out.defaultWriteObject();
    }

    /**
     * The <tt>readObject</tt> method is responsible for reading from the stream and restoring the classes fields. It may call
     * <tt>in.defaultReadObject</tt> to invoke the default mechanism for restoring the object's non-static and non-transient fields. The
     * <tt>defaultReadObject</tt> method uses information in the stream to assign the fields of the object saved in the stream with the
     * correspondingly named fields in the current object. This handles the case when the class has evolved to add new fields. The method
     * does not need to concern itself with the state belonging to its superclasses or subclasses. State is saved by writing the individual
     * fields to the <tt>ObjectOutputStream</tt> using the <tt>writeObject</tt> method or by using the methods for primitive data types
     * supported by <tt>DataOutput</tt>.
     *
     * @param in The object input stream
     * @throws java.io.IOException If deserialization fails
     * @throws ClassNotFoundException If appropriate class could not be found
     */
    private void readObject(final java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.readObject();
        final Map<String, Serializable> serializableMap = this.serializableMap;
        if (null == serializableMap) {
            this.map = null;
        } else {
            this.map = new LinkedHashMap<String, Object>(serializableMap);
        }
        this.serializableMap = null;
    }

}
