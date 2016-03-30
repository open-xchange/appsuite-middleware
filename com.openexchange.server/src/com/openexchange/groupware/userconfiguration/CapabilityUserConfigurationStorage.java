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

package com.openexchange.groupware.userconfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link CapabilityUserConfigurationStorage} - The database storage implementation of a user configuration storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CapabilityUserConfigurationStorage extends UserConfigurationStorage {

    /**
     * Initializes a new {@link CapabilityUserConfigurationStorage}
     */
    public CapabilityUserConfigurationStorage() {
        super();
    }

    @Override
    protected void startInternal() {
        /*
         * Nothing to start
         */
    }

    @Override
    protected void stopInternal() {
        /*
         * Nothing to stop
         */
    }

    @Override
    public UserConfiguration getUserConfiguration(int userId, int[] groups, Context ctx) throws OXException {
        return loadUserConfiguration(userId, groups, ctx);
    }

    @Override
    public UserConfiguration[] getUserConfiguration(final Context ctx, final User[] users) throws OXException {
        return loadUserConfiguration(ctx, users);
    }

    @Override
    public void clearStorage() {
        /*
         * Since this storage implementation directly fetches data from database this method has no effect
         */
    }

    @Override
    public void invalidateCache(final int userId, final Context ctx) {
        /*
         * Since this storage implementation directly fetches data from database this method has no effect
         */
    }

    /*-
     * ------------- Methods for loading -------------
     */

    /**
     * Loads the user configuration from database specified through user ID and context
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @return the instance of <code>{@link UserConfiguration}</code>
     * @throws OXException - if user's groups are <code>null</code> and could not be determined by <code>{@link UserStorage}</code>
     *             implementation
     */
    public static UserConfiguration loadUserConfiguration(int userId, Context ctx) throws OXException {
        return loadUserConfiguration(userId, null, ctx);
    }

    private static Set<String> getCapabilities(final int userId, final int cid) throws OXException {
        CapabilityService capabilityService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
        if (capabilityService == null) {
            return new HashSet<String>();
        }
        return stringify(capabilityService.getCapabilities(userId, cid));
    }

    private static Set<String> stringify(final CapabilitySet capabilities) {
        Set<String> set = new HashSet<String>(capabilities.size());
        for (Capability capability : capabilities) {
            set.add(com.openexchange.java.Strings.toLowerCase(capability.getId()));
        }
        return set;
    }

    /**
     * Loads the user configuration from database specified through user ID and context
     *
     * @param userId - the user ID
     * @param groupsArg - the group IDs the user belongs to; may be <code>null</code>
     * @param ctx - the context
     * @return the instance of <code>{@link UserConfiguration}</code>
     * @throws OXException - if user's groups are <code>null</code> and could not be determined by <code>{@link UserStorage}</code>
     *             implementation
     * @throws OXException - if a readable connection could not be obtained from connection pool
     * @throws OXException - if no matching user configuration is kept in database
     */
    public static UserConfiguration loadUserConfiguration(int userId, int[] groupsArg, Context ctx) throws OXException {
        final int[] groups = groupsArg == null ? UserStorage.getInstance().getUser(userId, ctx).getGroups() : groupsArg;
        // Check existence of the user
        UserStorage.getInstance().getUser(userId, ctx);
        final UserConfiguration userConfiguration = new UserConfiguration(getCapabilities(userId, ctx.getContextId()), userId, groups, ctx);
        return userConfiguration;
    }


    public static UserConfiguration[] loadUserConfiguration(Context ctx, User[] users) throws OXException {

        UserConfiguration[] retval = new UserConfiguration[users.length];
        // Here we just assume the users exist
        for (int i = 0; i < users.length; i++) {
            final User user = users[i];
            final UserConfiguration userConfiguration = new UserConfiguration(
                getCapabilities(user.getId(), ctx.getContextId()),
                user.getId(),
                user.getGroups(),
                ctx);

            retval[i] = userConfiguration;
        }

        return retval;
    }

    @Override
    public UserConfiguration[] getUserConfigurations(Context ctx, int[] userIds, int[][] groups) throws OXException {
        if (0 == userIds.length) {
            return new UserConfiguration[0];
        }
        return loadUserConfigurations(ctx, userIds, groups);
    }

    private static UserConfiguration[] loadUserConfigurations(Context ctx, int[] userIds, int[][] groupsArg) throws OXException {
        final List<UserConfiguration> list = new ArrayList<UserConfiguration>(userIds.length);
        for (int i = 0; i < userIds.length; i++) {
            final int userId = userIds[i];
            final int[] groups = groupsArg[i] == null ? UserStorage.getInstance().getUser(userId, ctx).getGroups() : groupsArg[i];
            list.add(new UserConfiguration(getCapabilities(userId, ctx.getContextId()), userId, groups, ctx));
        }
        return list.toArray(new UserConfiguration[userIds.length]);
    }
}
