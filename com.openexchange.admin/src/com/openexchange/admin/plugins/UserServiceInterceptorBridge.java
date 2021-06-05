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

package com.openexchange.admin.plugins;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.exception.OXException;
import com.openexchange.user.interceptor.UserServiceInterceptor;
import com.openexchange.user.interceptor.UserServiceInterceptorRegistry;


/**
 * A bridge that delegates call-backs to {@link OXUserPluginInterface} to tracked instances of {@link UserServiceInterceptor}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class UserServiceInterceptorBridge implements OXUserPluginInterfaceExtended {

    private final UserServiceInterceptorRegistry interceptorRegistry;

    public UserServiceInterceptorBridge(UserServiceInterceptorRegistry interceptorRegistry) {
        super();
        this.interceptorRegistry = interceptorRegistry;
    }

    @Override
    public void create(Context ctx, User user, UserModuleAccess access, Credentials cred) throws PluginException {
        ContextAdapter contextAdapter = new ContextAdapter(ctx);
        UserAdapter userAdapter = new UserAdapter(user);
        ContactAdapter contactAdapter = new ContactAdapter(user, ctx.getId().intValue());
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        for (UserServiceInterceptor interceptor : interceptors) {
            try {
                interceptor.afterCreate(contextAdapter, userAdapter, contactAdapter);
            } catch (OXException e) {
                throw new PluginException(e);
            }
        }
    }

    @Override
    public void delete(Context ctx, User[] users, Credentials cred) throws PluginException {
        ContextAdapter contextAdapter = new ContextAdapter(ctx);
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        for (User user : users) {
            UserAdapter userAdapter = new UserAdapter(user);
            ContactAdapter contactAdapter = new ContactAdapter(user, ctx.getId().intValue());
            for (UserServiceInterceptor interceptor : interceptors) {
                try {
                    interceptor.beforeDelete(contextAdapter, userAdapter, contactAdapter);
                } catch (OXException e) {
                    throw new PluginException(e);
                }
            }
        }
    }

    @Override
    public void beforeChange(Context ctx, User user, Credentials auth) throws PluginException {
        ContextAdapter contextAdapter = new ContextAdapter(ctx);
        UserAdapter userAdapter = new UserAdapter(user);
        ContactAdapter contactAdapter = new ContactAdapter(user, ctx.getId().intValue());
        Map<String, Object> props = UserServiceInterceptor.EMPTY_PROPS;
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        for (UserServiceInterceptor interceptor : interceptors) {
            try {
                interceptor.beforeUpdate(contextAdapter, userAdapter, contactAdapter, props);
            } catch (OXException e) {
                throw new PluginException(e);
            }
        }
    }

    @Override
    public void change(Context ctx, User user, Credentials auth) throws PluginException {
        ContextAdapter contextAdapter = new ContextAdapter(ctx);
        UserAdapter userAdapter = new UserAdapter(user);
        ContactAdapter contactAdapter = new ContactAdapter(user, ctx.getId().intValue());
        Map<String, Object> props = UserServiceInterceptor.EMPTY_PROPS;
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        for (UserServiceInterceptor interceptor : interceptors) {
            try {
                interceptor.afterUpdate(contextAdapter, userAdapter, contactAdapter, props);
            } catch (OXException e) {
                throw new PluginException(e);
            }
        }
    }

    @Override
    public User[] getData(Context ctx, User[] users, Credentials cred) {
        return users;
    }

    @Override
    public boolean canHandleContextAdmin() {
        return true;
    }

    @Override
    public void changeCapabilities(Context ctx, User user, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws PluginException {
        ContextAdapter contextAdapter = new ContextAdapter(ctx);
        UserAdapter userAdapter = new UserAdapter(user);
        ContactAdapter contactAdapter = new ContactAdapter(user, ctx.getId().intValue());
        Map<String, Object> props = UserServiceInterceptor.EMPTY_PROPS;
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        for (UserServiceInterceptor interceptor : interceptors) {
            try {
                interceptor.afterUpdate(contextAdapter, userAdapter, contactAdapter, props);
            } catch (OXException e) {
                throw new PluginException(e);
            }
        }
    }

    @Override
    public void changeMailAddressPersonal(Context ctx, User user, String personal, Credentials auth) throws PluginException {
        ContextAdapter contextAdapter = new ContextAdapter(ctx);
        UserAdapter userAdapter = new UserAdapter(user);
        ContactAdapter contactAdapter = new ContactAdapter(user, ctx.getId().intValue());
        Map<String, Object> props = UserServiceInterceptor.EMPTY_PROPS;
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        for (UserServiceInterceptor interceptor : interceptors) {
            try {
                interceptor.afterUpdate(contextAdapter, userAdapter, contactAdapter, props);
            } catch (OXException e) {
                throw new PluginException(e);
            }
        }
    }

    @Override
    public void changeModuleAccess(Context ctx, User user, String access_combination_name, Credentials auth) throws PluginException {
        ContextAdapter contextAdapter = new ContextAdapter(ctx);
        UserAdapter userAdapter = new UserAdapter(user);
        ContactAdapter contactAdapter = new ContactAdapter(user, ctx.getId().intValue());
        Map<String, Object> props = UserServiceInterceptor.EMPTY_PROPS;
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        for (UserServiceInterceptor interceptor : interceptors) {
            try {
                interceptor.afterUpdate(contextAdapter, userAdapter, contactAdapter, props);
            } catch (OXException e) {
                throw new PluginException(e);
            }
        }
    }

    @Override
    public void changeModuleAccess(Context ctx, User user, UserModuleAccess moduleAccess, Credentials auth) throws PluginException {
        ContextAdapter contextAdapter = new ContextAdapter(ctx);
        UserAdapter userAdapter = new UserAdapter(user);
        ContactAdapter contactAdapter = new ContactAdapter(user, ctx.getId().intValue());
        Map<String, Object> props = UserServiceInterceptor.EMPTY_PROPS;
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        for (UserServiceInterceptor interceptor : interceptors) {
            try {
                interceptor.afterUpdate(contextAdapter, userAdapter, contactAdapter, props);
            } catch (OXException e) {
                throw new PluginException(e);
            }
        }
    }

}
