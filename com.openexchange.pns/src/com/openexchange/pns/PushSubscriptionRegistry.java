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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns;

import com.openexchange.exception.OXException;

/**
 * {@link PushSubscriptionRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushSubscriptionRegistry {

    /**
     * Gets all subscriptions interested in specified topic belonging to given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param topic The topic
     * @return All subscriptions for specified affiliation mapped to the associated transport
     * @throws OXException If interested subscriptions cannot be returned
     */
    Hits getInterestedSubscriptions(int userId, int contextId, String topic) throws OXException;

    /**
     * Registers specified subscription.
     *
     * @param subscription The subscription to register
     * @throws OXException If registration fails
     */
    void registerSubscription(PushSubscription subscription) throws OXException;

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
