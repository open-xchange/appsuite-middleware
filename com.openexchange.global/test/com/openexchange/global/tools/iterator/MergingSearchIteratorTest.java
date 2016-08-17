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

package com.openexchange.global.tools.iterator;

import java.util.Comparator;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.MergingSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link MergingSearchIteratorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MergingSearchIteratorTest extends TestCase {

    public void testMerge() throws OXException {
        final Integer[] a = new Integer[] { 0, 3, 4, 7, 9, 12, 13, 16 };
        final Integer[] b = new Integer[] { 1, 2, 5, 10, 18 };
        final Integer[] c = new Integer[] { 1, 6, 8, 11, 14, 20 };

        final Integer[] expected = new Integer[] { 0, 1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 18, 20 };

        final SearchIterator<Integer> complete = new MergingSearchIterator<Integer>(
            new IntegerComparator(),
            true,
            new ArrayIterator<Integer>(a),
            new ArrayIterator<Integer>(b),
            new ArrayIterator<Integer>(c)
            );

        for(int i = 0; i < complete.size(); i++) {
            assertTrue(complete.hasNext());
            assertEquals(expected[i], complete.next());
        }

        assertFalse(complete.hasNext());
    }

    public void testMergeEmptyWithFull() throws OXException {
        final Integer[] a = new Integer[0];
        final Integer[] b = new Integer[] { 1, 2, 5, 10, 18 };

        final Integer[] expected = b;


        final SearchIterator<Integer> complete = new MergingSearchIterator<Integer>(
            new IntegerComparator(),
            true,
            new ArrayIterator<Integer>(a),
            new ArrayIterator<Integer>(b)
            );

        for(int i = 0; i < complete.size(); i++) {
            assertTrue(complete.hasNext());
            assertEquals(expected[i], complete.next());
        }

        assertFalse(complete.hasNext());
    }

    private static final class IntegerComparator implements Comparator<Integer> {

        @Override
        public int compare(final Integer o1, final Integer o2) {
            return o2 - o1;
        }

    }
}
