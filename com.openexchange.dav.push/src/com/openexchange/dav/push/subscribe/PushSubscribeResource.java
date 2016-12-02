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

package com.openexchange.dav.push.subscribe;

import static com.openexchange.dav.push.DAVPushUtility.PUSH_NS;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.push.DAVPushUtility;
import com.openexchange.dav.push.apn.DAVApnOptions;
import com.openexchange.dav.push.apn.DAVApnOptionsProvider;
import com.openexchange.dav.push.gcm.DavPushGateway;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.pns.DefaultPushSubscription;
import com.openexchange.pns.KnownTransport;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link PushSubscribeResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class PushSubscribeResource extends DAVResource {

    private final String clientId;
    private final PushSubscribeFactory factory;

    /**
     * Initializes a new {@link PushSubscribeResource}.
     *
     * @param factory The factory
     * @param clientId The client identifier to use
     * @param url The WebDAV path of the resource
     */
    public PushSubscribeResource(PushSubscribeFactory factory, String clientId, WebdavPath url) {
        super(factory, url);
        this.clientId = clientId;
        this.factory = factory;
    }

    /**
     * Handles a push subscription request.
     *
     * @param request The WebDAV request
     * @param response The WebDAV response
     * @return <code>true</code> if handled, <code>false</code>, otherwise
     */
    public boolean handle(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        if (Strings.isNotEmpty(request.getParameter("token")) && Strings.isNotEmpty(request.getParameter("key"))) {
            /*
             * Apple (APSD) push transport
             */
            handleAPSDSubscribe(request.getParameter("token"), request.getParameter("key"));
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        Document document = null;
        try {
            document = request.getBodyAsDocument();
            Element rootElement = document.getRootElement();
            if (null != rootElement && PUSH_NS.equals(rootElement.getNamespace()) && "subscribe".equals(rootElement.getName())) {
                /*
                 * DAV-PUSH subscribe
                 */
                handleDavPushSubscribe(rootElement);
                response.setStatus(HttpServletResponse.SC_OK);
                return true;
            }
        } catch (JDOMException | IOException e) {
            throw DAVProtocol.protocolException(request.getUrl(), e, HttpServletResponse.SC_BAD_REQUEST);
        }
        /*
         * bad request, otherwise
         */
        throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
    }

    private boolean handleDavPushSubscribe(Element subscribeElement) throws WebdavProtocolException {
        /*
         * get appropriate gateway
         */
        Element selectedTransportElement = subscribeElement.getChild("selected-transport", PUSH_NS);
        String transportUri = selectedTransportElement.getChildText("transport-uri", PUSH_NS);
        DavPushGateway pushGateway = getPushGateway(transportUri);
        if (null == pushGateway) {
            throw DAVProtocol.protocolException(getUrl(), PushExceptionCodes.NO_SUCH_TRANSPORT.create(transportUri), HttpServletResponse.SC_BAD_REQUEST);
        }
        String clientData = selectedTransportElement.getChildText("client-data", PUSH_NS);
        List<String> pushKeys = extractPushKeys(subscribeElement.getChildren("topic", PUSH_NS));
        Date expires = extractExpires(subscribeElement.getChild("expires", PUSH_NS));
        if (null != pushKeys && 0 < pushKeys.size()) {
            /*
             * perform the registration at push gateway and store the subscribed topics
             */
            try {
                List<String> topics = DAVPushUtility.extractTopics(pushKeys, factory.getContext().getContextId(), factory.getUser().getId());
                String token = pushGateway.subscribe(pushKeys, clientData, expires);
                registerSubscription(pushGateway.getOptions().getTransportID(), token, topics, expires);
            } catch (OXException e) {
                throw DAVProtocol.protocolException(getUrl(), e, HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            /*
             * unsubscribe for all topics
             */
            try {
                String token = pushGateway.unsubscribe(clientData);
                unregisterSubscription(pushGateway.getOptions().getTransportID(), token);
            } catch (OXException e) {
                throw DAVProtocol.protocolException(getUrl(), e, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return true;
    }

    private boolean handleAPSDSubscribe(String token, String key) throws WebdavProtocolException {
        String topic;
        try {
            topic = DAVPushUtility.extractTopic(key, factory.getContext().getContextId(), factory.getUser().getId());
        } catch (OXException e) {
            throw DAVProtocol.protocolException(getUrl(), e, HttpServletResponse.SC_BAD_REQUEST);
        }
        /*
         * subscribe
         */
        DAVApnOptions apnOptions = getApnOptions();
        if (null == apnOptions) {
            throw DAVProtocol.protocolException(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        registerSubscription(KnownTransport.APNS.getTransportId(), token, Collections.singletonList(topic), getExpires(apnOptions.getRefreshInterval()));
        return true;
    }

    /**
     * Subscribes a client to one or more push topics using this resource's transport- and client-identifiers.
     *
     * @param transportId The trasnport identifier to use
     * @param token The unqiue client token to use
     * @param topics The topics to subscribe to
     * @param expires The expiration date of the subscription, or <code>null</code> if not defined
     */
    protected void registerSubscription(String transportId, String token, List<String> topics, Date expires) throws WebdavProtocolException {
        PushSubscriptionRegistry subscriptionRegistry = factory.getOptionalService(PushSubscriptionRegistry.class);
        if (null == subscriptionRegistry) {
            DAVProtocol.protocolException(getUrl(), ServiceExceptionCode.absentService(PushSubscriptionRegistry.class), HttpServletResponse.SC_BAD_REQUEST);
        }
        DefaultPushSubscription subscription = DefaultPushSubscription.builder()
            .client(clientId)
            .topics(topics)
            .contextId(factory.getContext().getContextId())
            .token(token)
            .transportId(transportId)
            .userId(factory.getUser().getId())
//TODO            .expires(expires)
        .build();
        try {
            subscriptionRegistry.registerSubscription(subscription);
        } catch (OXException e) {
            DAVProtocol.protocolException(getUrl(), e, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    protected void unregisterSubscription(String transportId, String token) throws WebdavProtocolException {
        PushSubscriptionRegistry subscriptionRegistry = factory.getOptionalService(PushSubscriptionRegistry.class);
        if (null == subscriptionRegistry) {
            DAVProtocol.protocolException(getUrl(), ServiceExceptionCode.absentService(PushSubscriptionRegistry.class), HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            subscriptionRegistry.unregisterSubscription(token, transportId);
        } catch (OXException e) {
            DAVProtocol.protocolException(getUrl(), e, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return url.name();
    }

    private DAVApnOptions getApnOptions() {
        DAVApnOptionsProvider optionsProvider = factory.getApnOptionsProvider();
        return null != optionsProvider ? optionsProvider.getOptions(clientId) : null;
    }

    private static Date getExpires(int refreshInterval) {
        Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.add(Calendar.SECOND, refreshInterval);
        return calendar.getTime();
    }

    private DavPushGateway getPushGateway(String transportUri) {
        List<DavPushGateway> pushGateways = factory.getGateways();
        if (null != pushGateways && 0 < pushGateways.size()) {
            for (DavPushGateway pushGateway : pushGateways) {
                if (pushGateway.getOptions().getTransportURI().equals(transportUri)) {
                    return pushGateway;
                }
            }
        }
        return null;
    }

    private Date extractExpires(Element expiresElement) throws WebdavProtocolException {
        if (null != expiresElement && Strings.isNotEmpty(expiresElement.getText())) {
            try {
                return DAVPushUtility.UTC_DATE_FORMAT.get().parse(expiresElement.getText());
            } catch (ParseException e) {
                throw DAVProtocol.protocolException(getUrl(), e, HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return null;
    }

    private List<String> extractPushKeys(List<Element> topicElements) throws WebdavProtocolException {
        if (null != topicElements && 0 < topicElements.size()) {
            List<String> pushKeys = new ArrayList<String>(topicElements.size());
            for (Element topicElement : topicElements) {
                pushKeys.add(topicElement.getText());
            }
            return pushKeys;
        }
        return null;
    }

}
