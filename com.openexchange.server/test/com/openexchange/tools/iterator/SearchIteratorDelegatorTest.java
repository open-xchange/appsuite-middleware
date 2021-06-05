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

package com.openexchange.tools.iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Test;

/**
 * {@link SearchIteratorDelegatorTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SearchIteratorDelegatorTest {

    /**
     * Tests if bug 16420 appears again.
     */
    @Test
    public void testZeroSizeCollection() throws Throwable {
        List<Object> list = new ArrayList<Object>();
        try (SearchIteratorDelegator<Object> delegator = new SearchIteratorDelegator<Object>(list)) {
            assertTrue("Collection do have a determined size.", delegator.hasSize());
            assertEquals("Size should be zero.", 0, delegator.size());
            assertFalse("There should be no available element.", delegator.hasNext());
            delegator.next();
            fail("An exception must be given.");
        } catch (NoSuchElementException e) {
            // Everythign is fine.
        }

    }
}
