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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.events.gcm.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link GCMDriveEventPublisher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GCMDriveEventPublisher implements DriveEventPublisher {

    private static final String SERIVCE_ID = "gcm";
    private static final Log LOG = com.openexchange.log.Log.loggerFor(GCMDriveEventPublisher.class);

    private final Sender sender;

    public GCMDriveEventPublisher(String key) {
        super();
        this.sender = new Sender(key);
    }

    public GCMDriveEventPublisher() throws OXException {
        this(getKey());
    }

    @Override
    public void publish(DriveEvent event) {
        List<Subscription> subscriptions = null;
        try {
            subscriptions = Services.getService(DriveSubscriptionStore.class, true).getSubscriptions(
                SERIVCE_ID, event.getContextID(), event.getFolderIDs());
        } catch (OXException e) {
            LOG.error("unable to get subscriptions for service " + SERIVCE_ID, e);
        }
        if (null != subscriptions && 0 < subscriptions.size()) {
            List<String> regIDs = getRegIDs(subscriptions); //TODO: split - multicast limit is 1000?
            MulticastResult result = null;
            try {
                result = sender.sendNoRetry(getMessage(event), regIDs);
            } catch (IOException e) {
                LOG.warn("error publishing drive event", e);
            }
            if (null != result && LOG.isDebugEnabled()) {
                LOG.debug(result);
            }
        }
    }

    private static Message getMessage(DriveEvent event) {
        return new Message.Builder().addData("folders", event.getFolderIDs().toString()).build();
    }

    private static List<String> getRegIDs(List<Subscription> subscriptions) {
        List<String> regIDs = new ArrayList<String>(subscriptions.size());
        for (Subscription subscription : subscriptions) {
            regIDs.add(subscription.getToken());
        }
        return regIDs;
    }

    private static String getKey() throws OXException {
        String property = "com.openexchange.drive.events.gcm.key";
        String key = Services.getService(ConfigurationService.class, true).getProperty(property);
        if (Strings.isEmpty(key)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property);
        }
        return key;
    }

}
