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

package com.openexchange.dav.push.gcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.dav.push.DAVPushUtility;
import com.openexchange.dav.push.subscribe.PushSubscribeFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link DavPushGateway}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class DavPushGateway implements PushNotificationTransport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DavPushGateway.class);

    private final HttpClient httpClient;
    private final PushTransportOptions transportOptions;
    private final PushSubscribeFactory factory;

    /**
     * Initializes a new {@link DavPushGateway}.
     *
     * @param factory The factory
     * @param transportOptions The transport options to use
     */
    public DavPushGateway(PushSubscribeFactory factory, PushTransportOptions transportOptions) {
        super();
        this.factory = factory;
        this.httpClient = HttpClients.createDefault();
        this.transportOptions = transportOptions;
    }

    @Override
    public boolean isEnabled(String topic, String client, int userId, int contextId) throws OXException {
        return servesClient(client);
    }

    @Override
    public String getId() {
        return transportOptions.getTransportID();
    }

    @Override
    public boolean servesClient(String client) throws OXException {
        return transportOptions.getClientID().equals(client);
    }

    @Override
    public void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        JSONObject pushData = createPushData(notification, matches);
        if (null != pushData) {
            JSONObject responseObject = doPost(transportOptions.getGatewayUrl() + transportOptions.getApplicationID(), pushData);
            if (null != responseObject) {
                handlePushResponse(responseObject);
            }
        }
    }

    /**
     * Gets the used transport options.
     *
     * @return The transport options
     */
    public PushTransportOptions getOptions() {
        return transportOptions;
    }

    /**
     * Subscribes a client for one or more push topics at the gateway.
     *
     * @param topics The topics to subscribe for
     * @param clientData The optional client data
     * @param expires The expiration date as indicated by the client
     * @return The subscription token
     */
    public String subscribe(List<String> topics, Object clientData, Date expires) throws OXException {
        JSONObject subscribeData = createSubscribeData(topics, clientData, expires);
        if (null != subscribeData) {
            doPost(transportOptions.getGatewayUrl() + transportOptions.getApplicationID(), subscribeData);
            return transportOptions.getApplicationID();
//            TODO: extract token from or use response's "push-url"?
//            JSONObject responseObject = doPost(transportOptions.getGatewayUrl() + transportOptions.getApplicationID(), subscribeData);
//            if (null != responseObject) {
//                String pushUrl = responseObject.optString("push-url");
//            }
        }
        return null;
    }

    /**
     * Unsubscribes a client at the gateway.
     *
     * @param clientData The client data to pass
     * @return The subscription token
     */
    public String unsubscribe(Object clientData) throws OXException {
        //TODO necessary?
        return transportOptions.getApplicationID(); //TODO: extract from or use response's "push-url"?
    }

    private void handlePushResponse(JSONObject responseObject) throws OXException {
        if (null == responseObject) {
            return;
        }
        JSONObject pushResponseObject = responseObject.optJSONObject("push-response");
        if (null == pushResponseObject) {
            return;
        }
        JSONArray noSubscribersArray = pushResponseObject.optJSONArray("no-subscribers");
        if (null != noSubscribersArray && 0 < noSubscribersArray.length()) {
            List<String> unsubscribedTopics = new ArrayList<String>(noSubscribersArray.length());
            try {
                for (int i = 0; i < noSubscribersArray.length(); i++) {
                    JSONObject noSubscribersObject = noSubscribersArray.getJSONObject(i);
                    unsubscribedTopics.add(noSubscribersObject.getString("topic"));
                }
            } catch (JSONException e) {
                LOG.warn("Error parsing push response", e);
            }
            if (0 < unsubscribedTopics.size()) {
                PushSubscriptionRegistry subscriptionRegistry = factory.getOptionalService(PushSubscriptionRegistry.class);
                if (null == subscriptionRegistry) {
                    LOG.warn("Unable to remove unsubscribed topics", ServiceExceptionCode.absentService(PushSubscriptionRegistry.class));
                } else {
                    for (String unsubscribedTopic : unsubscribedTopics) {
                        try {
                            subscriptionRegistry.unregisterSubscription(unsubscribedTopic, transportOptions.getTransportID());
                        } catch (OXException e) {
                            LOG.error("Error unregistering subscriptions for {}", unsubscribedTopic, e);
                        }
                    }
                }
            }
        }
    }

    private JSONObject createSubscribeData(List<String> topics, Object clientData, Date expires) throws OXException {
        try {
            JSONObject subscribeData = new JSONObject();
            JSONObject pushSubscribeObject = new JSONObject();
            pushSubscribeObject.put("topics", topics);
            JSONObject transportObject = new JSONObject();
            transportObject.put("transport-uri", transportOptions.getTransportURI());
            transportObject.put("client-data", clientData);
            pushSubscribeObject.put("selected-transport", transportObject);
            pushSubscribeObject.put("expires", DAVPushUtility.UTC_DATE_FORMAT.get().format(expires));
            subscribeData.put("push-subscribe", pushSubscribeObject);
            return subscribeData;
        } catch (JSONException e) {
            throw OXException.general("", e);
        }
    }

    private static JSONObject createPushData(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        if (null == matches || 0 == matches.size()) {
            return null;
        }
        Map<String, Object> messageData = notification.getMessageData();
        try {
            JSONObject pushData = new JSONObject();
            JSONObject pushObject = new JSONObject();
            pushData.put("push", pushObject);
            JSONArray jsonMessages = new JSONArray(matches.size());
            for (PushMatch match : matches) {
                JSONObject messageObject = new JSONObject();
                messageObject.put("topic", DAVPushUtility.getPushKey(match.getTopic(), match.getContextId(), match.getUserId()));
                Integer priority = (Integer) messageData.get(DAVPushUtility.PARAMETER_PRIORITY);
                messageObject.put("priority", null == priority ? 50 : priority.intValue());
                Long timestamp = (Long) messageData.get(DAVPushUtility.PARAMETER_TIMESTAMP);
                messageObject.put("timestamp", DAVPushUtility.UTC_DATE_FORMAT.get().format(null != timestamp ? new Date(timestamp.longValue()) : new Date()));
                messageObject.putOpt("client-id", messageData.get(DAVPushUtility.PARAMETER_CLIENTTOKEN));
                jsonMessages.put(messageObject);
            }
            pushObject.put("messages", jsonMessages);
            return pushData;
        } catch (JSONException e) {
            throw PushExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Posts an <code>application/json</code> request body to a specific URI.
     *
     * @param uri The target URI
     * @param body The request body
     * @return The JSON response body, or <code>null</code> if there was none
     */
    private JSONObject doPost(String uri, JSONObject body) throws OXException {
        HttpPost post = null;
        HttpResponse response = null;
        try {
            post = new HttpPost(uri);
            post.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));
            LOG.trace(">>> POST {}{}    {}", uri, System.lineSeparator(), body);
            response = httpClient.execute(post);
            StatusLine statusLine = response.getStatusLine();
            if (null != statusLine && HttpServletResponse.SC_OK == statusLine.getStatusCode()) {
                JSONObject responseBody = parseJSONObject(response.getEntity());
                LOG.trace("<<< {}{}    {}", response.getStatusLine(), System.lineSeparator(), responseBody);
                return responseBody;
            }
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(String.valueOf(statusLine));
        } catch (IOException e) {
            throw PushExceptionCodes.IO_ERROR.create(e);
        } finally {
            close(post, response);
        }
    }

    private static JSONObject parseJSONObject(HttpEntity entity) throws OXException {
        if (null != entity && ContentType.APPLICATION_JSON.equals(ContentType.get(entity))) {
            InputStream inputStream = null;
            try {
                inputStream = entity.getContent();
                try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                    return new JSONObject(reader);
                }
            } catch (UnsupportedOperationException | IOException e) {
                throw PushExceptionCodes.IO_ERROR.create(e);
            } catch (JSONException e) {
                throw PushExceptionCodes.JSON_ERROR.create(e);
            } finally {
                Streams.close(inputStream);
            }
        }
        return null;
    }

    /**
     * Closes the supplied HTTP request / response resources silently.
     *
     * @param request The HTTP request to reset
     * @param response The HTTP response to consume and close
     */
    private static void close(HttpRequestBase request, HttpResponse response) {
        if (null != response) {
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                try {
                    EntityUtils.consume(entity);
                } catch (Exception e) {
                    LOG.debug("Error consuming HTTP response entity", e);
                }
            }
        }
        if (null != request) {
            try {
                request.reset();
            } catch (Exception e) {
                LOG.debug("Error resetting HTTP request", e);
            }
        }
    }

}
