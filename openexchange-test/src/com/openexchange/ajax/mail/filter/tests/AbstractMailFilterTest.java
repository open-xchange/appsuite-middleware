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

package com.openexchange.ajax.mail.filter.tests;

import static org.junit.Assert.assertArrayEquals;
import java.util.List;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mail.filter.api.MailFilterAPI;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.ActionParserFactory;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.AddFlagsParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.DiscardParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.KeepParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.MoveParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.PGPParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.RedirectParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.RejectParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.StopParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.action.VacationParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.ComparisonParserRegistry;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.ContainsJSONParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.IsJSONParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.MatchesJSONParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.OverJSONParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.RegexJSONParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.UnderJSONParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.AddressParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.AllOfParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.AnyOfParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.CurrenttDateParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.EnvelopeParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.HeaderParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.NotParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.SizeTestParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.TestParserFactory;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.TrueParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.ActionWriterFactory;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.AddFlagsWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.DiscardWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.KeepWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.MoveWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.PGPWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.RedirectWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.RejectWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.StopWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.action.VacationWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.ComparisonWriterRegistry;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.ContainsJSONWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.IsJSONWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.MatchesJSONWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.OverJSONWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.RegexJSONWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.UnderJSONWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.comparison.UserComparisonWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.AddressWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.AllOfWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.AnyOfWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.CurrentDateWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.EnvelopeWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.HeaderWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.NotWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.SizeTestWriterImpl;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.TestWriterFactory;
import com.openexchange.ajax.mail.filter.api.conversion.writer.test.TrueWriterImpl;
import com.openexchange.ajax.mail.filter.api.dao.ActionCommand;
import com.openexchange.ajax.mail.filter.api.dao.MatchType;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.test.AbstractTest;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.test.AjaxInit;

public class AbstractMailFilterTest extends AbstractAJAXSession {

    public static final int[] cols = { DataObject.OBJECT_ID };

    protected static final String HOSTNAME = "hostname";

    protected String hostname = null;

    protected MailFilterAPI mailFilterAPI;

    /**
     * Initialises a new {@link AbstractMailFilterTest}.
     * 
     * @param name The name of the test case
     */
    public AbstractMailFilterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mailFilterAPI = new MailFilterAPI(client);

        hostname = AjaxInit.getAJAXProperty(HOSTNAME);

        // parser
        ActionParserFactory.addParser(ActionCommand.ADDFLAGS, new AddFlagsParserImpl());
        ActionParserFactory.addParser(ActionCommand.DISCARD, new DiscardParserImpl());
        ActionParserFactory.addParser(ActionCommand.KEEP, new KeepParserImpl());
        ActionParserFactory.addParser(ActionCommand.MOVE, new MoveParserImpl());
        ActionParserFactory.addParser(ActionCommand.REDIRECT, new RedirectParserImpl());
        ActionParserFactory.addParser(ActionCommand.REJECT, new RejectParserImpl());
        ActionParserFactory.addParser(ActionCommand.STOP, new StopParserImpl());
        ActionParserFactory.addParser(ActionCommand.VACATION, new VacationParserImpl());
        ActionParserFactory.addParser(ActionCommand.PGP, new PGPParserImpl());

        TestParserFactory.addParser("address", new AddressParserImpl());
        TestParserFactory.addParser("allof", new AllOfParserImpl());
        TestParserFactory.addParser("anyof", new AnyOfParserImpl());
        TestParserFactory.addParser("envelope", new EnvelopeParserImpl());
        TestParserFactory.addParser("header", new HeaderParserImpl());
        TestParserFactory.addParser("not", new NotParserImpl());
        TestParserFactory.addParser("true", new TrueParserImpl());
        TestParserFactory.addParser("size", new SizeTestParserImpl());
        TestParserFactory.addParser("currentdate", new CurrenttDateParserImpl());

        ComparisonParserRegistry.addParser(MatchType.is, new IsJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.matches, new MatchesJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.contains, new ContainsJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.regex, new RegexJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.under, new UnderJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.over, new OverJSONParserImpl());

