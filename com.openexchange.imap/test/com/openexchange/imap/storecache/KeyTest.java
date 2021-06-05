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

package com.openexchange.imap.storecache;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.imap.storecache.IMAPStoreCache.Key;


/**
 * {@link KeyTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class KeyTest {
    public KeyTest() {
        super();
    }

         @Test
     public void testDifferingKeys() {
        final Key key1 = new Key(0, "imap.host.org", 143, "proxyuser", 11, 1);
        final int hc1 = key1.hashCode();

        final Key key2 = new Key(0, "imap.host.org", 143, "proxyuser", 12, 1);
        final int hc2 = key2.hashCode();

        assertFalse("Hashcodes should be different", hc1 == hc2);

        assertFalse("Equals() should yield false", key1.equals(key2));
    }

}
