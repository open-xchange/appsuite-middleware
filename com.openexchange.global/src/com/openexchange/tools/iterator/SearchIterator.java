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

package com.openexchange.tools.iterator;

import java.io.Closeable;
import com.openexchange.exception.OXException;

/**
 * {@link SearchIterator} - An extended iterator over a collection or a (releasable) resource.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SearchIterator<T> extends Closeable {

    /**
     * Returns <code>true</code> if the iteration has more elements. (In other words, returns <code>true</code> if {@link #next()} would
     * return an element.)
     *
     * @return <code>true</code> if the iterator has more elements; otherwise <code>false</code>
     * @throws OXException If check for further elements fails
     */
    boolean hasNext() throws OXException;

    /**
     * Returns the next element in the iteration. Calling this method repeatedly until the {@link #hasNext()} method returns
     * <code>false</code> will return each element in the underlying collection exactly once.
     *
     * @return The next element in the iteration.
     * @throws OXException If next element cannot be returned
     */
    T next() throws OXException;

    /**
     * Closes the search iterator
     */
    @Override
    void close();

    /**
     * This iterator's size
     *
     * @return The size
     */
    int size();

    /**
     * Indicates if this iterator has warnings
     *
     * @return <code>true</code> if this iterator has warnings; otherwise <code>false</code>
     */
    boolean hasWarnings();

    /**
     * Adds specified warning to this iterator's warnings
     *
     * @param warning The warning to add
     */
    void addWarning(OXException warning);

    /**
     * Gets the iterator's warnings as an array
     *
     * @return The iterator's warnings as an array or <code>null</code>
     */
    OXException[] getWarnings();

}
