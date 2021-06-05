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

package com.openexchange.user.internal;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.config.admin.HideAdminService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link FilteringUserService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class FilteringUserService implements UserService {

    private final UserService delegate;
    private final ServiceLookup services;

    public FilteringUserService(UserService lDelegate, ServiceLookup lServices) {
        this.delegate = lDelegate;
        this.services = lServices;
    }

    private User[] removeAdminUser(Context ctx, User[] users) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return users;
        }
        return hideAdminService.removeAdminFromUsers(ctx.getContextId(), users);
    }

    private User removeAdminUser(String email, Context context, User user) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return user;
        }
        User[] users = hideAdminService.removeAdminFromUsers(context.getContextId(), new User[] { user });
        if (users == null || users.length == 0) {
            throw LdapExceptionCode.NO_USER_BY_MAIL.create(email).setPrefix("USR");
        }
        return users[0];
    }

    private int[] removeAdminUserId(int contextID, int[] users) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return users;
        }
        return hideAdminService.removeAdminFromUserIds(contextID, users);
    }

    @Override
    public Context getContext(int contextId) throws OXException {
        return delegate.getContext(contextId);
    }

    @Override
    public String getUserAttribute(String name, int userId, Context context) throws OXException {
        return delegate.getUserAttribute(name, userId, context);
    }

    @Override
    public void setUserAttribute(String name, String value, int userId, Context context) throws OXException {
        delegate.setUserAttribute(name, value, userId, context);
    }

    @Override
    public void setAttribute(String name, String value, int userId, Context context) throws OXException {
        delegate.setAttribute(name, value, userId, context);
    }

    @Override
    public void setAttribute(Connection con, String name, String value, int userId, Context context) throws OXException {
        delegate.setAttribute(con, name, value, userId, context);
    }

    @Override
    public void setAttribute(Connection con, String name, String value, int userId, Context context, boolean invalidate) throws OXException {
        delegate.setAttribute(con, name, value, userId, context, invalidate);
    }

    @Override
    public boolean isGuest(int userId, int contextId) throws OXException {
        return delegate.isGuest(userId, contextId);
    }

    @Override
    public boolean isGuest(int userId, Context context) throws OXException {
        return delegate.isGuest(userId, context);
    }

    @Override
    public int getUserId(String loginInfo, Context context) throws OXException {
        return delegate.getUserId(loginInfo, context);
    }

    @Override
    public boolean exists(int userId, int contextId) throws OXException {
        return delegate.exists(userId, contextId);
    }

    @Override
    public User getUser(int uid, Context context) throws OXException {
        return delegate.getUser(uid, context);
    }

    @Override
    public User getUser(int uid, int contextId) throws OXException {
        return delegate.getUser(uid, contextId);
    }

    @Override
    public User getUser(Connection con, int uid, Context context) throws OXException {
        return delegate.getUser(con, uid, context);
    }

    @Override
    public int createUser(Connection con, Context context, User user) throws OXException {
        return delegate.createUser(con, context, user);
    }

    @Override
    public void deleteUser(Context context, User user) throws OXException {
        delegate.deleteUser(context, user);
    }

    @Override
    public void deleteUser(Connection con, Context context, User user) throws OXException {
        delegate.deleteUser(con, context, user);
    }

    @Override
    public void deleteUser(Context context, int userId) throws OXException {
        delegate.deleteUser(context, userId);
    }

    @Override
    public void deleteUser(Connection con, Context context, int userId) throws OXException {
        delegate.deleteUser(con, context, userId);
    }

    @Override
    public int createUser(Context context, User user) throws OXException {
        return delegate.createUser(context, user);
    }

    @Override
    public User[] getUser(Context context, int[] userIds) throws OXException {
        return delegate.getUser(context, userIds);
    }

    @Override
    public User[] getUser(Context ctx) throws OXException {
        User[] users = delegate.getUser(ctx);
        return removeAdminUser(ctx, users);
    }

    @Override
    public User[] getUser(Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException {
        User[] users = delegate.getUser(ctx, includeGuests, excludeUsers);
        return removeAdminUser(ctx, users);
    }

    @Override
    public User[] getUser(Connection con, Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException {
        User[] users = delegate.getUser(con, ctx, includeGuests, excludeUsers);
        return removeAdminUser(ctx, users);
    }

    @Override
    public User[] getGuestsCreatedBy(Connection connection, Context context, int userId) throws OXException {
        return delegate.getGuestsCreatedBy(connection, context, userId);
    }

    @Override
    public void updateUser(User user, Context context) throws OXException {
        delegate.updateUser(user, context);
    }

    @Override
    public void updateUser(Connection con, User user, Context context) throws OXException {
        delegate.updateUser(con, user, context);
    }

    @Override
    public void updatePassword(User user, Context context) throws OXException {
        delegate.updatePassword(user, context);
    }

    @Override
    public void updatePassword(Connection connection, User user, Context context) throws OXException {
        delegate.updatePassword(connection, user, context);
    }

    @Override
    public User searchUser(String email, Context context) throws OXException {
        User user = delegate.searchUser(email, context);
        return removeAdminUser(email, context, user);
    }

    @Override
    public User searchUser(String email, Context context, boolean considerAliases) throws OXException {
        User user = delegate.searchUser(email, context, considerAliases);
        return removeAdminUser(email, context, user);
    }

    @Override
    public User searchUser(String email, Context context, boolean considerAliases, boolean includeGuests, boolean excludeUsers) throws OXException {
        User user = delegate.searchUser(email, context, considerAliases, includeGuests, excludeUsers);
        return removeAdminUser(email, context, user);
    }

    @Override
    public User[] searchUserByName(String name, Context context, int searchType) throws OXException {
        User[] users = delegate.searchUserByName(name, context, searchType);
        return removeAdminUser(context, users);
    }

    @Override
    public int[] listAllUser(Context context) throws OXException {
        int[] users = delegate.listAllUser(context);
        return removeAdminUserId(context.getContextId(), users);
    }

    @Override
    public int[] listAllUser(Context context, boolean includeGuests, boolean excludeUsers) throws OXException {
        int[] users = delegate.listAllUser(context, includeGuests, excludeUsers);
        return removeAdminUserId(context.getContextId(), users);
    }

    @Override
    public int[] listAllUser(int contextID, boolean includeGuests, boolean excludeUsers) throws OXException {
        int[] users = delegate.listAllUser(contextID, includeGuests, excludeUsers);
        return removeAdminUserId(contextID, users);
    }

    @Override
    public int[] resolveIMAPLogin(String imapLogin, Context context) throws OXException {
        int[] users = delegate.resolveIMAPLogin(imapLogin, context);
        return removeAdminUserId(context.getContextId(), users);
    }

    @Override
    public int[] listModifiedUser(Date modifiedSince, Context context) throws OXException {
        int[] users = delegate.listModifiedUser(modifiedSince, context);
        return removeAdminUserId(context.getContextId(), users);
    }

    @Override
    public void invalidateUser(Context ctx, int userId) throws OXException {
        delegate.invalidateUser(ctx, userId);
    }

    @Override
    public boolean authenticate(User user, String password) throws OXException {
        return delegate.authenticate(user, password);
    }

}
