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
        return DAVPushUtility.CLIENT_CALDAV.equals(client) || DAVPushUtility.CLIENT_CARDDAV.equals(client);
    }

    @Override
    public void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        DavPushResponse pushResponse = push(notification, matches);
        handlePushResponse(pushResponse);
    }

    private void handlePushResponse(DavPushResponse pushResponse) {
        if (null == pushResponse) {
            return;
        }
        List<String> noSubscribers = pushResponse.getNoSubscribers();
        if (null != noSubscribers && 0 < noSubscribers.size()) {
            PushSubscriptionRegistry subscriptionRegistry = factory.getOptionalService(PushSubscriptionRegistry.class);
            if (null == subscriptionRegistry) {
                LOG.warn("unable to remove no-subscribers", ServiceExceptionCode.absentService(PushSubscriptionRegistry.class));
            } else {
                for (String obsoleteTopic : noSubscribers) {
                    try {
                        subscriptionRegistry.unregisterSubscription(obsoleteTopic, transportOptions.getTransportID());
                    } catch (OXException e) {
                        LOG.error("error unregistering subsciptions for {}", obsoleteTopic, e);
                    }
                }
            }
        }
    }

    @Override
    public String getId() {
        return transportOptions.getTransportID();
    }

    @Override
    public boolean servesClient(String client) throws OXException {
        return DAVPushUtility.CLIENT_CALDAV.equals(client) || DAVPushUtility.CLIENT_CARDDAV.equals(client);
    }

    /**
     * Gets the used tranport options.
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
        HttpPost post = null;
        HttpResponse response = null;
        try {
            post = new HttpPost(transportOptions.getGatewayUrl() + transportOptions.getApplicationID());
            post.setEntity(new StringEntity(subscribeData.toString(), ContentType.APPLICATION_JSON));
            LOG.trace("Performing subscribe request at {}:{}{}", post.getURI(), System.lineSeparator(), subscribeData);
            response = httpClient.execute(post);
            StatusLine statusLine = response.getStatusLine();
            LOG.trace("Got {} from push gateway.", statusLine);
            if (null != statusLine && HttpServletResponse.SC_OK == statusLine.getStatusCode()) {
                return parseSubscribeResponse(response.getEntity());
            }
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(String.valueOf(statusLine));

        } catch (IOException e) {
            throw OXException.general("", e);
        } finally {
            close(post, response);
        }
    }

    /**
     * Unsubscribes a client at the gateway.
     *
     * @param clientData The client data to pass
     */
    public String unsubscribe(Object clientData) throws OXException {
        //TODO necessary?
        return transportOptions.getApplicationID(); //TODO: extract from or use response's "push-url"?
    }

    private DavPushResponse push(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        JSONObject pushData = createPushData(notification, matches);
        HttpPost post = null;
        HttpResponse response = null;
        try {
            post = new HttpPost(transportOptions.getGatewayUrl() + transportOptions.getApplicationID());
            post.setEntity(new StringEntity(pushData.toString(), ContentType.APPLICATION_JSON));
            LOG.trace("Performing push notification request at {}:{}{}", post.getURI(), System.lineSeparator(), pushData);
            response = httpClient.execute(post);
            StatusLine statusLine = response.getStatusLine();
            LOG.trace("Got {} from push gateway.", statusLine);
            if (null != statusLine && HttpServletResponse.SC_OK == statusLine.getStatusCode()) {
                return parsePushResponse(response.getEntity());
            }
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(String.valueOf(statusLine));
        } catch (IOException e) {
            throw OXException.general("", e);
        } finally {
            close(post, response);
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
            pushSubscribeObject.put("transport", transportObject);
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
        Integer priority = (Integer) messageData.get("priority");
        Long timestamp = (Long) messageData.get("timestamp");
        Object clientId = messageData.get("client-id");
        try {
            JSONObject pushData = new JSONObject();
            JSONObject pushObject = new JSONObject();
            pushData.put("push", pushObject);
            JSONArray jsonMessages = new JSONArray(matches.size());
            for (PushMatch match : matches) {
                JSONObject messageObject = new JSONObject();
                messageObject.put("topic", DAVPushUtility.getPushKey(match.getTopic(), match.getContextId(), match.getUserId()));
                messageObject.put("priority", null == priority ? 50 : priority.intValue());
                messageObject.put("timestamp", DAVPushUtility.UTC_DATE_FORMAT.get().format(new Date(timestamp.longValue())));
                messageObject.putOpt("client-id", clientId);
                jsonMessages.put(messageObject);
            }
            pushObject.put("messages", jsonMessages);
            return pushData;
        } catch (JSONException e) {
            throw OXException.general("", e);
        }
    }

    private String parseSubscribeResponse(HttpEntity entity) throws OXException {
        JSONObject jsonObject = parseJSONObject(entity);
        LOG.trace("Got subscribe response {}", jsonObject);
        return transportOptions.getApplicationID(); //TODO: extract from or use response's "push-url"?
    }

    private DavPushResponse parsePushResponse(HttpEntity entity) throws OXException {
        JSONObject jsonObject = parseJSONObject(entity);
        LOG.trace("Got push response {}", jsonObject);
        if (null != jsonObject) {
            JSONObject pushResponseObject = jsonObject.optJSONObject("push-response");
            if (null != pushResponseObject) {
                JSONArray noSubscribersArray = pushResponseObject.optJSONArray("no-subscribers");
                if (null != noSubscribersArray && 0 < noSubscribersArray.length()) {
                    List<String> noSubscribers = new ArrayList<String>(noSubscribersArray.length());
                    try {
                        for (int i = 0; i < noSubscribersArray.length(); i++) {
                            JSONObject noSubscribersObject = noSubscribersArray.getJSONObject(i);
                            noSubscribers.add(noSubscribersObject.getString("topic"));
                        }
                        return new DavPushResponse(noSubscribers);
                    } catch (JSONException e) {
                        throw PushExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
                    }
                }
            }
        }
        return null;
    }

    private static JSONObject parseJSONObject(HttpEntity entity) throws OXException {
        if (null != entity && ContentType.APPLICATION_JSON.equals(ContentType.get(entity))) {
            InputStream inputStream = null;
            try {
                inputStream = entity.getContent();
                try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                    return new JSONObject(reader);
                }
            } catch (UnsupportedOperationException | IOException | JSONException e) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
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
