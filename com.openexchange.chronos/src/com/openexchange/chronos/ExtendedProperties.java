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

}
