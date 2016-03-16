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

package com.openexchange.folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;

/**
 * {@link MutableUserConfigurationStorage}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MutableUserConfigurationStorage extends UserConfigurationStorage {

    private UserConfigurationStorage delegate;

    private ConcurrentHashMap<Integer, UserConfiguration> overrides = new ConcurrentHashMap<Integer, UserConfiguration>();

    public MutableUserConfigurationStorage(UserConfigurationStorage delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void startInternal() throws OXException {
    }

    @Override
    protected void stopInternal() throws OXException {
    }

    @Override
    public UserConfiguration getUserConfiguration(int userId, int[] groups, Context ctx) throws OXException {
        UserConfiguration userConfiguration = overrides.get(userId);
        if (userConfiguration != null) {
            return userConfiguration;
        }

        return delegate.getUserConfiguration(userId, groups, ctx);
    }

    @Override
    public UserConfiguration[] getUserConfiguration(Context ctx, User[] users) throws OXException {
        Map<Integer, UserConfiguration> ucs = new HashMap<Integer, UserConfiguration>();

        List<User> toLoad = new ArrayList<User>(users.length);

        for (User user : users) {
            UserConfiguration configuration = overrides.get(user.getId());
            if (configuration == null) {
                toLoad.add(user);
            } else {
                ucs.put(user.getId(), configuration);
            }
        }

        if (!toLoad.isEmpty()) {
            UserConfiguration[] userConfigurations = delegate.getUserConfiguration(ctx, toLoad.toArray(new User[toLoad.size()]));
            for (UserConfiguration userConfiguration : userConfigurations) {
                ucs.put(userConfiguration.getUserId(), userConfiguration);
            }
        }

        UserConfiguration[] retval = new UserConfiguration[users.length];

        int i = 0;
        for (User user : users) {
            retval[i++] = ucs.get(user.getId());
        }

        return retval;
    }

    @Override
    public UserConfiguration[] getUserConfigurations(Context ctx, int[] userIds, int[][] groups) throws OXException {
        Map<Integer, UserConfiguration> ucs = new HashMap<Integer, UserConfiguration>();

        List<Integer> toLoad = new ArrayList<Integer>(userIds.length);
        List<int[]> groupArr = new ArrayList<int[]>(userIds.length);
        
        for (int i = 0; i < userIds.length; i++) {
            int userId = userIds[i];
            UserConfiguration configuration = overrides.get(userId);
            if (configuration == null) {
                toLoad.add(userId);
                groupArr.add(groups[i]);
            } else {
                ucs.put(userId, configuration);
            }
        }

        if (!toLoad.isEmpty()) {
            int[] ids = new int[toLoad.size()];
            int[][] groups2 = new int[toLoad.size()][];
            for(int i = 0; i < toLoad.size(); i++) {
                ids[i] = toLoad.get(i);
                groups2[i] = groupArr.get(i);
            }
            UserConfiguration[] userConfigurations = delegate.getUserConfigurations(ctx, ids, groups2);
            for (UserConfiguration userConfiguration : userConfigurations) {
                ucs.put(userConfiguration.getUserId(), userConfiguration);
            }
        }

        UserConfiguration[] retval = new UserConfiguration[userIds.length];

        int i = 0;
        for (int userId : userIds) {
            retval[i++] = ucs.get(userId);
        }

        return retval;
    }

    public void saveUserConfiguration(UserConfiguration configuration) {
        overrides.put(configuration.getUserId(), configuration);
    }

    @Override
    public void clearStorage() throws OXException {
        overrides.clear();
    }

    @Override
    public void invalidateCache(int userId, Context ctx) throws OXException {
        overrides.remove(userId);
    }

}
