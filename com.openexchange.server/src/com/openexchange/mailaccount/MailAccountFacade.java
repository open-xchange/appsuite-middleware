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

package com.openexchange.mailaccount;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailAccountFacade}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public interface MailAccountFacade {

    /**
     * Deletes the mail account identified by specified identifier.
     *
     * @param accountId The mail account identifier
     * @param properties Optional properties for delete event (passed to {@link MailAccountDeleteListener} instances)
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If the mail account cannot be deleted
     */
    void deleteMailAccount(int accountId, Map<String, Object> properties, int userId, int contextId) throws OXException;

    /**
     * Gets the default mail account belonging to specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The user's default mail account
     * @throws OXException If the default mail account cannot be returned
     */
    MailAccount getDefaultMailAccount(int userId, int contextId) throws OXException;

    /**
     * Gets the mail account identified by specified identifier.
     *
     * @param accountId The mail account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The mail account
     * @throws OXException If the mail account cannot be returned
     */
    MailAccount getMailAccount(int accountId, int userId, int contextId) throws OXException;

    /**
     * Gets the mail account identified by specified identifier.
     *
     * @param accountId The mail account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The mail account
     * @throws OXException If the mail account cannot be returned
     */
    MailAccount getMailAccount(int accountId, int userId, int contextId, Connection con) throws OXException;

    /**
     * Gets the mail accounts belonging to specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The user's mail accounts
     * @throws OXException If the mail accounts cannot be returned
     */
    MailAccount[] getUserMailAccounts(int userId, int contextId) throws OXException;

    /**
     * Inserts mail account's value taken from specified mail account.
     *
     * @param mailAccount The mail account containing the values to update.
     * @param userId The user identifier
     * @param context The context
     * @param session The session; set to <code>null</code> to insert mail account with an empty password
     * @param wcon writable database connection
     * @return The identifier of the newly created mail account
     * @throws OXException If the mail account cannot be updated
     */
    int insertMailAccount(MailAccountDescription mailAccount, int userId, Context context, ServerSession session, Connection wcon) throws OXException;

    /**
     * Invalidates user mail accounts.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If invalidation fails
     */
    void invalidateMailAccounts(int userId, int contextId) throws OXException;

    /**
     * Updates mail account's value taken specified {@code MailAccountDescription} instance.
     *
     * @param mailAccount The {@code MailAccountDescription} instance to read from
     * @param attributes The attributes to update
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param session The session
     * @param wcon writable database connection.
     * @param changePrimary <code>true</code> to change primary account, too.
     * @throws OXException If the mail account cannot be updated
     */
    void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int contextId, ServerSession session, Connection wcon, boolean changePrimary) throws OXException;


}
