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

package com.openexchange.realtime.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.realtime.packet.ID;

/**
 * {@link IDMap} - Maps {@link ID}s to arbitrary values.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class IDMap<T> implements Map<ID, T> {

    /** The delegate {@code java.util.Map} instance */
    protected final Map<ID, T> delegate;

    /**
     * Initializes a new {@link IDMap}.
     */
    public IDMap() {
        this(true);
    }

    /**
     * Initializes a new {@link IDMap}.
     *
     * @param concurrent Whether this map should be created in a thread-safe manner or not
     */
    public IDMap(final boolean concurrent) {
        super();
        delegate = concurrent ? new NonBlockingHashMap<ID, T>() : new HashMap<ID, T>();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object id) {
        return delegate.containsKey(id);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public T get(Object key) {
        return delegate.get(key);
    }

    @Override
    public T put(ID key, T value) {
        return delegate.put(key, value);
    }

    @Override
    public T remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends ID, ? extends T> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<ID> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<T> values() {
        return delegate.values();
    }

    @Override
    public Set<java.util.Map.Entry<ID, T>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Get a set of equivalent IDs. Equivalent in that way that they represent
     * the same user@context entity. An entity may still be reachable via
     * another channel and/or resource although the original id isn't reachable
     * anylonger.
     * @param id the id of the entity we are looking for
     * @return a Set of key-value pairs mapping ID to arbitrary value types.
     */
    public Set<Map.Entry<ID, T>> getEquivalents(ID id) {
        // Maybe make this more efficient. Linear searches are very out
        Set<Map.Entry<ID, T>> equivalents = new HashSet<Map.Entry<ID, T>>();
        for (Map.Entry<ID, T> entry : delegate.entrySet()) {
            if (isEquivalent(entry.getKey(), id)) {
                equivalents.add(entry);
            }
        }
        return equivalents;
    }

    private boolean isEquivalent(ID id1, ID id2) {
        return id1.getUser().equals(id2.getUser()) && id1.getContext().equals(id2.getContext());
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
