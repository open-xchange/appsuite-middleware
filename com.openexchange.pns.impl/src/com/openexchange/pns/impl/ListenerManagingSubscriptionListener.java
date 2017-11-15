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

package com.openexchange.pns.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
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

            Session session = sessiond.getSession(sessionId);
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

            Session session = sessiond.getSession(sessionId);
            if (null == session) {
                // No such session
                return;
            }

            pushListenerService.unregisterPermanentListenerFor(session, subscription.getClient());
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
