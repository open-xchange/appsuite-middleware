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

package com.openexchange.ajax.framework;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.exception.OXException;
import com.openexchange.test.AttachmentTestManager;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTaskTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.ReminderTestManager;
import com.openexchange.test.ResourceTestManager;
import com.openexchange.test.TaskTestManager;
import com.openexchange.test.TestManager;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;

@RunWith(ConcurrentTestRunner.class)
@Concurrent(count = 5)
public abstract class AbstractAJAXSession {
    
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAJAXSession.class);

    private AJAXClient client;
    private AJAXClient client2;
    protected TestContext testContext;
    protected TestUser admin;
    protected TestUser testUser;
    protected TestUser testUser2;

    protected CalendarTestManager catm;
    protected ContactTestManager cotm;
    protected FolderTestManager ftm;
    protected InfostoreTestManager itm;
    protected TaskTestManager ttm;
    protected ReminderTestManager remTm;
    protected ResourceTestManager resTm;
    protected AttachmentTestManager atm;
    protected MailTestManager mtm;
    protected FolderTaskTestManager fttm;

    private List<TestManager> testManager = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    protected AbstractAJAXSession() {
        super();
    }

    protected final AJAXClient getClient() {
        return client;
    }

    protected final AJAXClient getClient2() {
        return client2;
    }

    /**
     * Gets the client identifier to use when performing a login
     *
     * @return The client identifier or <code>null</code> to use default one (<code>"com.openexchange.ajax.framework.AJAXClient"</code>)
     */
    protected String getClientId() {
        return null;
    }

    @Before
    public void setUp() throws Exception {
        ProvisioningSetup.init();

        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        Assert.assertNotNull("Unable to retrieve a context!", testContext);
        testUser = testContext.acquireUser();
        testUser2 = testContext.acquireUser();
        client = generateClient(testUser);
        client2 = generateClient(testUser2);
        admin = testContext.getAdmin();

        catm = new CalendarTestManager(client);
        testManager.add(catm);
        cotm = new ContactTestManager(client);
        testManager.add(cotm);
        ftm = new FolderTestManager(client);
        testManager.add(ftm);
        itm = new InfostoreTestManager(client);
        testManager.add(itm);
        ttm = new TaskTestManager(client);
        testManager.add(ttm);
        atm = new AttachmentTestManager(client);
        testManager.add(atm);
        mtm = new MailTestManager(client);
        testManager.add(mtm);
        resTm = new ResourceTestManager(client);
        testManager.add(resTm);
        remTm = new ReminderTestManager(client);
        testManager.add(remTm);
        fttm = new FolderTaskTestManager(client, client2);
        testManager.add(fttm);
    }

    @After
    public void tearDown() throws Exception {
        try {
            for (TestManager manager : testManager) {
                if (manager != null) {
                    try {
                        manager.cleanUp();
                    } catch (Exception e) {
                        LOG.error("", e);
                    }
                }
            }            
            client = logoutClient(client, true);
            client2 = logoutClient(client2, true);
        } finally {
            TestContextPool.backContext(testContext);
        }
    }

    public AJAXSession getSession() {
        return client.getSession();
    }
    
    /**
     * Does a logout for the client. Errors won't be logged.
     * Example:
     * <p>
     * <code>
     * client = logoutClient(client);
     * </code>
     * </p>
     * 
     * @param client to logout
     * @return <code>null</code> to prepare client for garbage collection
     */
    protected final AJAXClient logoutClient(AJAXClient client) {
        return logoutClient(client, false);
    }
    
    /**
     * Does a logout for the client.
     * Example:
     * <p>
     * <code>
     * client = logoutClient(client, true);
     * </code>
     * </p>
     * 
     * @param client to logout
     * @param loggin Whether to log an error or not
     * @return <code>null</code> to prepare client for garbage collection
     */
    protected final AJAXClient logoutClient(AJAXClient client, boolean loggin) {
        try {
            if (client != null) {
                client.logout();
            }
        } catch (Exception e) {
            if (loggin) {
                LOG.error("Unable to correctly tear down test setup.", e);
            }
        }
        return null;
    }

    /**
     * Generates a new {@link AJAXClient}. Uses standard client identifier.
     * Generated client needs a <b>logout in tearDown()</b>
     * 
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final AJAXClient generateDefaultClient() throws OXException {
        return generateClient(getClientId());
    }

    /**
     * Generates a new {@link AJAXClient}.
     * Generated client needs a <b>logout in tearDown()</b>
     * 
     * @param client The client identifier to use when performing a login
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final AJAXClient generateClient(String client) throws OXException {
        return generateClient(client, testContext.acquireUser());
    }
    
    /**
     * Generates a new {@link AJAXClient} for the {@link TestUser}.
     * Generated client needs a <b>logout in tearDown()</b>
     * 
     * @param user The {@link TestUser} to create a client for
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final AJAXClient generateClient(TestUser user) throws OXException {
        return generateClient(getClientId(), user);
    }
    
    /**
     * Generates a new {@link AJAXClient} for the {@link TestUser}.
     * Generated client needs a <b>logout in tearDown()</b>
     * 
     * @param client The client identifier to use when performing a login
     * @param user The {@link TestUser} to create a client for
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final AJAXClient generateClient(String client, TestUser user) throws OXException {
        if (null == user) {
            LOG.error("Can only create a client for an valid user");
            throw new OXException();
        }
        AJAXClient newClient;
        try {
            if (null == client || client.isEmpty()) {
                newClient = new AJAXClient(user);
            } else {
                newClient = new AJAXClient(user, client);
            }
        } catch (Exception e) {
            LOG.error("Could not generate new client for user {} in context {} ", user.getUser(), user.getContext());
            throw new OXException();
        }
        return newClient;
    }
}
