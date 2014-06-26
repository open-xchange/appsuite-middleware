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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.chat;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ChatAccountManager} - Manages chat accounts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ChatAccountManager {

    /**
     * Adds a new account.
     *
     * @param account The account to add
     * @param session The session providing needed user data
     * @return The identifier of the newly created account
     * @throws OXException If insertion fails
     */
    String addAccount(ChatAccount account, Session session) throws OXException;

    /**
     * Updates an existing account.
     *
     * @param account The account providing the identifier and the data to update
     * @param session The session providing needed user data
     * @throws OXException If update fails
     */
    void updateAccount(ChatAccount account, Session session) throws OXException;

    /**
     * Deletes an existing account.
     *
     * @param account The account to delete
     * @param session The session providing needed user data
     * @throws OXException If deletion fails
     */
    void deleteAccount(ChatAccount account, Session session) throws OXException;

    /**
     * Gets all accounts associated with session user.
     *
     * @param session The session providing needed user data
     * @return All accounts associated with session user.
     * @throws OXException If listing fails
     */
    List<ChatAccount> getAccounts(Session session) throws OXException;

    /**
     * Gets an existing messaging account.
     *
     * @param id The identifier
     * @param session The session providing needed user data
     * @return The messaging account.
     * @throws OXException If retrieval fails
     */
    ChatAccount getAccount(String id, Session session) throws OXException;

    /**
     * Checks whether the given secret can be used to decrypt secret strings in this account.
     *
     * @param session The session providing needed user data
     * @param secret The secret to use for decrypting
     * @return true when all accounts could be decrypted, false otherwise
     * @throws OXException If check fails
     */
    String checkSecretCanDecryptStrings(Session session, String secret) throws OXException;

    /**
     * Migrates all encrypted strings from an old secret to a new one.
     *
     * @param oldSecret The old secret for decrypting stored secret strings
     * @param newSecret The new secret used for encrypting the secret strings
     * @param session The session providing needed user data
     * @throws OXException If migration fails
     */
    void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException;

}
