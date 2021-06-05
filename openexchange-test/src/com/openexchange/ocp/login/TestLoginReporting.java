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
    }

    @Override
    protected void postTearDown() throws Exception {}

    /**
     * Performs a login in the {@link OCPConfig.Property#BRAND} and
     * asserts that the event was written to the remote reporting database
     *
     * @throws ApiException if login fails
     * @throws InterruptedException if login fails
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
