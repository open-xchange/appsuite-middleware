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

package com.openexchange.passwordchange.history;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.fail;
import java.rmi.Naming;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.data.conversion.ical.Assert;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.TestServiceRegistry;
import com.openexchange.passwordchange.history.registry.PasswordChangeTrackerRegistry;
import com.openexchange.passwordchange.history.tracker.PasswordChangeInfo;
import com.openexchange.passwordchange.history.tracker.PasswordChangeTracker;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;

/**
 * {@link PasswordChangeHistoryTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeHistoryTest {

    private static final String SYMBOLIC_NAME = "RuntimeTracker";
    private static final String RMI_HOST = "rmi://localhost:1099/";
    private static final String CHANGED_PASSWORD = "changed";

    private PasswordChangeTrackerRegistry registry;
    private PasswordChangeTracker tracker;

    @BeforeClass
    public static void startServices() throws Exception {
        Init.startServer();
        Init.startAndInjectPasswordChangeHistoryService();
    }

    @Before
    public void setUp() throws Exception {
        registry = TestServiceRegistry.getInstance().getService(PasswordChangeTrackerRegistry.class, true);
        ConfigViewFactory casscade = TestServiceRegistry.getInstance().getService(ConfigViewFactory.class, true);
        ConfigView view = casscade.getView();
        view.set("server", "com.openexchange.passwordchange.history", true);
        view.set("server", "com.openexchange.passwordchange.limit", 10);
        view.set("server", "com.openexchange.passwordchange.tracker", SYMBOLIC_NAME);
        tracker = new RuntimeTracker();

        registry.register(SYMBOLIC_NAME, tracker);
    }

    @After
    public void tearDown() throws Exception {
        if (null != registry) {
            registry.unregister(SYMBOLIC_NAME);
        }
    }

    @Test
    public void testChange() throws Exception {

        OXContextInterface oxctx = null;
        oxctx = (OXContextInterface) Naming.lookup(RMI_HOST + OXContextInterface.RMI_NAME);

        Context[] all = oxctx.listAll(getMasterCredentials());
        Assert.assertThat(all.length, greaterThan(0));
        Context context = all[0];

        User[] users = null;
        User changedUser = null;
        OXUserInterface oxuser = null;

        oxuser = (OXUserInterface) Naming.lookup(RMI_HOST + OXUserInterface.RMI_NAME);

        users = oxuser.listAll(context, getContextAdminCredentials());
        Assert.assertThat(users.length, greaterThan(1));

        // Change password
        changedUser = users[1];
        changedUser.setPassword(CHANGED_PASSWORD);

        oxuser.change(context, changedUser, getContextAdminCredentials());

        // Lookup if change was recorded
        PasswordChangeTracker pTracker = registry.getTracker(SYMBOLIC_NAME);
        Assert.assertThat("Tracker registration was not successful", pTracker, is(notNullValue()));
        List<PasswordChangeInfo> pList = pTracker.listPasswordChanges(changedUser.getId(), context.getId());
        Assert.assertThat("Error while getting password change histroy", pList, is(notNullValue()));
        Assert.assertThat("No entries recorded", pList.isEmpty(), is(false));
        Assert.assertThat("Tracker should have only recorded one change!", pList.size(), is(1));

        changedUser.setPassword("secret");
        oxuser.change(context, changedUser, getContextAdminCredentials());
    }

    private Credentials getContextAdminCredentials() {
        return new Credentials("oxadmin", "secret");
    }

    private Credentials getMasterCredentials() {
        Credentials credentials = new Credentials();
        credentials.setLogin("oxadminmaster");
        credentials.setPassword("secret");

        return credentials;
    }
}
