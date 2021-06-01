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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Keep;
import com.openexchange.ajax.mail.filter.api.dao.action.Move;
import com.openexchange.ajax.mail.filter.api.dao.action.Redirect;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.comparison.Comparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.ContainsComparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.OverComparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.UserComparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.argument.UserComparisonArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.AddressTest;
import com.openexchange.ajax.mail.filter.api.dao.test.AllOfTest;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.api.dao.test.SizeTest;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;
import com.openexchange.exception.OXException;

/**
 * {@link NewTest}. Tests for the PUT /ajax/mailfilter?action=new API call
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class NewTest extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link NewTest}.
     *
     * @param name The test case's name
     */
    public NewTest() {
        super();
    }

    /**
     * Test a simple creation of a rule
     */
    @org.junit.Test
    public void testNew() throws Exception {
        // Create the rule
        final Rule expected;
        {
            expected = new Rule();
            expected.setName("testNew");
            expected.addAction(new Stop());
            expected.setTest(new HeaderTest(new IsComparison(), new String[] { "testheader" }, new String[] { "testvalue" }));

            int id = mailFilterAPI.createRule(expected);
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        // Assert
        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test the 'allof' test command
     */
    @org.junit.Test
    public void testNewAllOf() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("");

            expected.addAction(new Move("default0/INBOX/Spam"));
            expected.addAction(new Stop());

            Comparison<UserComparisonArgument> comparison = new UserComparison();
            AddressTest userHeaderTest = new AddressTest(comparison, new String[] { "from" }, new String[] { "zitate.at" });
            HeaderTest headerTest = new HeaderTest(new ContainsComparison(), new String[] { "subject" }, new String[] { "Zitat des Tages" });

            Test<?>[] tests = new Test<?>[] { userHeaderTest, headerTest };
            AllOfTest allOfTest = new AllOfTest(tests);

            expected.setTest(allOfTest);

            int id = mailFilterAPI.createRule(expected);
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        // Assert
        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test the 'size' test command
     */
    @org.junit.Test
    public void testNewSize() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("sizeTest");
            expected.addAction(new Keep());

            SizeTest sizeTest = new SizeTest(new OverComparison(88));
            expected.setTest(sizeTest);
            expected.setActive(true);

            int id = mailFilterAPI.createRule(expected);
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test the error case of missing headers
     */
    @org.junit.Test
    public void testNewMissingHeaders() throws Exception {
        Rule expected = new Rule();
        expected.setName("");

        expected.addAction(new Move("INBOX/Spam"));
        expected.addAction(new Stop());

        AddressTest userHeaderTest = new AddressTest(new UserComparison(), null, new String[] { "zitate.at" });
        HeaderTest headerTest = new HeaderTest(new ContainsComparison(), new String[] { "subject" }, new String[] { "Zitat des Tages" });

        Test<?>[] tests = new Test<?>[] { userHeaderTest, headerTest };
        AllOfTest allOfTest = new AllOfTest(tests);

        expected.setTest(allOfTest);

        int id = -1;
        boolean exceptionThrown = true;
        try {
            mailFilterAPI.setFailOnError(false);
            id = mailFilterAPI.createRule(expected);
            rememberRule(id);
            exceptionThrown = false;
            fail("Expected an exception");
        } catch (Exception e) {
            assertTrue(e instanceof OXException);
            OXException oxe = (OXException) e;
            assertEquals("Exception while parsing JSON: \"Error while reading command address. The parameter 'headers' is missing: : JSONObject[\"headers\"] not found.\".", oxe.getPlainLogMessage());
        } finally {
            if (!exceptionThrown) {
                mailFilterAPI.deleteRule(id);
            }
        }
    }

    /**
     * Test adding multiple filters
     */
    @org.junit.Test
    public void testNewWithTwoEntries() throws Exception {
        // Create first rule
        final Rule rule1;
        {
            rule1 = new Rule();
            rule1.setName("testNewWithTwoEntries1");
            rule1.addAction(new Stop());

            IsComparison isComp = new IsComparison();
            rule1.setTest(new HeaderTest(isComp, new String[] { "test" }, new String[] { "test" }));

            int id = mailFilterAPI.createRule(rule1);
            rememberRule(id);
            rule1.setId(id);
            rule1.setPosition(0);
        }

        // Create second rule
        final Rule rule2;
        {
            rule2 = new Rule();
            rule2.setName("testNewWithTwoEntries2");
            rule2.addAction(new Stop());

            IsComparison isComp = new IsComparison();
            rule2.setTest(new HeaderTest(isComp, new String[] { "test" }, new String[] { "test" }));

            int id = mailFilterAPI.createRule(rule2);
            rememberRule(id);
            rule2.setId(id);
            rule2.setPosition(1);
        }

        // Assert
        List<Rule> expectedRules = new ArrayList<>(2);
        Collections.addAll(expectedRules, rule1, rule2);
        getAndAssert(expectedRules);
    }

    /**
     * Test adding a filter with multiple headers and redirect action command
     */
    @org.junit.Test
    public void testNewWithHeadersAndRedirect() throws Exception {
        final Rule expected;
        {
            expected = new Rule();
            expected.setName("testNewWithHeadersAndRedirect");
            expected.setActive(true);

            expected.addAction(new Redirect("xyz@bla.de"));
            expected.setTest(new HeaderTest(new ContainsComparison(), new String[] { "X-Been-There", "X-MailingList" }, new String[] { "" }));

            int id = mailFilterAPI.createRule(expected);
            rememberRule(id);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }
}
