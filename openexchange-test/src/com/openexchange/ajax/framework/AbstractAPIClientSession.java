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

package com.openexchange.ajax.framework;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import com.openexchange.exception.Category;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.modules.LoginApi;

/**
 *
 * {@link AbstractAPIClientSession}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public abstract class AbstractAPIClientSession extends AbstractClientSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAPIClientSession.class);

    protected LoginApi loginApi;

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    protected AbstractAPIClientSession() {
        super();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().build();
    }

    /**
     * Get the API client for the test user
     *
     * @return The {@link ApiClient}l
     * @throws ApiException In case client creation failed
     */
    protected ApiClient getApiClient() throws ApiException {
        return testUser.getApiClient();
    }

    /**
     * Returns the session id of the default session
     *
     * @return The session id
     * @throws ApiException In case client creation failed
     */
    protected String getSessionId() throws ApiException {
        return getApiClient().getSession();
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
    protected final ApiClient logoutClient(ApiClient client) {
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
     * @param logging Whether to log an error or not
     * @return <code>null</code> to prepare client for garbage collection
     */
    protected final ApiClient logoutClient(ApiClient client, boolean logging) {
        try {
            if (client != null) {
                client.logout();
                LOG.info("Logout succesfull for user " + client.getUser());
            }
        } catch (Exception e) {
            if (logging) {
                LOG.error("Unable to correctly tear down test setup.", e);
            }
        }
        return null;
    }

    protected void setBasePath(ApiClient newClient) {
        String hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        if (hostname == null) {
            hostname = "localhost";
        }
        String protocol = AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL);
        if (protocol == null) {
            protocol = "http";
        }
        newClient.setBasePath(protocol + "://" + hostname + "/ajax");
    }

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     */
    protected static void checkResponse(CommonResponse response) {
        assertNull(response.getError(), response.getErrorDesc());
    }

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     */
    protected static void checkResponse(String error, String errorDesc) {
        assertNull(errorDesc, error);
    }

    /**
     * Checks if a response doesn't contain any errors.
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @return The data
     */
    protected static <T> T checkResponse(String error, String errorDesc, T data) {
        return checkResponse(error, errorDesc, null, data);
    }

    /**
     * Checks if a response doesn't contain any errors. Errors of category "WARNING" are ignored implicitly.
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param categories The error categories if the response
     * @param data The data element of the response
     * @return The data
     */
    protected static <T> T checkResponse(String error, String errorDesc, String categories, T data) {
        return checkResponse(error, errorDesc, categories, true, data);
    }

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param categories The error categories if the response
     * @param ignoreWarnings <code>true</code> to ignore warnings (as indicated through the categories), <code>false</code>, otherwise
     * @param data The data element of the response
     * @return The data
     */
    protected static <T> T checkResponse(String error, String errorDesc, String categories, boolean ignoreWarnings, T data) {
        if (false == ignoreWarnings || false == Category.EnumType.WARNING.name().equals(categories)) {
            assertNull(errorDesc, error);
        }
        assertNotNull(data);
        return data;
    }

}
