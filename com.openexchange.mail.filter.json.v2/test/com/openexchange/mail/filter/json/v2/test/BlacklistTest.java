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
