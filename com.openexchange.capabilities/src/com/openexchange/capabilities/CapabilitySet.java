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

package com.openexchange.capabilities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link CapabilitySet} - A capability set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CapabilitySet implements Iterable<Capability>, Serializable, Cloneable {

    private static final long serialVersionUID = -7226304751781497934L;

    /** The capability map */
    private final transient Map<String, Capability> capabilities;

    /**
     * Initializes a new {@link CapabilitySet}.
     *
     * @param capacity The initial capacity
     */
    public CapabilitySet(final int capacity) {
        super();
        capabilities = new ConcurrentHashMap<String, Capability>(capacity);
    }

    private CapabilitySet(CapabilitySet source) {
        super();
        Map<String, Capability> m = source.capabilities;
        capabilities = null == m ? null : new ConcurrentHashMap<String, Capability>(m);
    }

    @Override
    public CapabilitySet clone() {
        return new CapabilitySet(this);
    }

    /**
     * Gets the size
     *
     * @return The size
     */
    public int size() {
        return capabilities.size();
    }

    /**
     * Checks if set is empty
     *
     * @return <code>true</code> if empty; else <code>false</code>
     */
    public boolean isEmpty() {
        return capabilities.isEmpty();
    }

    /**
     * Checks for presence of given capability.
     *
     * @param capability The capability to look for
     * @return <code>true</code> if contained; else <code>false</code>
     */
    public boolean contains(final Capability capability) {
        return null == capability ? false : capabilities.containsKey(capability.getId());
    }

    /**
     * Checks for presence of denoted capability.
     *
     * @param id The capability identifier to look for
     * @return <code>true</code> if contained; else <code>false</code>
     */
    public boolean contains(final String id) {
        return null == id ? false : capabilities.containsKey(id);
    }

    /**
     * Gets the capability identifies by the supplied ID.
     *
     * @param id The capability identifier to look for
     * @return The capability, or <code>null</code> if not found
     */
    public Capability get(final String id) {
        return null == id ? null : capabilities.get(id);
    }

    /**
     * Gets an iterator for capabilities.
     *
     * @return An iterator for capabilities
     */
    @Override
    public Iterator<Capability> iterator() {
        return capabilities.values().iterator();
    }

    /**
     * Adds given capability.
     *
     * @param capability The capability to add
     * @return <code>true</code> if set changed; otherwise <code>false</code> if already contained
     */
    public boolean add(final Capability capability) {
        if (null == capability) {
            return false;
        }
        return null == capabilities.put(capability.getId(), capability);
    }

    /**
     * Removes the given capability.
     *
     * @param capability The capability
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such capability was contained
     */
    public boolean remove(final Capability capability) {
        if (null == capability) {
            return false;
        }
        return null != capabilities.remove(capability.getId());
    }

    /**
     * Removes the denoted capability.
     *
     * @param id The capability identifier
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such capability was contained
     */
    public boolean remove(final String id) {
        if (null == id) {
            return false;
        }
        return null != capabilities.remove(id);
    }

    /**
     * Clears this set.
     */
    public void clear() {
        capabilities.clear();
    }

    /**
     * Creates the {@link Set set} view for this capability set.
     * <p>
     * Changes to returned set are <b>not</b> reflected in this capability set.
     *
     * @return The {@link Set set} view for this capability set
     */
    public Set<Capability> asSet() {
        return new HashSet<Capability>(capabilities.values());
    }

    @Override
    public String toString() {
        return new TreeSet<Capability>(capabilities.values()).toString();
    }

}
