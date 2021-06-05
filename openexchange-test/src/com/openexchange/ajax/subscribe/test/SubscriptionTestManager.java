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

package com.openexchange.ajax.subscribe.test;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.subscribe.actions.AbstractSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.AllSubscriptionsRequest;
import com.openexchange.ajax.subscribe.actions.AllSubscriptionsResponse;
import com.openexchange.ajax.subscribe.actions.DeleteSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.DeleteSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.GetSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.GetSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.ListSubscriptionsRequest;
import com.openexchange.ajax.subscribe.actions.ListSubscriptionsResponse;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.RefreshSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.RefreshSubscriptionResponse;
import com.openexchange.ajax.subscribe.actions.UpdateSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.UpdateSubscriptionResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;

/**
 * {@link SubscriptionTestManager}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class SubscriptionTestManager {

    private AbstractSubscriptionResponse lastResponse;

    private final Set<Integer> createdItems;

    private boolean failOnError;

    private AJAXClient client;

    private DynamicFormDescription formDescription;

    private SubscriptionSourceDiscoveryService subscriptionSourceRecoveryService;

    public AbstractSubscriptionResponse getLastResponse() {
        return lastResponse;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean getFailOnError() {
        return failOnError;
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    public void setFormDescription(DynamicFormDescription formDescription) {
        this.formDescription = formDescription;
    }

    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    public void setSubscriptionSourceDiscoveryService(SubscriptionSourceDiscoveryService service) {
        this.subscriptionSourceRecoveryService = service;
    }

    public SubscriptionSourceDiscoveryService getSubscriptionSourceRecoveryService() {
        return subscriptionSourceRecoveryService;
    }

    public SubscriptionTestManager() {
        createdItems = new HashSet<Integer>();
    }

    public SubscriptionTestManager(AJAXClient client) {
        this();
        setClient(client);
    }

    public Subscription newAction(Subscription sub) throws OXException, IOException, JSONException {
        NewSubscriptionRequest newReq = new NewSubscriptionRequest(sub, getFormDescription());
        newReq.setFailOnError(getFailOnError());
        NewSubscriptionResponse newResp = getClient().execute(newReq);
        lastResponse = newResp;
        createdItems.add(I(newResp.getId()));
        sub.setId(newResp.getId());
        return sub;
    }

    public Subscription getAction(int id) throws OXException, IOException, JSONException {
        GetSubscriptionRequest getReq = new GetSubscriptionRequest(id);
        getReq.setFailOnError(getFailOnError());
        GetSubscriptionResponse getResp = getClient().execute(getReq);
        lastResponse = getResp;
        return getResp.getSubscription(getSubscriptionSourceRecoveryService());
    }

    public void deleteAction(Subscription sub) throws OXException, IOException, JSONException {
        int id = sub.getId();
        DeleteSubscriptionRequest delReq = new DeleteSubscriptionRequest(id);
        delReq.setFailOnError(getFailOnError());
        DeleteSubscriptionResponse delResp = getClient().execute(delReq);
        createdItems.remove(I(id));
        lastResponse = delResp;
    }

    public void deleteAction(Collection<Integer> ids) throws OXException, IOException, JSONException {
        DeleteSubscriptionRequest delReq = new DeleteSubscriptionRequest(ids);
        delReq.setFailOnError(getFailOnError());
        DeleteSubscriptionResponse delResp = getClient().execute(delReq);
        createdItems.removeAll(ids);
        lastResponse = delResp;
    }

    public JSONArray listAction(List<Integer> ids, List<String> columns) throws OXException, IOException, JSONException {
        ListSubscriptionsRequest listReq = new ListSubscriptionsRequest(ids, columns);
        listReq.setFailOnError(getFailOnError());
        ListSubscriptionsResponse listResp = getClient().execute(listReq);
        lastResponse = listResp;
        return listResp.getList();
    }

    public JSONArray listAction(List<Integer> ids, List<String> columns, Map<String, List<String>> dynamicColumns) throws OXException, IOException, JSONException {
        ListSubscriptionsRequest listReq = new ListSubscriptionsRequest(ids, columns, dynamicColumns);
        listReq.setFailOnError(getFailOnError());
        ListSubscriptionsResponse listResp = getClient().execute(listReq);
        lastResponse = listResp;
        return listResp.getList();
    }

    public JSONArray allAction(String folder, List<String> columns) throws OXException, IOException, JSONException {
        AllSubscriptionsRequest allReq = null;
        if (folder == null) {
            allReq = new AllSubscriptionsRequest(columns);
        } else {
            allReq = new AllSubscriptionsRequest(folder, columns);
        }

        allReq.setFailOnError(getFailOnError());
        AllSubscriptionsResponse allResp = getClient().execute(allReq);
        lastResponse = allResp;
        return allResp.getAll();
    }

    public JSONArray allAction(int folder, List<String> columns) throws OXException, IOException, JSONException {
        return allAction(String.valueOf(folder), columns);
    }

    public JSONArray allAction(List<String> columns) throws OXException, IOException, JSONException {
        return allAction(null, columns);
    }

    public JSONArray allAction(String folder, List<String> columns, Map<String, List<String>> dynamicColumns) throws OXException, IOException, JSONException {
        AllSubscriptionsRequest allReq = new AllSubscriptionsRequest(folder, columns, dynamicColumns);
        allReq.setFailOnError(getFailOnError());
        AllSubscriptionsResponse allResp = getClient().execute(allReq);
        lastResponse = allResp;
        return allResp.getAll();
    }

    public JSONArray allAction(int folder, List<String> columns, Map<String, List<String>> dynamicColumns) throws OXException, IOException, JSONException {
        return allAction(String.valueOf(folder), columns, dynamicColumns);
    }

    public void updateAction(Subscription subscription) throws OXException, IOException, JSONException {
        UpdateSubscriptionRequest updReq = new UpdateSubscriptionRequest(subscription, formDescription);
        updReq.setFailOnError(getFailOnError());
        UpdateSubscriptionResponse updResp = getClient().execute(updReq);
        lastResponse = updResp;
    }

    public void refreshAction(int id) throws OXException, IOException, JSONException {
        RefreshSubscriptionRequest refreshReq = new RefreshSubscriptionRequest(id, null);
        refreshReq.setFailOnError(getFailOnError());
        RefreshSubscriptionResponse refreshResponse = getClient().execute(refreshReq);
        lastResponse = refreshResponse;
    }

    public void cleanUp() throws OXException, IOException, JSONException {
        if (createdItems.size() > 0) {
            deleteAction(createdItems);
        }
    }

}
