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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.mail.filter.action.AbstractAction;
import com.openexchange.ajax.mail.filter.action.Keep;
import com.openexchange.ajax.mail.filter.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.test.HeaderTest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetResponse;
import com.openexchange.mailaccount.MailAccountDescription;

public class AdminListTest extends AbstractMailFilterTest {

    private AJAXClient userClient;

    private AJAXSession userSession;

    private AJAXClient adminClient;

    private AJAXSession adminSession;

    private Rule rule;

    public AdminListTest(final String name) {
        super(name);
    }

    public void testUserHasAccessToOtherUsersRules() throws Exception {
        userClient = getClient();
        userSession = userClient.getSession();

        adminClient = new AJAXClient(User.OXAdmin);
        adminSession = adminClient.getSession();

        // Insert new rule as user
        rule = new Rule();
        rule.setName("testUserHasAccessToOtherUsersRules");
        rule.setActioncmds(new AbstractAction[] { new Keep() });

        final IsComparison isComp = new IsComparison();
        rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue" }));

        final String rid = insertRule(rule, null, userSession);

        // Get rules of user
        final Rule[] userRules = listRules(userSession);

        // Get rules of user as admin
        MailAccountGetRequest getMailAcc = new MailAccountGetRequest(0, false);
        MailAccountGetResponse response = userClient.execute(getMailAcc);
        MailAccountDescription description = response.getAsDescription();
        final String userImapLogin = description.getLogin();

        final Rule[] adminRules = listRulesForUser(adminSession, userImapLogin);


        for (final Rule ur : userRules) {
            boolean foundRule = false;
            inner: for (final Rule ar : adminRules) {
                if (ar.getId().equals(ur.getId())) {
                    foundRule = true;
                    break inner;
                }
            }
            assertTrue("Did not find rule.", foundRule);
        }

        deleteRule(rid, null, userSession);
        adminClient.logout();
    }

}
