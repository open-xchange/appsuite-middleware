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

package com.openexchange.mail.filter.json.v2.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import com.openexchange.mail.filter.json.v2.config.Blacklist;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.BasicGroup;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.Field;

/**
 * {@link BlacklistTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class BlacklistTest {

    /**
     * Initializes a new {@link BlacklistTest}.
     */
    public BlacklistTest() {
        super();
    }

    private static final Map<String, Set<String>> MAP = new HashMap<>();

    static {
        MAP.put(Blacklist.key(BasicGroup.tests, "from", Field.comparisons), Collections.singleton("is"));
        MAP.put(Blacklist.key(BasicGroup.tests, "address", Field.headers), Collections.singleton("from"));
        MAP.put(BasicGroup.actions.name(), Collections.singleton("keep"));
        MAP.put(BasicGroup.tests.name(), Collections.singleton("envelope"));
        MAP.put(BasicGroup.comparisons.name(), Collections.singleton("not is"));
    }

    @Test
    public void testBlacklist() {
        Blacklist blacklist = new Blacklist(MAP);

        // Test basic groups
        assertTrue(blacklist.isBlacklisted(BasicGroup.actions, "keep"));
        assertTrue(blacklist.isBlacklisted(BasicGroup.tests, "envelope"));
        assertTrue(blacklist.isBlacklisted(BasicGroup.comparisons, "not is"));

        assertFalse(blacklist.isBlacklisted(BasicGroup.actions, "redirect"));
        assertFalse(blacklist.isBlacklisted(BasicGroup.tests, "true"));
        assertFalse(blacklist.isBlacklisted(BasicGroup.comparisons, "regex"));

        // Tests subgroups
        assertTrue(blacklist.isBlacklisted(BasicGroup.tests, "from", Field.comparisons, "is"));
        assertTrue(blacklist.isBlacklisted(BasicGroup.tests, "address", Field.headers, "from"));

        assertFalse(blacklist.isBlacklisted(BasicGroup.tests, "from", Field.comparisons, "regex"));

        // Test get
        Set<String> fromBlacklist = blacklist.get(BasicGroup.tests, "from", Field.comparisons);
        assertNotNull(fromBlacklist);
        assertTrue(fromBlacklist.contains("is"));

        Set<String> testBlacklist = blacklist.get(BasicGroup.tests, null, null);
        assertNotNull(testBlacklist);
        assertTrue(testBlacklist.contains("envelope"));

        Set<String> nullBlacklist = blacklist.get(BasicGroup.tests, "to", Field.comparisons);
        assertNull(nullBlacklist);
    }

}
