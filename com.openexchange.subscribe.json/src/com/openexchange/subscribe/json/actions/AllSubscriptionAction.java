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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllSubscriptionAction}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class AllSubscriptionAction extends AbstractSubscribeAction {

    /**
     * Initializes a new {@link AllSubscriptionAction}.
     *
     * @param services
     */
    public AllSubscriptionAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException, JSONException {
        String folderId = null;
        boolean containsFolder = false;

        if (subscribeRequest.getRequestData().getParameter("folder") != null) {
            folderId = subscribeRequest.getRequestData().getParameter("folder");
            containsFolder = true;
        }

        List<Subscription> allSubscriptions = null;
        if (containsFolder) {
            SecretService secretService = services.getService(SecretService.class);
            allSubscriptions = getSubscriptionsInFolder(subscribeRequest.getServerSession(), folderId, secretService.getSecret(subscribeRequest.getServerSession()));
        } else {
            allSubscriptions = getAllSubscriptions(subscribeRequest.getServerSession(), services.getService(SecretService.class).getSecret(subscribeRequest.getServerSession()));
        }

        JSONObject parameters = new JSONObject(subscribeRequest.getRequestData().getParameters());
        final String[] basicColumns = getBasicColumns(parameters);
        Map<String, String[]> dynamicColumns = getDynamicColumns(parameters);
        final List<String> dynamicColumnOrder = getDynamicColumnOrder(parameters);
        JSONArray jsonArray = (JSONArray) createResponse(allSubscriptions, basicColumns, dynamicColumns, dynamicColumnOrder, subscribeRequest.getTimeZone());
        return new AJAXRequestResult(jsonArray, "json");
    }

    private List<Subscription> getAllSubscriptions(final ServerSession session, final String secret) throws OXException {
        final List<SubscriptionSource> sources = getDiscovery(session).getSources();
        final List<Subscription> allSubscriptions = new ArrayList<Subscription>();
        for (final SubscriptionSource subscriptionSource : sources) {
            final SubscribeService subscribeService = subscriptionSource.getSubscribeService();
            final Collection<Subscription> subscriptions = subscribeService.loadSubscriptions(session.getContext(), session.getUserId(), secret);
            allSubscriptions.addAll(subscriptions);
        }
        return allSubscriptions;
    }

}
