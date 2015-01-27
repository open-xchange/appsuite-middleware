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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.infostore.thirdparty;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.infostore.fileaccount.actions.DeleteFileaccountRequest;
import com.openexchange.ajax.infostore.fileaccount.actions.NewFileaccountRequest;
import com.openexchange.ajax.infostore.fileaccount.actions.NewFileaccountResponse;
import com.openexchange.ajax.oauth.actions.DeleteOAuthAccountRequest;
import com.openexchange.ajax.oauth.provider.actions.AuthenticationProvider;
import com.openexchange.ajax.oauth.provider.actions.InitOAuthAccountRequest;
import com.openexchange.ajax.oauth.provider.actions.InitOAuthAccountResponse;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.AbstractSubscribeTestEnvironment;

/**
 * {@link AbstractInfostoreThirdpartyEnvironment}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public abstract class AbstractInfostoreThirdpartyEnvironment {
    protected AJAXClient ajaxClient;

    private final AuthenticationProvider authProvider;

    private int accountId = -1;

    private String filestoreId;

    /**
     * Initializes a new {@link AbstractSubscribeTestEnvironment}.
     *
     * @param authProvider The authentication provider
     */
    protected AbstractInfostoreThirdpartyEnvironment(AuthenticationProvider authProvider) {
        this.authProvider = authProvider;
    }

    /**
     * Initialize the test environment
     *
     * @throws OXException
     */
    public void init() {
        try {
            initAJAXClient();
            initEnvironment(authProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the client
     *
     * @throws OXException
     * @throws JSONException
     * @throws IOException
     */
    private void initAJAXClient() throws OXException, IOException, JSONException {
        ajaxClient = new AJAXClient(User.User1);
    }

    /**
     * Clean up
     * @throws Exception
     */
    public void cleanup() throws Exception {
        deleteFilestorageFor(authProvider);
        deleteOAuthAccount();
        logout();
    }

    /**
     * Logout the client
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    protected void logout() throws OXException, IOException, JSONException {
        if (ajaxClient != null) {
            ajaxClient.logout();
        }
    }

    /**
     * Delete the test oauth account
     *
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    private void deleteOAuthAccount() throws OXException, IOException, JSONException {
        DeleteOAuthAccountRequest req = new DeleteOAuthAccountRequest(accountId);
        ajaxClient.execute(req);
    }

    /**
     * If you need any extra initialization, implement this method
     * @throws Exception
     */
    private void initEnvironment(AuthenticationProvider auth) throws Exception {
        int accountId = initOAuthAccountFor(auth);
        createFilestorageFor(auth, accountId);
    }

    private int initOAuthAccountFor(AuthenticationProvider auth) throws Exception {
        InitOAuthAccountRequest req = new InitOAuthAccountRequest(auth);
        InitOAuthAccountResponse resp = ajaxClient.execute(req);
        JSONObject json = (JSONObject) resp.getData();
        Integer account = (Integer) json.get("accountId");
        this.accountId = account;
        return account;
    }

    private void createFilestorageFor(AuthenticationProvider authProvider, int account) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", String.valueOf(account));
        NewFileaccountRequest nfReq = new NewFileaccountRequest(authProvider.name(), authProvider.getFilestorageService(), jsonObject);
        NewFileaccountResponse nfResp = ajaxClient.execute(nfReq);
        String filestoreId = (String) nfResp.getData();
        this.filestoreId = filestoreId;
    }

    private void deleteFilestorageFor(AuthenticationProvider authProvider) throws Exception {
        DeleteFileaccountRequest delReq = new DeleteFileaccountRequest(authProvider.getFilestorageService(), filestoreId);
        ajaxClient.execute(delReq);
    }
}
