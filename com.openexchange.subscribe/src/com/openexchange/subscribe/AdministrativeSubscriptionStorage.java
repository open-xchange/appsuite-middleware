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

package com.openexchange.subscribe;

import java.sql.Connection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link AdministrativeSubscriptionStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public interface AdministrativeSubscriptionStorage extends SubscriptionStorage {

    /**
     * Returns all {@link Subscription}s for the specified context
     *
     * @param ctx The {@link Context}
     * @return all {@link Subscription}s for the specified context
     * @throws OXException if an error is occurred
     */
    List<Subscription> getSubscriptionsForContext(Context ctx) throws OXException;

    /**
     * Returns all {@link Subscription}s for the sspecified context
     *
     * @param ctx The {@link Context}
     * @param sourceId The source id a.k.a. the provider id
     * @return all {@link Subscription}s for the specified context
     * @throws OXException if an error is occurred
     */
    List<Subscription> getSubscriptionsForContextAndProvider(Context ctx, String sourceId) throws OXException;

    /**
     * Gets all {@link Subscription}s of a given source for a given context
     *
     * @param ctx The {@link Context}
     * @param sourceId The source id
     * @param con The connection to use
     * @return A list of {@link Subscription}s
     * @throws OXException if an error is occurred
     */
    List<Subscription> getSubscriptionsForContext(Context ctx, String sourceId, Connection con) throws OXException;

    /**
     * Removes the specified subscription
     *
     * @param ctx The {@link Context}
     * @param userId the user identifier
     * @param id The subscription identifier
     * @throws OXException if an error is occurred
     */
    void deleteSubscription(Context ctx, int userId, int id) throws OXException;

    /**
     * Removes the specified subscription
     *
     * @param ctx The {@link Context}
     * @param userId the user identifier
     * @param id The subscription identifier
     * @param connection The writable connection
     * @throws OXException if an error is occurred
     */
    void deleteSubscription(Context ctx, int userId, int id, Connection connection) throws OXException;

    /**
     * Deletes all {@link Subscription}s for the given user
     * 
     * @param userId The user id
     * @param ctx The {@link Context}
     * @param connection The writable connection
     * @throws OXException if an error is occurred
     */
    void deleteAllSubscriptionsForUser(int userId, Context ctx, Connection connection) throws OXException;
}
