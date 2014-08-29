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

package com.openexchange.ajax.subscribe;

import java.io.IOException;
import java.util.LinkedHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.oauth.actions.AllOAuthAccountRequest;
import com.openexchange.ajax.oauth.actions.AllOAuthAccountResponse;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.RefreshSubscriptionRequest;
import com.openexchange.ajax.subscribe.source.action.GetSourceRequest;
import com.openexchange.ajax.subscribe.source.action.GetSourceResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.test.FolderTestManager;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link SubscribeMSLiveContactsTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SubscribeMSLiveContactsTest extends AbstractAJAXSession {

    private static final String SERVICE_ID = "com.openexchange.oauth.msliveconnect";

    protected static final String CONTACT_SOURCE_ID = "com.openexchange.subscribe.mslive.contact";

    private FolderTestManager folderMgr;

    /**
     * Initializes a new {@link SubscribeMSLiveContactsTest}.
     * 
     * @param name
     */
    public SubscribeMSLiveContactsTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        folderMgr = new FolderTestManager(client);
    }
    
    public void tearDown() throws Exception {
        if (folderMgr != null) {
            folderMgr.cleanUp();
        }
        super.tearDown();
    }

    public void testSubscribe() throws OXException, IOException, JSONException, Exception {
        createSubscription(
            getAccountId(),
            CONTACT_SOURCE_ID,
            FolderObject.CONTACT,
            client.getValues().getPrivateContactFolder(),
            client.getValues().getUserId());
    }

    /**
     * Create a google subscription
     * 
     * @param accountId
     * @param sourceId
     * @param module
     * @param parent
     * @param userId
     * @throws Exception
     */
    private void createSubscription(final int accountId, final String sourceId, final int module, final int parent, final int userId) throws Exception {
        // Get subscription source for calendar
        FolderObject folder = createSubscriptionFolder(sourceId, module, parent, userId);
        final DynamicFormDescription formDescription = createDynamicFormDescription(sourceId);
        final Subscription calendarSubscription = createSubscription(formDescription, sourceId, accountId, folder.getObjectID());

        final NewSubscriptionRequest subReq = new NewSubscriptionRequest(calendarSubscription, formDescription);
        NewSubscriptionResponse subResp = client.execute(subReq);
        int subId = (Integer) subResp.getData();

        final RefreshSubscriptionRequest refreshReq = new RefreshSubscriptionRequest(subId, Integer.toString(folder.getObjectID()));
        client.execute(refreshReq);
    }

    @SuppressWarnings("unchecked")
    private int getAccountId() throws OXException, IOException, JSONException {
        AllOAuthAccountRequest oauthReq = new AllOAuthAccountRequest();
        AllOAuthAccountResponse oauthResp = client.execute(oauthReq);
        Object data = oauthResp.getData();

        if (data instanceof JSONArray) {
            final JSONArray array = (JSONArray) data;
            for (Object o : array.asList()) {
                LinkedHashMap<String, Object> json = (LinkedHashMap<String, Object>) o;
                if (json.get("serviceId").equals(SERVICE_ID)) {
                    return (Integer) json.get("id");
                }
            }
        }
        return 0;
    }

    /**
     * Create a subscription folder
     * 
     * @param name
     * @param module
     * @param parentId
     * @param userId
     * @return
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private FolderObject createSubscriptionFolder(final String name, final int module, final int parentId, final int userId) throws OXException, IOException, JSONException {
        FolderObject object = folderMgr.generatePublicFolder(name, module, parentId, userId);
        folderMgr.insertFolderOnServer(object);
        return object;
    }

    /**
     * Create a DynamicFormDescription object for the specified sourceId.
     * 
     * @param sourceId
     * @return
     * @throws Exception
     */
    private DynamicFormDescription createDynamicFormDescription(final String sourceId) throws Exception {
        GetSourceRequest getSrcReq = new GetSourceRequest(sourceId);
        GetSourceResponse getSrcResp = client.execute(getSrcReq);
        Object data = getSrcResp.getData();
        if (getSrcResp.hasError()) {
            throw new Exception("Returned with errors: " + data);
        }

        final JSONObject json = (JSONObject) data;
        final JSONObject jsonFormDesc = json.getJSONArray("formDescription").getJSONObject(0);

        DynamicFormDescription formDescription = new DynamicFormDescription();
        FormElement fe = FormElement.custom(
            jsonFormDesc.getString("widget"),
            jsonFormDesc.getString("name"),
            jsonFormDesc.getString("displayName"));
        fe.setOption("type", jsonFormDesc.getJSONObject("options").getString("type"));
        fe.setMandatory(jsonFormDesc.getBoolean("mandatory"));
        formDescription.add(fe);
        return formDescription;
    }

    /**
     * Create a Subscription object
     * 
     * @param desc
     * @param sourceId
     * @param accountId
     * @param folderId
     * @return
     */
    @SuppressWarnings("unchecked")
    private Subscription createSubscription(final DynamicFormDescription desc, final String sourceId, final int accountId, final int folderId) {
        SubscriptionSource source = new SubscriptionSource();
        source.setId(sourceId);
        source.setFormDescription(desc);

        Subscription subscription = new Subscription();
        subscription.setSource(source);
        subscription.setFolderId(folderId);
        subscription.setConfiguration(Collections.singletonMap("account", accountId));

        return subscription;
    }

}
