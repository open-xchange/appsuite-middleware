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

package com.openexchange.chronos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * {@link ExtendedProperties}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ExtendedProperties extends ArrayList<ExtendedProperty> {

    private static final long serialVersionUID = 9200866341821558376L;

    /**
     * Initializes a new {@link ExtendedProperties}.
     */
    public ExtendedProperties() {
        super();
    }

    /**
     * Initializes a new {@link ExtendedProperties} initially containing the properties of the specified collection, in the order they
     * are returned by the collection's iterator.
     *
     * @param c The collection of extended properties
     */
    public ExtendedProperties(Collection<? extends ExtendedProperty> c) {
        super(c);
    }

    /**
     * Gets a value indicating whether a specific property is contained or not.
     *
     * @param name The name of the property to check
     * @return <code>true</code> if at least one such property is contained, <code>false</code>, otherwise
     */
    public boolean contains(String name) {
        return null != get(name);
    }

    /**
     * Gets the (first) extended property matching the supplied name.
     *
     * @param name The name of the property to get
     * @return The property, or <code>null</code> if not found
     */
    public ExtendedProperty get(String name) {
        for (ExtendedProperty property : this) {
            if (name.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    /**
     * Gets all extended properties matching the supplied name.
     *
     * @param name The name of the properties to get
     * @return The properties, or an empty list if there are none
     */
    public List<ExtendedProperty> getAll(String name) {
        List<ExtendedProperty> properties = new ArrayList<ExtendedProperty>();
        for (ExtendedProperty property : this) {
            if (name.equals(property.getName())) {
                properties.add(property);
            }
        }
        return properties;
    }

    /**
     * Removes all extended properties matching the supplied name.
     *
     * @param name The name of the properties to remove
     * @return <code>true</code> if this list changed as a result of the call
     */
    public boolean removeAll(String name) {
        boolean removed = false;
        for (Iterator<ExtendedProperty> iterator = iterator(); iterator.hasNext();) {
            if (name.equals(iterator.next().getName())) {
                iterator.remove();
                removed |= true;
            }
        }
        return removed;
    }

    /**
     * Adds an extended property to this collection after all previous extended properties with the same property name have been removed,
     * thus effectively replacing any previously set properties with the same name.
     *
     * @param e The replacing property
     * @return <code>true</code> if this list changed as a result of the call
     */
    public boolean replace(ExtendedProperty e) {
        removeAll(e.getName());
        return add(e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        return new ExtendedProperties((ArrayList<ExtendedProperty>)super.clone());
    }

}
