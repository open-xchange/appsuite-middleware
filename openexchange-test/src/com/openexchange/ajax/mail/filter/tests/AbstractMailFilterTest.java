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
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mail.filter.api.MailFilterAPI;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.AbstractAction;
import com.openexchange.ajax.mail.filter.api.dao.test.AbstractTest;
import com.openexchange.ajax.mail.filter.api.writer.action.ActionWriterFactory;
import com.openexchange.ajax.mail.filter.api.writer.action.AddFlagsWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.action.MoveWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.action.RedirectWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.action.RejectWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.action.SimpleActionWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.action.VacationWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.comparison.ComparisonWriterFactory;
import com.openexchange.ajax.mail.filter.api.writer.comparison.ContainsWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.comparison.IsWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.comparison.MatchesWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.comparison.RegexWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.comparison.SizeComparisonWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.test.AddressWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.test.AllOfWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.test.AnyOfWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.test.EnvelopeWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.test.HeaderWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.test.NotWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.test.SizeTestWriterImpl;
import com.openexchange.ajax.mail.filter.api.writer.test.TestWriterFactory;
import com.openexchange.ajax.mail.filter.api.writer.test.TrueWriterImpl;
import com.openexchange.ajax.mail.filter.parser.action.ActionParserFactory;
import com.openexchange.ajax.mail.filter.parser.action.AddFlagsParserImpl;
import com.openexchange.ajax.mail.filter.parser.action.MoveParserImpl;
import com.openexchange.ajax.mail.filter.parser.action.RedirectParserImpl;
import com.openexchange.ajax.mail.filter.parser.action.RejectParserImpl;
import com.openexchange.ajax.mail.filter.parser.action.SimpleActionParserImpl;
import com.openexchange.ajax.mail.filter.parser.action.VacationParserImpl;
import com.openexchange.ajax.mail.filter.parser.comparison.ComparisonParserFactory;
import com.openexchange.ajax.mail.filter.parser.comparison.ContainsParserImpl;
import com.openexchange.ajax.mail.filter.parser.comparison.IsParserImpl;
import com.openexchange.ajax.mail.filter.parser.comparison.MatchesParserImpl;
import com.openexchange.ajax.mail.filter.parser.comparison.RegexParserImpl;
import com.openexchange.ajax.mail.filter.parser.comparison.SizeComparisonParserImpl;
import com.openexchange.ajax.mail.filter.parser.test.AddressParserImpl;
import com.openexchange.ajax.mail.filter.parser.test.AllOfParserImpl;
import com.openexchange.ajax.mail.filter.parser.test.AnyOfParserImpl;
import com.openexchange.ajax.mail.filter.parser.test.EnvelopeParserImpl;
import com.openexchange.ajax.mail.filter.parser.test.HeaderParserImpl;
import com.openexchange.ajax.mail.filter.parser.test.NotParserImpl;
import com.openexchange.ajax.mail.filter.parser.test.SizeTestParserImpl;
import com.openexchange.ajax.mail.filter.parser.test.TestParserFactory;
import com.openexchange.ajax.mail.filter.parser.test.TrueParserImpl;
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
        ActionParserFactory.addParser("addflags", new AddFlagsParserImpl());
        ActionParserFactory.addParser("discard", new SimpleActionParserImpl());
        ActionParserFactory.addParser("keep", new SimpleActionParserImpl());
        ActionParserFactory.addParser("move", new MoveParserImpl());
        ActionParserFactory.addParser("redirect", new RedirectParserImpl());
        ActionParserFactory.addParser("reject", new RejectParserImpl());
        ActionParserFactory.addParser("stop", new SimpleActionParserImpl());
        ActionParserFactory.addParser("vacation", new VacationParserImpl());

        TestParserFactory.addParser("address", new AddressParserImpl());
        TestParserFactory.addParser("allof", new AllOfParserImpl());
        TestParserFactory.addParser("anyof", new AnyOfParserImpl());
        TestParserFactory.addParser("envelope", new EnvelopeParserImpl());
        TestParserFactory.addParser("header", new HeaderParserImpl());
        TestParserFactory.addParser("not", new NotParserImpl());
        TestParserFactory.addParser("true", new TrueParserImpl());
        TestParserFactory.addParser("size", new SizeTestParserImpl());

        ComparisonParserFactory.addParser("is", new IsParserImpl());
        ComparisonParserFactory.addParser("matches", new MatchesParserImpl());
        ComparisonParserFactory.addParser("contains", new ContainsParserImpl());
        ComparisonParserFactory.addParser("regex", new RegexParserImpl());
        ComparisonParserFactory.addParser("size", new SizeComparisonParserImpl());

        // writer
        ActionWriterFactory.addWriter("addflags", new AddFlagsWriterImpl());
        ActionWriterFactory.addWriter("discard", new SimpleActionWriterImpl());
        ActionWriterFactory.addWriter("keep", new SimpleActionWriterImpl());
        ActionWriterFactory.addWriter("move", new MoveWriterImpl());
        ActionWriterFactory.addWriter("redirect", new RedirectWriterImpl());
        ActionWriterFactory.addWriter("reject", new RejectWriterImpl());
        ActionWriterFactory.addWriter("stop", new SimpleActionWriterImpl());
        ActionWriterFactory.addWriter("vacation", new VacationWriterImpl());

        TestWriterFactory.addWriter("address", new AddressWriterImpl());
        TestWriterFactory.addWriter("allof", new AllOfWriterImpl());
        TestWriterFactory.addWriter("anyof", new AnyOfWriterImpl());
        TestWriterFactory.addWriter("envelope", new EnvelopeWriterImpl());
        TestWriterFactory.addWriter("header", new HeaderWriterImpl());
        TestWriterFactory.addWriter("not", new NotWriterImpl());
        TestWriterFactory.addWriter("true", new TrueWriterImpl());
        TestWriterFactory.addWriter("size", new SizeTestWriterImpl());

        ComparisonWriterFactory.addWriter("is", new IsWriterImpl());
        ComparisonWriterFactory.addWriter("matches", new MatchesWriterImpl());
        ComparisonWriterFactory.addWriter("contains", new ContainsWriterImpl());
        ComparisonWriterFactory.addWriter("regex", new RegexWriterImpl());
        ComparisonWriterFactory.addWriter("size", new SizeComparisonWriterImpl());

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
     * Asserts that the expected {@link Rule} is equal the actual {@link Rule}
     * 
     * @param expected The expected {@link Rule}
     * @param actual The actual {@link Rule}
     */
    public void assertRule(Rule expected, Rule actual) {
        assertEquals("The 'id' attribute differs", expected.getId(), actual.getId());
        assertEquals("The 'name' attribute differs", expected.getName(), actual.getName());
        assertEquals("The 'active' attribute differs", expected.isActive(), actual.isActive());
        assertEquals("The 'position' attribute differs", expected.getPosition(), actual.getPosition());

        assertArrayEquals("The 'flags' differ", expected.getFlags(), actual.getFlags());
        assertActions(expected.getActioncmds(), actual.getActioncmds());
        assertTest(expected.getTest(), actual.getTest());
    }

    /**
     * Asserts that the expected {@link AbstractAction} is equal to actual {@link AbstractAction}
     * 
     * @param expected the expected {@link AbstractAction}
     * @param actual the actual {@link AbstractAction}
     */
    // TODO Complete assertions
    private void assertActions(AbstractAction[] expected, AbstractAction[] actual) {
        assertEquals("The size differs", expected.length, actual.length);
        for (int index = 0; index < expected.length; index++) {
            assertEquals("The 'actionCommand' differs", expected[index].getName(), actual[index].getName());
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
