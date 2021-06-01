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

package com.openexchange.ajax.folder.manager;

import org.json.JSONObject;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.LoginResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.JSlobApi;
import com.openexchange.testing.httpclient.modules.LoginApi;

/**
 * {@link FolderApi}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class FolderApi {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderApi.class);

    private FoldersApi foldersApi;
    private final ApiClient client;
    private final TestUser user;
    private String infostoreFolder = null;

    public FolderApi(ApiClient client, TestUser user) throws Exception {
        this.client = client;
        this.user = user;
        foldersApi = new FoldersApi(client);
        JSlobApi jslobApi = new JSlobApi(client);
        JSONObject result = new JSONObject(jslobApi.getJSlob("io.ox/core", "folder/infostore", null));
        infostoreFolder = String.valueOf(result.get("data"));
    }

    public String getInfostoreFolder() {
        return infostoreFolder;
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
        LoginResponse doLogin = new LoginApi(client).doLogin(login, password, null, "true", null, null, null, null, null, null, null);
        if (doLogin.getError() == null) {
            LOG.info("Login succesfull for user " + login);
            return doLogin;
        }
        throw new Exception("Error during login: " + doLogin.getError());
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

}
