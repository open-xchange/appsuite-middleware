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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.tools.iterator;

import java.lang.reflect.Array;
import java.util.Iterator;

import com.openexchange.api2.OXException;

/**
 * SearchIteratorAdapter
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchIteratorAdapter implements SearchIterator {

    private Iterator delegate;
	
	private int size;
	
	private boolean b_size;

	public SearchIteratorAdapter(Iterator iter) {
		delegate = iter;
	}
	
	public SearchIteratorAdapter(Iterator iter, int size) {
		delegate = iter;
		this.size = size;
		b_size = true;
	}

	public boolean hasNext() {
		return delegate.hasNext();
	}

	public Object next() throws SearchIteratorException {
		return delegate.next();
	}

	public void close() {
	}
	
	public int size() throws UnsupportedOperationException {
		if (!b_size) {
			throw new UnsupportedOperationException("Size has not been set for this iterator");
		}
		return size;
	}
	
	public boolean hasSize() {
		return b_size;
	}
	
	public static SearchIterator createEmptyIterator() {
		return new SearchIterator() {

			public boolean hasNext() {
				return false;
			}

			public Object next() throws SearchIteratorException, OXException {
				return null;
			}

			public void close() throws SearchIteratorException {
			}
			
			public int size() {
				return 0;
			}
			
			public boolean hasSize() {
				return true;
			}

		};
	}
	
	public static SearchIterator createArrayIterator(final Object array) {
		/*
		 * Tiny iterator implementation for arrays
		 */
		class ArrayIterator implements SearchIterator {

			private final int size;

			private int cursor;

			private final Object array;

			ArrayIterator(Object array) {
				final Class type = array.getClass();
				if (!type.isArray()) {
					throw new IllegalArgumentException(
							new StringBuilder("Can not create an array iterator from type: ").append(type).toString());
				}
				this.array = array;
				this.size = Array.getLength(array);
			}

			@SuppressWarnings("unused")
			public void remove() {
				throw new UnsupportedOperationException();
			}

			public boolean hasNext() {
				return (cursor < size);
			}

			public Object next() {
				return Array.get(array, cursor++);
			}

			public void close() throws SearchIteratorException {
			}
			
			public int size() {
				return Array.getLength(array);
			}
			
			public boolean hasSize() {
				return true;
			}

		}
		return new ArrayIterator(array);
	}

    public static <T> Iterable<T> toIterable(final SearchIterator<T> iterator) {
        class SIIterator implements Iterator<T> {

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public T next() {
                try {
                    return iterator.next();
                } catch (SearchIteratorException e) {
                   //IGNORE
                } catch (OXException e) {
                   //IGNORE
                }
                return null;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        return new Iterable<T>() {

            public Iterator<T> iterator() {
                return new SIIterator();
            }
        };
    }
}
