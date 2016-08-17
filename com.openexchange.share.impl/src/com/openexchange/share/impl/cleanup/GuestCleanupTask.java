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

package com.openexchange.share.impl.cleanup;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.slf4j.LoggerFactory.getLogger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.impl.ConnectionHelper;
import com.openexchange.share.impl.DefaultGuestInfo;
import com.openexchange.share.impl.ShareUtils;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link GuestCleanupTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class GuestCleanupTask extends AbstractTask<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(GuestCleanupTask.class);

    protected final ServiceLookup services;
    protected final int contextID;
    protected final long guestExpiry;
    private final int guestID;

    /**
     * Creates guest cleanup tasks for multiple guest users of a context.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestIDs The identifiers of the guests to cleanup
     * @param guestExpiry the timespan (in milliseconds) after which an unused guest user can be deleted permanently
     * @return
     */
    public static List<GuestCleanupTask> create(ServiceLookup services, int contextID, int[] guestIDs, long guestExpiry) {
        if (null == guestIDs || 0 == guestIDs.length) {
            return Collections.emptyList();
        }
        List<GuestCleanupTask> tasks = new ArrayList<GuestCleanupTask>(guestIDs.length);
        for (int guestID : guestIDs) {
            tasks.add(new GuestCleanupTask(services, contextID, guestID, guestExpiry));
        }
        return tasks;
    }

    /**
     * Initializes a new {@link GuestCleanupTask}.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestID The identifier of the guest to cleanup
     * @param guestExpiry the timespan (in milliseconds) after which an unused guest user can be deleted permanently
     */
    public GuestCleanupTask(ServiceLookup services, int contextID, int guestID, long guestExpiry) {
        super();
        this.services = services;
        this.contextID = contextID;
        this.guestExpiry = guestExpiry;
        this.guestID = guestID;
    }

    @Override
    public Void call() throws Exception {
        try {
            cleanGuest(contextID, guestID);
            return null;
        } catch (OXException e) {
            if ("USR-0010".equals(e.getErrorCode())) {
                LOG.debug("Guest user {} in context {} already deleted.", I(guestID), I (contextID));
                return null;
            }
            throw e;
        }
    }

    private void cleanGuest(int contextID, int guestID) throws OXException {
        Context context = services.getService(ContextService.class).getContext(contextID);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            User guestUser = services.getService(UserService.class).getUser(connectionHelper.getConnection(), guestID, context);
            if (false == guestUser.isGuest()) {
                LOG.warn("Cancelling cleanup task for non-guest user {}.", guestUser);
                return;
            }
            cleanGuest(connectionHelper, context, guestUser);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
    }

    private void cleanGuest(ConnectionHelper connectionHelper, Context context, User guestUser) throws OXException {
        /*
         * Check if entity is consistent
         */
        ShareToken shareToken;
        try {
            shareToken = new ShareToken(contextID, guestUser);
        } catch (OXException e) {
            if (ShareExceptionCodes.INVALID_TOKEN.equals(e)) {
                LOG.info("Found invalid guest entity {} in context {} without a valid base token. Guest user will be deleted...", guestID, contextID);
                deleteGuest(connectionHelper.getConnection(), context, guestID);
                return;
            }

            throw e;
        }

        /*
         * check to which share targets the guest user has still access to (if any)
         */
        DefaultGuestInfo guestInfo = new DefaultGuestInfo(services, guestUser, shareToken, null);
        Collection<Integer> modules = services.getService(ModuleSupport.class).getAccessibleModules(contextID, guestID);
        if (0 == modules.size()) {
            /*
             * no shares remaining
             */
            if (RecipientType.ANONYMOUS == guestInfo.getRecipientType() || 0 >= guestExpiry) {
                /*
                 * delete guest user immediately if anonymous or immediate expiry configured
                 */
                LOG.debug("No shares for {} remaining, deleting guest user.", guestInfo);
                deleteGuest(connectionHelper.getConnection(), context, guestID);
            } else if (RecipientType.GUEST == guestInfo.getRecipientType()) {
                /*
                 * delete named guest user if not used for defined expiry time
                 */
                Date now = new Date();
                Date lastModified = GuestLastModifiedMarker.getLastModified(guestUser);
                if (null == lastModified) {
                    /*
                     * set initial last modified value
                     */
                    LOG.debug("No shares for {} remaining, remembering current timestamp {} for delayed cleanup.",
                        guestInfo, L(now.getTime()));
                    GuestLastModifiedMarker.updateLastModified(services, context, guestUser, now);
                } else if (now.getTime() - guestExpiry > lastModified.getTime()) {
                    /*
                     * guest user not touched for configured interval, proceed with deletion
                     */
                    LOG.debug("No shares for {} remaining and not touched for {} days, deleting guest user.",
                        guestInfo, TimeUnit.MILLISECONDS.toDays(guestExpiry));
                    deleteGuest(connectionHelper.getConnection(), context, guestUser.getId());
                }
            } else {
                LOG.warn("Unexpected recipient type \"{}\" for {}, skipping cleanup", guestInfo.getRecipientType(), guestInfo);
                return;
            }
        } else {
            /*
             * guest user still has shares, check for an expired anonymous link first
             */
            if (RecipientType.ANONYMOUS == guestInfo.getRecipientType()) {
                Date expiryDate = null;
                String expiryDateValue = ShareTool.getUserAttribute(guestUser, ShareTool.EXPIRY_DATE_USER_ATTRIBUTE);
                if (Strings.isNotEmpty(expiryDateValue)) {
                    try {
                        expiryDate = new Date(Long.parseLong(expiryDateValue));
                    } catch (NumberFormatException e) {
                        getLogger(DefaultGuestInfo.class).warn("Invalid value for {}: {}", ShareTool.EXPIRY_DATE_USER_ATTRIBUTE, expiryDateValue, e);
                    }
                }
                if (null != expiryDate && expiryDate.before(new Date())) {
                    LOG.debug("Anonymous share for {} remaining, deleting guest user.", guestInfo);
                    deleteGuest(connectionHelper.getConnection(), context, guestID);
                    return;
                }
            }
            /*
             * adjust permissions for remaining shares as needed
             */
            ShareUtils utils = new ShareUtils(services);
            int requiredPermissionBits = utils.getRequiredPermissionBits(guestUser, modules);
            UserPermissionBits updatedPermissionBits = utils.setPermissionBits(connectionHelper.getConnection(), context, guestID, requiredPermissionBits, false);
            if (updatedPermissionBits.getPermissionBits() != requiredPermissionBits) {
                LOG.debug("Shares in modules {} still available for {}, permission bits adjusted to {}.",
                    modules, guestInfo, updatedPermissionBits);
            } else {
                LOG.debug("Shares in modules {} still available for {}, left permission bits unchanged at {}.",
                    modules, guestID, requiredPermissionBits);
            }
            /*
             * remove last-modified marker if set to reset counter
             */
            GuestLastModifiedMarker.clearLastModified(services, context, guestUser);
        }
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
