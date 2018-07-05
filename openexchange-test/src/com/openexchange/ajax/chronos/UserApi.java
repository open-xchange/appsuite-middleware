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

package com.openexchange.ajax.chronos;

import java.util.concurrent.TimeUnit;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.LoginResponse;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.FindApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.JSlobApi;
import com.openexchange.testing.httpclient.modules.LoginApi;

/**
 * {@link UserApi}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class UserApi {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserApi.class);

    private String session;
    private Integer calUser;
    private FoldersApi foldersApi;
    private final FindApi findApi;
    private JSlobApi jslob;
    private ChronosApi chronosApi;
    private EnhancedChronosApi enhancedChronosApi;
    private EnhancedApiClient enhancedApiClient;
    private final ApiClient client;
    private final TestUser user;

    private String enhancedSession;
    private int enhancedCalUser;

    /**
     * Initializes a new {@link UserApi}.
     *
     * @param client The normal client
     * @param enhancedApiClient The enhanced client
     * @param user The user
     * @param performLogin Whether to perform login for both clients
     *
     * @throws Exception
     */
    public UserApi(ApiClient client, EnhancedApiClient enhancedApiClient, TestUser user, boolean performLogin) throws Exception {
        this.client = client;
        this.client.setConnectTimeout(java.lang.Math.toIntExact(TimeUnit.MINUTES.toMillis(5)));
        this.enhancedApiClient = enhancedApiClient;
        this.user = user;
        chronosApi = new ChronosApi(client);
        findApi = new FindApi(client);
        jslob = new JSlobApi(client);
        foldersApi = new FoldersApi(client);
        setEnhancedChronosApi(new EnhancedChronosApi(enhancedApiClient));

        if (performLogin) {
            LoginResponse login = login(user.getLogin(), user.getPassword(), client);
            this.calUser = login.getUserId();
            this.session = login.getSession();
            login = login(user.getLogin(), user.getPassword(), enhancedApiClient);
            this.enhancedCalUser = login.getUserId();
            this.enhancedSession = login.getSession();
        } else {
            this.calUser = client.getUserId();
            this.session = client.getSession();
            this.enhancedCalUser = enhancedApiClient.getUserId();
            this.enhancedSession = enhancedApiClient.getSession();
        }
    }

    /**
     *
     * @param login
     * @param password
     * @param client
     * @return
     * @throws Exception
     */
    protected LoginResponse login(String login, String password, ApiClient client) throws Exception {
        LoginResponse doLogin = new LoginApi(client).doLogin(login, password, null, null, null, null, null, null, null);
        if (doLogin.getError() == null) {
            LOG.info("Login succesfull for user " + login);
            return doLogin;
        }
        throw new Exception("Error during login: " + doLogin.getError());
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public String getSession() {
        return session;
    }

    /**
     * Sets the session
     *
     * @param session The session to set
     */
    public void setSession(String session) {
        this.session = session;
    }

    /**
     * Gets the calUser
     *
     * @return The calUser
     */
    public Integer getCalUser() {
        return calUser;
    }

    /**
     * Sets the calUser
     *
     * @param calUser The calUser to set
     */
    public void setCalUser(Integer calUser) {
        this.calUser = calUser;
    }

    /**
     * Gets the foldersApi
     *
     * @return The foldersApi
     */
    public FoldersApi getFoldersApi() {
        return foldersApi;
    }

    /**
     * Sets the foldersApi
     *
     * @param foldersApi The foldersApi to set
     */
    public void setFoldersApi(FoldersApi foldersApi) {
        this.foldersApi = foldersApi;
    }

    /**
     * Gets the jslob
     *
     * @return The jslob
     */
    public JSlobApi getJslob() {
        return jslob;
    }

    /**
     * Sets the jslob
     *
     * @param jslob The jslob to set
     */
    public void setJslob(JSlobApi jslob) {
        this.jslob = jslob;
    }

    /**
     * Gets the api
     *
     * @return The api
     */
    public ChronosApi getChronosApi() {
        return chronosApi;
    }

    /**
     * Sets the api
     *
     * @param api The api to set
     */
    public void setApi(ChronosApi api) {
        this.chronosApi = api;
    }

    /**
     * Gets the client
     *
     * @return The client
     */
    public ApiClient getClient() {
        return client;
    }

    /**
     * Gets the user
     *
     * @return The user
     */
    public TestUser getUser() {
        return user;
    }

    /**
     * Gets the enhancedChronosApi
     *
     * @return The enhancedChronosApi
     */
    public EnhancedChronosApi getEnhancedChronosApi() {
        return enhancedChronosApi;
    }

    /**
     * Returns the findApi
     *
     * @return The findApi
     */
    public FindApi getFindApi() {
        return findApi;
    }

    /**
     * Sets the enhancedChronosApi
     *
     * @param enhancedChronosApi The enhancedChronosApi to set
     */
    public void setEnhancedChronosApi(EnhancedChronosApi enhancedChronosApi) {
        this.enhancedChronosApi = enhancedChronosApi;
    }

    /**
     * Gets the enhancedApiClient
     *
     * @return The enhancedApiClient
     */
    public EnhancedApiClient getEnhancedApiClient() {
        return enhancedApiClient;
    }

    /**
     * Sets the enhancedApiClient
     *
     * @param enhancedApiClient The enhancedApiClient to set
     */
    public void setEnhancedApiClient(EnhancedApiClient enhancedApiClient) {
        this.enhancedApiClient = enhancedApiClient;
    }

    /**
     * Gets the enhancedSession
     *
     * @return The enhancedSession
     */
    public String getEnhancedSession() {
        return enhancedSession;
    }

    /**
     * Sets the enhancedSession
     *
     * @param enhancedSession The enhancedSession to set
     */
    public void setEnhancedSession(String enhancedSession) {
        this.enhancedSession = enhancedSession;
    }

    /**
     * Gets the enhancedCalUser
     *
     * @return The enhancedCalUser
     */
    public int getEnhancedCalUser() {
        return enhancedCalUser;
    }

    /**
     * Sets the enhancedCalUser
     *
     * @param enhancedCalUser The enhancedCalUser to set
     */
    public void setEnhancedCalUser(int enhancedCalUser) {
        this.enhancedCalUser = enhancedCalUser;
    }

}
