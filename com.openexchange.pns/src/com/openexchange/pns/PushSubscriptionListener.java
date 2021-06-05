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

/**
 * {@link PushSubscriptionListener} - A listener for subscription registry events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushSubscriptionListener {

    /**
     * Invoked when a subscription is about to be added to registry.
     *
     * @param subscription The subscription to add
     * @return <code>true</code> to allow given subscription being added; otherwise <code>false</code>
     * @throws OXException If handling fails
     */
    boolean addingSubscription(PushSubscription subscription) throws OXException;

    /**
     * Invoked when a subscription was successfully added to registry.
     *
     * @param subscription The added subscription
     * @throws OXException If handling fails
     */
    void addedSubscription(PushSubscription subscription) throws OXException;

    /**
     * Invoked when a subscription was successfully removed from registry.
     *
     * @param subscription The removed subscription
     * @throws OXException If handling fails
     */
    void removedSubscription(PushSubscription subscription) throws OXException;

    // ----------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when a subscription provider is about to be added to registry.
     *
     * @param provider The subscription provider to add
     * @return <code>true</code> to allow given subscription provider being added; otherwise <code>false</code>
     * @throws OXException If handling fails
     */
    boolean addingProvider(PushSubscriptionProvider provider) throws OXException;

    /**
     * Invoked when a subscription provider was successfully added to registry.
     *
     * @param provider The added subscription provider
     * @throws OXException If handling fails
     */
    void addedProvider(PushSubscriptionProvider provider) throws OXException;

    /**
     * Invoked when a subscription provider was successfully removed from registry.
     *
     * @param provider The removed subscription provider
     * @throws OXException If handling fails
     */
    void removedProvider(PushSubscriptionProvider provider) throws OXException;

}
