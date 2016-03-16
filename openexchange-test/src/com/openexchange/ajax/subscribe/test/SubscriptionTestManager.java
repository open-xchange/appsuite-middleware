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
import org.xml.sax.SAXException;
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

    public Subscription newAction(Subscription sub) throws OXException, IOException, SAXException, JSONException {
        NewSubscriptionRequest newReq = new NewSubscriptionRequest(sub, getFormDescription());
        newReq.setFailOnError(getFailOnError());
        NewSubscriptionResponse newResp = getClient().execute(newReq);
        lastResponse = newResp;
        createdItems.add(I(newResp.getId()));
        sub.setId(newResp.getId());
        return sub;
    }

    public Subscription getAction(int id) throws OXException, IOException, SAXException, JSONException {
        GetSubscriptionRequest getReq = new GetSubscriptionRequest(id);
        getReq.setFailOnError(getFailOnError());
        GetSubscriptionResponse getResp = getClient().execute(getReq);
        lastResponse = getResp;
        return getResp.getSubscription(getSubscriptionSourceRecoveryService());
    }

    public void deleteAction(Subscription sub) throws OXException, IOException, SAXException, JSONException {
        int id = sub.getId();
        DeleteSubscriptionRequest delReq = new DeleteSubscriptionRequest(id);
        delReq.setFailOnError(getFailOnError());
        DeleteSubscriptionResponse delResp = getClient().execute(delReq);
        createdItems.remove(I(id));
        lastResponse = delResp;
    }

    public void deleteAction(Collection<Integer> ids) throws OXException, IOException, SAXException, JSONException {
        DeleteSubscriptionRequest delReq = new DeleteSubscriptionRequest(ids);
        delReq.setFailOnError(getFailOnError());
        DeleteSubscriptionResponse delResp = getClient().execute(delReq);
        createdItems.removeAll(ids);
        lastResponse = delResp;
    }

    public JSONArray listAction(List<Integer> ids, List<String> columns) throws OXException, IOException, SAXException, JSONException {
        ListSubscriptionsRequest listReq = new ListSubscriptionsRequest(ids,columns);
        listReq.setFailOnError(getFailOnError());
        ListSubscriptionsResponse listResp = getClient().execute(listReq);
        lastResponse = listResp;
        return listResp.getList();
    }

    public JSONArray listAction(List<Integer> ids, List<String> columns, Map<String,List<String>> dynamicColumns) throws OXException, IOException, SAXException, JSONException {
        ListSubscriptionsRequest listReq = new ListSubscriptionsRequest(ids,columns,dynamicColumns);
        listReq.setFailOnError(getFailOnError());
        ListSubscriptionsResponse listResp = getClient().execute(listReq);
        lastResponse = listResp;
        return listResp.getList();
    }

    public JSONArray allAction(String folder, List<String> columns) throws OXException, IOException, SAXException, JSONException{
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

    public JSONArray allAction(int folder, List<String> columns) throws OXException, IOException, SAXException, JSONException{
        return allAction(String.valueOf(folder), columns);
    }

    public JSONArray allAction(List<String> columns) throws OXException, IOException, SAXException, JSONException{
        return allAction(null, columns);
    }

    public JSONArray allAction(String folder, List<String> columns, Map<String,List<String>> dynamicColumns) throws OXException, IOException, SAXException, JSONException{
        AllSubscriptionsRequest allReq = new AllSubscriptionsRequest(folder, columns, dynamicColumns);
        allReq.setFailOnError(getFailOnError());
        AllSubscriptionsResponse allResp = getClient().execute(allReq);
        lastResponse = allResp;
        return allResp.getAll();
    }

    public JSONArray allAction(int folder, List<String> columns, Map<String,List<String>> dynamicColumns) throws OXException, IOException, SAXException, JSONException{
        return allAction(String.valueOf(folder), columns, dynamicColumns);
    }

    public void updateAction(Subscription subscription) throws OXException, IOException, SAXException, JSONException {
        UpdateSubscriptionRequest updReq = new UpdateSubscriptionRequest(subscription, formDescription);
        updReq.setFailOnError(getFailOnError());
        UpdateSubscriptionResponse updResp = getClient().execute(updReq);
        lastResponse = updResp;
    }

    public void refreshAction(int id) throws OXException, IOException, SAXException, JSONException{
        RefreshSubscriptionRequest refreshReq = new RefreshSubscriptionRequest(id, null);
        refreshReq.setFailOnError(getFailOnError());
        RefreshSubscriptionResponse refreshResponse = getClient().execute(refreshReq);
        lastResponse = refreshResponse;
    }

    public void cleanUp() throws OXException, IOException, SAXException, JSONException {
        if(createdItems.size() > 0) {
            deleteAction(createdItems);
        }
    }

}
