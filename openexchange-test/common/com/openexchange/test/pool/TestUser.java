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

package com.openexchange.test.pool;

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
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.tools.client.EnhancedApiClient;

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
    public void cleanUp() throws ApiException {
        for (AJAXClient ajaxClient : ajaxClients) {
            try {
                ajaxClient.logout();
            } catch (Exception e) {
                LoggerHolder.LOGGER.info("Unable to logout client", e);
            }
        }
        for (ApiClient apiClient : apiClients) {
            apiClient.logout();
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
