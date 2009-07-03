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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.subscribe.json;

import static com.openexchange.subscribe.json.MultipleHandlerTools.response;
import static com.openexchange.subscribe.json.MultipleHandlerTools.wrapThrowable;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.MISSING_PARAMETER;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.UNKNOWN_ACTION;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.tools.QueryStringPositionComparator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SubscriptionMultipleHandler}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionMultipleHandler implements MultipleHandler {

    private SubscriptionSourceDiscoveryService discovery;
    private SubscriptionExecutionService executor;

    
    
    private SubscriptionMultipleHandler(SubscriptionSourceDiscoveryService discovery, SubscriptionExecutionService executor) {
        super();
        this.discovery = discovery;
        this.executor = executor;
    }

    public void close() {

    }

    public Date getTimestamp() {
        return null;
    }

    public JSONValue performRequest(String action, JSONObject request, ServerSession session) throws AbstractOXException, JSONException {
        try {
            if (null == action) {
                MISSING_PARAMETER.throwException("action");
                return null;
            } else if (action.equals("new")) {
                return createSubscription(request, session);
            } else if (action.equals("update")) {
                return updateSubscription(request, session);
            } else if (action.equals("delete")) {
                return deleteSubscriptions(request, session);
            } else if (action.equals("get")) {
                return loadSubscription(request, session);
            } else if (action.equals("all")) {
                return loadAllSubscriptionsInFolder(request, session);
            } else if (action.equals("list")) {
                return listSubscriptions(request, session);
            } else if (action.equals("refresh")) {
                return refreshSubscriptions(request, session);
            } else {
                UNKNOWN_ACTION.throwException(action);
                return null;
            }
        } catch (AbstractOXException x) {
            throw x;
        } catch (JSONException x) {
            throw x;
        } catch (Throwable t) {
            throw wrapThrowable(t);
        }
    }

    private JSONValue refreshSubscriptions(JSONObject request, ServerSession session) throws AbstractOXException, JSONException {
        List<Subscription> subscriptionsToRefresh = new ArrayList<Subscription>(10);
        Context context = session.getContext();
        Set<Integer> ids = new HashSet<Integer>();
        if (request.has("id")) {
            int id = request.getInt("id");
            Subscription subscription = loadSubscription(id, context, request.optString("source"), session.getPassword());
            if (!ids.contains(ids)) {
                ids.add(id);
                subscriptionsToRefresh.add(subscription);
            }
        }
        if (request.has("folder")) {
            String folderId = request.getString("folder");
            List<Subscription> allSubscriptions = null;
            allSubscriptions = getSubscriptionsInFolder(context, folderId, session.getPassword());
            for(Subscription candidate : allSubscriptions) {
                if(!ids.contains(candidate.getId())) {
                    subscriptionsToRefresh.add(candidate);
                }
            }
        }

        executor.executeSubscriptions(subscriptionsToRefresh);
        
        
        return response(1);
    }

    private JSONValue listSubscriptions(JSONObject request, ServerSession session) throws JSONException, AbstractOXException {
        JSONArray ids = request.getJSONArray(ResponseFields.DATA);
        Context context = session.getContext();
        List<Subscription> subscriptions = new ArrayList<Subscription>(ids.length());
        for (int i = 0, size = ids.length(); i < size; i++) {
            int id = ids.getInt(i);
            SubscribeService subscribeService = discovery.getSource(context, id).getSubscribeService();
            Subscription subscription = subscribeService.loadSubscription(context, id, session.getPassword());
            if (subscription != null) {
                subscriptions.add(subscription);
            }
        }
        String[] basicColumns = getBasicColumns(request);
        Map<String, String[]> dynamicColumns = getDynamicColumns(request);
        List<String> dynamicColumnOrder = getDynamicColumnOrder(request);

        return createResponse(subscriptions, basicColumns, dynamicColumns, dynamicColumnOrder);
    }

    private JSONValue loadAllSubscriptionsInFolder(JSONObject request, ServerSession session) throws JSONException, AbstractOXException {
        String folderId = request.getString("folder");
        Context context = session.getContext();

        List<Subscription> allSubscriptions = null;
        allSubscriptions = getSubscriptionsInFolder(context, folderId, session.getPassword());

        String[] basicColumns = getBasicColumns(request);
        Map<String, String[]> dynamicColumns = getDynamicColumns(request);
        List<String> dynamicColumnOrder = getDynamicColumnOrder(request);

        return createResponse(allSubscriptions, basicColumns, dynamicColumns, dynamicColumnOrder);
    }

    private List<Subscription> getSubscriptionsInFolder(Context context, String folder, String secret) throws AbstractOXException {
        List<SubscriptionSource> sources = discovery.getSources();
        List<Subscription> allSubscriptions = new ArrayList<Subscription>(10);
        for (SubscriptionSource subscriptionSource : sources) {
            Collection<Subscription> subscriptions = subscriptionSource.getSubscribeService().loadSubscriptions(context, folder, secret);
            allSubscriptions.addAll(subscriptions);
        }
        return allSubscriptions;
    }

    private JSONValue createResponse(List<Subscription> allSubscriptions, String[] basicColumns, Map<String, String[]> dynamicColumns, List<String> dynamicColumnOrder) throws SubscriptionJSONException, JSONException {
        JSONArray rows = new JSONArray();
        SubscriptionJSONWriter writer = new SubscriptionJSONWriter();
        for (Subscription subscription : allSubscriptions) {
            JSONArray row = writer.writeArray(
                subscription,
                basicColumns,
                dynamicColumns,
                dynamicColumnOrder,
                subscription.getSource().getFormDescription());
            rows.put(row);
        }
        return response(rows);
    }

    private Map<String, String[]> getDynamicColumns(JSONObject request) throws JSONException {
        List<String> identifiers = getDynamicColumnOrder(request);
        Map<String, String[]> dynamicColumns = new HashMap<String, String[]>();
        for (String identifier : identifiers) {
            String columns = request.optString(identifier);
            if (columns != null) {
                dynamicColumns.put(identifier, columns.split("\\s*,\\s*"));
            }
        }
        return dynamicColumns;
    }

    private static final Set<String> KNOWN_PARAMS = new HashSet<String>() {

        {
            add("folder");
            add("columns");
            add("session");
            add("action");
        }
    };

    private List<String> getDynamicColumnOrder(JSONObject request) throws JSONException {
        if (request.has("dynamicColumnPlugins")) {
            return Arrays.asList(request.getString("dynamicColumnPlugins").split("\\s*,\\s*"));
        }

        List<String> dynamicColumnIdentifiers = new ArrayList<String>();
        for (String paramName : request.keySet()) {
            if (!KNOWN_PARAMS.contains(paramName) && paramName.contains(".")) {
                dynamicColumnIdentifiers.add(paramName);
            }
        }
        String order = request.optString("__query");
        Collections.sort(dynamicColumnIdentifiers, new QueryStringPositionComparator(order));
        return dynamicColumnIdentifiers;
    }

    private String[] getBasicColumns(JSONObject request) {
        String columns = request.optString("columns");
        if (columns == null) {
            return new String[] { "id", "folder", "source" };
        }
        return columns.split("\\s*,\\s*");
    }

    private JSONValue loadSubscription(JSONObject request, ServerSession session) throws JSONException, AbstractOXException {
        int id = request.getInt("id");
        String source = request.optString("source");
        Context context = session.getContext();
        Subscription subscription = loadSubscription(id, context, source, session.getPassword());
        return createResponse(subscription);
    }

    private JSONValue createResponse(Subscription subscription) throws JSONException, SubscriptionJSONException {
        JSONObject object = new SubscriptionJSONWriter().write(subscription, subscription.getSource().getFormDescription());
        return response(object);
    }

    private Subscription loadSubscription(int id, Context context, String source, String secret) throws AbstractOXException {
        SubscribeService service = null;
        if (source != null) {
            service = discovery.getSource(source).getSubscribeService();
        } else {
            service = discovery.getSource(context, id).getSubscribeService();
        }
        return service.loadSubscription(context, id, secret);
    }

    private JSONValue deleteSubscriptions(JSONObject request, ServerSession session) throws JSONException, AbstractOXException {
        JSONArray ids = request.getJSONArray(ResponseFields.DATA);
        Context context = session.getContext();
        for (int i = 0, size = ids.length(); i < size; i++) {
            int id = ids.getInt(i);
            SubscribeService subscribeService = discovery.getSource(context, id).getSubscribeService();
            Subscription subscription = new Subscription();
            subscription.setContext(context);
            subscription.setId(id);
            subscribeService.unsubscribe(subscription);
        }
        return response(1);
    }

    private JSONValue updateSubscription(JSONObject request, ServerSession session) throws JSONException, AbstractOXException {
        Subscription subscription = getSubscription(request, session, session.getPassword());
        SubscribeService subscribeService = subscription.getSource().getSubscribeService();
        subscribeService.update(subscription);
        return response(1);
    }

    private JSONValue createSubscription(JSONObject request, ServerSession session) throws AbstractOXException, JSONException {
        Subscription subscription = getSubscription(request, session, session.getPassword());
        subscription.setId(-1);
        SubscribeService subscribeService = subscription.getSource().getSubscribeService();
        subscribeService.subscribe(subscription);
        return response(subscription.getId());
    }

    private Subscription getSubscription(JSONObject request, ServerSession session, String secret) throws JSONException {
        JSONObject object = request.getJSONObject(ResponseFields.DATA);
        Subscription subscription = new SubscriptionJSONParser(discovery).parse(object);
        subscription.setContext(session.getContext());
        subscription.setUserId(session.getUserId());
        subscription.getConfiguration().put("com.openexchange.crypto.secret", secret);
        return subscription;
    }

}
