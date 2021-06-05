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

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link PushSubscriptionRegistry} - A registry for retrieving and managing push subscriptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
@SingletonService
public interface PushSubscriptionRegistry extends PushSubscriptionProvider {

    /**
     * Registers specified subscription.
     *
     * @param subscription The subscription to register
     * @return The subscription result
     * @throws OXException If registration fails
     */
    PushSubscriptionResult registerSubscription(PushSubscription subscription) throws OXException;

    /**
     * Unregisters specified subscription.
     *
     * @param subscription The subscription to unregister
     * @return <code>true</code> if such a subscription has been deleted; otherwise <code>false</code> if no such subscription existed
     * @throws OXException If unregistration fails
     */
    boolean unregisterSubscription(PushSubscription subscription) throws OXException;

    /**
     * Unregisters all subscriptions associated with specified token and transport.
     *
     * @param token The token to unregister
     * @param transportId The identifier of the associated transport
     * @return The number of unregistered subscriptions
     * @throws OXException If unregistration fails
     */
    int unregisterSubscription(String token, String transportId) throws OXException;

   /**
    * Updates specified subscription.
    *
    * @param subscription The subscription to update
    * @param newToken The new token to set
    * @return <code>true</code> if such a subscription has been updated; otherwise <code>false</code> if no such subscription existed
    * @throws OXException If update fails
    */
   boolean updateToken(PushSubscription subscription, String newToken) throws OXException;

}
