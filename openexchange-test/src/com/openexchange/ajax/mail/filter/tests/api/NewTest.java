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
import com.openexchange.ajax.mail.filter.api.dao.test.AbstractTest;
import com.openexchange.ajax.mail.filter.api.dao.test.AddressTest;
import com.openexchange.ajax.mail.filter.api.dao.test.AllOfTest;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.api.dao.test.SizeTest;
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
    public NewTest(String name) {
        super(name);
    }

    /**
     * Test a simple creation of a rule
     */
    public void testNew() throws Exception {
        // Create the rule
        final Rule expected;
        {
            expected = new Rule();
            expected.setName("testNew");
            expected.addAction(new Stop());
            expected.setTest(new HeaderTest(new IsComparison(), new String[] { "testheader" }, new String[] { "testvalue" }));

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        // Assert
        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test the 'allof' test command
     */
    public void testNewAllOf() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("");

            expected.addAction(new Move("default.INBOX/Spam"));
            expected.addAction(new Stop());

            Comparison<UserComparisonArgument> comparison = new UserComparison();
            AddressTest userHeaderTest = new AddressTest(comparison, new String[] { "from" }, new String[] { "zitate.at" });
            HeaderTest headerTest = new HeaderTest(new ContainsComparison(), new String[] { "subject" }, new String[] { "Zitat des Tages" });

            AbstractTest[] tests = new AbstractTest[] { userHeaderTest, headerTest };
            AllOfTest allOfTest = new AllOfTest(tests);

            expected.setTest(allOfTest);

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        // Assert
        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test the 'size' test command
     */
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
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test the error case of missing headers
     */
    public void testNewMissingHeaders() throws Exception {
        Rule expected = new Rule();
        expected.setName("");

        expected.addAction(new Move("INBOX/Spam"));
        expected.addAction(new Stop());

        AddressTest userHeaderTest = new AddressTest(new UserComparison(), null, new String[] { "zitate.at" });
        HeaderTest headerTest = new HeaderTest(new ContainsComparison(), new String[] { "subject" }, new String[] { "Zitat des Tages" });

        AbstractTest[] tests = new AbstractTest[] { userHeaderTest, headerTest };
        AllOfTest allOfTest = new AllOfTest(tests);

        expected.setTest(allOfTest);

        int id = -1;
        boolean exceptionThrown = true;
        try {
            mailFilterAPI.setFailOnError(false);
            id = mailFilterAPI.createRule(expected);
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
    public void testNewWithHeadersAndRedirect() throws Exception {
        final Rule expected;
        {
            expected = new Rule();
            expected.setName("testNewWithHeadersAndRedirect");
            expected.setActive(true);

            expected.addAction(new Redirect("xyz@bla.de"));
            expected.setTest(new HeaderTest(new ContainsComparison(), new String[] { "X-Been-There", "X-MailingList" }, new String[] { "" }));

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
            expected.setPosition(0);
        }

        getAndAssert(Collections.singletonList(expected));
    }
}
