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
            LOG.info("Context '{}' does not exist. Probably already cleaned-up.", CONTEXT_ID);
        } catch (Exception e) {
            LOG.warn("Could not clean-up test context '{}'", CONTEXT_ID, e);
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
    public void testContextPreCreationPluginFails() throws Exception {
        testContextPreCreationPluginFails(resellerAdmin, resellerCredentials);
    }

    /**
     * Tests that a context post-creation plugin fails and thus two events are emitted:
     * One for the actual creation and one for the roll-back.
     */
    @Test
    public void testContextPostCreationPluginFails() throws Exception {
        testContextPostCreationPluginFails(resellerAdmin, resellerCredentials);
    }

    /**
     * Tests that a context deletion plugin fails and thus no delete events are emitted.
     */
    @Test
    public void testContextDeletePluginFails() throws Exception {
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
