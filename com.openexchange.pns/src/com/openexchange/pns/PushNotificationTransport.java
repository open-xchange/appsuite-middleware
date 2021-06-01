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

package com.openexchange.pns;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link PushNotificationTransport} - Transports specified events to the push service end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushNotificationTransport {

    /**
     * Checks if this transport is enabled for specified topic, client and user.
     *
     * @param topic The topic that is about to be sent; or <code>null</code> to ignore topic restrictions
     * @param client The identifier of the client to send to; or <code>null</code> to ignore client restrictions
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isEnabled(String topic, String client, int userId, int contextId) throws OXException;

    /**
     * Transports a notification to the service provider
     *
     * @param notification The notification to transport
     * @param matches The associated matches
     * @throws OXException If given notification cannot be transported
     */
    void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException;

    /**
     * Transports multiple notifications to the service provider.
     *
     * @param notifications The notifications with their corresponding push matches to transport
     * @throws OXException If given notification cannot be transported
     */
    default void transport(Map<PushNotification, List<PushMatch>> notifications) throws OXException {
        if (null != notifications) {
            for (Map.Entry<PushNotification, List<PushMatch>> entry : notifications.entrySet()) {
                transport(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Gets this service's identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Checks if this transport serves the client denoted by given identifier
     *
     * @param client The client identifier
     * @return <code>true</code> if this transport serves the client; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean servesClient(String client) throws OXException;

}
