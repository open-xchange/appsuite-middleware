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

package com.openexchange.admin.plugins;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.exception.OXException;
import com.openexchange.user.UserServiceInterceptor;
import com.openexchange.user.UserServiceInterceptorRegistry;


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
