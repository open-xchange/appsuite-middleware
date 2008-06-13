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

package com.openexchange.user;

import java.util.Date;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;

/**
 * {@link UserService} - Offers access method to user module.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface UserService {
	/**
	 * Searches for a user whose login matches the given uid.
	 * 
	 * @param loginInfo
	 *            Login name of the user.
	 * @param context
	 *            The context.
	 * @return The unique identifier of the user.
	 * @throws UserException
	 *             if an error occurs while searching the user or the user
	 *             doesn't exist.
	 */
	public int getUserId(String loginInfo, Context context) throws UserException;

	/**
	 * Reads the data from a user from the underlying persistent data storage.
	 * 
	 * @param uid
	 *            User identifier.
	 * @return a user object.
	 * @param context
	 *            The context.
	 * @throws UserException
	 *             if an error occurs while reading from the persistent storage
	 *             or the user doesn't exist.
	 */
	public User getUser(int uid, Context context) throws UserException;

	/**
	 * This method updates some values of a user.
	 * 
	 * @param user
	 *            user object with the updated values.
	 * @param context
	 *            The context.
	 * @throws UserException
	 *             if an error occurs.
	 */
	public void updateUser(User user, Context context) throws UserException;

	/**
	 * Searches a user by its email address. This is used for converting iCal to
	 * appointments.
	 * 
	 * @param email
	 *            the email address of the user.
	 * @param context
	 *            The context.
	 * @return a User object if the user was found by its email address or
	 *         <code>null</code> if no user could be found.
	 * @throws UserException
	 *             if an error occurs.
	 */
	public User searchUser(String email, Context context) throws UserException;

	/**
	 * Returns an array with all user identifier of the context.
	 * 
	 * @param context
	 *            The context.
	 * @return an array with all user identifier of the context.
	 * @throws UserException
	 *             if generating this list fails.
	 */
	public int[] listAllUser(Context context) throws UserException;

	/**
	 * Searches for a user whose IMAP login name matches the given login name.
	 * 
	 * @param imapLogin
	 *            the IMAP login name to search for
	 * @param context
	 *            The context.
	 * @return The unique identifier of the user.
	 * @throws UserException
	 *             if an error occurs during the search.
	 */
	public int resolveIMAPLogin(String imapLogin, Context context) throws UserException;

	/**
	 * Searches users who where modified later than the given date.
	 * 
	 * @param modifiedSince
	 *            Date after that the returned users are modified.
	 * @param context
	 *            The context.
	 * @return a string array with the uids of the matching user.
	 * @throws UserException
	 *             if an error occurs during the search.
	 */
	public int[] listModifiedUser(Date modifiedSince, Context context) throws UserException;

	/**
	 * Removes a user from the cache if caching is used.
	 * 
	 * @param ctx
	 *            Context.
	 * @param userId
	 *            unique identifier of the user.
	 * @throws UserException
	 *             if removing gives an exception.
	 */
	public void invalidateUser(Context ctx, int userId) throws UserException;

}
