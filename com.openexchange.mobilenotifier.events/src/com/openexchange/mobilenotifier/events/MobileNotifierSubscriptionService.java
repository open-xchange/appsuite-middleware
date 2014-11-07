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

package com.openexchange.mobilenotifier.events;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MobileNotifierSubscriptionService}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public interface MobileNotifierSubscriptionService {
    /**
     * Creates an subscription
     *
     * @param session
     * @param token
     * @param serviceId
     * @param providerId
     */
    Subscription createSubscription(ServerSession session, String token, String serviceId, MobileNotifierProviders providerId) throws OXException;

    /**
     * Updates a token to a new token
     *
     * @param userId
     * @param token
     * @param newToken
     */
    boolean updateToken(int userId, MobileNotifierProviders providerId, String token, String newToken) throws OXException;

    /**
     * Deletes a subscription by the userId and token
     *
     * @param userId
     * @param token
     */
    boolean deleteSubscription(int userId, String token, String serviceId) throws OXException;

    /**
     * Gets all subscriptions of the specified userId
     *
     * @param userId
     * @return
     */
    List<Subscription> getSubscriptions(int userId) throws OXException;

    /**
     * Gets a specific subscription from the user
     *
     * @param userId The userId
     * @param serviceId The serviceId e.g. gcm, apn, apn.macos
     * @param provider The provider
     * @return a subscription
     */
    Subscription getSubscription(int userId, String serviceId, MobileNotifierProviders provider) throws OXException;
}
