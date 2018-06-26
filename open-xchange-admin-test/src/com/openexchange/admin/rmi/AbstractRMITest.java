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

package com.openexchange.admin.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.factory.UserFactory;
import com.openexchange.admin.rmi.manager.ContextManager;
import com.openexchange.admin.rmi.manager.DatabaseManager;
import com.openexchange.admin.rmi.manager.FilestoreManager;
import com.openexchange.admin.rmi.manager.GroupManager;
import com.openexchange.admin.rmi.manager.MaintenanceReasonManager;
import com.openexchange.admin.rmi.manager.ResellerManager;
import com.openexchange.admin.rmi.manager.ResourceManager;
import com.openexchange.admin.rmi.manager.ServerManager;
import com.openexchange.admin.rmi.manager.TaskManagementManager;
import com.openexchange.admin.rmi.manager.UserManager;
import com.openexchange.admin.rmi.util.AssertUtil;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractRMITest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractRMITest {

    protected static String TEST_DOMAIN = "example.org";
    protected static String change_suffix = "-changed";

    protected Credentials contextAdminCredentials;
    protected Credentials superAdminCredentials;
    protected User superAdmin;
    protected User contextAdmin;

    /**
     * Initializes a new {@link AbstractRMITest}.
     */
    public AbstractRMITest() {
        super();
    }

    /**
     * Initialises the test configuration and creates one context for the tests
     * 
     * @throws Exception if an error occurs during initialisation of the configuration
     */
    @BeforeClass
    public static void setUpEnvironment() throws OXException {
        AJAXConfig.init();
    }

    /**
     * Clean up managers
     */
    @AfterClass
    public static void cleanUpManagers() throws Exception {
        // perform any clean-ups here
        getContextManager().cleanUp();
        getUserManager().cleanUp();
        getGroupManager().cleanUp();
        getResourceManager().cleanUp();
        getResellerManager().cleanUp();
        getDatabaseManager().cleanUp();
        getServerManager().cleanUp();
        getFilestoreManager().cleanUp();
        getMaintenanceReasonManager().cleanUp();
    }

    @Before
    public void setUp() throws Exception {
        contextAdminCredentials = getContextAdminCredentials();
        superAdminCredentials = getMasterAdminCredentials();

        superAdmin = UserFactory.createUser(superAdminCredentials.getLogin(), superAdminCredentials.getPassword(), "ContextCreatingAdmin", "Ad", "Min", "adminmaster@ox.invalid");
        contextAdmin = UserFactory.createUser(contextAdminCredentials.getLogin(), contextAdminCredentials.getPassword(), "ContextAdmin", "Context", "Admin", "contextAdmin@ox.invalid");
    }

    /**
     * Returns the RMI host URL
     * 
     * @return the RMI host URL
     */
    protected static String getRMIHostUrl() {
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
    protected static String getRMIHost() {
        String host = "localhost";

        if (System.getProperty("rmi_test_host") != null) {
            host = System.getProperty("rmi_test_host");
        } else if (AJAXConfig.getProperty(Property.RMI_HOST) != null) {
            host = AJAXConfig.getProperty(Property.RMI_HOST);
        }

        return host;
    }

    /**
     * Returns the master <code>oxadminmaster</code> {@link Credentials}.
     * Looks up the password through the system property <code>rmi_test_masterpw</code>
     * i
     * 
     * @return The <code>oxadminmaster</code> {@link Credentials}
     */
    private static Credentials getMasterAdminCredentials() {
        String mpw = "secret";
        if (System.getProperty("rmi_test_masterpw") != null) {
            mpw = System.getProperty("rmi_test_masterpw");
        }
        return new Credentials("oxadminmaster", mpw);
    }

    /**
     * Returns the context admin's {@link Credentials}
     * 
     * @return the context admin's {@link Credentials}
     */
    private static Credentials getContextAdminCredentials() {
        String oxadmin = AJAXConfig.getProperty(Property.OXADMIN, "oxadmin");
        String contextPassword = AJAXConfig.getProperty(Property.PASSWORD, "secret");
        return new Credentials(oxadmin, contextPassword);
    }

    /**
     * Asserts that the resource was created properly by fetching it and comparing it with the expected one.
     * 
     * @param expected The expected {@link Resource}
     * @param context The {@link Context}
     * @param credentials The admin credentials
     * @throws Exception if an error is occurred
     */
    public void assertResourceWasCreatedProperly(Resource expected, Context context, Credentials credentials) throws Exception {
        Resource lookup = new Resource();
        lookup.setId(expected.getId());
        lookup = getResourceManager().getData(lookup, context, credentials);
        AssertUtil.assertResourceEquals(expected, lookup);
    }

    /**
     * Asserts that the group was created properly by fetching it and comparing it with the expected one.
     * 
     * @param expected The expected {@link Group}
     * @param context The {@link Context}
     * @param credentials The admin credentials
     * @throws Exception if an error is occurred
     */
    public void assertGroupWasCreatedProperly(Group expected, Context context, Credentials credentials) throws Exception {
        Group lookup = new Group();
        lookup.setId(expected.getId());
        lookup = getGroupManager().getData(lookup, context, credentials);
        AssertUtil.assertGroupEquals(expected, lookup);
    }

    /**
     * Asserts that the user was created properly by fetching it and comparing it with the expected one.
     * 
     * @param expected The expected {@link User}
     * @param context The {@link Context}
     * @param credentials The admin credentials
     * @throws Exception if an error is occurred
     */
    public void assertUserWasCreatedProperly(User expected, Context context, Credentials credentials) throws Exception {
        User lookupUser = new User();
        lookupUser.setId(expected.getId());
        lookupUser = getUserManager().getData(context, lookupUser, credentials);
        AssertUtil.assertUserEquals(expected, lookupUser);
    }

    public static String getChangedEmailAddress(String address, String changed) {
        return address.replaceFirst("@", changed + "@");
    }

    /*** Interfaces ***/

    public OXTaskMgmtInterface getTaskInterface() throws MalformedURLException, RemoteException, NotBoundException {
        return (OXTaskMgmtInterface) Naming.lookup(getRMIHostUrl() + OXTaskMgmtInterface.RMI_NAME);
    }

    /*** ANY & friends ***/
    protected interface Verifier<T, S> {

        public boolean verify(T obj1, S obj2);
    }

    public <T, S> boolean any(Collection<T> collection, S searched, Verifier<T, S> verifier) {
        for (T elem : collection) {
            if (verifier.verify(elem, searched)) {
                return true;
            }
        }
        return false;
    }

    public <T, S> boolean any(T[] collection, S searched, Verifier<T, S> verifier) {
        return any(Arrays.asList(collection), searched, verifier);
    }

    /////////////////////////// MANAGERS ///////////////////////////////

    /**
     * Gets the {@link ContextManager}
     * 
     * @return The {@link ContextManager}
     */
    protected static ContextManager getContextManager() {
        return ContextManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Returns the {@link UserManager} instance
     * 
     * @return the {@link UserManager} instance
     */
    protected static UserManager getUserManager() {
        return UserManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Gets the {@link DatabaseManager}
     * 
     * @return the {@link DatabaseManager}
     */
    protected static DatabaseManager getDatabaseManager() {
        return DatabaseManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Gets the {@link ServerManager}
     * 
     * @return the {@link ServerManager}
     */
    protected static ServerManager getServerManager() {
        return ServerManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Gets the {@link MaintenanceReasonManager}
     * 
     * @return the {@link MaintenanceReasonManager}
     */
    protected static MaintenanceReasonManager getMaintenanceReasonManager() {
        return MaintenanceReasonManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Gets the {@link GroupManager}
     * 
     * @return the {@link GroupManager}
     */
    protected static GroupManager getGroupManager() {
        return GroupManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Gets the {@link ResourceManager}
     * 
     * @return the {@link ResourceManager}
     */
    protected static ResourceManager getResourceManager() {
        return ResourceManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Gets the {@link FilestoreManager}
     * 
     * @return the {@link FilestoreManager}
     */
    protected static FilestoreManager getFilestoreManager() {
        return FilestoreManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Gets the {@link ResellerManager}
     * 
     * @return the {@link ResellerManager}
     */
    protected static ResellerManager getResellerManager() {
        return ResellerManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }

    /**
     * Gets the {@link TaskManagementManager}
     * 
     * @return the {@link TaskManagementManager}
     */
    protected static TaskManagementManager getTaskManagementManager() {
        return TaskManagementManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }
}
