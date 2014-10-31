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

package com.openexchange.share.impl.cleanup;

import java.sql.Connection;
import java.util.Date;
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
import com.openexchange.share.impl.ConnectionHelper;
import com.openexchange.share.impl.ShareTool;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link GuestCleanupTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class GuestCleanupTask extends AbstractCleanupTask<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(GuestCleanupTask.class);

    private final int guestID;

    /**
     * Initializes a new {@link GuestCleanupTask}.
     *
     * @param services A service lookup reference
     * @param connectionHelper A (started) connection helper
     * @param contextID The context ID
     * @param guestID The identifier of the guest to cleanup
     */
    public GuestCleanupTask(ServiceLookup services, ConnectionHelper connectionHelper, int contextID, int guestID) {
        super(services, connectionHelper, contextID);
        this.guestID = guestID;
    }

    /**
     * Initializes a new {@link GuestCleanupTask}.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestID The identifier of the guest to cleanup
     */
    public GuestCleanupTask(ServiceLookup services, int contextID, int guestID) {
        this(services, null, contextID, guestID);
    }

    @Override
    public Void call() throws Exception {
        Context context = services.getService(ContextService.class).getContext(contextID);
        User guestUser = services.getService(UserService.class).getUser(guestID, context);
        if (null != connectionHelper) {
            cleanGuest(connectionHelper, context, guestUser);
        } else {
            cleanGuest(context, guestUser);
        }
        return null;
    }

    private void cleanGuest(Context context, User guestUser) throws OXException {
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            cleanGuest(connectionHelper, context, guestUser);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
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
            LOG.debug("No shares for guest user {} in context {} remaining, deleting guest user.", guestUser.getId(), context.getContextId());
            //TODO: delayed deletion if user is not anonymous
            deleteGuest(connectionHelper.getConnection(), context, guestUser.getId());
        } else {
            /*
             * guest user still has shares, adjust permissions as needed
             */
            int requiredPermissionBits = ShareTool.getRequiredPermissionBits(guestUser, modules);
            LOG.debug("Shares in modules {} still available for for guest user {} in context {}, adjusting permission bits to {}.",
                modules, guestUser.getId(), context.getContextId(), requiredPermissionBits);
            setPermissionBits(connectionHelper.getConnection(), context, guestUser.getId(), requiredPermissionBits, false);
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

    /**
     * Deletes a guest user along with an associated guest user contact and permission bits.
     *
     * @param connection The database connection to use
     * @param context The context
     * @param guestID The identifier of the guest user to delete
     * @throws OXException
     */
    private void deleteGuest(Connection connection, Context context, int guestID) throws OXException {
        /*
         * delete user permission bits
         */
        services.getService(UserPermissionService.class).deleteUserPermissionBits(connection, context, guestID);
        /*
         * delete user contact
         */
        services.getService(ContactUserStorage.class).deleteGuestContact(context.getContextId(), guestID, new Date(), connection);
        /*
         * delete user
         */
        try {
            services.getService(UserService.class).deleteUser(connection, context, guestID);
        } catch (OXException e) {
            if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.debug("Guest user no longer found, skipping deletion.", e);
            } else {
                throw e;
            }
        }
        LOG.info("Guest user {} in context {} deleted.", guestID, context.getContextId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextID;
        result = prime * result + guestID;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GuestCleanupTask)) {
            return false;
        }
        GuestCleanupTask other = (GuestCleanupTask) obj;
        if (contextID != other.contextID) {
            return false;
        }
        if (guestID != other.guestID) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GuestCleanupTask [contextID=" + contextID + ", guestID=" + guestID + "]";
    }

}

