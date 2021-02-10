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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ocp.provisioning;

import static com.openexchange.ocp.OCPConfig.Property.DATABASE_USER_DELTA_TABLE;
import static org.junit.Assert.fail;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.ocp.AbstractTestReporting;
import com.openexchange.ocp.DatabaseReportingEvent;
import com.openexchange.ocp.OCPConfig;
import com.openexchange.ocp.OCPConfig.Property;

/**
 * {@link AbstractTestProvisioningReporting}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.12.0
 */
abstract class AbstractTestProvisioningReporting extends AbstractTestReporting {

    static final String SQL_TABLE = OCPConfig.getProperty(DATABASE_USER_DELTA_TABLE);
    static final int CONTEXT_ID = 1138;

    OXContextInterface contextInterface;
    OXUserInterface userInterface;

    private String rmiEndPointURL;
    Context ctx;

    /**
     * Initializes a new {@link AbstractTestProvisioningReporting}.
     */
    public AbstractTestProvisioningReporting() {
        super();
    }

    @Override
    protected void postSetup() throws Exception {
        // Always start fresh
        cleanUp(SQL_TABLE);
        // Init RMI stuff
        rmiEndPointURL = getRMIHostUrl();
        contextInterface = getRemoteInterface(OXContextInterface.RMI_NAME, OXContextInterface.class);
        userInterface = getRemoteInterface(OXUserInterface.RMI_NAME, OXUserInterface.class);

        // Init ctx
        ctx = new Context(CONTEXT_ID);
        ctx.setMaxQuota(1000L);
    }

    @Override
    protected void postTearDown() throws Exception {
        cleanUp(SQL_TABLE);
    }

    /////////////////////////////////// Test Helpers //////////////////////////////// 

    /**
     * Test creation/deletion of context. There should be 2 user events total:
     * <ul>
     * <li>Admin User Creation</li>
     * <li>Admin User Deletion</li>
     * </ul>
     *
     * @param admin The admin user
     * @param credentials The admin user's credentials
     * @throws Exception If an error is occurred
     */
    void testCreateDeleteContext(User admin, Credentials credentials) throws Exception {
        contextInterface.create(ctx, admin, credentials);
        TimeUnit.SECONDS.sleep(1);

        List<DatabaseReportingEvent> events = getAndAssertReportingEvents(SQL_TABLE, 1);
        assertEvent(events.get(0), CONTEXT_ID, 1, admin.getName());

        contextInterface.delete(ctx, credentials);
        TimeUnit.SECONDS.sleep(1);

        events = getAndAssertReportingEvents(SQL_TABLE, 2);
        assertEvent(events.get(1), CONTEXT_ID, 0, admin.getName());
    }

    /**
     * Tests creation/deletion of user. There should be 2 user events total:
     * <ul>
     * <li>User Creation</li>
     * <li>User Deletion</li>
     * </ul>
     *
     * @param admin The context admin
     * @param credentials The admin's credentials
     * @throws Exception if an error is occurred
     */
    void testCreateDeleteUser(User admin, Credentials credentials) throws Exception {
        contextInterface.create(ctx, admin, credentials);

        User usr = createUser();
        userInterface.create(ctx, usr, credentials);
        TimeUnit.SECONDS.sleep(1);

        List<DatabaseReportingEvent> events = getAndAssertReportingEvents(SQL_TABLE, 2);
        assertEvent(events.get(0), CONTEXT_ID, 1, admin.getName());
        assertEvent(events.get(1), CONTEXT_ID, 1, admin.getName());

        userInterface.delete(ctx, usr, null, credentials);
        TimeUnit.SECONDS.sleep(1);

        events = getAndAssertReportingEvents(SQL_TABLE, 3);
        assertEvent(events.get(2), CONTEXT_ID, 0, admin.getName());

        contextInterface.delete(ctx, credentials);
    }

