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

package com.openexchange.ocp.login;

import static com.openexchange.ocp.OCPConfig.Property.DATABASE_LOGIN_TABLE;
import static com.openexchange.ocp.OCPConfig.Property.HOSTNAME;
import static com.openexchange.ocp.OCPConfig.Property.LOGIN;
import static com.openexchange.ocp.OCPConfig.Property.PASSWORD;
import static com.openexchange.ocp.OCPConfig.Property.PROTOCOL;
import static org.junit.Assert.fail;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ocp.AbstractTestReporting;
import com.openexchange.ocp.OCPConfig;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;

/**
 * {@link TestLoginReporting} - End-to-End test for the OCP Login Reporter.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.12.0
 */
public class TestLoginReporting extends AbstractTestReporting {

    private static final String SQL_TABLE = OCPConfig.getProperty(DATABASE_LOGIN_TABLE);

    /**
     * Initializes a new {@link TestLoginReporting}.
     */
    public TestLoginReporting() {
        super();
    }

    @Override
    protected void postSetup() throws Exception {
        cleanUp(SQL_TABLE);
        super.postSetup();
    }

    /**
     * Performs a login in the {@link OCPConfig.Property#BRAND} and
     * asserts that the event was written to the remote reporting database
     *
     * @throws ApiException if login fails
     * @throws InterruptedException
     * @throws SQLException if an SQL error is occurred
     */
    @Test
    public void testLogin() throws ApiException, InterruptedException {
        String hostname = OCPConfig.getProperty(HOSTNAME, "localhost");
        String protocol = OCPConfig.getProperty(PROTOCOL, "http");
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(protocol + "://" + hostname + "/ajax");
        apiClient.login(OCPConfig.getProperty(LOGIN), OCPConfig.getProperty(PASSWORD));

        try {
            TimeUnit.SECONDS.sleep(5); // Give time to the data to arrive at their end destination...
            getAndAssertReportingEvents(SQL_TABLE, 1);
        } finally {
            cleanUp(SQL_TABLE);
        }
    }

    /**
     * Performs a failed login attempt in the {@link OCPConfig.Property#BRAND} and
     * asserts that no event was written to the remote reporting database.
     *
     * @throws ApiException if login fails
     * @throws SQLException if an SQL error is occurred
     */
    @Test
    public void testFailedLogin() throws ApiException {
        String hostname = OCPConfig.getProperty(HOSTNAME, "localhost");
        String protocol = OCPConfig.getProperty(PROTOCOL, "http");
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(protocol + "://" + hostname + "/ajax");
        try {
            apiClient.login("some-dude", "wrong-password");
        } catch (ApiException e) {
            if (false == e.getMessage().contains("Login failed")) {
                fail("Login error occurred.");
                throw e;
            }
        }

        getAndAssertReportingEvents(SQL_TABLE, 0);
    }
}
