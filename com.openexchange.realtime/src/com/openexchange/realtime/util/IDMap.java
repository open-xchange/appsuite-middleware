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

package com.openexchange.realtime.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.openexchange.realtime.packet.ID;

/**
 * {@link IDMap}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IDMap<T> implements Map<ID, T>{
	
	private Map<ID, T> delegate = new HashMap<ID, T>();

	public int size() {
		return delegate.size();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	public T get(Object key) {
		return delegate.get(key);
	}

	public T put(ID key, T value) {
		return delegate.put(key, value);
	}

	public T remove(Object key) {
		return delegate.remove(key);
	}

	public void putAll(Map<? extends ID, ? extends T> m) {
		delegate.putAll(m);
	}

	public void clear() {
		delegate.clear();
	}

	public Set<ID> keySet() {
		return delegate.keySet();
	}

	public Collection<T> values() {
		return delegate.values();
	}

	public Set<java.util.Map.Entry<ID, T>> entrySet() {
		return delegate.entrySet();
	}

	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	public int hashCode() {
		return delegate.hashCode();
	}
	
	public Set<Map.Entry<ID, T>> getEquivalents(ID id) {
		// Maybe make this more efficient. Linear searches are very out
		Set<Map.Entry<ID, T>> equivalents = new HashSet<Map.Entry<ID, T>>();
		for(Map.Entry<ID, T> entry: delegate.entrySet()) {
			if (isEquivalent(entry.getKey(), id)) {
				equivalents.add(entry);
			}
		}
		return equivalents;
	}

	private boolean isEquivalent(ID id1, ID id2) {
		return id1.getUser().equals(id2.getUser()) && id1.getContext().equals(id2.getContext());
	}
	
}
