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

package com.openexchange.ajax.mail.filter;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.filter.action.AbstractAction;
import com.openexchange.ajax.mail.filter.api.MailFilterAPI;
import com.openexchange.ajax.mail.filter.api.request.AbstractMailFilterRequest;
import com.openexchange.ajax.mail.filter.api.request.AllRequest;
import com.openexchange.ajax.mail.filter.api.request.DeleteRequest;
import com.openexchange.ajax.mail.filter.api.request.InsertRequest;
import com.openexchange.ajax.mail.filter.api.request.UpdateRequest;
import com.openexchange.ajax.mail.filter.api.response.AllResponse;
import com.openexchange.ajax.mail.filter.api.response.InsertResponse;
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
import com.openexchange.ajax.mail.filter.test.AbstractTest;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.test.AjaxInit;
import com.openexchange.test.OXTestToolkit;

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
    }

    public String getHostName() {
        return hostname;
    }

    public static void deleteAllExistingRules(final String forUser, final AJAXSession ajaxSession) throws Exception {
        String[] idArray = getIdArray(forUser, ajaxSession);
        if (idArray != null) {
            for (int a = 0; a < idArray.length; a++) {
                deleteRule(idArray[a], forUser, ajaxSession);
            }
        }
    }

    public static String insertRule(final Rule rule, final String forUser, final AJAXSession ajaxSession) throws Exception {
        final InsertRequest insertRequest = new InsertRequest(rule, forUser);
        final InsertResponse insertResponse = (InsertResponse) Executor.execute(ajaxSession, insertRequest);
        return insertResponse.getId();
    }

    public static void updateRule(final Rule rule, final String forUser, final AJAXSession ajaxSession) throws Exception {
        final UpdateRequest updateRequest = new UpdateRequest(rule, forUser);
        Executor.execute(ajaxSession, updateRequest);
    }

    public static void deleteRule(final String id, final String forUser, final AJAXSession ajaxSession) throws Exception {
        final DeleteRequest deleteRequest = new DeleteRequest(id);
        Executor.execute(ajaxSession, deleteRequest);
    }

    public static String[] getIdArray(final String forUser, final AJAXSession ajaxSession) throws Exception {
        final AllRequest allRequest = new AllRequest(AbstractMailFilterRequest.URL);
        final AllResponse allResponse = (AllResponse) Executor.execute(ajaxSession, allRequest);
        allResponse.getTimestamp();

        final Rule[] ruleArray = allResponse.getRules();
        final String[] idArray = new String[ruleArray.length];
        for (int a = 0; a < ruleArray.length; a++) {
            idArray[a] = ruleArray[a].getId();
        }
        return idArray;
    }

    public static Date getLastModified(final AJAXSession ajaxSession) throws Exception {
        final AllRequest allRequest = new AllRequest(AbstractMailFilterRequest.URL);
        final AllResponse allResponse = (AllResponse) Executor.execute(ajaxSession, allRequest);
        return allResponse.getTimestamp();
    }

    public static Rule loadRules(final String forUser, final String id, final AJAXSession ajaxSession) throws Exception {
        final Rule[] rules = listRules(ajaxSession);
        for (int a = 0; a < rules.length; a++) {
            if (rules[a].getId().equals(id)) {
                return rules[a];
            }
        }

        return null;
    }

    public static Rule[] listRules(final AJAXSession ajaxSession) throws Exception {
        final AllRequest allRequest = new AllRequest(AbstractMailFilterRequest.URL);
        final AllResponse allResponse = (AllResponse) Executor.execute(ajaxSession, allRequest);

        return allResponse.getRules();
    }

    public static Rule[] listRulesForUser(final AJAXSession ajaxSession, final String userName) throws Exception {
        final AllRequest allRequest = new AllRequest(AbstractMailFilterRequest.URL, userName);
        final AllResponse allResponse = (AllResponse) Executor.execute(ajaxSession, allRequest);

        return allResponse.getRules();
    }

    public static void compareRule(final Rule rule1, final Rule rule2) throws Exception {
        OXTestToolkit.assertEqualsAndNotNull("id is not equals", rule1.getId(), rule2.getId());
        OXTestToolkit.assertEqualsAndNotNull("name is not equals", rule1.getName(), rule2.getName());
        assertEquals("active is not equals", rule1.isActive(), rule2.isActive());
        OXTestToolkit.assertEqualsAndNotNull("position is not equals", I(rule1.getPosition()), I(rule2.getPosition()));
        compareFlags(rule1.getFlags(), rule2.getFlags());
        compareActionCmds(rule1.getActioncmds(), rule2.getActioncmds());
        compareTest(rule1.getTest(), rule2.getTest());
    }

    public static void compareFlags(final String[] flags1, final String[] flags2) throws Exception {
        if (flags1 != null) {
            assertNotNull("flags are null", flags2);
            assertEquals("flags size is not equals", flags1.length, flags2.length);

            for (int a = 0; a < flags1.length; a++) {
                OXTestToolkit.assertEqualsAndNotNull("flag at position " + a + " is not equals", flags1[a], flags2[a]);
            }
        }
    }

    public static void compareActionCmds(final AbstractAction[] abstractAction1, final AbstractAction[] abstractAction2) throws Exception {
        if (abstractAction1 != null) {
            assertNotNull("abstract action array null", abstractAction2);
            assertEquals("abstract action size is not equals", abstractAction1.length, abstractAction2.length);

            for (int a = 0; a < abstractAction1.length; a++) {
                OXTestToolkit.assertEqualsAndNotNull("abstract action at position " + a + " is not equals", abstractAction1[a].getName(), abstractAction2[a].getName());
            }
        }
    }

    public static void compareTest(final AbstractTest abstractTest1, final AbstractTest abstractTest2) throws Exception {
        OXTestToolkit.assertEqualsAndNotNull("abstract test is not equals", abstractTest1, abstractTest2);
    }
}
