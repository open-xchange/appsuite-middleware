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

package com.openexchange.ajax.mail.filter.tests.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
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
    public VacationTest() {
        super();
    }

    @Before
    public void setup() throws Exception {
        mailFilterAPI.deleteScript();
    }

    /**
     * Test a basic vacation notice with multiple addresses
     */
    @org.junit.Test
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
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test a basic vacation notice with single address
     */
    @org.junit.Test
    public void testNewVacationWithSingleAddress() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Vacation Notice");
            expected.setFlags(new String[] { "vacation" });
            expected.setActive(true);

            Vacation vacation = new Vacation(1, Collections.singletonList("123@invalid.com"), "123", "123");
            expected.addAction(vacation);
            expected.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(expected);
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test a vacation notice without subject
     */
    @org.junit.Test
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
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test a vacation notice that has plain text and sieve keywords in the 'text' tag
     */
    @org.junit.Test
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
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Tests the week day field for the vacation rule
     */
    @org.junit.Test
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
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Tests the time field for the vacation rule
     */
    @org.junit.Test
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
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }
}
