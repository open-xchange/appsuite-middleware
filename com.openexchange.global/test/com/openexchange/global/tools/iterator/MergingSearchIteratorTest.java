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

package com.openexchange.global.tools.iterator;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Comparator;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.MergingSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link MergingSearchIteratorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SuppressWarnings("synthetic-access")
public class MergingSearchIteratorTest {
    
    @Test
    public void testMerge() throws OXException {
        final Integer[] a = new Integer[] { I(0), I(3), I(4), I(7), I(9), I(12), I(13), I(16) };
        final Integer[] b = new Integer[] { I(1), I(2), I(5), I(10), I(18) };
        final Integer[] c = new Integer[] { I(1), I(6), I(8), I(11), I(14), I(20) };

        final Integer[] expected = new Integer[] { I(0), I(1), I(1), I(2), I(3), I(4), I(5), I(6), I(7), I(8), I(9), I(10), I(11), I(12), I(13), I(14), I(16), I(18), I(20) };

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

    @Test
    public void testMergeEmptyWithFull() throws OXException {
        final Integer[] a = new Integer[0];
        final Integer[] b = new Integer[] { I(1), I(2), I(5), I(10), I(18) };

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
            return o2.intValue() - o1.intValue();
        }

    }
}