    /**
     * Tests context creation, user creation and implicit deletion by
     * context deletion. There should be 8 events in total in the following order:
     * 
     * <ul>
     * <li>Admin User Creation</li>
     * <li>Normal User Creation</li>
     * <li>Normal User Creation</li>
     * <li>Normal User Creation</li>
     * <li>Normal User Deletion</li>
     * <li>Normal User Deletion</li>
     * <li>Normal User Deletion</li>
     * <li>Admin User Deletion</li>
     * </ul>
     * 
     * @throws Exception
     */
    void testCreateUserDeleteContext(User admin, Credentials credentials) throws Exception {
        contextInterface.create(ctx, admin, credentials);

        // Create first user
        User usr = createUser();
        userInterface.create(ctx, usr, credentials);
        TimeUnit.SECONDS.sleep(1);

        List<DatabaseReportingEvent> events = getAndAssertReportingEvents(SQL_TABLE, 2);
        assertEvent(events.get(0), CONTEXT_ID, 1, admin.getName());
        assertEvent(events.get(1), CONTEXT_ID, 1, admin.getName());

        // Create second user
        usr = createUser();
        userInterface.create(ctx, usr, credentials);
        TimeUnit.SECONDS.sleep(1);

        events = getAndAssertReportingEvents(SQL_TABLE, 3);
        assertEvent(events.get(2), CONTEXT_ID, 1, admin.getName());

        // Create third user
        usr = createUser();
        userInterface.create(ctx, usr, credentials);
        TimeUnit.SECONDS.sleep(1);

        events = getAndAssertReportingEvents(SQL_TABLE, 4);
        assertEvent(events.get(3), CONTEXT_ID, 1, admin.getName());

        // Delete context
        contextInterface.delete(ctx, credentials);
        TimeUnit.SECONDS.sleep(1);

        events = getAndAssertReportingEvents(SQL_TABLE, 8);
        for (int i = 4; i < events.size(); i++) {
            assertEvent(events.get(i), CONTEXT_ID, 0, admin.getName());
        }
    }

    /**
     * Tests that a context pre-creation plugin fails and thus no events are emitted.
     */
    void testContextPreCreationPluginFails(User admin, Credentials credentials) throws Exception {
        ctx.setName("preCreateFail");
        try {
            contextInterface.create(ctx, admin, credentials);
        } catch (StorageException e) {
            if (false == e.getMessage().contains("Pre-creation of context")) {
                fail("Unexpected storage error occurred: " + e.getMessage());
            }
        } catch (Exception e) {
            fail("Unexpected error occurred: " + e.getMessage());
        }

        getAndAssertReportingEvents(SQL_TABLE, 0);
    }

    /**
     * Tests that a context post-creation plugin fails and thus two events are emitted:
     * One for the actual creation and one for the roll-back.
     */
    void testContextPostCreationPluginFails(User admin, Credentials credentials) throws Exception {
        ctx.setName("postCreateFail");
        try {
            contextInterface.create(ctx, admin, credentials);
            TimeUnit.SECONDS.sleep(1);
        } catch (StorageException e) {
            if (false == e.getMessage().contains("Post-creation of context")) {
                fail("Unexpected storage error occurred: " + e.getMessage());
            }
        } catch (Exception e) {
            fail("Unexpected error occurred: " + e.getMessage());
        }

        List<DatabaseReportingEvent> events = getAndAssertReportingEvents(SQL_TABLE, 2);
        // There should be two events: one for creation and one for deletion
        assertEvent(events.get(0), CONTEXT_ID, 1, admin.getName());
        assertEvent(events.get(1), CONTEXT_ID, 0, admin.getName());
    }

    /**
     * Tests that a context deletion plugin fails and thus no delete events are emitted.
     */
    void testContextDeletePluginFails(User admin, Credentials credentials) throws Exception {
        try {
            contextInterface.create(ctx, admin, credentials);
            TimeUnit.SECONDS.sleep(1);
            ctx.setName("deleteFail");
            contextInterface.delete(ctx, credentials);
        } catch (StorageException e) {
            if (false == e.getMessage().contains("Deletion of context")) {
                fail("Unexpected storage error occurred: " + e.getMessage());
            }
        } catch (Exception e) {
            fail("Unexpected error occurred: " + e.getMessage());
        }

        List<DatabaseReportingEvent> events = getAndAssertReportingEvents(SQL_TABLE, 3);
        // There should be three events: one for creation and one for deletion and the roll-back
        assertEvent(events.get(0), CONTEXT_ID, 1, admin.getName());
        assertEvent(events.get(1), CONTEXT_ID, 0, admin.getName());
        assertEvent(events.get(2), CONTEXT_ID, 1, admin.getName());
    }

