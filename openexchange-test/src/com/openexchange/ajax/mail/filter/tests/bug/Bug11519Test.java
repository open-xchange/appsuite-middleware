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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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
    public Bug11519Test(String name) {
        super(name);
    }

    /**
     * Test for Bug 11519 - sieve filter could not be saved
     */
    public void testBug11519() throws Exception {
        // Create rule
        Rule expectedRule = new Rule();
        expectedRule.setName("testBug11519");
        expectedRule.setActive(true);
        expectedRule.setFlags(new String[] { "vacation" });

        // Create tests
        Test<?>[] tests = new Test<?>[3];
        tests[0] = new CurrentDateTest(1183759200000L, "ge", "date");
        tests[1] = new CurrentDateTest(1183759200000L, "le", "date");
        tests[2] = new CurrentDateTest(1183759200000L, "is", "date");

        // Add test
        AllOfTest allOf = new AllOfTest(tests);
        expectedRule.setTest(allOf);

        // Add action
        Vacation vacation = new Vacation(7, Collections.singletonList("some.address@domain.tld"), null, "I'm out of office");
        expectedRule.addAction(vacation);

        // Insert
        int id = mailFilterAPI.createRule(expectedRule);
        expectedRule.setId(id);
        expectedRule.setPosition(0);

        // Assert
        getAndAssert(Collections.singletonList(expectedRule));
    }
}
