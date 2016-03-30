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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link CombinedSearchIterator} - Combines one or more instances of {@link SearchIterator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CombinedSearchIterator<T> implements SearchIterator<T> {

    private final SearchIterator<T>[] iterators;

    private int i = 0;

    private OXException[] warnings;

    private Boolean hasWarnings;

    /**
     * Initializes a new {@link CombinedSearchIterator}
     *
     * @param iterators The instances of {@link SearchIterator}
     */
    public CombinedSearchIterator(final SearchIterator<T>... iterators) {
        super();
        this.iterators = iterators;
    }

    private boolean next = false;

    @Override
    public boolean hasNext() throws OXException{
        if (iterators.length == 0) {
            return false;
        }
        next = false;
        while (i < iterators.length && !next) {
            if (iterators[i].hasNext()) {
                next = true;
            } else {
                i++;
            }
        }
        return next;
    }

    @Override
    public T next() throws OXException {
        if (iterators.length == 0 || !next) {
            throw SearchIteratorExceptionCodes.NO_SUCH_ELEMENT.create().setPrefix("NON");
        }
        return iterators[i].next();
    }

    @Override
    public void close() {
        for (final SearchIterator<T> iter : iterators) {
            SearchIterators.close(iter);
        }
    }

    @Override
    public int size() {
        return -1;
    }

    public boolean hasSize() {
        return false;
    }

    @Override
    public void addWarning(final OXException warning) {
        throw new UnsupportedOperationException("Mehtod addWarning() not implemented");
    }

    @Override
    public OXException[] getWarnings() {
        if (null == warnings) {
            if (iterators.length == 0) {
                warnings = new OXException[0];
            } else {
                final List<OXException> list = new ArrayList<OXException>(iterators.length << 1);
                for (final SearchIterator<?> iter : iterators) {
                    if (iter.hasWarnings()) {
                        list.addAll(Arrays.asList(iterators[i].getWarnings()));
                    }
                }
                warnings = list.toArray(new OXException[list.size()]);
            }
        }
        return warnings.length == 0 ? null : warnings;
    }

    @Override
    public boolean hasWarnings() {
        if (null == hasWarnings) {
            if (iterators.length == 0) {
                hasWarnings = Boolean.FALSE;
            } else {
                hasWarnings = Boolean.FALSE;
                for (final SearchIterator<?> iter : iterators) {
                    if (iter.hasWarnings()) {
                        hasWarnings = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        return hasWarnings.booleanValue();
    }
}
