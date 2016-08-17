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
     * @return The subscriptions
     */
    List<Subscription> subscribe(Session session, String serviceID, String token, List<String> rootFolderIDs) throws OXException;

    /**
     * Adds a subscription for the device identified by the supplied token to the specified root folder ID. Any previous registrations
     * of the device in the same context using the same service ID / root folder ID combination are replaced implicitly.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param token The device's registration token
     * @param rootFolderID The root folder ID
     * @return The subscription
     */
    Subscription subscribe(Session session, String serviceID, String token, String rootFolderID) throws OXException;

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
