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

package com.openexchange.subscribe.json.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.json.SubscriptionJSONErrorMessages;
import com.openexchange.subscribe.json.SubscriptionJSONParser;
import com.openexchange.subscribe.json.SubscriptionJSONWriter;
import com.openexchange.tools.QueryStringPositionComparator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractSubscribeAction}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public abstract class AbstractSubscribeAction extends AbstractSubscribeSourcesAction {

    protected static final String MICROFORMATS_ID = "com.openexchange.subscribe.microformats";

    /**
     * Initializes a new {@link AbstractSubscribeAction}.
     */
    protected AbstractSubscribeAction(final ServiceLookup services) {
        super(services);
    }

    /** The set of known parameter names */
    protected static final Set<String> KNOWN_PARAMS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("folder", "columns", "session", "action")));

    protected Subscription getSubscription(final AJAXRequestData requestData, final ServerSession session, final String secret) throws JSONException, OXException {
        final JSONObject object = (JSONObject) requestData.requireData();
        final Subscription subscription = new SubscriptionJSONParser(getDiscovery(session)).parse(object);
        subscription.setContext(session.getContext());
        subscription.setUserId(session.getUserId());
        subscription.setSecret(secret);
        return subscription;
    }

    protected SubscriptionSourceDiscoveryService getDiscovery(final ServerSession session) throws OXException {
        return services.getService(SubscriptionSourceDiscoveryService.class).filter(session.getUserId(), session.getContextId());
    }

    protected List<Subscription> getSubscriptionsInFolder(final ServerSession session, final String folder, final String secret) throws OXException {
        final List<SubscriptionSource> sources = getDiscovery(session).getSources();
        final List<Subscription> allSubscriptions = new ArrayList<Subscription>(10);
        for (final SubscriptionSource subscriptionSource : sources) {
            final Collection<Subscription> subscriptions = subscriptionSource.getSubscribeService().loadSubscriptions(session.getContext(), folder, secret);
            allSubscriptions.addAll(subscriptions);
        }
        return allSubscriptions;
    }

    protected Map<String, String[]> getDynamicColumns(final JSONObject request) throws JSONException {
        final List<String> identifiers = getDynamicColumnOrder(request);
        final Map<String, String[]> dynamicColumns = new HashMap<String, String[]>();
        for (final String identifier : identifiers) {
            final String columns = request.optString(identifier);
            if (columns != null && !columns.equals("")) {
                dynamicColumns.put(identifier, Strings.splitByComma(columns));
            }
        }
        return dynamicColumns;
    }

    protected List<String> getDynamicColumnOrder(final JSONObject request) throws JSONException {
        if (request.has("dynamicColumnPlugins")) {
            return Arrays.asList(Strings.splitByComma(request.getString("dynamicColumnPlugins")));
        }

        final List<String> dynamicColumnIdentifiers = new ArrayList<String>();
        for (final String paramName : request.keySet()) {
            if (!KNOWN_PARAMS.contains(paramName) && paramName.indexOf('.') >= 0) {
                dynamicColumnIdentifiers.add(paramName);
            }
        }
        final String order = request.optString("__query");
        Collections.sort(dynamicColumnIdentifiers, new QueryStringPositionComparator(order));
        return dynamicColumnIdentifiers;
    }

    protected String[] getBasicColumns(final JSONObject request) {
        final String columns = request.optString("columns");
        if (columns == null || columns.equals("")) {
            return new String[] { "id", "folder", "source", "displayName", "enabled" };
        }
        return Strings.splitByComma(columns);
    }

    protected JSONValue createResponse(final List<Subscription> allSubscriptions, final String[] basicColumns, final Map<String, String[]> dynamicColumns, final List<String> dynamicColumnOrder, TimeZone tz) throws OXException {
        final JSONArray rows = new JSONArray();
        final SubscriptionJSONWriter writer = new SubscriptionJSONWriter();
        for (final Subscription subscription : allSubscriptions) {
            final JSONArray row = writer.writeArray(subscription, basicColumns, dynamicColumns, dynamicColumnOrder, subscription.getSource().getFormDescription(), tz);
            rows.put(row);
        }
        return rows;
    }

    protected Subscription loadSubscription(final int id, final ServerSession session, final String source, final String secret) throws OXException {
        SubscribeService service = null;
        if (source != null && !source.equals("")) {
            final SubscriptionSource s = getDiscovery(session).getSource(source);
            if (s == null) {
                return null;
            }
            service = s.getSubscribeService();
        } else {
            final SubscriptionSource s = getDiscovery(session).getSource(session.getContext(), id);
            if (s == null) {
                return null;
            }
            service = s.getSubscribeService();
        }
        return service.loadSubscription(session.getContext(), id, secret);
    }

    private boolean isCreateModifyEnabled() {
        ConfigurationService configService = services.getService(ConfigurationService.class);
        return null != configService && configService.getBoolProperty("com.openexchange.subscribe.microformats.createModifyEnabled", false);
    }

    /**
     * Checks if the given subscription is allowed to become execute. Currently this check is only against OXMF subscriptions
     * 
     * @param subscription The {@link Subscription} to check
     * @throws OXException Thrown if the {@link Subscription} the subscription is defined as not allowed to update/create
     */
    protected void checkAllowed(Subscription subscription) throws OXException {
        if (subscription.containsSource() && subscription.getSource().getId().startsWith(MICROFORMATS_ID) && !isCreateModifyEnabled()) {
            throw SubscriptionJSONErrorMessages.FORBIDDEN_CREATE_MODIFY.create();
        }
    }
}
