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
