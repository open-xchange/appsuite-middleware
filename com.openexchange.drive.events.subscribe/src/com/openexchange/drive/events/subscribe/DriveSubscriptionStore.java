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

package com.openexchange.drive.events.subscribe;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DriveSubscriptionStore}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveSubscriptionStore {

    /**
     * Adds one ore more subscriptions for the device identified by the supplied token to the specified root folder IDs. Any previous
     * registrations of the device in the same context using the same service ID / root folder ID combination are replaced implicitly.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param token The device's registration token
     * @param rootFolderIDs The root folder IDs
     * @param mode The subscription mode
     * @return The subscriptions
     */
    List<Subscription> subscribe(Session session, String serviceID, String token, List<String> rootFolderIDs, SubscriptionMode mode) throws OXException;

    /**
     * Adds a subscription for the device identified by the supplied token to the specified root folder ID. Any previous registrations
     * of the device in the same context using the same service ID / root folder ID combination are replaced implicitly.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param token The device's registration token
     * @param rootFolderID The root folder ID
     * @param mode The subscription mode
     * @return The subscription
     */
    Subscription subscribe(Session session, String serviceID, String token, String rootFolderID, SubscriptionMode mode) throws OXException;

    /**
     * Removes all subscriptions of the device with the given registration token.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param token The device's registration token
     * @return <code>true</code> if a subscription was removed, <code>false</code>, otherwise
     */
    boolean unsubscribe(Session session, String serviceID, String token) throws OXException;

    /**
     * Removes subscriptions for specific root folder IDs of the device with the given registration token.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param token The device's registration token
     * @param rootFolderIDs The root folder IDs, or <code>null</code> to remove the subscriptions for all root folders
     * @return <code>true</code> if a subscription was removed, <code>false</code>, otherwise
     */
    boolean unsubscribe(Session session, String serviceID, String token, List<String> rootFolderIDs) throws OXException;

    /**
     * Updates the registration ID for a device.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param oldToken The old registration token
     * @param newToken The new registration token
     * @return <code>true</code> if a subscription was updated, <code>false</code>, otherwise
     */
    boolean updateToken(Session session, String serviceID, String oldToken, String newToken) throws OXException;

    /**
     * Updates the registration ID for a device.
     *
     * @param contextID The context ID
     * @param serviceID The service ID
     * @param oldToken The old registration token
     * @param newToken The new registration token
     * @return <code>true</code> if a subscription was updated, <code>false</code>, otherwise
     */
    boolean updateToken(int contextID, String serviceID, String oldToken, String newToken) throws OXException;

    /**
     * Gets the subscriptions of all devices registered to one of the supplied root folder IDs.
     *
     * @param contextID The context ID
     * @param serviceIDs The service IDs
     * @param rootFolderIDs The root folder IDs
     * @return The subscriptions
     */
    List<Subscription> getSubscriptions(int contextID, String[] serviceIDs, Collection<String> rootFolderIDs) throws OXException;

    /**
     * Removes all stored subscriptions for the device with the supplied registration token throughout all contexts.
     *
     * @param serviceID The service ID
     * @param token The device token to remove the subscriptions for
     * @param timestamp The timestamp to catch concurrent re-registrations, i.e. only subscriptions that were created before the supplied
     *                  timestamp are removed
     * @return The number of removed subscriptions
     * @throws OXException
     */
    int removeSubscriptions(String serviceID, String token, long timestamp) throws OXException;

    /**
     * Removes all stored subscriptions for the device with the supplied registration token.
     *
     * @param contextID The context ID
     * @param serviceID The service ID
     * @param token The device token to remove the subscriptions for
     * @return The number of removed subscriptions
     * @throws OXException
     */
    int removeSubscriptions(int contextID, String serviceID, String token) throws OXException;

    /**
     * Removes the supplied subscription from the store.
     *
     * @param subscription The subscription to remove
     * @return <code>true</code> if the subscription was removed, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean removeSubscription(Subscription subscription) throws OXException;

    /**
     * Gets all subscription for a service.
     *
     * @param serviceID The service ID
     * @return The subscriptions
     * @throws OXException
     */
    List<Subscription> getSubscriptions(String serviceID) throws OXException;

}
