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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
     * Add a subscription for the device with given registration ID to the specified root folder ID.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param token The device's registration token
     * @param rootFolderID The root folder ID
     * @return The new subscription
     * @throws OXException
     */
    Subscription subscribe(Session session, String serviceID, String token, String rootFolderID) throws OXException;

    /**
     * Removes a subscription for the device with given registration ID to the specified root folder ID.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param token The device's registration token
     * @param contextID The context ID
     * @param rootFolderID The root folder ID
     */
    boolean unsubscribe(Session session, String serviceID, String token, String rootFolderID) throws OXException;

    /**
     * Updates the registration ID for a device.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param oldToken The old registration token
     * @param newToken The new registration token
     * @return The number of updated registrations
     */
    int updateToken(Session session, String serviceID, String oldToken, String newToken) throws OXException;

    /**
     * Gets the subscriptions of all devices registered to one of the supplied root folder IDs
     *
     * @param contextID The context ID
     * @param serviceID The service ID
     * @param rootFolderIDs The root folder IDs
     * @return The subscribers
     */
    List<Subscription> getSubscriptions(int contextID, String serviceID, Collection<String> rootFolderIDs) throws OXException;

}
