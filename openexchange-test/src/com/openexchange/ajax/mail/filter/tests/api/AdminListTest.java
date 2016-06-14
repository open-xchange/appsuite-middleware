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

import java.rmi.Naming;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.mail.filter.api.MailFilterAPI;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.Keep;
import com.openexchange.ajax.mail.filter.api.dao.comparison.Comparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.mailaccount.MailAccountDescription;

public class AdminListTest extends AbstractMailFilterTest {

    private AJAXClient userClient;

    private AJAXClient adminClient;

    private Rule rule;
    private int rid = -1;

    public AdminListTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        adminClient = new AJAXClient(User.OXAdmin);
        Context ctx = new Context(adminClient.getValues().getContextId());
        ctx.setUserAttribute("config", "com.openexchange.mail.adminMailLoginEnabled", "true");

        Credentials credentials = new Credentials(AJAXConfig.getProperty(Property.OX_ADMIN_MASTER), AJAXConfig.getProperty(Property.OX_ADMIN_MASTER_PWD));
        OXContextInterface ctxInterface = (OXContextInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXContextInterface.RMI_NAME);
        ctxInterface.change(ctx, credentials);

        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(adminClient.getValues().getUserId());
        Set<String> cap = new HashSet<String>(1);
        cap.add("webmail");
        Credentials userCreds = new Credentials(AJAXConfig.getProperty(User.OXAdmin.getLogin()), AJAXConfig.getProperty(User.OXAdmin.getPassword()));
        OXUserInterface usrInterface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        Set<String> emptySet = Collections.emptySet();
        usrInterface.changeCapabilities(new Context(adminClient.getValues().getContextId()), user, cap, emptySet, emptySet, userCreds);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            Context ctx = new Context(adminClient.getValues().getContextId());
            ctx.setUserAttribute("config", "com.openexchange.mail.adminMailLoginEnabled", "false");
            Credentials credentials = new Credentials(AJAXConfig.getProperty(Property.OX_ADMIN_MASTER), AJAXConfig.getProperty(Property.OX_ADMIN_MASTER_PWD));
            OXContextInterface iface = (OXContextInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXContextInterface.RMI_NAME);
            iface.change(ctx, credentials);

            com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(adminClient.getValues().getUserId());
            Set<String> cap = new HashSet<String>(1);
            cap.add("webmail");
            Credentials userCreds = new Credentials(AJAXConfig.getProperty(User.OXAdmin.getLogin()), AJAXConfig.getProperty(User.OXAdmin.getPassword()));
            OXUserInterface usrInterface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
            Set<String> emptySet = Collections.emptySet();
            usrInterface.changeCapabilities(new Context(adminClient.getValues().getContextId()), user, emptySet, cap, emptySet, userCreds);
            if (rid > 0) {
                mailFilterAPI.deleteRule(rid);
            }
            adminClient.logout();
            adminClient = null;
        } finally {
            super.tearDown();
        }
    }

    public void testUserHasAccessToOtherUsersRules() throws Exception {
        userClient = getClient();

        // Insert new rule as user
        rule = new Rule();
        rule.setName("testUserHasAccessToOtherUsersRules");
        rule.setActionCommands(new Action[] { new Keep() });

        final Comparison isComp = new IsComparison();
        rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue" }));

        rid = mailFilterAPI.createRule(rule);

        // Get rules of user
        List<Rule> rules = mailFilterAPI.listRules();

        // Get rules of user as admin
        MailAccountGetRequest getMailAcc = new MailAccountGetRequest(0, false);
        MailAccountGetResponse response = userClient.execute(getMailAcc);
        MailAccountDescription description = response.getAsDescription();
        final String userImapLogin = description.getLogin();

        MailFilterAPI adminFilterAPI = new MailFilterAPI(adminClient);
        List<Rule> adminRules = adminFilterAPI.listRules(userImapLogin);

        for (final Rule ur : rules) {
            boolean foundRule = false;
            inner: for (final Rule ar : adminRules) {
                if (ar.getId() == ur.getId()) {
                    foundRule = true;
                    break inner;
                }
            }
            assertTrue("Did not find rule.", foundRule);
        }
    }

}