        // writer
        ActionWriterFactory.addWriter(ActionCommand.ADDFLAGS, new AddFlagsWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.DISCARD, new DiscardWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.KEEP, new KeepWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.MOVE, new MoveWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.REDIRECT, new RedirectWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.REJECT, new RejectWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.STOP, new StopWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.VACATION, new VacationWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.PGP, new PGPWriterImpl());

        TestWriterFactory.addWriter("address", new AddressWriterImpl());
        TestWriterFactory.addWriter("allof", new AllOfWriterImpl());
        TestWriterFactory.addWriter("anyof", new AnyOfWriterImpl());
        TestWriterFactory.addWriter("envelope", new EnvelopeWriterImpl());
        TestWriterFactory.addWriter("header", new HeaderWriterImpl());
        TestWriterFactory.addWriter("not", new NotWriterImpl());
        TestWriterFactory.addWriter("true", new TrueWriterImpl());
        TestWriterFactory.addWriter("size", new SizeTestWriterImpl());
        TestWriterFactory.addWriter("currentdate", new CurrentDateWriterImpl());

        ComparisonWriterRegistry.addWriter(MatchType.is, new IsJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.matches, new MatchesJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.contains, new ContainsJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.regex, new RegexJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.user, new UserComparisonWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.under, new UnderJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.over, new OverJSONWriterImpl());

        // Start fresh
        mailFilterAPI.purge();
    }

    @Override
    public void tearDown() throws Exception {
        // cleanup
        mailFilterAPI.purge();

        super.tearDown();
    }

    /**
     * Gets all rules and asserts with the expectedRules list
     * 
     * @param expectedRules The expected rules list
     * @throws Exception if getting all rules fails
     */
    protected void getAndAssert(List<Rule> expectedRules) throws Exception {
        int expectedAmount = expectedRules.size();

        // Get all rules
        List<Rule> rules = mailFilterAPI.listRules();
        assertEquals(expectedAmount + " rule(s) was/were expected", expectedAmount, rules.size());

        // Assert rules
        for (int index = 0; index < rules.size(); index++) {
            assertRule(expectedRules.get(index), rules.get(index));
        }
    }

    /**
     * Asserts that the expected {@link Rule} is equal the actual {@link Rule}
     * 
     * @param expected The expected {@link Rule}
     * @param actual The actual {@link Rule}
     */
    protected void assertRule(Rule expected, Rule actual) {
        assertEquals("The 'id' attribute differs", expected.getId(), actual.getId());
        assertEquals("The 'name' attribute differs", expected.getName(), actual.getName());
        assertEquals("The 'active' attribute differs", expected.isActive(), actual.isActive());
        assertEquals("The 'position' attribute differs", expected.getPosition(), actual.getPosition());

        assertArrayEquals("The 'flags' differ", expected.getFlags(), actual.getFlags());
        assertActions(expected.getActionCommands(), actual.getActionCommands());
        assertTest(expected.getTest(), actual.getTest());
    }

    /**
     * Asserts that the expected {@link Action} is equal to actual {@link Action}
     * 
     * @param expected the expected {@link Action}
     * @param actual the actual {@link Action}
     */
    // TODO Complete assertions
    private void assertActions(Action[] expected, Action[] actual) {
        assertEquals("The size differs", expected.length, actual.length);
        for (int index = 0; index < expected.length; index++) {
            assertEquals("The 'actionCommand' differs", expected[index].getAction(), actual[index].getAction());
        }
    }

    /**
     * Asserts that the expected {@link AbstractTest} is equal the actual {@link AbstractTest}
     * 
     * @param expected The expected {@link AbstractTest}
     * @param actual The actual {@link AbstractTest}
     */
    //TODO complete assertions
    private void assertTest(AbstractTest expected, AbstractTest actual) {
        assertEquals("The 'name' attribute of the test differs", expected.getName(), actual.getName());
    }
}
