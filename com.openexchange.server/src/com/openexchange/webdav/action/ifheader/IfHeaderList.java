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

package com.openexchange.webdav.action.ifheader;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class IfHeaderList implements List<IfHeaderEntity>{
	private final List<IfHeaderEntity> delegate;
	private String tag;

	public IfHeaderList(final List<IfHeaderEntity> delegate) {
		this.delegate = delegate;
	}

	public IfHeaderList(final String tag, final List<IfHeaderEntity> delegate) {
		this.delegate = delegate;
		this.tag = tag;
	}

	public boolean isTagged() {
		return tag != null;
	}

	public String getTag() {
		return tag;
	}

	// Delegate Methods

	@Override
    public boolean add(final IfHeaderEntity arg0) {
		return delegate.add(arg0);
	}

	@Override
    public void add(final int arg0, final IfHeaderEntity arg1) {
		delegate.add(arg0, arg1);
	}

	@Override
    public boolean addAll(final Collection<? extends IfHeaderEntity> arg0) {
		return delegate.addAll(arg0);
	}

	@Override
    public boolean addAll(final int arg0, final Collection<? extends IfHeaderEntity> arg1) {
		return delegate.addAll(arg0, arg1);
	}

	@Override
    public void clear() {
		delegate.clear();
	}

	@Override
    public boolean contains(final Object arg0) {
		return delegate.contains(arg0);
	}

	@Override
    public boolean containsAll(final Collection<?> arg0) {
		return delegate.containsAll(arg0);
	}

	@Override
	public boolean equals(final Object arg0) {
		return delegate.equals(arg0);
	}

	@Override
    public IfHeaderEntity get(final int arg0) {
		return delegate.get(arg0);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
    public int indexOf(final Object arg0) {
		return delegate.indexOf(arg0);
	}

	@Override
    public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
    public Iterator<IfHeaderEntity> iterator() {
		return delegate.iterator();
	}

	@Override
    public int lastIndexOf(final Object arg0) {
		return delegate.lastIndexOf(arg0);
	}

	@Override
    public ListIterator<IfHeaderEntity> listIterator() {
		return delegate.listIterator();
	}

	@Override
    public ListIterator<IfHeaderEntity> listIterator(final int arg0) {
		return delegate.listIterator(arg0);
	}

	@Override
    public IfHeaderEntity remove(final int arg0) {
		return delegate.remove(arg0);
	}

	@Override
    public boolean remove(final Object arg0) {
		return delegate.remove(arg0);
	}

	@Override
    public boolean removeAll(final Collection<?> arg0) {
		return delegate.removeAll(arg0);
	}

	@Override
    public boolean retainAll(final Collection<?> arg0) {
		return delegate.retainAll(arg0);
	}

	@Override
    public IfHeaderEntity set(final int arg0, final IfHeaderEntity arg1) {
		return delegate.set(arg0, arg1);
	}

	@Override
    public int size() {
		return delegate.size();
	}

	@Override
    public List<IfHeaderEntity> subList(final int arg0, final int arg1) {
		return delegate.subList(arg0, arg1);
	}

	@Override
    public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
    public <T> T[] toArray(final T[] arg0) {
		return delegate.toArray(arg0);
	}
}
