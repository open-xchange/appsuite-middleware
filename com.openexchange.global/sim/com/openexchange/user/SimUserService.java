/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.user;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;


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
        ConcurrentMap<Integer, User> users = contexts.get(I(contextId));
        if (users == null) {
            users = new ConcurrentHashMap<Integer, User>();
            ConcurrentMap<Integer, User> existing = contexts.putIfAbsent(I(contextId), users);
            if (existing != null) {
                users = existing;
            }
        }
        users.put(I(user.getId()), user);
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

    @Override
    public boolean authenticate(final User user, final String password) throws OXException {
        // Nothing to do
        return false;
    }

    @Override
    public User getUser(final int uid, final Context context) throws OXException {
        return getUser(uid, context.getContextId());
    }

    @Override
    public User getUser(int uid, int contextId) throws OXException {
        ConcurrentMap<Integer, User> users = contexts.get(I(contextId));
        if (users == null) {
            return null;
        }

        return users.get(I(uid));
    }

    @Override
    public User[] getUser(final Context context, final int[] userIds) throws OXException {
        // Nothing to do
        return null;
    }

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

    @Override
    public int getUserId(final String loginInfo, final Context context) throws OXException {
        // Nothing to do
        return 0;
    }

    @Override
    public void invalidateUser(final Context ctx, final int userId) throws OXException {
        // Nothing to do

    }

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

    @Override
    public int[] listModifiedUser(final Date modifiedSince, final Context context) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public User searchUser(final String email, final Context context) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public void updateUser(final User user, final Context context) throws OXException {
        // Nothing to do

    }

    @Override
    public void setAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        // Nothing to do

    }

    @Override
    public void setAttribute(Connection con, final String name, final String value, final int userId, final Context context) throws OXException {
        // Nothing to do
    }

    @Override
    public void setAttribute(Connection con, String name, String value, int userId, Context context, boolean invalidate) throws OXException {
        // Nothing to do
    }

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
