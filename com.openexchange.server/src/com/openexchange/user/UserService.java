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

package com.openexchange.user;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link UserService} - Offers access method to user module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
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
     *
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @throws OXException if writing the attribute fails.
     */
    void setUserAttribute(String name, String value, int userId, Context context) throws OXException;

    /**
     * Stores a internal user attribute. Internal user attributes must not be exposed to clients through the HTTP/JSON API.
     * <p>
     * This method might throw a {@link UserExceptionCode#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @throws OXException if writing the attribute fails.
     */
    void setAttribute(String name, String value, int userId, Context context) throws OXException;

    /**
     * Stores a internal user attribute. Internal user attributes must not be exposed to clients through the HTTP/JSON API.
     * <p>
     * This method might throw a {@link UserExceptionCode#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param con A (writable) database connection to use
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @throws OXException if writing the attribute fails.
     */
    void setAttribute(Connection con, String name, String value, int userId, Context context) throws OXException;

    /**
     * Stores an internal user attribute. Internal user attributes must not be exposed to clients through the HTTP/JSON API.
     * <p>
     * This method might throw a {@link UserExceptionCode#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param con A writable database connection, or <code>null</code> to acquire one dynamically
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param userId Identifier of the user that attribute should be set.
     * @param context Context the user resides in.
     * @param invalidate <code>true</code> to perform a cluster-wide cache invalidation for the updated user afterwards, or (preferably) <code>false</code> if not necessary
     * @throws OXException if writing the attribute fails.
     */
    void setAttribute(Connection con, String name, String value, int userId, Context context, boolean invalidate) throws OXException;

    /**
     * Checks if specified user is a guest.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if user is a guest; otherwise <code>false</code>
     * @throws OXException If check for a guest user fails
     */
    boolean isGuest(int userId, int contextId) throws OXException;

    /**
     * Checks if specified user is a guest.
     *
     * @param userId The user identifier
     * @param context The associated context
     * @return <code>true</code> if user is a guest; otherwise <code>false</code>
     * @throws OXException If check for a guest user fails
     */
    boolean isGuest(int userId, Context context) throws OXException;

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
     * Deletes a user from the underlying storage.
     *
     * @param context The context
     * @param user The user to delete
     */
    void deleteUser(Context context, User user) throws OXException;

    /**
     * Deletes a user from the underlying storage.
     *
     * @param con A (writable) database connection
     * @param context The context
     * @param user The user to delete
     */
    void deleteUser(Connection con, Context context, User user) throws OXException;

    /**
     * Deletes a user from the underlying storage.
     *
     * @param context The context
     * @param user The id of the user to delete
     */
    void deleteUser(Context context, int userId) throws OXException;

    /**
     * Deletes a user from the underlying storage.
     *
     * @param con A (writable) database connection
     * @param context The context
     * @param user The id of the user to delete
     */
    void deleteUser(Connection con, Context context, int userId) throws OXException;

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
     * Reads all user for the given context, optionally including/excluding guest- and regular users. This method
     * is faster than getting each user information with the {@link #getUser(Context)} and filtering them afterwards.
     *
     * @param ctx the context.
     * @param includeGuests <code>true</code> to also include guest users, <code>false</code>, otherwise
     * @param excludeUsers <code>true</code> to exclude regular users, <code>false</code>, otherwise
     * @return an array with all user objects from a context.
     * @throws OXException if all user objects can not be loaded from the persistent storage.
     */
    User[] getUser(Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException;

    /**
     * Reads all user for the given context, optionally including/excluding guest- and regular users. This method
     * is faster than getting each user information with the {@link #getUser(Context)} and filtering them afterwards.
     *
     * @param con A database connection
     * @param ctx the context.
     * @param includeGuests <code>true</code> to also include guest users, <code>false</code>, otherwise
     * @param excludeUsers <code>true</code> to exclude regular users, <code>false</code>, otherwise
     * @return an array with all user objects from a context.
     * @throws OXException if all user objects can not be loaded from the persistent storage.
     */
    User[] getUser(Connection con, Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException;

    /**
     * Gets all guest users that were created by a specific user.
     *
     * @param connection A (readable) database connection
     * @param context The context
     * @param userId The identifier of the user to load the created guests for
     * @return The created guest users, or an empty array if there are none
     * @throws OXException
     */
    User[] getGuestsCreatedBy(Connection connection, Context context, int userId) throws OXException;

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
     * For guest users, additionally the following property may be changed:
     * <ul>
     * <li>Shadow last change</li>
     * </ul>
     *
     * <b>
     * To update the user password and/or password mechanism you have to explicitly call com.openexchange.user.UserService.updatePassword(User, Context)
     * </b>
     *
     * @param user user object with the updated values.
     * @param context The context.
     * @throws OXException if an error occurs.
     * @see #getContext(int)
     */
    void updateUser(User user, Context context) throws OXException;

    /**
     * This method updates some values of a user, by re-using an existing database connection. In the given user object just set the user
     * identifier and the attributes you want to change. Every attribute with value <code>null</code> will not be touched.
     *
     * <b>
     * If you use this method within a transaction, you must(!) call {@link UserService#invalidateUser(Context, int)}
     * after you committed the connection!
     * </b>
     *
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
     * For guest users, additionally the following property may be changed:
     * <ul>
     * <li>Shadow last change</li>
     * </ul>
     *
     * <b>
     * To update the user password and/or password mechanism you have to explicitly call com.openexchange.user.UserService.updatePassword(Connection, User, Context)
     * </b>
     *
     * @param con a writable database connection
     * @param user user object with the updated values.
     * @param context The context.
     * @throws OXException if an error occurs.
     * @see #getContext(int)
     */
    void updateUser(Connection con, User user, Context context) throws OXException;

    /**
     * Updates the user password and password mechanism for the provided user. Both parameters have to be provided for the user!
     *
     * @param user User object with the (encoded) userPassword and passwordMech set.
     * @param context The context.
     * @throws OXException
     */
    void updatePassword(User user, Context context) throws OXException;

    /**
     * Updates the user password and password mechanism for the provided user. Both parameters have to be provided for the user!
     *
     * @param connection a writable database connection (or <code>null</code> if you do not have a connection)
     * @param user User object with the (encoded) userPassword and passwordMech set.
     * @param context The context.
     * @throws OXException if an error occurs.
     */
    void updatePassword(Connection connection, User user, Context context) throws OXException;

    /**
     * Searches a user by its email address. This is used for converting iCal to appointments.
     *
     * @param email The email address of the user.
     * @param context The context.
     * @return A {@link User} instance if the user was found by its email address or <code>null</code> if no user could be found.
     * @throws OXException If an error occurs.
     */
    User searchUser(String email, Context context) throws OXException;

    /**
     * Searches a user by its email address. This is used for converting iCal to appointments.
     *
     * @param email The email address of the user.
     * @param considerAliases <code>true</code> to consider a user's aliases, <code>false</code>, otherwise
     * @param context The context.
     * @param considerAliases Whether to consider alias E-Mail addresses when searching for an appropriate user
     * @return A {@link User} instance if the user was found by its email address or <code>null</code> if no user could be found.
     * @throws OXException If an error occurs.
     */
    User searchUser(String email, Context context, boolean considerAliases) throws OXException;

    /**
     * Searches a user by its email address. This is used for converting iCal to appointments.
     *
     * @param email The email address of the user.
     * @param context The context.
     * @param considerAliases <code>true</code> to consider a user's aliases, <code>false</code>, otherwise
     * @param includeGuests <code>true</code> to also include guest users, <code>false</code>, otherwise
     * @param excludeUsers <code>true</code> to exclude regular users, <code>false</code>, otherwise
     * @return A {@link User} instance if the user was found by its email address or <code>null</code> if no user could be found.
     * @throws OXException If an error occurs.
     */
    User searchUser(String email, Context context, boolean considerAliases, boolean includeGuests, boolean excludeUsers) throws OXException;

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
     * Returns an array with all user identifier of the context.
     *
     * @param context The context.
     * @param includeGuests <code>true</code> to also include guest users, <code>false</code>, otherwise
     * @param excludeUsers <code>true</code> to exclude regular users, <code>false</code>, otherwise
     * @return an array with all user identifier of the context.
     * @throws OXException If generating this list fails.
     * @see #getContext(int)
     */
    int[] listAllUser(Context context, boolean includeGuests, boolean excludeUsers) throws OXException;

    /**
     * Returns an array with all user identifiers of a context.
     *
     * @param contextID The identifier of the context to get the users for
     * @param includeGuests <code>true</code> to also include guest users, <code>false</code>, otherwise
     * @param excludeUsers <code>true</code> to exclude regular users, <code>false</code>, otherwise
     * @return An array with all user identifier of the context
     * @throws OXException If generating this list fails.
     */
    int[] listAllUser(int contextID, boolean includeGuests, boolean excludeUsers) throws OXException;

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
