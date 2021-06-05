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

package com.openexchange.test.common.test.pool;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CleanableResourceManager;
import com.openexchange.ajax.framework.ConfigurableResource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.tools.client.EnhancedApiClient;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;

/**
 * {@link TestUser}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TestUser implements Serializable, CleanableResourceManager, ConfigurableResource {

    /** Wrapper object that delays initialization of logger class until needed */
    private static class LoggerHolder {

        static final Logger LOGGER = LoggerFactory.getLogger(TestUser.LoggerHolder.class);
    }

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7616495104780779831L;

    private final String login;

    private final String password;

    private final String user;

    private final String context;

    private final int userId;

    private final int ctxId;

    private final List<AJAXClient> ajaxClients = new LinkedList<>();

    private final List<ApiClient> apiClients = new LinkedList<>();

    private final AtomicBoolean useEnhancedClients = new AtomicBoolean(false);

    /**
     * Initializes a new {@link TestUser} using <code>-1</code> for context
     * and user identifier.
     * 
     * @param user The user name
     * @param context The context of the user
     * @param password The password of the user
     */
    public TestUser(String user, String context, String password) {
        this(user, context, password, I(-1), I(-1));
    }

    /**
     * Initializes a new {@link TestUser}.
     * 
     * @param user The user name
     * @param context The context of the user
     * @param password The password of the user
     * @param userId The user identifier
     * @param ctxId The context identifier
     */
    public TestUser(String user, String context, String password, Integer userId, Integer ctxId) {
        this.user = user;
        this.context = context;
        this.login = user + "@" + context;
        this.password = password;
        this.userId = null == userId ? -1 : userId.intValue();
        this.ctxId = null == ctxId ? -1 : ctxId.intValue();

    }

    /**
     * Get the login name
     * E.g. <code>anton@context1.ox.test</code>
     *
     * @return The login name
     */
    public String getLogin() {
        return login;
    }

    /**
     * gets the password for the user
     *
     * @return The user password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the user name
     * E.g. <code>anton</code>
     *
     * @return The user name
     */
    public String getUser() {
        return user;
    }

    /**
     * Get the context identifier the user belongs to
     *
     * @return The users context
     */
    public String getContext() {
        return context;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context id
     *
     * @return The context id
     */
    public int getContextId() {
        return ctxId;
    }

    /**
     * Gets or creates the standard {@link AJAXClient} for this user
     *
     * @return The {@link AJAXClient}
     * @throws OXException If creation fails
     * @throws IOException If creation fails
     * @throws JSONException If creation fails
     */
    public AJAXClient getAjaxClient() throws OXException, IOException, JSONException {
        if (ajaxClients.isEmpty()) {
            synchronized (ajaxClients) {
                if (ajaxClients.isEmpty()) {
                    ajaxClients.add(new AJAXClient(this));
                }
            }
        }
        return ajaxClients.get(0);
    }

    /**
     * Generates a new {@link AJAXClient} in addition to the standard
     * {@link AJAXClient}. If you want to get "just a client" use
     * {@link #getAjaxClient()}
     * 
     * @param clientName The name of the client or <code>null</code> to use the default
     * @return A new created {@link AJAXClient}
     * @throws OXException If creation fails
     * @throws IOException If creation fails
     * @throws JSONException If creation fails
     */
    public AJAXClient generateAjaxClient(String clientName) throws OXException, IOException, JSONException {
        synchronized (ajaxClients) {
            AJAXClient ajaxClient = Strings.isEmpty(clientName) ? new AJAXClient(this) : new AJAXClient(this, clientName);
            ajaxClients.add(ajaxClient);
            return ajaxClient;
        }
    }

    /**
     * Gets or creates the standard {@link ApiClient} for this user
     *
     * @return The {@link ApiClient}
     * @throws ApiException If creating fails
     */
    public ApiClient getApiClient() throws ApiException {
        if (apiClients.isEmpty()) {
            synchronized (ajaxClients) {
                if (apiClients.isEmpty()) {
                    apiClients.add(configureApiClient());
                }
            }
        }
        return apiClients.get(0);
    }

    /**
     * Generates a new {@link ApiClient} in addition to the standard
     * {@link ApiClient}. If you want to get "just a client" use
     * {@link #getApiClient()}
     * 
     * @return A new created {@link ApiClient}
     * @throws ApiException If generation fails
     */
    public ApiClient generateApiClient() throws ApiException {
        synchronized (apiClients) {
            ApiClient apiClient = configureApiClient();
            apiClients.add(apiClient);
            return apiClient;
        }
    }

    private ApiClient configureApiClient() throws ApiException {
        ApiClient apiClient = useEnhancedClients.get() ? new EnhancedApiClient() : new ApiClient();
        setBasePath(apiClient);
        apiClient.setUserAgent("HTTP API Testing Agent");
        apiClient.login(getLogin(), getPassword());
        return apiClient;
    }

    private void setBasePath(ApiClient newClient) {
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

    @Override
    public void configure(TestClassConfig testConfig) throws Exception {
        if (testConfig.createAjaxClients()) {
            getAjaxClient();
        }
        useEnhancedClients.set(testConfig.useEnhancedApiClients());
        if (testConfig.createApiClients()) {
            getApiClient();
        }
    }

    @Override
    public void cleanUp() {
        for (AJAXClient ajaxClient : ajaxClients) {
            try {
                if (null != ajaxClient.getSession()) {
                    ajaxClient.logout();
                }
            } catch (Exception e) {
                LoggerHolder.LOGGER.info("Unable to logout client", e);
            }
        }
        for (ApiClient apiClient : apiClients) {
            try {
                apiClient.logout();
            } catch (ApiException e) {
                LoggerHolder.LOGGER.info("Unable to logout client", e);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("TestUser [");
        if (Strings.isNotEmpty(login)) {
            builder.append("login=").append(login);
        }
        builder.append("]");
        return builder.toString();
    }

}
