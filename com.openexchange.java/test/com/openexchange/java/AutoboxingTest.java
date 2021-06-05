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

package com.openexchange.java;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static org.junit.Assert.assertArrayEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Tests the {@link Autoboxing} class methods.
 * TODO should be moved to common bundle where the {@link Autoboxing} class is located but this results in cyclic bundle dependencies.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AutoboxingTest {

    public AutoboxingTest() {
        super();
    }

    @Test
    public final void testI2iIntegerArray() {
        final Integer[] test = new Integer[] { null, I(1), null, I(2), null };
        assertArrayEquals("Array conversion failed.", new int[] { 1, 2 }, I2i(test));
    }

    @Test
    public final void testI2iCollectionOfIntegerWithNullValues() {
        List<Integer> test = new ArrayList<Integer>();
        test.add(null);
        test.add(I(1));
        test.add(null);
        test.add(I(2));
        test.add(null);
        assertArrayEquals("Collection conversion not correct.", new int[] { 1, 2 }, I2i(test));
    }

}
