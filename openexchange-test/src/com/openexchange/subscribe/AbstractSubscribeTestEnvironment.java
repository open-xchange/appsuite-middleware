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

package com.openexchange.subscribe;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
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
import com.openexchange.test.FolderTestManager;

/**
 * {@link AbstractSubscribeTestEnvironment}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractSubscribeTestEnvironment extends AbstractAJAXSession {

    protected AJAXClient ajaxClient;

    protected FolderTestManager folderMgr;

    private final Map<String, Integer> testFolders = new HashMap<String, Integer>();

    private final String serviceId;

    private int accountId = -1;

    /**
     * Initializes a new {@link AbstractSubscribeTestEnvironment}.
     *
     * @param serviceId The service identifier
     */
    protected AbstractSubscribeTestEnvironment(final String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Initialize the test environment
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderMgr = new FolderTestManager(ajaxClient);
        initEnvironment();
        createSubscriptions();
    }

    /**
     * If you need any extra initialization, implement this method
     */
    protected abstract void initEnvironment() throws Exception;

    /**
     * Create the subscriptions
     */
    protected abstract void createSubscriptions() throws Exception;

    /**
     * Create a subscription
     *
     * @param accountId
     * @param sourceId
     * @param module
     * @param parent
     * @param userId
     * @throws Exception
     */
    protected void createSubscription(final int accountId, final String sourceId, final int module, final int parent, final int userId) throws Exception {
        // Get subscription source
        FolderObject folder = createSubscriptionFolder(sourceId, module, parent, userId);
        final DynamicFormDescription formDescription = createDynamicFormDescription(sourceId);
        final Subscription subscription = createSubscription(formDescription, sourceId, accountId, folder.getObjectID());

        final NewSubscriptionRequest subReq = new NewSubscriptionRequest(subscription, formDescription);
        NewSubscriptionResponse subResp = ajaxClient.execute(subReq);
        int subId = ((Integer) subResp.getData()).intValue();

        final RefreshSubscriptionRequest refreshReq = new RefreshSubscriptionRequest(subId, Integer.toString(folder.getObjectID()));
        ajaxClient.execute(refreshReq);
        testFolders.put(sourceId, I(folder.getObjectID()));
    }

    /**
     * Returns the account identifier that is bound to the specified service identifier
     *
     * @param serviceID
     * @return
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    protected int getAccountId() throws OXException, IOException, JSONException {
        if (accountId < 0) {
            AllOAuthAccountRequest oauthReq = new AllOAuthAccountRequest();
            AllOAuthAccountResponse oauthResp = ajaxClient.execute(oauthReq);
            Object data = oauthResp.getData();

            if (data instanceof JSONArray) {
                final JSONArray array = (JSONArray) data;
                for (Object o : array.asList()) {
                    LinkedHashMap<String, Object> json = (LinkedHashMap<String, Object>) o;
                    if (json.get("serviceId").equals(serviceId)) {
                        accountId = ((Integer) json.get("id")).intValue();
                    }
                }
            }
        }

        return accountId;
    }

    /**
     * Create a subscription folder
     *
     * @param name
     * @param module
     * @param parentId
     * @param userId
     * @return
     */
    private FolderObject createSubscriptionFolder(final String name, final int module, final int parentId, final int userId) {
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
        GetSourceResponse getSrcResp = ajaxClient.execute(getSrcReq);
        Object data = getSrcResp.getData();
        if (getSrcResp.hasError()) {
            throw new Exception("Returned with errors: " + data);
        }

        final JSONObject json = (JSONObject) data;
        final JSONObject jsonFormDesc = json.getJSONArray("formDescription").getJSONObject(0);

        DynamicFormDescription formDescription = new DynamicFormDescription();
        FormElement fe = FormElement.custom(jsonFormDesc.getString("widget"), jsonFormDesc.getString("name"), jsonFormDesc.getString("displayName"));
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
    private Subscription createSubscription(final DynamicFormDescription desc, final String sourceId, final int accountId, final int folderId) {
        SubscriptionSource source = new SubscriptionSource();
        source.setId(sourceId);
        source.setFormDescription(desc);

        Subscription subscription = new Subscription();
        subscription.setSource(source);
        subscription.setFolderId(folderId);
        subscription.setConfiguration(Collections.singletonMap("account", I(accountId)));

        return subscription;
    }

    public Map<String, Integer> getTestFolders() {
        return testFolders;
    }

}
