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

package com.openexchange.user;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;

/**
 * {@link UserService} - Offers access method to user module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface UserService {

    /**
     * Gets the denoted context.
     *
     * @param contextId The context identifier
     * @return The context
     * @throws OXException If context cannot be returned
     */
    Context getContext(int contextId) throws OXException;

    /**
     * Gets specified user attribute.
     *
     * @param name The attribute name
     * @param userId The user identifier
     * @param context The context
     * @return The attribute value
     * @throws OXException If user attribute cannot be returned
     * @see #getContext(int)
     */
    String getUserAttribute(String name, int userId, Context context) throws OXException;

    /**
     * Stores a public user attribute. This attribute is prepended with "attr_". This prefix is used to separate public user attributes from
     * internal user attributes. Public user attributes prefixed with "attr_" can be read and written by every client through the HTTP/JSON
     * API.
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @throws OXException if writing the attribute fails.
     */
    void setUserAttribute(String name, String value, int userId, Context context) throws OXException;

    /**
     * Stores a internal user attribute. Internal user attributes must not be exposed to clients through the HTTP/JSON API.
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @throws OXException if writing the attribute fails.
     */
    void setAttribute(String name, String value, int userId, Context context) throws OXException;

    /**
     * Searches for a user whose login matches the given <code>loginInfo</code>.
     *
     * @param loginInfo The login name of the user.
     * @param context The context.
     * @return The unique identifier of the user.
     * @throws OXException If an error occurs while searching the user or the user doesn't exist.
     * @see #getContext(int)
     */
    int getUserId(String loginInfo, Context context) throws OXException;

    /**
     * Reads the data from a user from the underlying persistent data storage.
     *
     * @param uid The user identifier.
     * @return The user
     * @param context The context.
     * @throws OXException If an error occurs while reading from the persistent storage or the user doesn't exist.
     * @see #getContext(int)
     */
    User getUser(int uid, Context context) throws OXException;

    /**
     * Reads the data from a user from the underlying persistent data storage.
     *
     * @param uid The user identifier.
     * @param contextId The context identifier.
     * @return The user
     * @throws OXException If an error occurs while reading from the persistent storage or the user doesn't exist.
     * @see #getContext(int)
     */
    User getUser(int uid, int contextId) throws OXException;

    /**
     * Reads the data from a user from the underlying persistent data storage.
     *
     * @param con The database connection
     * @param uid The user identifier.
     * @param context The context
     * @return A user
     * @throws OXException If an error occurs while reading from the persistent storage or the user doesn't exist.
     * @see #getContext(int)
     */
    User getUser(Connection con, int uid, Context context) throws OXException;

    /**
     * Writes a new user into the database.
     *
     * @param con The database connection.
     * @param context The context.
     * @param user The user
     * @return ID of the new user.
     * @throws OXException If an error occurs while creating the user.
     * @see #getContext(int)
     */
    int createUser(Connection con, Context context, User user) throws OXException;

    /**
     * Writes a new user into the database.
     *
     * @param context The context.
     * @param user The user.
     * @return ID of the new user.
     * @throws OXException If an error occurs while creating the user.
     * @see #getContext(int)
     */
    int createUser(Context context, User user) throws OXException;

    /**
     * Reads the data for a set of user from the underlying persistent data storage.
     *
     * @param context The context
     * @param userIds The user identifier
     * @return The users
     * @throws OXException If an error occurs while reading from the persistent storage or the user doesn't exist.
     * @see #getContext(int)
     */
    User[] getUser(Context context, int[] userIds) throws OXException;

    /**
     * Reads all user for the given context. Use this method if you need a lot of users from that context because this method uses optimized
     * storage loading mechanisms to get all user information from the storage in a fast manner.
     *
     * @param ctx The context
     * @return An array with all users from the given context
     * @see #getContext(int)
     */
    User[] getUser(Context ctx) throws OXException;

    /**
     * This method updates some values of a user. In the given user object just set the user identifier and the attributes you want to
     * change. Every attribute with value <code>null</code> will not be touched.
     * <p>
     * Currently supported values for update:
     * <ul>
     * <li>Time zone</li>
     * <li>Language</li>
     * <li>IMAP server</li>
     * <li>SMTP server</li>
     * <li>IMAP login</li>
     * <li>Attributes (if present, not <code>null</code>)</li>
     * </ul>
     * @param user user object with the updated values.
     * @param context The context.
     * @throws OXException  if an error occurs.
     * @see #getContext(int)
     */
    void updateUser(User user, Context context) throws OXException;

    /**
     * Searches a user by its email address. This is used for converting iCal to appointments.
     *
     * @param email The email address of the user.
     * @param context The context.
     * @return A {@link User} instance if the user was found by its email address or <code>null</code> if no user could be found.
     * @throws OXException If an error occurs.
     * @see #getContext(int)
     */
    User searchUser(String email, Context context) throws OXException;

    /**
     * Searches a user by its email address. This is used for converting iCal to appointments.
     *
     * @param email The email address of the user.
     * @param context The context.
     * @return A {@link User} instance if the user was found by its email address or <code>null</code> if no user could be found.
     * @return considerAliases
     * @throws OXException If an error occurs.
     * @see #getContext(int)
     */
    User searchUser(String email, Context context, boolean considerAliases) throws OXException;

    /**
     * Search for matching login name.
     */
    public static final int SEARCH_LOGIN_NAME = 1;

    /**
     * Search for matching display name.
     */
    public static final int SEARCH_DISPLAY_NAME = 2;

    /**
     * Searches a user by its login/display name.
     *
     * @param name The login/display name of the user.
     * @param context The context.
     * @param searchType The search type; e.g.
     *            <code>{@link UserService#SEARCH_DISPLAY_NAME SEARCH_DISPLAY_NAME} | {@link UserService#SEARCH_LOGIN_NAME SEARCH_LOGIN_NAME}</code>
     * @return The matching users
     * @throws OXException If an error occurs.
     * @see #SEARCH_DISPLAY_NAME
     * @see #SEARCH_LOGIN_NAME
     * @see #getContext(int)
     */
    User[] searchUserByName(String name, Context context, int searchType) throws OXException;

    /**
     * Returns an array with all user identifier of the context.
     *
     * @param context The context.
     * @return an array with all user identifier of the context.
     * @throws OXException If generating this list fails.
     * @see #getContext(int)
     */
    int[] listAllUser(Context context) throws OXException;

    /**
     * Searches for users whose IMAP login name matches the given login name.
     *
     * @param imapLogin the IMAP login name to search for
     * @param context The context.
     * @return The unique identifiers of the users.
     * @throws OXException If an error occurs during the search.
     * @see #getContext(int)
     */
    int[] resolveIMAPLogin(String imapLogin, Context context) throws OXException;

    /**
     * Searches users who where modified later than the given date.
     *
     * @param modifiedSince Date after that the returned users are modified.
     * @param context The context.
     * @return a string array with the uids of the matching user.
     * @throws OXException If an error occurs during the search.
     * @see #getContext(int)
     */
    int[] listModifiedUser(Date modifiedSince, Context context) throws OXException;

    /**
     * Removes a user from the cache if caching is used.
     *
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @throws OXException If removing gives an exception.
     * @see #getContext(int)
     */
    void invalidateUser(Context ctx, int userId) throws OXException;

    /**
     * Authenticates the given password against the given user object.
     *
     * @param user user that password is compared with given one.
     * @param password password to check.
     * @return <code>true</code> if the password matches.
     * @throws OXException If password check mechanism has problems.
     */
    boolean authenticate(User user, String password) throws OXException;

}
