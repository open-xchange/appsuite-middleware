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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link MailAccountStorageService} - The storage service for mail accounts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface MailAccountStorageService {

    /**
     * Invalidates specified mail account.
     *
     * @param id The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If invalidation fails
     */
    void invalidateMailAccount(int id, int userId, int contextId) throws OXException;

    /**
     * Invalidates user mail accounts.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If invalidation fails
     */
    void invalidateMailAccounts(int userId, int contextId) throws OXException;

    /**
     * Clears full names for specified mail account.
     *
     * @param id The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If invalidation fails
     */
    void clearFullNamesForMailAccount(int id, int userId, int contextId) throws OXException;

    /**
     * Clears specified full names for specified mail account.
     *
     * @param id The account identifier
     * @param indexes The indexes of the full names to clear
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If invalidation fails
     */
    void clearFullNamesForMailAccount(int id, int[] indexes, int userId, int contextId) throws OXException;

    /**
     * Sets specified full names for specified mail account.
     *
     * @param id The account identifier
     * @param indexes The indexes of the full names to set
     * @param fullNames The full names to set
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If invalidation fails
     */
    void setFullNamesForMailAccount(int id, int[] indexes, String[] fullNames, int userId, int contextId) throws OXException;

    /**
     * Sets specified names for specified mail account.
     *
     * @param id The account identifier
     * @param indexes The indexes of the full names to set
     * @param names The names to set
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If invalidation fails
     */
    void setNamesForMailAccount(int id, int[] indexes, String[] names, int userId, int contextId) throws OXException;

    /**
     * Checks if the mail account referenced by specified identifier does exist.
     *
     * @param id The mail account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws OXException If check for existence fails
     */
    boolean existsMailAccount(int id, int userId, int contextId) throws OXException;

    /**
     * Gets the mail account identified by specified identifier.
     *
     * @param id The mail account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The mail account
     * @throws OXException If the mail account cannot be returned
     */
    MailAccount getMailAccount(int id, int userId, int contextId) throws OXException;

    /**
     * Gets the mail account identified by specified identifier.
     * <p>
     * <b>For internal use only.</b>
     *
     * @param id The mail account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The mail account
     * @throws OXException If the mail account cannot be returned
     */
    MailAccount getRawMailAccount(int id, int userId, int contextId) throws OXException;

    /**
     * Gets the mail account identified by specified identifier.
     *
     * @param id The mail account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The mail account
     * @throws OXException If the mail account cannot be returned
     */
    MailAccount getMailAccount(int id, int userId, int cid, Connection con) throws OXException;

    /**
     * Gets the transport account identified by specified identifier.
     *
     * @param accountId The transport account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The transport account
     * @throws OXException If the transport account cannot be returned
     */
    TransportAccount getTransportAccount(int accountId, int userId, int contextId, Connection con) throws OXException;

    /**
     * Gets the transport account identified by specified identifier.
     *
     * @param accountId The transport account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The transport account
     * @throws OXException If the transport account cannot be returned
     */
    TransportAccount getTransportAccount(int accountId, int userId, int contextId) throws OXException;

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
     * Gets the mail accounts belonging to specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The user's mail accounts
     * @throws OXException If the mail accounts cannot be returned
     */
    MailAccount[] getUserMailAccounts(int userId, int cid, Connection con) throws OXException;

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
     * Gets the prefix of the default mail account belonging to specified user in given context.
     *
     * @param session The session
     * @return The prefix or <code>null</code>
     * @throws OXException If the default mail account cannot be returned
     */
    String getDefaultFolderPrefix(Session session) throws OXException;

    /**
     * Updates mail account's value taken specified {@code MailAccountDescription} instance.
     *
     * @param mailAccount TThe {@code MailAccountDescription} instance to read from
     * @param attributes The attributes to update
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param session The session
     * @throws OXException If the mail account cannot be updated
     */
    void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int cid, Session session) throws OXException;

    /**
     * Updates mail account's value taken specified {@code MailAccountDescription} instance.
     *
     * @param mailAccount The {@code MailAccountDescription} instance to read from
     * @param attributes The attributes to update
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param session The session
     * @param con writable database connection.
     * @param changePrimary <code>true</code> to change primary account, too.
     * @throws OXException If the mail account cannot be updated
     */
    void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int cid, Session session, Connection con, boolean changePrimary) throws OXException;

    /**
     * Updates mail account's value taken from specified {@code MailAccountDescription} instance.
     *
     * @param mailAccount The {@code MailAccountDescription} instance to read from
     * @param attributes The attributes to update
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param updateProperties Additional update properties
     * @throws OXException If the mail account cannot be updated
     */
    void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int contextId, UpdateProperties updateProperties) throws OXException;

    /**
     * Updates mail account's value taken from specified mail account.
     *
     * @param mailAccount The mail account containing the values to update.
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param session The session
     * @throws OXException If the mail account cannot be updated
     */
    void updateMailAccount(MailAccountDescription mailAccount, int userId, int cid, Session session) throws OXException;

    /**
     * Updates transport account's value taken from specified transport account.
     *
     * @param transportAccount The transport account containing the values to update.
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param session The session
     * @throws OXException If the mail account cannot be updated
     */
    void updateTransportAccount(TransportAccountDescription transportAccount, int userId, int cid, Session session) throws OXException;

    /**
     * Acquires next available mail/transport account identifier.
     *
     * @param userId The user identifier
     * @param ctx The context
     * @return The identifier
     * @throws OXException If identifier cannot be returned
     */
    int acquireId(int userId, Context ctx) throws OXException;

    /**
     * Inserts mail account's value taken from specified mail account.
     *
     * @param mailAccount The mail account containing the values to update.
     * @param userId The user identifier
     * @param ctx The context
     * @param session The session; set to <code>null</code> to insert mail account with an empty password
     * @return The identifier of the newly created mail account
     * @throws OXException If the mail account cannot be inserted
     */
    int insertMailAccount(MailAccountDescription mailAccount, int userId, Context ctx, Session session) throws OXException;

    /**
     * Inserts transport only account's value taken from specified transport account.
     *
     * @param transportAccount The transport account containing the values to update.
     * @param userId The user identifier
     * @param ctx The context
     * @param session The session; set to <code>null</code> to insert transport account with an empty password
     * @return The identifier of the newly created transport account
     * @throws OXException If the transport account cannot be updated
     */
    int insertTransportAccount(TransportAccountDescription transportAccount, int userId, Context ctx, Session session) throws OXException;

    /**
     * Inserts mail account's value taken from specified mail account.
     *
     * @param mailAccount The mail account containing the values to update.
     * @param userId The user identifier
     * @param ctx The context
     * @param session The session; set to <code>null</code> to insert mail account with an empty password
     * @param con writable database connection
     * @return The identifier of the newly created mail account
     * @throws OXException If the mail account cannot be updated
     */
    int insertMailAccount(MailAccountDescription mailAccount, int userId, Context ctx, Session session, Connection con) throws OXException;

    /**
     * Deletes the mail account identified by specified identifier.
     *
     * @param id The mail account identifier
     * @param properties Optional properties for delete event (passed to {@link MailAccountDeleteListener} instances)
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If the mail account cannot be deleted
     */
    void deleteMailAccount(int id, Map<String, Object> properties, int userId, int contextId) throws OXException;

    /**
     * Deletes the transport account identified by specified identifier.
     *
     * @param id The transport account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If the mail account cannot be deleted
     */
    void deleteTransportAccount(int id, int userId, int contextId) throws OXException;

    /**
     * Deletes the mail account identified by specified identifier.
     *
     * @param id The mail account identifier
     * @param properties Optional properties for delete event (passed to {@link MailAccountDeleteListener} instances)
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param deletePrimary <code>true</code> to delete also the primary mail account if the user is deleted.
     * @throws OXException If the mail account cannot be deleted
     */
    void deleteMailAccount(int id, Map<String, Object> properties, int userId, int cid, boolean deletePrimary) throws OXException;

    /**
     * Deletes the mail account identified by specified identifier.
     *
     * @param id The mail account identifier
     * @param properties Optional properties for delete event (passed to {@link MailAccountDeleteListener} instances)
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param deletePrimary <code>true</code> to delete also the primary mail account if the user is deleted.
     * @param con The connection to use
     * @throws OXException If the mail account cannot be deleted
     */
    void deleteMailAccount(int id, Map<String, Object> properties, int userId, int cid, boolean deletePrimary, Connection con) throws OXException;

    /**
     * Gets the mail accounts of the users whose login matches specified login.
     *
     * @param login The login
     * @param contextId The context identifier
     * @return The mail accounts of the users whose login matches specified login
     * @throws OXException If resolving the login fails
     */
    MailAccount[] resolveLogin(String login, int contextId) throws OXException;

    /**
     * Gets the mail accounts of the users whose login matches specified login on specified server.
     *
     * @param login The login
     * @param serverUrl The server URL; e.g. <code>"mail.company.org:143"</code>
     * @param contextId The context identifier
     * @return The mail accounts of the users whose login matches specified login on specified server
     * @throws OXException If resolving the login fails
     */
    MailAccount[] resolveLogin(String login, String serverUrl, int contextId) throws OXException;

    /**
     * Gets the mail accounts of the users whose primary email address matches specified email on specified server.
     *
     * @param primaryAddress The primary email address
     * @param contextId The context identifier
     * @return The mail accounts of the users whose login matches specified login on specified server
     * @throws OXException If resolving the primary address fails
     */
    MailAccount[] resolvePrimaryAddr(String primaryAddress, int contextId) throws OXException;

    /**
     * Gets the mail account matching specified primary email address of given user in given context.
     *
     * @param primaryAddress The primary address to look for
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The identifier of the mail account matching specified primary email address or <code>-1</code> if none found
     * @throws OXException If look-up by primary address caused a conflict
     */
    int getByPrimaryAddress(String primaryAddress, int userId, int contextId) throws OXException;

    /**
     * Gets the transport account matching specified primary email address of given user in given context.
     *
     * @param primaryAddress The primary address to look for
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The identifier of the transport account matching specified primary email address or <code>-1</code> if none found
     * @throws OXException If look-up by primary address caused a conflict
     */
    int getTransportByPrimaryAddress(String primaryAddress, int userId, int contextId) throws OXException;

    /**
     * Gets the transport account matching specified reference for given user in given context.
     *
     * @param reference The reference to look for
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The identifier of the transport account or <code>null</code> if none found
     * @throws OXException If look-up by reference caused a conflict
     */
    TransportAccount getTransportByReference(String reference, int userId, int contextId) throws OXException;

    /**
     * Gets those mail accounts of given user in given context whose host name occurs in specified collection of host names.
     *
     * @param hostNames The host names
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The binary-sorted identifiers of matching mail accounts
     * @throws OXException If look-up by host names caused an error
     */
    int[] getByHostNames(Collection<String> hostNames, int userId, int contextId) throws OXException;

    /**
     * Decodes stored encrypted strings using the old secret and encode them again using the new secret.
     *
     * @param oldSecret The secret used for decrypting the stored passwords
     * @param newSecret The secret to use for encrypting the passwords again
     * @param session The session
     * @throws OXException If migrate attempt fails
     */
    void migratePasswords(String oldSecret, String newSecret, Session session) throws OXException;

    /**
     * Finds out whether the user has items that are encrypted
     */
    boolean hasAccounts(Session session) throws OXException;

    /**
     * Cleans-up accounts that could no more be decrypted with given secret
     *
     * @param secret The current secret
     * @param session The session providing user information
     * @throws OXException If operation fails
     */
    void cleanUp(String secret, Session session) throws OXException;

    /**
     * Removes accounts that could no more be decrypted with given secret
     *
     * @param secret The current secret
     * @param session The session providing user information
     * @throws OXException If operation fails
     */
    void removeUnrecoverableItems(String secret, Session session) throws OXException;

}
