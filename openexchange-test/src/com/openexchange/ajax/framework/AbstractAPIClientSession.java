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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentHashSet;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
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
    protected ApiClient apiClient;

    private Set<ApiClient> apiClients = new ConcurrentHashSet<>(1);

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    protected AbstractAPIClientSession() {
        super();
    }

    protected ApiClient getApiClient() {
        return apiClient;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        apiClient = generateApiClient(testUser);
        rememberClient(apiClient);
    }

    protected void rememberClient(ApiClient client) {
        apiClients.add(client);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            Iterator<ApiClient> iterator = apiClients.iterator();
            while (iterator.hasNext()) {
                ApiClient client = iterator.next();
                if (client.getSession() != null) {
                    logoutClient(client, true);
                }
                iterator.remove();
            }
        } finally {
            super.tearDown();
        }
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

    /**
     * Generates a new {@link AJAXClient}. Uses standard client identifier.
     * Generated client needs a <b>logout in tearDown()</b>
     *
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final ApiClient generateDefaultApiClient() throws OXException {
        return generateApiClient(getClientId());
    }

    /**
     * Generates a new {@link AJAXClient}.
     * Generated client needs a <b>logout in tearDown()</b>
     *
     * @param client The client identifier to use when performing a login
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final ApiClient generateApiClient(String client) throws OXException {
        return generateApiClient(client, testContext.acquireUser());
    }

    /**
     * Generates a new {@link AJAXClient} for the {@link TestUser}.
     * Generated client needs a <b>logout in tearDown()</b>
     *
     * @param user The {@link TestUser} to create a client for
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final ApiClient generateApiClient(TestUser user) throws OXException {
        return generateApiClient(getClientId(), user);
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
    protected final ApiClient generateApiClient(String client, TestUser user) throws OXException {
        if (null == user) {
            LOG.error("Can only create a client for an valid user");
            throw new OXException();
        }
        ApiClient newClient;
        try {
            newClient = new ApiClient();
            setBasePath(newClient);
            newClient.setUserAgent("ox-test-client");
            newClient.login(user.getLogin(), user.getPassword());
        } catch (Exception e) {
            LOG.error("Could not generate new client for user {} in context {} ", user.getUser(), user.getContext());
            throw new OXException();
        }
        return newClient;
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
     * @param data The data element of the response
     * @return The data
     */
    protected <T> T checkResponse(String error, String errorDesc, T data) {
        assertNull(errorDesc, error);
        assertNotNull(data);
        return data;
    }
}
