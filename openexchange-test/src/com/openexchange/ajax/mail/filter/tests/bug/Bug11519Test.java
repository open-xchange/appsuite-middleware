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

package com.openexchange.ajax.mail.filter.tests.bug;

import java.util.Collections;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Vacation;
import com.openexchange.ajax.mail.filter.api.dao.test.AllOfTest;
import com.openexchange.ajax.mail.filter.api.dao.test.CurrentDateTest;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

/**
 * {@link Bug11519Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug11519Test extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link Bug11519Test}.
     *
     * @param name
     */
    public Bug11519Test() {
        super();
    }

    /**
     * Test for Bug 11519 - sieve filter could not be saved
     */
    @org.junit.Test
    public void testBug11519() throws Exception {
        // Create rule
        Rule expectedRule = new Rule();
        expectedRule.setName("testBug11519");
        expectedRule.setActive(true);
        expectedRule.setFlags(new String[] { "vacation" });

        // Create tests
        Test<?>[] tests = new Test<?>[3];
        tests[0] = new CurrentDateTest("date", "ge", 1183759200000L);
        tests[1] = new CurrentDateTest("date", "le", 1183759200000L);
        tests[2] = new CurrentDateTest("date", "is", 1183759200000L);

        // Add test
        AllOfTest allOf = new AllOfTest(tests);
        expectedRule.setTest(allOf);

        // Add action
        Vacation vacation = new Vacation(7, Collections.singletonList("some.address@domain.tld"), null, "I'm out of office");
        expectedRule.addAction(vacation);

        // Insert
        int id = mailFilterAPI.createRule(expectedRule);
        rememberRule(id);
        expectedRule.setId(id);
        expectedRule.setPosition(0);

        // Assert
        getAndAssert(Collections.singletonList(expectedRule));
    }
}
