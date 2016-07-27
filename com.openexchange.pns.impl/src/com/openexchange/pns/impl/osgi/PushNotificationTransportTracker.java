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

package com.openexchange.pns.impl.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.impl.PushNotificationTransportRegistry;

/**
 * {@link PushNotificationTransportTracker} - The tracker for transports.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public final class PushNotificationTransportTracker implements ServiceTrackerCustomizer<PushNotificationTransport, PushNotificationTransport>, PushNotificationTransportRegistry {

    private final ConcurrentMap<String, PushNotificationTransport> transportMap;
    private final BundleContext context;

    /**
     * Initializes a new {@link PushNotificationTransportTracker}.
     */
    public PushNotificationTransportTracker(BundleContext context) {
        super();
        this.transportMap = new ConcurrentHashMap<>(4, 0.9F, 1);;
        this.context = context;
    }

    @Override
    public PushNotificationTransport addingService(ServiceReference<PushNotificationTransport> reference) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationTransportTracker.class);

        PushNotificationTransport transport = context.getService(reference);
        if (null == transportMap.putIfAbsent(transport.getId(), transport)) {
            logger.info("Successfully registered '{}' push notification transport", transport.getId());
            return transport;
        }

        logger.error("Failed to register '{}' push notification transport for class {}. There is already such a transport.", transport.getId(), transport.getClass().getName());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<PushNotificationTransport> reference, PushNotificationTransport service) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<PushNotificationTransport> reference, PushNotificationTransport transport) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationTransportTracker.class);

        if (null != transportMap.remove(transport.getId())) {
            logger.info("Successfully unregistered '{}' push notification transport", transport.getId());
            return;
        }

        context.ungetService(reference);
    }

    @Override
    public PushNotificationTransport getTransportFor(String client, String transportId) throws OXException {
        if (null == client || null == transportId) {
            return null;
        }
        PushNotificationTransport transport = transportMap.get(transportId);
        return transport.servesClient(client) ? transport : null;
    }
}