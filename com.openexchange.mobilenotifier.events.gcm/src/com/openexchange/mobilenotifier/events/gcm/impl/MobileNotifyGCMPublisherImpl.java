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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier.events.gcm.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.android.gcm.Constants;
import com.google.android.gcm.Message;
import com.google.android.gcm.MulticastResult;
import com.google.android.gcm.Result;
import com.google.android.gcm.Sender;
import com.openexchange.exception.OXException;
import com.openexchange.mobilenotifier.events.MobileNotifyEvent;
import com.openexchange.mobilenotifier.events.MobileNotifyPublisher;
import com.openexchange.mobilenotifier.events.gcm.osgi.Services;
import com.openexchange.mobilenotifier.events.storage.MobileNotifierSubscriptionService;
import com.openexchange.mobilenotifier.events.storage.Subscription;


/**
 * {@link MobileNotifyGCMPublisherImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifyGCMPublisherImpl implements MobileNotifyPublisher {

    private static final int MULTICAST_LIMIT = 1000;

    private static final String SERIVCE_ID = "gcm";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobileNotifyGCMPublisherImpl.class);


    @Override
    public void publish(MobileNotifyEvent event) {
        List<Subscription> subscriptions = null;
        try {
            MobileNotifierSubscriptionService mnss = Services.getService(MobileNotifierSubscriptionService.class);
            subscriptions = mnss.getSubscription(event.getSession(), SERIVCE_ID, event.getProvider());
        } catch(OXException e) {
            LOG.error("Could not get subscription {}", SERIVCE_ID, e);
        }
        if(subscriptions != null && subscriptions.size() > 0) {
            if (null != subscriptions && 0 < subscriptions.size()) {
                Sender sender = null;
                try {
                    sender = getSender();
                } catch (OXException e) {
                    LOG.error("Error getting GCM sender", e);
                }
                if (null == sender) {
                    return;
                }
                for (int i = 0; i < subscriptions.size(); i += MULTICAST_LIMIT) {
                    /*
                     * prepare chunk
                     */
                    int length = Math.min(subscriptions.size(), i + MULTICAST_LIMIT) - i;
                    List<String> registrationIDs = new ArrayList<String>(length);
                    for (int j = 0; j < length; j++) {
                        registrationIDs.add(subscriptions.get(i + j).getToken());
                    }
                    /*
                     * send chunk
                     */
                    if (0 < registrationIDs.size()) {
                        MulticastResult result = null;
                        try {
                            result = sender.sendNoRetry(getMessage(event), registrationIDs);
                        } catch (IOException e) {
                            LOG.warn("error publishing mobile notification event", e);
                        }
                        if (null != result) {
                            LOG.debug("{}", result);
                        }
                        /*
                         * process results
                         */
                        processResult(event, registrationIDs, result);
                    }
                }
            }
        }
    }

    private static Message getMessage(MobileNotifyEvent event) {
        Message.Builder message = new Message.Builder();
        message.collapseKey(event.getCollapseKey());

        Map<String, String> messageData = event.getMessageData();
        for (Entry<String, String> entry : messageData.entrySet()) {
            message.addData(entry.getKey(), entry.getValue());
        }
        return message.build();
    }

    /*
     * http://developer.android.com/google/gcm/http.html#success
     */
    private void processResult(MobileNotifyEvent event, List<String> registrationIDs, MulticastResult multicastResult) {
        if (null == registrationIDs || null == multicastResult) {
            LOG.warn("Unable to process empty results");;
            return;
        }
        /*
         * If the value of failure and canonical_ids is 0, it's not necessary to parse the remainder of the response.
         */
        if (0 == multicastResult.getFailure() && 0 == multicastResult.getCanonicalIds()) {
            return;
        }
        /*
         * Otherwise, we recommend that you iterate through the results field...
         */
        List<Result> results = multicastResult.getResults();
        if (null != results && 0 < results.size()) {
            if (results.size() != registrationIDs.size()) {
                LOG.warn("Number of multicast results different from used regsitrations IDs, unable to process results");
            }
            /*
             *  ...and do the following for each object in that list:
             */
            for (int i = 0; i < results.size(); i++) {
                Result result = results.get(i);
                String registrationID = registrationIDs.get(i);
                if (null != result.getMessageId()) {
                    /*
                     * If message_id is set, check for registration_id:
                     */
                    if (null != result.getCanonicalRegistrationId()) {
                        /*
                         * If registration_id is set, replace the original ID with the new value (canonical ID) in your server database.
                         * Note that the original ID is not part of the result, so you need to obtain it from the list of
                         * code>registration_ids passed in the request (using the same index).
                         */
                        updateRegistrationIDs(event, registrationID, result.getCanonicalRegistrationId());
                    }
                } else {
                    /*
                     * Otherwise, get the value of error:
                     */
                    String error = result.getErrorCodeName();
                    if (Constants.ERROR_UNAVAILABLE.equals(error)) {
                        /*
                         * If it is Unavailable, you could retry to send it in another request.
                         */
                        LOG.warn("Push message could not be sent because the GCM servers were not available.");
                    } else if (Constants.ERROR_NOT_REGISTERED.equals(error)) {
                        /*
                         * If it is NotRegistered, you should remove the registration ID from your server database because the application
                         * was uninstalled from the device or it does not have a broadcast receiver configured to receive
                         * com.google.android.c2dm.intent.RECEIVE intents.
                         */
                        removeRegistrations(event, registrationID);
                    } else {
                        /*
                         * Otherwise, there is something wrong in the registration ID passed in the request; it is probably a non-
                         * recoverable error that will also require removing the registration from the server database. See Interpreting
                         * an error response for all possible error values.
                         */
                        LOG.warn("Received error {} when sending push message to {}, removing registration ID.", error, registrationID);
                        removeRegistrations(event, registrationID);
                    }
                }
            }
        }
    }

    private static void updateRegistrationIDs(MobileNotifyEvent event, String oldRegistrationID, String newRegistrationID) {
        try {
            if (Services.getService(MobileNotifierSubscriptionService.class, true).updateToken(
                event.getSession(), oldRegistrationID, SERIVCE_ID, newRegistrationID)) {
                LOG.info("Successfully updated registration ID from {} to {}", oldRegistrationID, newRegistrationID);
            } else {
                LOG.warn("Registration ID {} not updated.", oldRegistrationID);
            }
        } catch (OXException e) {
            LOG.error("Error updating registration IDs", e);
        }
    }

    private static void removeRegistrations(MobileNotifyEvent event, String registrationID) {
        try {
            if (true == Services.getService(MobileNotifierSubscriptionService.class, true).deleteSubscriptions(
                event.getSession(), registrationID, SERIVCE_ID)) {
                LOG.info("Successfully removed registration ID {}.", registrationID);
            } else {
                LOG.warn("Registration ID {} not removed.", registrationID);
            }
        } catch (OXException e) {
            LOG.error("Error removing registrations", e);
        }
    }

    /**
     * Gets a GCM sender based on the configured API key.
     *
     * @return The GCM sender
     * @throws OXException
     */
    private static Sender getSender() throws OXException {
        return new Sender("");
    }
}
