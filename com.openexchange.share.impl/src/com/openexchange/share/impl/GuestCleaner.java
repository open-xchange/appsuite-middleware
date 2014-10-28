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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link GuestCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class GuestCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(GuestCleaner.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link GuestCleaner}.
     *
     * @param services A service lookup reference
     */
    public GuestCleaner(ServiceLookup services) {
        super();
        this.services = services;
    }

    public void triggerForContext(ConnectionHelper connectionHelper, int contextID) throws OXException {
        Context context = services.getService(ContextService.class).getContext(contextID);
        int[] guestIDs = services.getService(UserService.class).listAllUser(context, true, true);
        if (null != guestIDs && 0 < guestIDs.length) {
            triggerForGuests(connectionHelper, context, guestIDs);
        }
    }

    public void triggerForGuests(ConnectionHelper connectionHelper, int contextID, int[] guestIDs) throws OXException {
        triggerForGuests(connectionHelper, services.getService(ContextService.class).getContext(contextID), guestIDs);
    }

    public void triggerForGuests(ConnectionHelper connectionHelper, Context context, int[] guestIDs) throws OXException {
        cleanGuests(connectionHelper, context, guestIDs);
    }

    private void cleanGuests(ConnectionHelper connectionHelper, Context context, int[] guestIDs) throws OXException {
        List<User> guestUsers = getGuestUsers(context, guestIDs);
        if (null != guestUsers && 0 < guestUsers.size()) {
            for (User guestUser : guestUsers) {
                cleanGuest(connectionHelper, context, guestUser);
            }
        }
    }

    private void cleanGuest(ConnectionHelper connectionHelper, Context context, User guestUser) throws OXException {
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        /*
         * check to which modules the user has access to (if any)
         */
        Set<Integer> modules = shareStorage.getSharedModules(context.getContextId(), guestUser.getId(), connectionHelper.getParameters());
        if (0 == modules.size()) {
            /*
             * no shares remaining, delete guest user
             */
            deleteGuest(connectionHelper, context, guestUser.getId());
        } else {
            /*
             * guest user still has shares, adjust permissions as needed
             */
            int requiredPermissionBits = ShareTool.getRequiredPermissionBits(guestUser, modules);
            setPermissionBits(connectionHelper.getConnection(), context, guestUser.getId(), requiredPermissionBits, true);
        }
    }

    /**
     * Sets a user's permission bits. This includes assigning initial permission bits, as well as updating already existing permissions.
     *
     * @param connection The database connection to use
     * @param context The context
     * @param userID The identifier of the user to set the permission bits for
     * @param permissionBits The permission bits to set
     * @param merge <code>true</code> to merge with the previously assigned permissions, <code>false</code> to overwrite
     * @return The updated permission bits
     * @throws OXException
     */
    private UserPermissionBits setPermissionBits(Connection connection, Context context, int userID, int permissionBits, boolean merge) throws OXException {
        UserPermissionService userPermissionService = services.getService(UserPermissionService.class);
        UserPermissionBits userPermissionBits = null;
        try {
            userPermissionBits = userPermissionService.getUserPermissionBits(connection, userID, context);
        } catch (OXException e) {
            if (false == UserConfigurationCodes.NOT_FOUND.equals(e)) {
                throw e;
            }
        }
        if (null == userPermissionBits) {
            /*
             * save permission bits
             */
            userPermissionBits = new UserPermissionBits(permissionBits, userID, context.getContextId());
            userPermissionService.saveUserPermissionBits(connection, userPermissionBits);
        } else if (userPermissionBits.getPermissionBits() != permissionBits) {
            /*
             * update permission bits
             */
            userPermissionBits.setPermissionBits(merge ? permissionBits | userPermissionBits.getPermissionBits() : permissionBits);
            userPermissionService.saveUserPermissionBits(connection, userPermissionBits);
            /*
             * invalidate affected user configuration
             */
            services.getService(UserConfigurationService.class).removeUserConfiguration(userID, context);
        }
        return userPermissionBits;
    }

    private void deleteGuest(ConnectionHelper connectionHelper, Context context, int guestID) throws OXException {
        /*
         * delete user permission bits
         */
        services.getService(UserPermissionService.class).deleteUserPermissionBits(
            connectionHelper.getConnection(), context, guestID);
        /*
         * delete user contact
         */
        services.getService(ContactUserStorage.class).deleteGuestContact(
            context.getContextId(), guestID, new Date(), connectionHelper.getConnection());
        /*
         * delete user
         */
        try {
            services.getService(UserService.class).deleteUser(connectionHelper.getConnection(), context, guestID);
        } catch (OXException e) {
            if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.debug("Guest user no longer found, skipping deletion.", e);
            } else {
                throw e;
            }
        }
    }

    private List<User> getGuestUsers(Context context, int[] guestIDs) throws OXException {
        UserService userService = services.getService(UserService.class);
        /*
         * try and get all users at once
         */
        try {
            return Arrays.asList(userService.getUser(context, guestIDs));
        } catch (OXException e) {
            if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.warn("User not found during guest cleaner run", e);
                if (1 == guestIDs.length) {
                    return Collections.emptyList();
                }
            } else {
                throw e;
            }
        }
        /*
         * fallback and load one user after the other
         */
        List<User> users = new ArrayList<User>(guestIDs.length);
        for (int id : guestIDs) {
            try {
                users.add(userService.getUser(id, context));
            } catch (OXException e) {
                if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                    LOG.warn("User not found during guest cleaner run", e);
                } else {
                    throw e;
                }
            }
        }
        return users;
    }

}
