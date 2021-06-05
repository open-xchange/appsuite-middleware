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

package com.openexchange.caching.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.caching.CacheKey;

/**
 * {@link CacheKeyImplTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CacheKeyImplTest {

    public CacheKeyImplTest() {
        super();
    }

    /**
     * Tests if the class generates the same hash codes even if different constructors are used.
     */
    @Test
    public final void testSameHashCode() {
        final CacheKey key1 = new CacheKeyImpl(424242669, "teststring");
        final CacheKey key2 = new CacheKeyImpl(424242669, new String[] { "teststring" });
        assertEquals("Generated hashes are not the same.", key1.hashCode(), key2.hashCode());
    }

    /**
     * Tests if the class generates the same hash codes even if different constructors are used.
     */
    @Test
    public final void testEqualsObject() {
        final CacheKey key1 = new CacheKeyImpl(424242669, "teststring");
        final CacheKey key2 = new CacheKeyImpl(424242669, new String[] { "teststring" });
        assertTrue("Equals failes when using different constructors.", key1.equals(key2));
        assertTrue("Equals failes when using different constructors.", key2.equals(key1));
    }
}
