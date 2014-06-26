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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link SearchIteratorDelegator} - An implementation of {@link SearchIterator} backed by a common instance of {@link Iterator} to which
 * calls are delegated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchIteratorDelegator<T> implements SearchIterator<T> {

    private final Iterator<T> delegate;

    private final int size;

    private final List<OXException> warnings;

    /**
     * Initializes a new {@link SearchIteratorDelegator}
     *
     * @param iter The delegate iterator
     */
    public SearchIteratorDelegator(final Iterator<T> iter) {
        super();
        delegate = iter;
        warnings = new ArrayList<OXException>(2);
        size = -1;
    }

    /**
     * Initializes a new {@link SearchIteratorDelegator}
     *
     * @param iter The delegate iterator
     * @param size The delegate iterator's size
     * @throws IllegalArgumentException If specified size is equal to or less than zero
     */
    public SearchIteratorDelegator(final Iterator<T> iter, final int size) {
        super();
        if (size < 0) {
            throw new IllegalArgumentException("invalid size: " + size);
        }
        delegate = iter;
        this.size = size;
        warnings = new ArrayList<OXException>(2);
    }

    /**
     * Initializes a new {@link SearchIteratorDelegator}.
     *
     * @param collection The collection to iterate
     */
    public SearchIteratorDelegator(final Collection<T> collection) {
        this(collection.iterator(), collection.size());
    }

    @Override
    public boolean hasNext() throws OXException {
        return delegate.hasNext();
    }

    @Override
    public T next() throws OXException {
        return delegate.next();
    }

    @Override
    public void close() throws OXException {
        // Nothing to do
    }

    @Override
    public int size() {
        if (size < 0) {
            return -1;
        }
        return size;
    }

    public boolean hasSize() {
        return (size >= 0);
    }

    @Override
    public void addWarning(final OXException warning) {
        warnings.add(warning);
    }

    @Override
    public OXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
