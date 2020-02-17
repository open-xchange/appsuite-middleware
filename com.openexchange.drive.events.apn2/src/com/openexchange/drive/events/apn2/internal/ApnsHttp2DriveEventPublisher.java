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

package com.openexchange.drive.events.apn2.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;


/**
 * {@link ApnsHttp2DriveEventPublisher} - The event publisher using APNS HTTP/2.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public abstract class ApnsHttp2DriveEventPublisher implements DriveEventPublisher {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnsHttp2DriveEventPublisher.class);
    }

    /** The tracked OSGi services */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link ApnsHttp2DriveEventPublisher}.
     *
     * @param services The tracked OSGi services
     */
    protected ApnsHttp2DriveEventPublisher(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean isLocalOnly() {
        return true;
    }

    /**
     * Gets the service identifier for this APNS HTTP/2 event publisher
     *
     * @return The service identifier
     */
    protected abstract String getServiceID();

    /**
     * Gets the APNS HTTP/2 options to use for specified context and user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The options
     * @throws OXException If options cannot be returned
     */
    protected abstract ApnsHttp2Options getOptions(int contextId, int userId) throws OXException;

    @Override
    public void publish(DriveEvent event) {
        // Query available subscriptions
        List<Subscription> subscriptions = null;
        try {
            DriveSubscriptionStore subscriptionStore = services.getService(DriveSubscriptionStore.class);
            subscriptions = subscriptionStore.getSubscriptions( event.getContextID(), new String[] { getServiceID() }, event.getFolderIDs());
        } catch (Exception e) {
            LoggerHolder.LOG.error("unable to get subscriptions for service {}", getServiceID(), e);
        }

        if (null == subscriptions) {
            // Nothing to do
            return;
        }

        int numOfSubscriptions = subscriptions.size();
        if (numOfSubscriptions == 0) {
            // Nothing to do
            return;
        }

        // Send push notification & handle response
        ThreadPoolService threadPool = services.getService(ThreadPoolService.class);
        if (null == threadPool || numOfSubscriptions == 1) {
            for (Subscription subscription : subscriptions) {

                // Get the APNS HTTP/2 options to use
                ApnsHttp2Options options;
                try {
                    options = getOptions(subscription.getContextID(), subscription.getUserID());
                } catch (OXException e) {
                    LoggerHolder.LOG.error("Unable to get APNS HTTP/2 options for service {} for user {} in context {}", getServiceID(), I(subscription.getUserID()), I(subscription.getContextID()), e);
                    return;
                }

                Task<Void> task = new ApnsSubscriptionDeliveryTask(subscription, event, options, services);
                try {
                    ThreadPools.execute(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LoggerHolder.LOG.warn("Interrupted while sending push notification for drive event for device token {}", subscription.getToken(), e);
                    return;
                } catch (Exception e) {
                    LoggerHolder.LOG.warn("Failed sending push notification for drive event to device with token {}", subscription.getToken(), e);
                }
            }
        } else {
            for (Subscription subscription : subscriptions) {

                // Get the APNS HTTP/2 options to use
                ApnsHttp2Options options;
                try {
                    options = getOptions(subscription.getContextID(), subscription.getUserID());
                } catch (OXException e) {
                    LoggerHolder.LOG.error("unable to get APNS HTTP/2 options for service {}", getServiceID(), e);
                    return;
                }

                Task<Void> task = new ApnsSubscriptionDeliveryTask(subscription, event, options, services);
                threadPool.submit(task, CallerRunsBehavior.getInstance());
            }
        }
    }

}
