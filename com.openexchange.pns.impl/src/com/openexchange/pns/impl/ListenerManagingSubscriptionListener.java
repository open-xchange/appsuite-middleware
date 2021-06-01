/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.pns.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.pns.Interest;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.PushSubscriptionListener;
import com.openexchange.pns.PushSubscriptionProvider;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushUser;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;


/**
 * {@link ListenerManagingSubscriptionListener} - Starts/stops a mail push listener on added/removed permanent subscriptions having an interest for <code>"ox:mail:new"</code> topic.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ListenerManagingSubscriptionListener implements PushSubscriptionListener {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ListenerManagingSubscriptionListener.class);

    private final PushListenerService pushListenerService;
    private final SessiondService sessiond;
    private final PushSubscriptionRegistry subscriptionRegistry;

    /**
     * Initializes a new {@link ListenerManagingSubscriptionListener}.
     */
    public ListenerManagingSubscriptionListener(PushSubscriptionRegistry subscriptionRegistry, PushListenerService pushListenerService, SessiondService sessiond) {
        super();
        this.subscriptionRegistry = subscriptionRegistry;
        this.pushListenerService = pushListenerService;
        this.sessiond = sessiond;
    }

    @Override
    public boolean addingSubscription(PushSubscription subscription) throws OXException {
        // Ignore
        return true;
    }

    @Override
    public void addedSubscription(PushSubscription subscription) throws OXException {
        boolean interestedInNewMail = false;
        String newMailTopic = KnownTopic.MAIL_NEW.getName();
        for (Iterator<Interest> interests = Interest.interestsFor(subscription.getTopics()).iterator(); !interestedInNewMail && interests.hasNext();) {
            if (interests.next().isInterestedIn(newMailTopic)) {
                // Found interest for "ox:mail:new"
                interestedInNewMail = true;
            }
        }

        if (interestedInNewMail) {
            // Added a new subscription interested in "ox:mail:new"
            String sessionId = LogProperties.get(LogProperties.Name.SESSION_SESSION_ID);
            if (Strings.isEmpty(sessionId)) {
                // No chance to determine session
                return;
            }

            Session session = sessiond.peekSession(sessionId);
            if (null == session) {
                // No such session
                return;
            }

            boolean registered = pushListenerService.registerPermanentListenerFor(session, subscription.getClient());
            if (registered) {
                LOGGER.info("Successfully registered a permanent (mail) push listener for subscription interested in topic {} for client {} from user {} in context {}", newMailTopic, subscription.getClient(), I(session.getUserId()), I(session.getContextId()));
            }
        }
    }

    @Override
    public void removedSubscription(PushSubscription subscription) throws OXException {
        // Check if last subscription for that client was removed
        boolean stillInterestedInNewMail = subscriptionRegistry.hasInterestedSubscriptions(subscription.getClient(), subscription.getUserId(), subscription.getContextId(), KnownTopic.MAIL_NEW.getName());
        if (false == stillInterestedInNewMail) {
            // No subscription left having interest for "ox:mail:new"
            String sessionId = LogProperties.get(LogProperties.Name.SESSION_SESSION_ID);
            if (Strings.isEmpty(sessionId)) {
                // No chance to determine session
                return;
            }

            Session session = sessiond.peekSession(sessionId);
            if (null == session) {
                // No such session
                return;
            }

            pushListenerService.unregisterPermanentListenerFor(new PushUser(session.getUserId(), session.getContextId(), Optional.of(session.getSessionID())), subscription.getClient());
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean addingProvider(PushSubscriptionProvider provider) throws OXException {
        // Ignore
        return true;
    }

    @Override
    public void addedProvider(PushSubscriptionProvider provider) throws OXException {
        // Ignore
    }

    @Override
    public void removedProvider(PushSubscriptionProvider provider) throws OXException {
        // Ignore
    }

}
