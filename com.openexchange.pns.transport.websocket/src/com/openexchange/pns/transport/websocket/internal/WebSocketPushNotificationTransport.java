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

package com.openexchange.pns.transport.websocket.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.osgi.util.RankedService;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.websocket.WebSocketOptions;
import com.openexchange.pns.transport.websocket.WebSocketOptionsPerClient;
import com.openexchange.pns.transport.websocket.WebSocketOptionsProvider;

/**
 * {@link WebSocketPushNotificationTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketPushNotificationTransport extends ServiceTracker<WebSocketOptionsProvider, WebSocketOptionsProvider> implements PushNotificationTransport {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketPushNotificationTransport.class);

    private static final String ID = "websocket";

    // https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html
    private static final int STATUS_INVALID_TOKEN_SIZE = 5;

    private static final int STATUS_INVALID_TOKEN = 8;

    private static final int MAX_PAYLOAD_SIZE = 256;

    // ---------------------------------------------------------------------------------------------------------------

    private final PushSubscriptionRegistry subscriptionRegistry;
    private final PushMessageGeneratorRegistry generatorRegistry;
    private final SortableConcurrentList<RankedService<WebSocketOptionsProvider>> trackedProviders;
    private ServiceRegistration<PushNotificationTransport> registration; // non-volatile, protected by synchronized blocks

    /**
     * Initializes a new {@link WebSocketPushNotificationTransport}.
     */
    public WebSocketPushNotificationTransport(PushSubscriptionRegistry subscriptionRegistry, PushMessageGeneratorRegistry generatorRegistry, BundleContext context) {
        super(context, WebSocketOptionsProvider.class, null);
        this.trackedProviders = new SortableConcurrentList<RankedService<WebSocketOptionsProvider>>();
        this.generatorRegistry = generatorRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public synchronized WebSocketOptionsProvider addingService(ServiceReference<WebSocketOptionsProvider> reference) {
        int ranking = RankedService.getRanking(reference);
        WebSocketOptionsProvider provider = context.getService(reference);

        trackedProviders.addAndSort(new RankedService<WebSocketOptionsProvider>(provider, ranking));

        if (null == registration) {
            registration = context.registerService(PushNotificationTransport.class, this, null);
        }

        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<WebSocketOptionsProvider> reference, WebSocketOptionsProvider provider) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<WebSocketOptionsProvider> reference, WebSocketOptionsProvider provider) {
        trackedProviders.remove(new RankedService<WebSocketOptionsProvider>(provider, RankedService.getRanking(reference)));

        if (trackedProviders.isEmpty() && null != registration) {
            registration.unregister();
            registration = null;
        }

        context.ungetService(reference);
    }

    // ---------------------------------------------------------------------------------------------------------

    private WebSocketOptions getHighestRankedApnOptionsFor(String client) throws OXException {
        List<RankedService<WebSocketOptionsProvider>> list = trackedProviders.getSnapshot();
        for (RankedService<WebSocketOptionsProvider> rankedService : list) {
            WebSocketOptions options = rankedService.service.getOptions(client);
            if (null != options) {
                return options;
            }
        }
        throw PushExceptionCodes.UNEXPECTED_ERROR.create("No options found for client: " + client);
    }

    private Map<String, WebSocketOptions> getAllHighestRankedApnOptions() {
        List<RankedService<WebSocketOptionsProvider>> list = trackedProviders.getSnapshot();
        Collections.reverse(list);
        Map<String, WebSocketOptions> options = new LinkedHashMap<>();
        for (RankedService<WebSocketOptionsProvider> rankedService : list) {
            Collection<WebSocketOptionsPerClient> availableOptions = rankedService.service.getAvailableOptions();
            for (WebSocketOptionsPerClient ao : availableOptions) {
                options.put(ao.getClient(), ao.getOptions());
            }
        }
        return options;
    }

    @Override
    public boolean servesClient(String client) throws OXException {
        try {
            return null != getHighestRankedApnOptionsFor(client);
        } catch (OXException x) {
            return false;
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException {
        if (null != notification && null != matches) {

        }
    }

}
