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

package com.openexchange.ocp.provisioning;

import static com.openexchange.java.Autoboxing.I;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.ocp.OCPConfig;

/**
 * {@link TestSubResellerProvisioningReporting} - End-to-End tests for the OCP Provisioning Reporter.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.12.0
 */
public class TestSubResellerProvisioningReporting extends AbstractTestProvisioningReporting {

    private static final Logger LOG = LoggerFactory.getLogger(TestSubResellerProvisioningReporting.class);

    private Credentials resellerCredentials;
    private User resellerAdmin;

    /**
     * Initializes a new {@link TestSubResellerProvisioningReporting}.
     */
    public TestSubResellerProvisioningReporting() {
        super();
    }

    @Override
    protected void postSetup() throws Exception {
        super.postSetup();
        resellerCredentials = new Credentials(OCPConfig.getProperty(OCPConfig.Property.SUB_BRAND), OCPConfig.getProperty(OCPConfig.Property.SUB_BRAND_ADMIN_PASSWORD));
        resellerAdmin = createUser(resellerCredentials.getLogin(), resellerCredentials.getPassword(), "ContextAdmin", "Context", "Admin", resellerCredentials.getLogin());
    }

    @Override
    protected void postTearDown() throws Exception {
        try {
            ctx.setName(null);
            contextInterface.delete(ctx, resellerCredentials);
        } catch (NoSuchContextException e) {
            LOG.info("Context '{}' does not exist. Probably already cleaned-up.", I(CONTEXT_ID));
        } catch (Exception e) {
            LOG.warn("Could not clean-up test context '{}'", I(CONTEXT_ID), e);
        }
        super.postTearDown();
    }

    ////////////////////////////////////// MAIN BRAND //////////////////////////////

    /**
     * Tests creation/deletion of user. There should be 2 user events total:
     * <ul>
     * <li>User Creation</li>
     * <li>User Deletion</li>
     * </ul>
     */
    @Test
    public void testCreateDeleteUser() throws Exception {
        testCreateDeleteUser(resellerAdmin, resellerCredentials);
    }

    /**
     * Test creation/deletion of context. There should be 2 user events total:
     * <ul>
     * <li>Admin User Creation</li>
     * <li>Admin User Deletion</li>
     * </ul>
     */
    @Test
    public void testCreateDeleteContext() throws Exception {
        testCreateDeleteContext(resellerAdmin, resellerCredentials);
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
     */
    @Test
    public void testCreateUserDeleteContext() throws Exception {
        testCreateUserDeleteContext(resellerAdmin, resellerCredentials);
    }

    /**
     * Tests that a context pre-creation plugin fails and thus no events are emitted.
     */
    @Test
    public void testContextPreCreationPluginFails() {
        testContextPreCreationPluginFails(resellerAdmin, resellerCredentials);
    }

    /**
     * Tests that a context post-creation plugin fails and thus two events are emitted:
     * One for the actual creation and one for the roll-back.
     */
    @Test
    public void testContextPostCreationPluginFails() {
        testContextPostCreationPluginFails(resellerAdmin, resellerCredentials);
    }

    /**
     * Tests that a context deletion plugin fails and thus no delete events are emitted.
     */
    @Test
    public void testContextDeletePluginFails() {
        testContextDeletePluginFails(resellerAdmin, resellerCredentials);
    }

    /**
     * Tests that a user creation plugin fails
     */
    @Test
    public void testCreateUserFails() throws Exception {
        testCreateUserFails(resellerAdmin, resellerCredentials);
    }

    /**
     * Tests that a user deletion plugin fails
     */
    @Test
    public void testDeleteUserFails() throws Exception {
        testDeleteUserFails(resellerAdmin, resellerCredentials);
    }
}
