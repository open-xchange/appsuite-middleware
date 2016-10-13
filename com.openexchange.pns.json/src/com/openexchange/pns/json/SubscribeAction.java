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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.pns.DefaultPushSubscription;
import com.openexchange.pns.PushNotifications;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SubscribeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SubscribeAction extends AbstractPushJsonAction {

    /**
     * Initializes a new {@link SubscribeAction}.
     */
    public SubscribeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        JSONObject jRequestBody = (JSONObject) requestData.requireData();

        PushSubscriptionRegistry subscriptionRegistry = services.getOptionalService(PushSubscriptionRegistry.class);

        String client = jRequestBody.optString("client", null);
        if (null == client) {
            client = session.getClient();
        } else if (null != session.getClient() && !client.equals(session.getClient())) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("client", client);
        }
        if (Strings.isEmpty(client)) {
            throw AjaxExceptionCodes.MISSING_FIELD.create("client");
        }

        String token = requireStringField("token", jRequestBody);
        String transportId = requireStringField("transport", jRequestBody);
        JSONArray jTopics = requireArrayField("topics", jRequestBody);

        List<String> topics = new ArrayList<>(jTopics.length());
        for (Object topicObj : jTopics) {
            String topic = topicObj.toString();
            try {
                PushNotifications.validateTopicName(topic);
            } catch (IllegalArgumentException e) {
                throw PushExceptionCodes.INVALID_TOPIC.create(e, topic);
            }
            topics.add(topic);
        }

        DefaultPushSubscription.Builder builder = DefaultPushSubscription.builder()
            .client(client)
            .topics(topics)
            .contextId(session.getContextId())
            .token(token)
            .transportId(transportId)
            .userId(session.getUserId());
        DefaultPushSubscription subscription = builder.build();

        subscriptionRegistry.registerSubscription(subscription);

        return new AJAXRequestResult(new JSONObject(2).put("success", true), "json");
    }

    @Override
    public String getAction() {
        return "subscribe";
    }

}
