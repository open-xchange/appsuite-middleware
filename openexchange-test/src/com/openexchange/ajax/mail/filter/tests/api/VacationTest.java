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

package com.openexchange.ajax.mail.filter.tests.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Vacation;
import com.openexchange.ajax.mail.filter.api.dao.test.AllOfTest;
import com.openexchange.ajax.mail.filter.api.dao.test.CurrentDateTest;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.api.dao.test.TrueTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

/**
 * {@link VacationTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class VacationTest extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link VacationTest}.
     * 
     * @param name The test case's name
     */
    public VacationTest(String name) {
        super(name);
    }

    /**
     * Test a basic vacation notice with multiple addresses
     */
    public void testNewVacationWithMultipleAddresses() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Vacation Notice");
            expected.setFlags(new String[] { "vacation" });
            expected.setActive(true);

            List<String> addresses = new ArrayList<>(2);
            Collections.addAll(addresses, "root@localhost", "billg@microsoft.com");
            Vacation vacation = new Vacation(13, addresses, "Betreff", "Text\\u000aText");
            expected.addAction(vacation);
            expected.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test a basic vacation notice with single address
     */
    public void testNewVacationWithSingleAddress() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Vacation Notice");
            expected.setFlags(new String[] { "vacation" });
            expected.setActive(false);

            Vacation vacation = new Vacation(1, Collections.singletonList("dsfa"), "123", "123");
            expected.addAction(vacation);
            expected.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test a vacation notice without subject
     */
    public void testNewVacationWithoutSubject() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Abwesenheitsbenachrichtigung");
            expected.setActive(true);
            expected.setFlags(new String[] { "vacation" });

            Vacation vacation = new Vacation(7, Collections.singletonList("foo@invalid.tld"), null, "I'm out of office");
            expected.addAction(vacation);
            expected.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test a vacation notice that has plain text and sieve keywords in the 'text' tag
     */
    public void testNewVacationPlainAtTheEnd() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Vacation Notice");
            expected.setActive(true);
            expected.setFlags(new String[] { "vacation" });

            Vacation vacation = new Vacation(7, Collections.singletonList("foo@invalid.tld"), null, "if true \r\n{\r\n    vacation :days 13 :addresses [ \"root@localhost\" , \"billg@microsoft.com\" ] :mime :subject \"Betreff\" \"Text\r\nText\" ;\r\n}\r\n");
            expected.addAction(vacation);
            expected.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Tests the week day field for the vacation rule
     */
    public void testWeekDayField() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Vacation Notice (testWeekDayField)");
            expected.setActive(true);
            expected.setFlags(new String[] { "vacation" });

            Vacation vacation = new Vacation(7, Collections.singletonList("foo@invalid.tld"), null, "if true \r\n{\r\n    vacation :days 13 :addresses [ \"root@localhost\" , \"billg@microsoft.com\" ] :mime :subject \"Betreff\" \"Text\r\nText\" ;\r\n}\r\n");
            expected.addAction(vacation);

            Test<?>[] tests = new Test<?>[] { new CurrentDateTest("weekday", "is", 3) };
            expected.setTest(new AllOfTest(tests));

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Tests the time field for the vacation rule
     */
    public void testTimeField() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Vacation Notice (testTimeField)");
            expected.setActive(true);
            expected.setFlags(new String[] { "vacation" });

            Vacation vacation = new Vacation(7, Collections.singletonList("foo@invalid.tld"), null, "if true \r\n{\r\n    vacation :days 13 :addresses [ \"root@localhost\" , \"billg@microsoft.com\" ] :mime :subject \"Betreff\" \"Text\r\nText\" ;\r\n}\r\n");
            expected.addAction(vacation);

            Test<?>[] tests = new Test<?>[] { new CurrentDateTest("time", "is", 3627279000000L) };
            expected.setTest(new AllOfTest(tests));

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }
}
