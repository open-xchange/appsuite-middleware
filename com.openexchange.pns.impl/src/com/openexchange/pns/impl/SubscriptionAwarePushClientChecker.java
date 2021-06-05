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

import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.push.PushClientChecker;
import com.openexchange.session.Session;


/**
 * {@link SubscriptionAwarePushClientChecker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SubscriptionAwarePushClientChecker implements PushClientChecker {

    private final PushSubscriptionRegistry registry;

    /**
     * Initializes a new {@link SubscriptionAwarePushClientChecker}.
     */
    public SubscriptionAwarePushClientChecker(PushSubscriptionRegistry subscriptionRegistry) {
        super();
        this.registry = subscriptionRegistry;

    }

    @Override
    public boolean isAllowed(String clientId, Session session) throws OXException {
        if (null == session || Strings.isEmpty(clientId)) {
            // Unable to check
            return false;
        }

        return registry.hasInterestedSubscriptions(clientId, session.getUserId(), session.getContextId(), KnownTopic.MAIL_NEW.getName());

        /*-
         * Apparently PNS service has been registered.
         *
         * Thus simply signal to allow as potentially every client may subscribe to "ox:mail:new" event, since:
         * Conditions/dependencies like:
         *  - Exists such a subscription?
         *  - Or is there a Web Socket?
         *  - Will a Web Socket be opened later on?
         *  - Will there be subscription later on?
         *  - Is it safe to drop listener because Web Socket teared down?
         * are too complex to handle robustly
         */
    }

}
