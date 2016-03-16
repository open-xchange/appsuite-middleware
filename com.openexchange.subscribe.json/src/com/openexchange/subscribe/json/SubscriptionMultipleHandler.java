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

package com.openexchange.subscribe.json;

import static com.openexchange.subscribe.json.MultipleHandlerTools.wrapThrowable;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.MISSING_PARAMETER;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.UNKNOWN_ACTION;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.secret.SecretService;
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

    /** The actions that require a request body */
    public static final Set<String> ACTIONS_REQUIRING_BODY = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("new","update","delete","list","fetch")));

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final SubscriptionExecutionService executor;
    private final SecretService secretService;
    private final SubscriptionSourceDiscoveryService discovery;

    /**
     * Initializes a new {@link SubscriptionMultipleHandler}.
     */
    public SubscriptionMultipleHandler(SubscriptionSourceDiscoveryService discovery, SubscriptionExecutionService executor, SecretService secretService) {
        super();
        this.discovery = discovery;
        this.executor = executor;
        this.secretService = secretService;
    }

    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public Collection<OXException> getWarnings() {
        return Collections.<OXException> emptySet();
    }

    @Override
    public Object performRequest(final String action, final JSONObject request, final ServerSession session, final boolean secure) throws JSONException, OXException {
        try {
            if (null == action) {
                MISSING_PARAMETER.create("action");
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
                return loadAllSubscriptions(request, session);
            } else if (action.equals("list")) {
                return listSubscriptions(request, session);
            } else if (action.equals("refresh")) {
                return refreshSubscriptions(request, session);
            } else if (action.equals("fetch")) {
                return fetchSubscription(request, session);
            } else {
                UNKNOWN_ACTION.create(action);
                return null;
            }
        } catch (final JSONException x) {
            throw x;
        } catch (final Throwable t) {
            throw wrapThrowable(t);
        }
    }

    private Object fetchSubscription(JSONObject request, ServerSession session) throws JSONException, OXException {
        final Subscription subscription = getSubscription(request, session, secretService.getSecret(session));
        subscription.setId(-1);
        return Integer.valueOf(executor.executeSubscriptions(Arrays.asList(subscription), session, null));
    }


    private Object refreshSubscriptions(final JSONObject request, final ServerSession session) throws OXException, JSONException {
        List<Subscription> subscriptionsToRefresh = new LinkedList<Subscription>();
        TIntSet ids = new TIntHashSet();
        if (request.has("folder")) {
            final String folderId = request.getString("folder");
            List<Subscription> allSubscriptions = null;
            allSubscriptions = getSubscriptionsInFolder(session, folderId, secretService.getSecret(session));
            Collections.sort(allSubscriptions, new Comparator<Subscription>() {

                @Override
                public int compare(final Subscription o1, final Subscription o2) {
                    if(o1.getLastUpdate() == o2.getLastUpdate()) {
                        return o2.getId() - o1.getId();
                    }
                    return (int) (o2.getLastUpdate() - o1.getLastUpdate());
                }

            });
            for (final Subscription subscription : allSubscriptions) {
                ids.add(subscription.getId());
                subscriptionsToRefresh.add(subscription);
            }
        }
        if (request.has("id")) {
            final int id = request.getInt("id");
            final Subscription subscription = loadSubscription(id, session, request.optString("source"), secretService.getSecret(session));
            if (ids.add(id)) {
                subscriptionsToRefresh.add(subscription);
            }
        }

        return Integer.valueOf(executor.executeSubscriptions(subscriptionsToRefresh, session, null));
    }


    private Object listSubscriptions(JSONObject request, ServerSession session) throws JSONException, OXException {
        final JSONArray ids = request.getJSONArray(ResponseFields.DATA);
        final Context context = session.getContext();
        final List<Subscription> subscriptions = new ArrayList<Subscription>(ids.length());
        for (int i = 0, size = ids.length(); i < size; i++) {
            final int id = ids.getInt(i);
            final SubscriptionSource source = getDiscovery(session).getSource(context, id);
            if(source != null) {
                final SubscribeService subscribeService = source.getSubscribeService();
                final Subscription subscription = subscribeService.loadSubscription(context, id, secretService.getSecret(session));
                if (subscription != null) {
                    subscriptions.add(subscription);
                }
            }
        }
        final String[] basicColumns = getBasicColumns(request);
        final Map<String, String[]> dynamicColumns = getDynamicColumns(request);
        final List<String> dynamicColumnOrder = getDynamicColumnOrder(request);
        String sTimeZone = request.optString("timezone");
        TimeZone tz;
        if (sTimeZone != null) {
            tz = TimeZone.getTimeZone(sTimeZone);
        } else {
            tz = TimeZone.getTimeZone(session.getUser().getTimeZone());
        }

        return createResponse(subscriptions, basicColumns, dynamicColumns, dynamicColumnOrder, tz);
    }

    private Object loadAllSubscriptions(JSONObject request, ServerSession session) throws JSONException, OXException {
        String folderId = null;
        boolean containsFolder = false;
        if (request.has("folder")) {
            folderId = request.getString("folder");
            containsFolder = true;
        }

        List<Subscription> allSubscriptions = null;
        if (containsFolder) {
            allSubscriptions = getSubscriptionsInFolder(session, folderId, secretService.getSecret(session));
        } else {
            allSubscriptions = getAllSubscriptions(session, secretService.getSecret(session));
        }
        String sTimeZone = request.optString("timezone");
        TimeZone tz;
        if (sTimeZone != null) {
            tz = TimeZone.getTimeZone(sTimeZone);
        } else {
            tz = TimeZone.getTimeZone(session.getUser().getTimeZone());
        }

        final String[] basicColumns = getBasicColumns(request);
        final Map<String, String[]> dynamicColumns = getDynamicColumns(request);
        final List<String> dynamicColumnOrder = getDynamicColumnOrder(request);

        return createResponse(allSubscriptions, basicColumns, dynamicColumns, dynamicColumnOrder, tz);
    }

    private List<Subscription> getSubscriptionsInFolder(ServerSession session, String folder, String secret) throws OXException {
        final List<SubscriptionSource> sources = getDiscovery(session).getSources();
        final List<Subscription> allSubscriptions = new ArrayList<Subscription>(10);
        for (final SubscriptionSource subscriptionSource : sources) {
            final Collection<Subscription> subscriptions = subscriptionSource.getSubscribeService().loadSubscriptions(session.getContext(), folder, secret);
            allSubscriptions.addAll(subscriptions);
        }
        return allSubscriptions;
    }

    private List<Subscription> getAllSubscriptions(ServerSession session, String secret) throws OXException {
        final List<SubscriptionSource> sources = getDiscovery(session).getSources();
        final List<Subscription> allSubscriptions = new ArrayList<Subscription>();
        for (final SubscriptionSource subscriptionSource : sources) {
            final SubscribeService subscribeService = subscriptionSource.getSubscribeService();
            final Collection<Subscription> subscriptions = subscribeService.loadSubscriptions(session.getContext(), session.getUserId(), secret);
            allSubscriptions.addAll(subscriptions);
        }

        return allSubscriptions;
    }

    private Object createResponse(List<Subscription> allSubscriptions, String[] basicColumns, Map<String, String[]> dynamicColumns, List<String> dynamicColumnOrder, TimeZone tz) throws OXException {
        final JSONArray rows = new JSONArray();
        final SubscriptionJSONWriter writer = new SubscriptionJSONWriter();
        for (final Subscription subscription : allSubscriptions) {
            final JSONArray row = writer.writeArray(
                subscription,
                basicColumns,
                dynamicColumns,
                dynamicColumnOrder,
                subscription.getSource().getFormDescription(), tz);
            rows.put(row);
        }
        return rows;
    }

    private Map<String, String[]> getDynamicColumns(JSONObject request) throws JSONException {
        final List<String> identifiers = getDynamicColumnOrder(request);
        final Map<String, String[]> dynamicColumns = new HashMap<String, String[]>();
        for (final String identifier : identifiers) {
            final String columns = request.optString(identifier);
            if (columns != null && !columns.equals("")) {
                dynamicColumns.put(identifier, columns.split("\\s*,\\s*"));
            }
        }
        return dynamicColumns;
    }

    private static final Set<String> KNOWN_PARAMS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("folder","columns","session","action")));

    private List<String> getDynamicColumnOrder(JSONObject request) throws JSONException {
        if (request.has("dynamicColumnPlugins")) {
            return Arrays.asList(request.getString("dynamicColumnPlugins").split("\\s*,\\s*"));
        }

        List<String> dynamicColumnIdentifiers = new ArrayList<String>();
        for (String paramName : request.keySet()) {
            if (!KNOWN_PARAMS.contains(paramName) && paramName.indexOf('.') >= 0) {
                dynamicColumnIdentifiers.add(paramName);
            }
        }
        String order = request.optString("__query");
        Collections.sort(dynamicColumnIdentifiers, new QueryStringPositionComparator(order));
        return dynamicColumnIdentifiers;
    }

    private String[] getBasicColumns(final JSONObject request) {
        final String columns = request.optString("columns");
        if (columns == null || columns.equals("")) {
            return new String[] { "id", "folder", "source", "displayName", "enabled" };
        }
        return columns.split("\\s*,\\s*");
    }

    private Object loadSubscription(final JSONObject request, final ServerSession session) throws JSONException, OXException {
        final int id = request.getInt("id");
        final String source = request.optString("source");
        final Subscription subscription = loadSubscription(id, session, source, secretService.getSecret(session));
        String sTimeZone = request.optString("timezone");
        TimeZone tz;
        if (sTimeZone != null) {
            tz = TimeZone.getTimeZone(sTimeZone);
        } else {
            tz = TimeZone.getTimeZone(session.getUser().getTimeZone());
        }

        return createResponse(subscription, request.optString("__serverURL"), tz);
    }

    private Object createResponse(final Subscription subscription, final String urlPrefix, TimeZone tz) throws JSONException, OXException {
        final JSONObject object = new SubscriptionJSONWriter().write(subscription, subscription.getSource().getFormDescription(), urlPrefix, tz);
        return object;
    }

    private Subscription loadSubscription(final int id, final ServerSession session, final String source, final String secret) throws OXException {
        SubscribeService service = null;
        if (source != null && !source.equals("")) {
            final SubscriptionSource s = getDiscovery(session).getSource(source);
            if(s == null) {
                return null;
            }
            service = s.getSubscribeService();
        } else {
            final SubscriptionSource s = getDiscovery(session).getSource(session.getContext(), id);
            if(s == null) {
                return null;
            }
            service = s.getSubscribeService();
        }
        return service.loadSubscription(session.getContext(), id, secret);
    }

    private Object deleteSubscriptions(final JSONObject request, final ServerSession session) throws JSONException, OXException {
        final JSONArray ids = request.getJSONArray(ResponseFields.DATA);
        final Context context = session.getContext();
        for (int i = 0, size = ids.length(); i < size; i++) {
            final int id = ids.getInt(i);
            final SubscriptionSource s = getDiscovery(session).getSource(context, id);
            if(s == null) {
                continue;
            }
            final SubscribeService subscribeService = s.getSubscribeService();
            final Subscription subscription = new Subscription();
            subscription.setContext(context);
            subscription.setId(id);
            if (null == subscription.getSession()) {
                subscription.setSession(session);
            }
            subscribeService.unsubscribe(subscription);
        }
        return Integer.valueOf(1);
    }

    private Object updateSubscription(final JSONObject request, final ServerSession session) throws JSONException, OXException {
        final Subscription subscription = getSubscription(request, session, secretService.getSecret(session));
        final SubscribeService subscribeService = subscription.getSource().getSubscribeService();
        subscribeService.update(subscription);
        return Integer.valueOf(1);
    }

    private Object createSubscription(final JSONObject request, final ServerSession session) throws OXException, JSONException {
        final Subscription subscription = getSubscription(request, session, secretService.getSecret(session));
        subscription.setId(-1);
        final SubscribeService subscribeService = subscription.getSource().getSubscribeService();
        subscribeService.subscribe(subscription);
        return Integer.valueOf(subscription.getId());
    }

    private Subscription getSubscription(final JSONObject request, final ServerSession session, final String secret) throws JSONException, OXException {
        final JSONObject object = request.getJSONObject(ResponseFields.DATA);
        final Subscription subscription = new SubscriptionJSONParser(getDiscovery(session)).parse(object);
        subscription.setContext(session.getContext());
        subscription.setUserId(session.getUserId());
        subscription.setSecret(secret);
        return subscription;
    }

    private SubscriptionSourceDiscoveryService getDiscovery(final ServerSession session) throws OXException {
        return discovery.filter(session.getUserId(), session.getContextId());
    }

}
