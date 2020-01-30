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

package com.openexchange.drive.events.apn.internal;

import java.io.FileInputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.apn.APNAccess;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link APNDriveEventPublisher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class APNDriveEventPublisher implements DriveEventPublisher {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(APNDriveEventPublisher.class);
    protected static final String TOPIC_VANILLA_APP_IOS = "com.openexchange.drive";
    protected static final String TOPIC_VANILLA_APP_MACOS = "com.openxchange.drive.macos.OXDrive";

    /**
     * Initializes a new {@link APNDriveEventPublisher}.
     */
    public APNDriveEventPublisher() {
        super();
    }

    protected abstract String getServiceID();

    protected abstract APNAccess getAccess() throws OXException;

    @Override
    public void publish(DriveEvent event) {
        List<Subscription> subscriptions = null;
        try {
            subscriptions = Services.getService(DriveSubscriptionStore.class, true).getSubscriptions(
                event.getContextID(), new String[] { getServiceID() }, event.getFolderIDs());
        } catch (OXException e) {
            LOG.error("unable to get subscriptions for service {}", getServiceID(), e);
        }
        if (null != subscriptions && 0 < subscriptions.size()) {
            for (Subscription subscription : subscriptions) {
                try {
                    APNAccess access = getAccess();
                    ApnsHttp2Options options = getApn2Options(access);
                    Task<Void> task = new APNSubscriptionDeliveryTask(subscription, event, options, Services.getService(DriveSubscriptionStore.class));
                    ThreadPools.execute(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.warn("Interrupted while sending push notification for drive event for device token {}", subscription.getToken(), e);
                    return;
                } catch (Exception e) {
                    LOG.warn("Failed sending push notification for drive event to device with token {}", subscription.getToken(), e);
                }
            }
        }
    }

    private ApnsHttp2Options getApn2Options(APNAccess access) {
        Object store = access.getKeystore();
        String password = access.getPassword();
        boolean production = access.isProduction();
        String topic = access.getTopic();
        if (store instanceof byte[]) {
            return new ApnsHttp2Options((byte[]) store, password, production, topic);
        }
        if (store instanceof String) {
            FileInputStream in = null;
            try {
                in = new FileInputStream((String) store);
                byte[] data = IOUtils.toByteArray(in);
                return new ApnsHttp2Options(data, password, production, topic);
            } catch (Exception e) {
                LOG.error("Error loading keystore", e);
            } finally {
                Streams.close(in);
            }
        }
        return null;
    }

    @Override
    public boolean isLocalOnly() {
        return true;
    }

}
