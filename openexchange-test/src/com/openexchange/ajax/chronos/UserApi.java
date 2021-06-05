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

package com.openexchange.ajax.chronos;

import java.util.concurrent.TimeUnit;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.test.common.tools.client.EnhancedApiClient;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.FindApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.JSlobApi;

/**
 * {@link UserApi}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class UserApi {

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
     */
    public UserApi(ApiClient client, EnhancedApiClient enhancedApiClient, TestUser user) {
        this.client = client;
        this.client.setConnectTimeout(java.lang.Math.toIntExact(TimeUnit.MINUTES.toMillis(5)));
        this.enhancedApiClient = enhancedApiClient;
        this.user = user;
        chronosApi = new ChronosApi(client);
        findApi = new FindApi(client);
        jslob = new JSlobApi(client);
        foldersApi = new FoldersApi(client);
        setEnhancedChronosApi(new EnhancedChronosApi(enhancedApiClient));

        this.calUser = client.getUserId();
        this.session = client.getSession();
        this.enhancedCalUser = enhancedApiClient.getUserId().intValue();
        this.enhancedSession = enhancedApiClient.getSession();
        enhancedApiClient.setApiKey(enhancedSession);
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