    /**
     * Tests that a user creation plugin fails
     */
    void testCreateUserFails(User admin, Credentials credentials) throws Exception {
        contextInterface.create(ctx, admin, credentials);

        User u = createUser();
        u.setNickname("CreateFail");
        try {
            userInterface.create(ctx, u, credentials);
        } catch (StorageException e) {
            if (false == e.getMessage().contains("User creation failed.")) {
                fail("Unexpected storage error occurred: " + e.getMessage());
            }
        }

        List<DatabaseReportingEvent> events = getAndAssertReportingEvents(SQL_TABLE, 3);
        assertEvent(events.get(0), CONTEXT_ID, 1, admin.getName());
        assertEvent(events.get(1), CONTEXT_ID, 1, admin.getName());
        assertEvent(events.get(2), CONTEXT_ID, 0, admin.getName());
    }

    /**
     * Tests that a user deletion plugin fails
     */
    void testDeleteUserFails(User admin, Credentials credentials) throws Exception {
        contextInterface.create(ctx, admin, credentials);

        User u = createUser();
        u.setNickname("DeleteFail");
        userInterface.create(ctx, u, credentials);
        try {
            userInterface.delete(ctx, u, null, credentials);
        } catch (StorageException e) {
            if (false == e.getMessage().contains("User deletion failed.")) {
                fail("Unexpected storage error occurred: " + e.getMessage());
            }
        }

        TimeUnit.SECONDS.sleep(1);
        List<DatabaseReportingEvent> events = getAndAssertReportingEvents(SQL_TABLE, 3);
        assertEvent(events.get(0), CONTEXT_ID, 1, admin.getName());
        assertEvent(events.get(1), CONTEXT_ID, 1, admin.getName());
        assertEvent(events.get(2), CONTEXT_ID, 0, admin.getName());
    }

    ////////////////////////////////////RMI Helpers ////////////////////////////

    /**
     * Creates a user object
     *
     * @return The new user object
     */
    User createUser() {
        String random = new Long(System.currentTimeMillis()).toString();
        User oxuser = new User();
        oxuser.setName("oxuser" + random);
        oxuser.setDisplay_name("oxuser" + random);
        oxuser.setGiven_name("oxuser" + random);
        oxuser.setSur_name("oxuser" + random);
        oxuser.setPrimaryEmail("oxuser" + random + "@example.com");
        oxuser.setEmail1("oxuser" + random + "@example.com");
        oxuser.setPassword("secret");
        return oxuser;
    }

    /**
     * Creates a new user object
     */
    User createUser(String name, String passwd, String displayName, String givenName, String surname, String email) {
        User user = new User();
        user.setName(name);
        user.setPassword(passwd);
        user.setDisplay_name(displayName);
        user.setGiven_name(givenName);
        user.setSur_name(surname);
        user.setPrimaryEmail(email);
        user.setEmail1(email);
        return user;
    }

    /**
     * Returns the RMI host URL
     * 
     * @return the RMI host URL
     */
    String getRMIHostUrl() {
        String host = getRMIHost();

        if (!host.startsWith("rmi://")) {
            host = "rmi://" + host;
        }
        if (!host.endsWith("/")) {
            host += "/";
        }
        return host;
    }

    /**
     * Returns the RMI host name. It first looks up the <code>rmi_test_host</code>
     * system property and then the {@link Property#RMI_HOST} via the {@link AJAXConfig}
     * 
     * @return The RMI host name.
     */
    String getRMIHost() {
        String host = "localhost";

        if (System.getProperty("rmi_test_host") != null) {
            host = System.getProperty("rmi_test_host");
        } else if (OCPConfig.getProperty(Property.HOSTNAME) != null) {
            host = OCPConfig.getProperty(Property.HOSTNAME);
        }

        return host;
    }

    /**
     * Returns the {@link Remote} interface with the specified rmi name
     * 
     * @param rmiName The rmi name of the {@link Remote} interface
     * @return The {@link Remote} interface
     * @throws Exception if an error is occurred during RMI look-up
     */
    <T extends Remote> T getRemoteInterface(String rmiName, Class<T> clazz) throws Exception {
        return clazz.cast(Naming.lookup(rmiEndPointURL + rmiName));
    }
}
