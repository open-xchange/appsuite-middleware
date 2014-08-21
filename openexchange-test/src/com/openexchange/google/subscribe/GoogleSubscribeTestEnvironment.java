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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.google.subscribe;

import java.io.IOException;
import java.util.LinkedHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.oauth.actions.AllOAuthAccountRequest;
import com.openexchange.ajax.oauth.actions.AllOAuthAccountResponse;
import com.openexchange.ajax.oauth.actions.GetOAuthServiceRequest;
import com.openexchange.ajax.oauth.actions.InitOAuthAccountRequest;
import com.openexchange.ajax.oauth.actions.InitOAuthAccountResponse;
import com.openexchange.ajax.oauth.actions.OAuthServicesResponse;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.RefreshSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.RefreshSubscriptionResponse;
import com.openexchange.ajax.subscribe.source.action.GetSourceRequest;
import com.openexchange.ajax.subscribe.source.action.GetSourceResponse;
import com.openexchange.configuration.GoogleConfig;
import com.openexchange.configuration.GoogleConfig.Property;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.test.FolderTestManager;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link GoogleSubscribeTestEnvironment}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleSubscribeTestEnvironment {

    private static final GoogleSubscribeTestEnvironment INSTANCE = new GoogleSubscribeTestEnvironment();

    private static final String SERVICE_ID = "com.openexchange.oauth.google";

    private static final String ACCOUNT_NAME = "My Google account";

    private AJAXClient ajaxClient;

    private GoogleOAuthClient oauthClient;
    
    private FolderTestManager folderMgr;

    /**
     * Get the instance of the environment
     * 
     * @return the instance
     */
    public static final GoogleSubscribeTestEnvironment getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------- //

    /**
     * Initialize the test environment
     * 
     * @throws OXException
     */
    public void init() {
        try {
            GoogleConfig.init();
            initAJAXClient();
            initManagers();
            initGoogleOAuthClient();
            initGoogleOAuthAccount();
            createGoogleSubscription();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clean up
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void cleanup() throws OXException, IOException, JSONException {
        logout();
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
    
    private void initManagers() {
        folderMgr = new FolderTestManager(ajaxClient);
    }

    /**
     * Initialize the google oauth client and perform a login
     * 
     * @throws Exception
     */
    private void initGoogleOAuthClient() throws Exception {
        oauthClient = new GoogleOAuthClient();
        oauthClient.login(GoogleConfig.getProperty(Property.EMAIL), GoogleConfig.getProperty(Property.PASSWORD));
    }

    /**
     * Initialize the google oauth account
     * 
     * @throws Exception
     */
    private void initGoogleOAuthAccount() throws Exception {
        final InitOAuthAccountRequest req = new InitOAuthAccountRequest(SERVICE_ID, ACCOUNT_NAME, true);
        final InitOAuthAccountResponse response = ajaxClient.execute(req);
        final Object data = response.getData();
        if (data instanceof JSONObject) {
            final JSONObject j = (JSONObject) data;
            oauthClient.requestCallback(j.getString("authUrl"));
        } else {
            throw new Exception("Invalid response body: " + data);
        }
    }

    /**
     * Create a google subscription for the current user
     * 
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    @SuppressWarnings("unchecked")
    private void createGoogleSubscription() throws Exception {
        // Create new folder for the subscription
        FolderObject folder = folderMgr.generatePublicFolder("subscriptionTestForGoogle", FolderObject.CALENDAR, ajaxClient.getValues().getPrivateContactFolder(), ajaxClient.getValues().getUserId());
        folderMgr.insertFolderOnServer(folder);
        
        // Get account id
        AllOAuthAccountRequest oauthReq = new AllOAuthAccountRequest();
        AllOAuthAccountResponse oauthResp = ajaxClient.execute(oauthReq);
        Object data = oauthResp.getData();

        int accountId = 0;
        if (data instanceof JSONArray) {
            final JSONArray array = (JSONArray) data;
            for (Object o : array.asList()) {
                LinkedHashMap<String, Object> json = (LinkedHashMap<String, Object>) o;
                if (json.get("serviceId").equals(SERVICE_ID)) {
                    accountId = (Integer) json.get("id");
                }
            }
        }
        
        final String sourceId = "com.openexchange.subscribe.google.calendar";
        GetSourceRequest getSrcReq = new GetSourceRequest(sourceId);
        GetSourceResponse getSrcResp = ajaxClient.execute(getSrcReq);
        data = getSrcResp.getData();
        if (data instanceof JSONObject) {
            final JSONObject json = (JSONObject) data;
            json.getString("id");
            final JSONObject jsonFormDesc = json.getJSONArray("formDescription").getJSONObject(0);
            DynamicFormDescription formDescription = new DynamicFormDescription();
            FormElement fe = FormElement.custom(
                jsonFormDesc.getString("widget"),
                jsonFormDesc.getString("name"),
                jsonFormDesc.getString("displayName"));
            fe.setOption("type", jsonFormDesc.getJSONObject("options").getString("type"));
            fe.setMandatory(jsonFormDesc.getBoolean("mandatory"));
            formDescription.add(fe);

            SubscriptionSource source = new SubscriptionSource();
            source.setId(sourceId);
            source.setFormDescription(formDescription);

            Subscription subscription = new Subscription();
            subscription.setSource(source);
            subscription.setFolderId(folder.getObjectID());
            subscription.setConfiguration(Collections.singletonMap("account", accountId));

            final NewSubscriptionRequest subReq = new NewSubscriptionRequest(subscription, formDescription);
            NewSubscriptionResponse subResp = ajaxClient.execute(subReq);
            int subId = (Integer) subResp.getData();
            
            final RefreshSubscriptionRequest refreshReq = new RefreshSubscriptionRequest(subId, Integer.toString(folder.getObjectID()));
            ajaxClient.execute(refreshReq);
        }
    }

    /**
     * Logout the client
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private void logout() throws OXException, IOException, JSONException {
        if (ajaxClient != null) {
            ajaxClient.logout();
        }
    }

}
