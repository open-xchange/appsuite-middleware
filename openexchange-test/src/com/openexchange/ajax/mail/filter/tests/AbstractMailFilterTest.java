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

package com.openexchange.ajax.mail.filter.tests;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
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
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.UserJSONParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.AddressParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.AllOfParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.AnyOfParserImpl;
import com.openexchange.ajax.mail.filter.api.conversion.parser.test.CurrentDateParserImpl;
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
import com.openexchange.ajax.mail.filter.api.dao.TestCommand;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.ActionArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.test.common.test.AjaxInit;

public class AbstractMailFilterTest extends AbstractAJAXSession {

    public static final int[] cols = { DataObject.OBJECT_ID };

    protected static final String HOSTNAME = "hostname";

    protected String hostname = null;

    protected MailFilterAPI mailFilterAPI;

    private final List<Integer> rulesToDelete = new ArrayList<>();

    /**
     * Initialises a new {@link AbstractMailFilterTest}.
     *
     * @param name The name of the test case
     */
    public AbstractMailFilterTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        mailFilterAPI = new MailFilterAPI(getClient());

        hostname = AjaxInit.getAJAXProperty(HOSTNAME);

        // parser
        ActionParserFactory.addParser(ActionCommand.addflags, new AddFlagsParserImpl());
        ActionParserFactory.addParser(ActionCommand.discard, new DiscardParserImpl());
        ActionParserFactory.addParser(ActionCommand.keep, new KeepParserImpl());
        ActionParserFactory.addParser(ActionCommand.move, new MoveParserImpl());
        ActionParserFactory.addParser(ActionCommand.redirect, new RedirectParserImpl());
        ActionParserFactory.addParser(ActionCommand.reject, new RejectParserImpl());
        ActionParserFactory.addParser(ActionCommand.stop, new StopParserImpl());
        ActionParserFactory.addParser(ActionCommand.vacation, new VacationParserImpl());
        ActionParserFactory.addParser(ActionCommand.pgp, new PGPParserImpl());

        TestParserFactory.addParser(TestCommand.ADDRESS, new AddressParserImpl());
        TestParserFactory.addParser(TestCommand.ALLOF, new AllOfParserImpl());
        TestParserFactory.addParser(TestCommand.ANYOF, new AnyOfParserImpl());
        TestParserFactory.addParser(TestCommand.ENVELOPE, new EnvelopeParserImpl());
        TestParserFactory.addParser(TestCommand.HEADER, new HeaderParserImpl());
        TestParserFactory.addParser(TestCommand.NOT, new NotParserImpl());
        TestParserFactory.addParser(TestCommand.TRUE, new TrueParserImpl());
        TestParserFactory.addParser(TestCommand.SIZE, new SizeTestParserImpl());
        TestParserFactory.addParser(TestCommand.CURRENTDATE, new CurrentDateParserImpl());

        ComparisonParserRegistry.addParser(MatchType.is, new IsJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.matches, new MatchesJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.contains, new ContainsJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.regex, new RegexJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.under, new UnderJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.user, new UserJSONParserImpl());
        ComparisonParserRegistry.addParser(MatchType.over, new OverJSONParserImpl());

        // writer
        ActionWriterFactory.addWriter(ActionCommand.addflags, new AddFlagsWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.discard, new DiscardWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.keep, new KeepWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.move, new MoveWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.redirect, new RedirectWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.reject, new RejectWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.stop, new StopWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.vacation, new VacationWriterImpl());
        ActionWriterFactory.addWriter(ActionCommand.pgp, new PGPWriterImpl());

        TestWriterFactory.addWriter(TestCommand.ADDRESS, new AddressWriterImpl());
        TestWriterFactory.addWriter(TestCommand.ALLOF, new AllOfWriterImpl());
        TestWriterFactory.addWriter(TestCommand.ANYOF, new AnyOfWriterImpl());
        TestWriterFactory.addWriter(TestCommand.ENVELOPE, new EnvelopeWriterImpl());
        TestWriterFactory.addWriter(TestCommand.HEADER, new HeaderWriterImpl());
        TestWriterFactory.addWriter(TestCommand.NOT, new NotWriterImpl());
        TestWriterFactory.addWriter(TestCommand.TRUE, new TrueWriterImpl());
        TestWriterFactory.addWriter(TestCommand.SIZE, new SizeTestWriterImpl());
        TestWriterFactory.addWriter(TestCommand.CURRENTDATE, new CurrentDateWriterImpl());

        ComparisonWriterRegistry.addWriter(MatchType.is, new IsJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.matches, new MatchesJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.contains, new ContainsJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.regex, new RegexJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.user, new UserComparisonWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.under, new UnderJSONWriterImpl());
        ComparisonWriterRegistry.addWriter(MatchType.over, new OverJSONWriterImpl());
    }

    protected void rememberRule(int ruleId) {
        rulesToDelete.add(I(ruleId));
    }

    protected void forgetRules() {
        rulesToDelete.clear();
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
        assertTrue("The 'active' attribute differs", expected.isActive() == actual.isActive());
        assertEquals("The 'position' attribute differs", expected.getPosition(), actual.getPosition());

        assertArrayEquals("The 'flags' differ", expected.getFlags(), actual.getFlags());
        assertActions(expected.getActions(), actual.getActions());
        assertTest(expected.getTest(), actual.getTest());
    }

    /**
     * Asserts that the expected {@link Action} is equal to actual {@link Action}
     *
     * @param expected the expected {@link Action}
     * @param actual the actual {@link Action}
     */
    // TODO Complete assertions
    private void assertActions(List<Action<? extends ActionArgument>> expected, List<Action<? extends ActionArgument>> actual) {
        assertEquals("The size differs", expected.size(), actual.size());
        for (int index = 0; index < expected.size(); index++) {
            assertEquals("The 'actionCommand' differs", expected.get(index).getActionCommand(), actual.get(index).getActionCommand());
        }
    }

    /**
     * Asserts that the expected {@link AbstractTest} is equal the actual {@link AbstractTest}
     *
     * @param expected The expected {@link AbstractTest}
     * @param actual The actual {@link AbstractTest}
     */
    //TODO complete assertions
    private void assertTest(Test<?> expected, Test<?> actual) {
        assertEquals("The 'name' attribute of the test differs", expected.getTestCommand(), actual.getTestCommand());
    }
}
