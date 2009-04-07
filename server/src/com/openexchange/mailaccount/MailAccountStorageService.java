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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.net.InetSocketAddress;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link MailAccountStorageService} - The storage service for mail accounts.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MailAccountStorageService {

    /**
     * Gets the mail account identified by specified ID.
     * 
     * @param id The mail account ID
     * @param user The user ID
     * @param cid The context ID
     * @return The mail account
     * @throws MailAccountException If the mail account cannot be returned
     */
    public MailAccount getMailAccount(int id, int user, int cid) throws MailAccountException;

    /**
     * Gets the mail accounts belonging to specified user in given context.
     * 
     * @param user The user ID
     * @param cid The context ID
     * @return The user's mail accounts
     * @throws MailAccountException If the mail accounts cannot be returned
     */
    public MailAccount[] getUserMailAccounts(int user, int cid) throws MailAccountException;

    /**
     * Gets the default mail account belonging to specified user in given context.
     * 
     * @param user The user ID
     * @param cid The context ID
     * @return The user's default mail account
     * @throws MailAccountException If the default mail account cannot be returned
     */
    public MailAccount getDefaultMailAccount(int user, int cid) throws MailAccountException;

    /**
     * Updates mail account's value taken from specified mail account.
     * 
     * @param mailAccount The mail account containing the values to update.
     * @param user The user ID
     * @param cid The context ID
     * @param sessionPassword The session password
     * @throws MailAccountException If the mail account cannot be updated
     */
    public void updateMailAccount(MailAccountDescription mailAccount, int user, int cid, String sessionPassword) throws MailAccountException;

    /**
     * Inserts mail account's value taken from specified mail account.
     * 
     * @param mailAccount The mail account containing the values to update.
     * @param user The user ID
     * @param ctx The context
     * @param sessionPassword The session password
     * @return The ID of the newly created mail account
     * @throws MailAccountException If the mail account cannot be updated
     */
    public int insertMailAccount(MailAccountDescription mailAccount, int user, Context ctx, String sessionPassword) throws MailAccountException;

    /**
     * Deletes the mail account identified by specified ID.
     * 
     * @param id The mail account ID
     * @param user The user ID
     * @param cid The context ID
     * @throws MailAccountException If the mail account cannot be deleted
     */
    public void deleteMailAccount(int id, int user, int cid) throws MailAccountException;

    /**
     * Gets the mail accounts of the users whose login matches specified login.
     * 
     * @param login The login
     * @param cid The context ID
     * @return The mail accounts of the users whose login matches specified login
     * @throws MailAccountException If resolving the login fails
     */
    public MailAccount[] resolveLogin(String login, int cid) throws MailAccountException;

    /**
     * Gets the mail accounts of the users whose login matches specified login on specified server.
     * 
     * @param login The login
     * @param server The server's internet address
     * @param cid The context ID
     * @return The mail accounts of the users whose login matches specified login on specified server
     * @throws MailAccountException If resolving the login fails
     */
    public MailAccount[] resolveLogin(String login, InetSocketAddress server, int cid) throws MailAccountException;

    /**
     * Gets the mail accounts of the users whose primary email address matches specified email on specified server.
     * 
     * @param primaryAddress The primary email address
     * @param server The server's internet address
     * @param cid The context ID
     * @return The mail accounts of the users whose login matches specified login on specified server
     * @throws MailAccountException If resolving the primary address fails
     */
    public MailAccount[] resolvePrimaryAddr(String primaryAddress, InetSocketAddress server, int cid) throws MailAccountException;

    /**
     * Gets the mail account matching specified primary email address of given user in given context.
     * 
     * @param primaryAddress The primary address to look for
     * @param user The user ID
     * @param cid The context ID
     * @return The ID of the mail account matching specified primary email address or <code>-1</code> if none found
     * @throws MailAccountException If look-up by primary address caused a conflict
     */
    public int getByPrimaryAddress(String primaryAddress, int user, int cid) throws MailAccountException;
}
