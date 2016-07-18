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

package com.openexchange.pns.transport.apn;

import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.PushSubscriptionRegistry;
import javapns.Push;
import javapns.devices.Device;

/**
 * {@link ApnPushNotificationTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ApnPushNotificationTransport implements PushNotificationTransport {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnPushNotificationTransport.class);

    private static final String ID = "apn";

    private final ApnOptions options;
    private final PushSubscriptionRegistry subscriptionRegistry;

    /**
     * Initializes a new {@link ApnPushNotificationTransport}.
     */
    public ApnPushNotificationTransport(ApnOptions options, PushSubscriptionRegistry subscriptionRegistry) {
        super();
        this.options = options;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    @Override
    public void transport(PushNotification notification, Collection<PushSubscription> subscriptions) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getId() {
        return ID;
    }

    /**
     * Queries the feedback service and processes the received results, removing reported tokens from the subscription store if needed.
     */
    public void queryFeedbackService() {
        LOG.info("Querying APN feedback service for 'apn'...");
        long start = System.currentTimeMillis();

        List<Device> devices = null;
        try {
            devices = Push.feedback(options.getKeystore(), options.getPassword(), options.isProduction());
        } catch (Exception e) {
            LOG.warn("error querying feedback service", e);
        }

        if (null != devices && !devices.isEmpty()) {
            for (Device device : devices) {
                LOG.debug("Got feedback for device with token: {}, last registered: {}", device.getToken(), device.getLastRegister());
                int numRemoved = removeSubscriptions(device);
                LOG.info("Removed {} subscriptions for device with token: {}.", numRemoved, device.getToken());
            }
        } else {
            LOG.info("No devices to unregister received from feedback service.");
        }

        LOG.info("Finished processing APN feedback for 'apn' after {} ms.", (System.currentTimeMillis() - start));
    }

    private int removeSubscriptions(Device device) {
        if (null == device || null == device.getToken()) {
            LOG.warn("Unsufficient device information to remove subscriptions for: {}", device);
            return 0;
        }

        try {
            return subscriptionRegistry.unregisterSubscription(device.getToken(), ID);
        } catch (OXException e) {
            LOG.error("Error removing subscription", e);
        }
        return 0;
    }

}
