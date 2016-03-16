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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.User;


/**
 * {@link SimUserService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimUserService implements UserService {

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, User>> contexts = new ConcurrentHashMap<Integer, ConcurrentMap<Integer, User>>();

    /**
     * Adds a user, its ID must be set!
     */
    public void addUser(User user, int contextId) {
        ConcurrentMap<Integer, User> users = contexts.get(contextId);
        if (users == null) {
            users = new ConcurrentHashMap<Integer, User>();
            ConcurrentMap<Integer, User> existing = contexts.putIfAbsent(contextId, users);
            if (existing != null) {
                users = existing;
            }
        }
        users.put(user.getId(), user);
    }

    @Override
    public Context getContext(int contextId) throws OXException {
        return new SimContext(contextId);
    }

    @Override
    public String getUserAttribute(final String name, final int userId, final Context context) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public void setUserAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#authenticate(com.openexchange.groupware.ldap.User, java.lang.String)
     */
    @Override
    public boolean authenticate(final User user, final String password) throws OXException {
        // Nothing to do
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#getUser(int, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public User getUser(final int uid, final Context context) throws OXException {
        return getUser(uid, context.getContextId());
    }

    @Override
    public User getUser(int uid, int contextId) throws OXException {
        ConcurrentMap<Integer, User> users = contexts.get(contextId);
        if (users == null) {
            return null;
        }

        return users.get(uid);
    }

    @Override
    public User[] getUser(final Context context, final int[] userIds) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#getUser(com.openexchange.groupware.contexts.Context)
     */
    @Override
    public User[] getUser(final Context ctx) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public User[] getUser(Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#getUserId(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public int getUserId(final String loginInfo, final Context context) throws OXException {
        // Nothing to do
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#invalidateUser(com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public void invalidateUser(final Context ctx, final int userId) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#listAllUser(com.openexchange.groupware.contexts.Context)
     */
    @Override
    public int[] listAllUser(final Context context) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public int[] listAllUser(Context context, boolean includeGuests, boolean excludeUsers) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public int[] listAllUser(int contextID, boolean includeGuests, boolean excludeUsers) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#listModifiedUser(java.util.Date, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public int[] listModifiedUser(final Date modifiedSince, final Context context) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#resolveIMAPLogin(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#searchUser(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public User searchUser(final String email, final Context context) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#updateUser(com.openexchange.groupware.ldap.User, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public void updateUser(final User user, final Context context) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#setAttribute(java.lang.String, java.lang.String, int, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public void setAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        // Nothing to do

    }

    @Override
    public void setAttribute(Connection con, final String name, final String value, final int userId, final Context context) throws OXException {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#searchUserByName(java.lang.String, com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public User[] searchUserByName(final String name, final Context context, final int searchType) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public User getUser(Connection con, int uid, Context context) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public int createUser(Connection con, Context context, User user) throws OXException {
        // Nothing to do
        return 0;
    }

    @Override
    public int createUser(Context context, User user) throws OXException {
        // Nothing to do
        return 0;
    }

    @Override
    public User searchUser(String email, Context context, boolean considerAliases) throws OXException {
        return null;
    }

    @Override
    public User searchUser(String email, Context context, boolean considerAliases, boolean includeGuests, boolean excludeUsers) throws OXException {
        return null;
    }

    @Override
    public void deleteUser(Context context, User user) throws OXException {
        // Nothing to do
    }

    @Override
    public void deleteUser(Connection con, Context context, User user) throws OXException {
        // Nothing to do
    }

    @Override
    public void deleteUser(Context context, int userId) throws OXException {
        // Nothing to do
    }

    @Override
    public void deleteUser(Connection con, Context context, int userId) throws OXException {
        // Nothing to do
    }

    @Override
    public User[] getUser(Connection con, Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isGuest(int userId, Context context) throws OXException {
        User user = getUser(userId, context);
        return null == user ? false : user.isGuest();
    }

    @Override
    public boolean isGuest(int userId, int contextId) throws OXException {
        User user = getUser(userId, contextId);
        return null == user ? false : user.isGuest();
    }

    @Override
    public void updateUser(Connection con, User user, Context context) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public User[] getGuestsCreatedBy(Connection connection, Context context, int userId) throws OXException {
        return new User[0];
    }

    @Override
    public void updatePassword(User user, Context context) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePassword(Connection connection, User user, Context context) throws OXException {
        // TODO Auto-generated method stub

    }

}
