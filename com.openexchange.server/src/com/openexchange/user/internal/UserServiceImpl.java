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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.password.mechanism.exceptions.PasswordMechExceptionCodes;
import com.openexchange.user.User;
import com.openexchange.user.UserExceptionCode;
import com.openexchange.user.UserService;
import com.openexchange.user.interceptor.UserServiceInterceptor;
import com.openexchange.user.interceptor.UserServiceInterceptorRegistry;

/**
 * {@link UserServiceImpl} - The {@link UserService} implementation
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserServiceInterceptorRegistry interceptorRegistry;

    private final PasswordMechRegistry passwordMechRegistry;

    /**
     * Initializes a new {@link UserServiceImpl}
     */
    public UserServiceImpl(UserServiceInterceptorRegistry interceptorRegistry, PasswordMechRegistry registry) {
        super();
        this.interceptorRegistry = interceptorRegistry;
        this.passwordMechRegistry = registry;

    }

    @Override
    public boolean isGuest(int userId, Context context) throws OXException {
        return UserStorage.getInstance().isGuest(userId, context);
    }

    @Override
    public boolean isGuest(int userId, int contextId) throws OXException {
        return UserStorage.getInstance().isGuest(userId, contextId);
    }

    @Override
    public Context getContext(int contextId) throws OXException {
        return ContextStorage.getStorageContext(contextId);
    }

    @Override
    public String getUserAttribute(final String name, final int userId, final Context context) throws OXException {
        return UserStorage.getInstance().getUserAttribute(name, userId, context);
    }

    @Override
    public void setUserAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        UserStorage.getInstance().setUserAttribute(name, value, userId, context);
    }

    @Override
    public void setAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        UserStorage.getInstance().setAttribute(name, value, userId, context);
    }

    @Override
    public void setAttribute(Connection con, final String name, final String value, final int userId, final Context context) throws OXException {
        UserStorage.getInstance().setAttribute(con, name, value, userId, context);
    }

    @Override
    public void setAttribute(Connection con, String name, String value, int userId, Context context, boolean invalidate) throws OXException {
        UserStorage.getInstance().setAttribute(con, name, value, userId, context, invalidate);
    }

    @Override
    public boolean exists(int userId, int contextId) throws OXException {
        return UserStorage.getInstance().exists(userId, contextId);
    }

    @Override
    public User getUser(final int uid, final Context context) throws OXException {
        return UserStorage.getInstance().getUser(uid, context);
    }

    @Override
    public User getUser(final int userId, final int contextId) throws OXException {
        return UserStorage.getInstance().getUser(userId, ContextStorage.getInstance().getContext(contextId));
    }

    @Override
    public User getUser(final Connection con, final int uid, final Context context) throws OXException {
        return UserStorage.getInstance().getUser(context, uid, con);
    }

    @Override
    public User[] getUser(final Context context, final int[] userIds) throws OXException {
        return UserStorage.getInstance().getUser(context, userIds);
    }

    @Override
    public User[] getUser(final Context context) throws OXException {
        return UserStorage.getInstance().getUser(context);
    }

    @Override
    public User[] getUser(Connection con, Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException {
        return UserStorage.getInstance().getUser(con, ctx, includeGuests, excludeUsers);
    }

    @Override
    public User[] getUser(Context ctx, boolean includeGuests, boolean excludeUsers) throws OXException {
        return UserStorage.getInstance().getUser(ctx, includeGuests, excludeUsers);
    }

    @Override
    public User[] getGuestsCreatedBy(Connection connection, Context context, int userId) throws OXException {
        return UserStorage.getInstance().getGuestsCreatedBy(connection, context, userId);
    }

    @Override
    public int createUser(final Context context, final User user) throws OXException {
        checkUser(user);
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        beforeCreate(context, user, interceptors);
        int userId = UserStorage.getInstance().createUser(context, user);
        UserImpl created = new UserImpl(user);
        created.setId(userId);
        afterCreate(context, created, interceptors, UserServiceInterceptor.EMPTY_PROPS);
        return userId;
    }

    @Override
    public int createUser(final Connection con, final Context context, final User user) throws OXException {
        checkUser(user);
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        beforeCreate(context, user, interceptors);
        int userId = UserStorage.getInstance().createUser(con, context, user);
        UserImpl created = new UserImpl(user);
        created.setId(userId);
        afterCreate(context, created, interceptors, Collections.singletonMap(UserServiceInterceptor.PROP_CONNECTION, con));
        return userId;
    }

    @Override
    public void deleteUser(Context context, User user) throws OXException {
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        beforeDelete(context, user, interceptors);
        UserStorage.getInstance().deleteUser(context, user.getId());
        afterDelete(context, user, interceptors);
    }

    @Override
    public void deleteUser(Connection con, Context context, User user) throws OXException {
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        beforeDelete(context, user, interceptors);
        UserStorage.getInstance().deleteUser(con, context, user.getId());
        afterDelete(context, user, interceptors);
    }

    @Override
    public void deleteUser(Context context, int userId) throws OXException {
        deleteUser(context, getUser(userId, context));
    }

    @Override
    public void deleteUser(Connection con, Context context, int userId) throws OXException {
        deleteUser(con, context, getUser(con, userId, context));
    }

    @Override
    public int getUserId(final String loginInfo, final Context context) throws OXException {
        return UserStorage.getInstance().getUserId(loginInfo, context);
    }

    @Override
    public void invalidateUser(final Context ctx, final int userId) throws OXException {
        UserStorage.getInstance().invalidateUser(ctx, userId);
    }

    @Override
    public int[] listAllUser(final Context context) throws OXException {
        return UserStorage.getInstance().listAllUser(context);
    }

    @Override
    public int[] listAllUser(final Context context, boolean includeGuests, boolean excludeUsers) throws OXException {
        return UserStorage.getInstance().listAllUser(null, context, includeGuests, excludeUsers);
    }

    @Override
    public int[] listAllUser(int contextID, boolean includeGuests, boolean excludeUsers) throws OXException {
        return UserStorage.getInstance().listAllUser(null, contextID, includeGuests, excludeUsers);
    }

    @Override
    public int[] listModifiedUser(final Date modifiedSince, final Context context) throws OXException {
        return UserStorage.getInstance().listModifiedUser(modifiedSince, context);
    }

    @Override
    public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws OXException {
        return UserStorage.getInstance().resolveIMAPLogin(imapLogin, context);
    }

    @Override
    public User searchUser(final String email, final Context context) throws OXException {
        return UserStorage.getInstance().searchUser(email, context);
    }

    @Override
    public User searchUser(final String email, final Context context, boolean considerAliases) throws OXException {
        return UserStorage.getInstance().searchUser(email, context, considerAliases);
    }

    @Override
    public User searchUser(final String email, final Context context, boolean considerAliases, boolean includeGuests, boolean excludeUsers) throws OXException {
        return UserStorage.getInstance().searchUser(email, context, considerAliases, includeGuests, excludeUsers);
    }

    @Override
    public User[] searchUserByName(final String name, final Context context, final int searchType) throws OXException {
        return UserStorage.getInstance().searchUserByName(name, context, searchType);
    }

    @Override
    public void updateUser(final User user, final Context context) throws OXException {
        updateUser(null, user, context);
    }

    @Override
    public void updateUser(Connection con, User user, Context context) throws OXException {
        if (null != user.getPreferredLanguage()) {
            checkLanguage(user.getPreferredLanguage());
        }
        if (null != user.getTimeZone()) {
            checkTimezone(user.getTimeZone());
        }
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        beforeUpdate(context, user, UserServiceInterceptor.EMPTY_PROPS, interceptors);
        UserStorage.getInstance().updateUser(con, user, context);
        afterUpdate(context, user, UserServiceInterceptor.EMPTY_PROPS, interceptors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authenticate(final User user, final String password) throws OXException {
        PasswordMech passwordMech = passwordMechRegistry.get(user.getPasswordMech());
        if (passwordMech == null) {
            throw PasswordMechExceptionCodes.UNKNOWN_PASSWORD_MECHANISM.create(user.getPasswordMech(), I(user.getId()));
        }
        return passwordMech.check(password, user.getUserPassword(), user.getSalt());
    }

    private void beforeCreate(Context context, User user, List<UserServiceInterceptor> interceptors) throws OXException {
        if (!user.isGuest()) {
            for (UserServiceInterceptor interceptor : interceptors) {
                interceptor.beforeCreate(context, user, null);
            }
        }
    }

    private void afterCreate(Context context, User user, List<UserServiceInterceptor> interceptors, Map<String, Object> properties) {
        if (!user.isGuest()) {
            for (UserServiceInterceptor interceptor : interceptors) {
                try {
                    interceptor.afterCreate(context, user, null, properties);
                } catch (OXException e) {
                    LOG.error("Error while calling interceptor.", e);
                }
            }
        }
    }

    private void beforeUpdate(Context context, User user, Map<String, Object> properties, List<UserServiceInterceptor> interceptors) throws OXException {
        if (!user.isGuest()) {
            for (UserServiceInterceptor interceptor : interceptors) {
                interceptor.beforeUpdate(context, user, null, properties);
            }
        }
    }

    private void afterUpdate(Context context, User user, Map<String, Object> properties, List<UserServiceInterceptor> interceptors) {
        if (!user.isGuest()) {
            for (UserServiceInterceptor interceptor : interceptors) {
                try {
                    interceptor.afterUpdate(context, user, null, properties);
                } catch (OXException e) {
                    LOG.error("Error while calling interceptor.", e);
                }
            }
        }
    }

    private void beforeDelete(Context context, User user, List<UserServiceInterceptor> interceptors) throws OXException {
        if (!user.isGuest()) {
            for (UserServiceInterceptor interceptor : interceptors) {
                interceptor.beforeDelete(context, user, null);
            }
        }
    }

    private void afterDelete(Context context, User user, List<UserServiceInterceptor> interceptors) {
        if (!user.isGuest()) {
            for (UserServiceInterceptor interceptor : interceptors) {
                try {
                    interceptor.afterDelete(context, user, null);
                } catch (OXException e) {
                    LOG.error("Error while calling interceptor.", e);
                }
            }
        }
    }

    private void checkUser(final User user) throws OXException {
        final String mail = user.getMail();
        final String language = user.getPreferredLanguage();
        final String timeZone = user.getTimeZone();
        final String passwordMech = user.getPasswordMech();

        /*
         * Mail address
         */
        if (mail == null) {
            throw UserExceptionCode.MISSING_PARAMETER.create("mail address");
        }

        /*
         * Preferred language & timezone
         */
        checkLanguage(language);
        checkTimezone(timeZone);

        /*
         * Password mech
         */
        if (passwordMech == null) {
            throw UserExceptionCode.MISSING_PASSWORD_MECH.create();
        }

        // TODO: Maybe we have to check the contact id here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePassword(User user, Context context) throws OXException {
        PasswordMech passwordMech = passwordMechRegistry.get(user.getPasswordMech());
        UserStorage.getInstance().updatePassword(null, context, user.getId(), passwordMech, user.getUserPassword(), user.getSalt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePassword(Connection connection, User user, Context context) throws OXException {
        PasswordMech passwordMech = passwordMechRegistry.get(user.getPasswordMech());
        UserStorage.getInstance().updatePassword(connection, context, user.getId(), passwordMech, user.getUserPassword(), user.getSalt());
    }

    private static void checkTimezone(String timezone) throws OXException {
        if (null == timezone) {
            throw UserExceptionCode.MISSING_PARAMETER.create("timezone");
        }
        TimeZone timeZone = TimeZone.getTimeZone(timezone);
        if (null == timeZone || false == timeZone.getID().equals(timezone)) {
            throw UserExceptionCode.INVALID_TIMEZONE.create(timezone);
        }
    }

    private static void checkLanguage(String language) throws OXException {
        if (null == language) {
            throw UserExceptionCode.MISSING_PARAMETER.create("preferred language");
        }
        if (null == LocaleTools.getLocale(language)) {
            throw UserExceptionCode.INVALID_LOCALE.create(language);
        }
    }

}
