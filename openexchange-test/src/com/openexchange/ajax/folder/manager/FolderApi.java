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

package com.openexchange.ajax.folder.manager;

import java.util.HashMap;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.LoginResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.LoginApi;

/**
 * {@link FolderApi}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class FolderApi {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderApi.class);

    private String session;
    private FoldersApi foldersApi;
    private final ApiClient client;
    private final TestUser user;
    private final HashMap<String, Object> rampup;
    private Integer infostoreFolder = null;

    @SuppressWarnings("unchecked")
    public FolderApi(ApiClient client, TestUser user) throws Exception {
        this.client = client;
        this.user = user;
        foldersApi = new FoldersApi(client);

        LoginResponse login = login(user.getLogin(), user.getPassword(), client);
        this.session = login.getSession();
        rampup = (HashMap<String, Object>) login.getRampup();
    }

    @SuppressWarnings("unchecked")
    public Integer getInfostoreFolder() {
        if (infostoreFolder == null) {
            infostoreFolder = (Integer) ((HashMap<String, Object>) ((HashMap<String, Object>) ((HashMap<String, Object>) ((HashMap<String, Object>) rampup.get("jslobs")).get("io.ox/core")).get("tree")).get("folder")).get("infostore");
        }
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
        LoginResponse doLogin = new LoginApi(client).doLogin(login, password, true, null, null, null, null, null, null);
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
