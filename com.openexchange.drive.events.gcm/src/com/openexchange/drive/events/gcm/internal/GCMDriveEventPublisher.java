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

package com.openexchange.drive.events.gcm.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.android.gcm.Constants;
import com.google.android.gcm.Message;
import com.google.android.gcm.MulticastResult;
import com.google.android.gcm.Result;
import com.google.android.gcm.Sender;
import com.google.common.collect.Lists;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.gcm.GCMKeyProvider;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GCMDriveEventPublisher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GCMDriveEventPublisher implements DriveEventPublisher {

    private static final int MULTICAST_LIMIT = 1000;
    private static final String SERIVCE_ID = "gcm";
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GCMDriveEventPublisher.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link GCMDriveEventPublisher}.
     * 
     * @param services The service lookup reference
     */
    public GCMDriveEventPublisher(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean isLocalOnly() {
        return true;
    }

    @Override
    public void publish(DriveEvent event) {
        /*
         * get subscriptions from storage
         */
        DriveSubscriptionStore subscriptionStore = null;
        List<Subscription> subscriptions = null;
        try {
            subscriptionStore = requireService(DriveSubscriptionStore.class, services);
            subscriptions = subscriptionStore.getSubscriptions(event.getContextID(), new String[] { SERIVCE_ID }, event.getFolderIDs());
        } catch (OXException e) {
            LOG.error("unable to get subscriptions for service {}", SERIVCE_ID, e);
        }
        if (null == subscriptions || subscriptions.isEmpty()) {
            return;
        }
        /*
         * associate subscriptions per API key
         */
        String pushTokenReference = event.getPushTokenReference();
        Map<String, List<String>> registrationsByKey = new HashMap<String, List<String>>();
        for (Subscription subscription : subscriptions) {
            if (null != pushTokenReference && subscription.matches(pushTokenReference)) {
                LOG.trace("Skipping push notification for subscription: {}", subscription);
                continue;
            }
            String key = getGCMKey(subscription.getContextID(), subscription.getUserID());
            if (null == key) {
                LOG.debug("No API key available for push via FCM for subscription {}, skipping notification.", subscription);
                continue;
            }
            com.openexchange.tools.arrays.Collections.put(registrationsByKey, key, subscription.getToken());
        }
        /*
         * send push messages in chunks per API key & process results
         */
        Message message = getMessage(event);
        for (Entry<String, List<String>> entry : registrationsByKey.entrySet()) {
            Sender sender = new Sender(entry.getKey());
            for (List<String> registrationIDs : Lists.partition(entry.getValue(), MULTICAST_LIMIT)) {
                MulticastResult result = null;
                try {
                    result = sender.sendNoRetry(message, registrationIDs);
                } catch (IOException e) {
                    LOG.warn("error publishing drive event", e);
                }
                if (null != result) {
                    LOG.debug("{}", result);
                }
                /*
                 * process results
                 */
                processResult(subscriptionStore, event.getContextID(), registrationIDs, result);
            }
        }
    }

    /*
     * http://developer.android.com/google/gcm/http.html#success
     */
    private void processResult(DriveSubscriptionStore subscriptionStore, int contextID, List<String> registrationIDs, MulticastResult multicastResult) {
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
                        updateRegistrationIDs(subscriptionStore, contextID, registrationID, result.getCanonicalRegistrationId());
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
                        removeRegistrations(subscriptionStore, contextID, registrationID);
                    } else {
                        /*
                         * Otherwise, there is something wrong in the registration ID passed in the request; it is probably a non-
                         * recoverable error that will also require removing the registration from the server database. See Interpreting
                         * an error response for all possible error values.
                         */
                        LOG.warn("Received error {} when sending push message to {}, removing registration ID.", error, registrationID);
                        removeRegistrations(subscriptionStore, contextID, registrationID);
                    }
                }
            }
        }
    }

    private static void updateRegistrationIDs(DriveSubscriptionStore subscriptionStore, int contextID, String oldRegistrationID, String newRegistrationID) {
        try {
            if (subscriptionStore.updateToken(contextID, SERIVCE_ID, oldRegistrationID, newRegistrationID)) {
                 LOG.info("Successfully updated registration ID from {} to {}", oldRegistrationID, newRegistrationID);
            } else {
                LOG.warn("Registration ID {} not updated.", oldRegistrationID);
            }
        } catch (OXException e) {
            if ("DRV-0037".equals(e.getErrorCode())) {
                // Token is already registered, so delete obsolete registration instead
                LOG.warn("Registration ID {} already exists, removing obsolete registration ID {} instead.", newRegistrationID, oldRegistrationID);
                removeRegistrations(subscriptionStore, contextID, oldRegistrationID);
            } else {
                LOG.error("Error updating registration IDs", e);
            }
        }
    }

    private static void removeRegistrations(DriveSubscriptionStore subscriptionStore, int contextID, String registrationID) {
        try {
            if (0 < subscriptionStore.removeSubscriptions(contextID, SERIVCE_ID, registrationID)) {
                LOG.info("Successfully removed registration ID {}.", registrationID);
            } else {
                LOG.warn("Registration ID {} not removed.", registrationID);
            }
        } catch (OXException e) {
            LOG.error("Error removing registrations", e);
        }
    }

    private static Message getMessage(DriveEvent event) {
        return new Message.Builder()
            .collapseKey("TRIGGER_SYNC")
            .addData("action", "sync")
//            .addData("folders", event.getFolderIDs().toString())
        .build();
    }
    
    /**
     * Gets the API key to use for push notifications to GCM.
     *
     * @param contextId The context id
     * @param userId The user Id
     * @return The suitable GCM API key, or <code>null</code> if not available or configured
     */
    private String getGCMKey(int contextId, int userId) {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        if (null == configService) {
            LOG.warn("Unable to get configuration service to determine API key for push via FCM for user {} in context {}.", I(userId), I(contextId));
            return null;
        }
        /*
         * check if push via GCM is enabled
         */
        if (false == configService.getBooleanProperty(userId, contextId, DriveEventsGCMProperty.ENABLED)) {
            LOG.trace("Push via FCM is disabled for user {} in context {}.", I(userId), I(contextId));
            return null;
        }
        /*
         * get API key via config cascade if configured
         */
        String key = configService.getProperty(userId, contextId, DriveEventsGCMProperty.KEY);
        if (Strings.isNotEmpty(key)) {
            LOG.trace("Using configured API key for push via FCM for user {} in context {}.", I(userId), I(contextId));
            return key;
        }
        /*
         * check for a registered key provider as fallback, otherwise
         */
        GCMKeyProvider keyProvider = services.getOptionalService(GCMKeyProvider.class);
        if (null != keyProvider) {
            LOG.trace("Using fallback API key for push via FCM for user {} in context {}.", I(userId), I(contextId));
            return keyProvider.getKey();
        }
        LOG.trace("No API key available for push via FCM for user {} in context {}.", I(userId), I(contextId));
        return null;
    }
    
}
