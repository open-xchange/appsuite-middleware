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

package com.openexchange.share.json.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link UpdatePerformer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class UpdatePerformer extends AbstractPerformer<Void> {

    private final String token;
    private final Date clientLastModified;

    private AnonymousRecipient recipient;
    private Date expiry;
    private boolean expirySet;
    private Map<String, Object> meta;
    private boolean metaSet;

    /**
     * Initializes a new {@link UpdatePerformer}.
     *
     * @param token The (base) token of the anonymous to update
     * @param clientLastModified The last client timestamp to catch concurrent modifications
     * @param session The session
     * @param services A service lookup reference
     */
    protected UpdatePerformer(String token, Date clientLastModified, ServerSession session, ServiceLookup services) {
        super(session, services);
        this.token = token;
        this.clientLastModified = clientLastModified;
    }

    public UpdatePerformer setRecipient(AnonymousRecipient recipient) {
        this.recipient = recipient;
        return this;
    }

    public UpdatePerformer setExpiry(Date expiry) {
        this.expiry = expiry;
        this.expirySet = true;
        return this;
    }

    public UpdatePerformer setMeta(Map<String, Object> meta) {
        this.meta = meta;
        this.metaSet = true;
        return this;
    }

    private boolean needsGuestUpdate() {
        return null != recipient;
    }

    private boolean needsTargetUpdate() {
        return metaSet || expirySet;
    }

    private boolean needsPermissionUpdate() {
        return null != recipient && 0 < recipient.getBits();
    }

    @Override
    protected Void perform() throws OXException {
        if (false == needsGuestUpdate() && false == needsTargetUpdate() && false == needsPermissionUpdate()) {
            return null;
        }
        /*
         * prepare transaction
         */
        ShareService shareService = getShareService();
        DatabaseService dbService = services.getService(DatabaseService.class);
        Context context = session.getContext();
        Connection connection = dbService.getWritable(context);
        session.setParameter(Connection.class.getName(), connection);
        try {
            Databases.startTransaction(connection);
            boolean guestUserUpdated = false;
            GuestInfo originalGuest = null;
            if (needsTargetUpdate() || needsPermissionUpdate()) {
                /*
                 * resolve all original share targets and the anonymous guest user behind the token
                 */
                List<ShareInfo> shares = shareService.getShares(session, token);
                if (null == shares || 0 == shares.size()) {
                    throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
                }
                originalGuest = shares.get(0).getGuest();
                /*
                 * update guest user as needed
                 */
                if (needsGuestUpdate()) {
                    guestUserUpdated = updateRecipient(connection, originalGuest);
                }
                /*
                 * update share targets as needed
                 */
                if (needsTargetUpdate()) {
                    List<ShareTarget> targetsForShareUpdate = getTargetsForShareUpdate(shares);
                    if (null != targetsForShareUpdate && 0 < targetsForShareUpdate.size()) {
                        shareService.updateTargets(session, targetsForShareUpdate, originalGuest.getGuestID(), clientLastModified);
                    }
                }
                /*
                 * update target permissions
                 */
                if (needsPermissionUpdate()) {
                    TargetPermission permission = new TargetPermission(originalGuest.getGuestID(), false, recipient.getBits());
                    List<ShareTarget> targets = getTargets(shares);
                    TargetUpdate update = getModuleSupport().prepareUpdate(session, connection);
                    try {
                        update.fetch(targets);
                        for (ShareTarget target : targets) {
                            update.get(target).applyPermissions(Collections.singletonList(permission));
                        }
                        update.run();
                    } finally {
                        update.close();
                    }
                }
            } else if (needsGuestUpdate()) {
                /*
                 * resolve token to guest user, update guest as needed
                 */
                originalGuest = shareService.resolveGuest(token);
                guestUserUpdated = updateRecipient(connection, originalGuest);
            }
            /*
             * commit changes, invalidate guest user afterwards if modified
             */
            connection.commit();
            if (guestUserUpdated && null != originalGuest) {
                getUserService().invalidateUser(context, originalGuest.getGuestID());
            }
            return null;
        } catch (OXException e) {
            Databases.rollback(connection);
            throw e;
        } catch (SQLException e) {
            Databases.rollback(connection);
            throw ShareExceptionCodes.SQL_ERROR.create(e.getMessage());
        } finally {
            session.setParameter(Connection.class.getName(), null);
            Databases.autocommit(connection);
            dbService.backWritable(context, connection);
        }
    }

    private List<ShareTarget> getTargetsForShareUpdate(List<ShareInfo> originalShares) {
        if (null == originalShares || (false == metaSet && false == expirySet)) {
            return Collections.emptyList();
        }
        List<ShareTarget> modifiedTargets = new ArrayList<ShareTarget>(originalShares.size());
        for (ShareInfo originalShare : originalShares) {
            ShareTarget targetForShareUpdate = getTargetForShareUpdate(originalShare);
            if (null != targetForShareUpdate) {
                modifiedTargets.add(targetForShareUpdate);
            }
        }
        return modifiedTargets;
    }

    private ShareTarget getTargetForShareUpdate(ShareInfo originalShare) {
        ShareTarget originalTarget = originalShare.getShare().getTarget();
        ShareTarget modifiedTarget = null;
        if (expirySet) {
            Date originalExpiry = originalTarget.getExpiryDate();
            if (null == expiry && null != originalExpiry ||
                null != expiry && null == originalExpiry ||
                null != expiry && null != originalExpiry && false == expiry.equals(originalExpiry)) {
                if (null == modifiedTarget) {
                    modifiedTarget = originalTarget.clone();
                }
                modifiedTarget.setExpiryDate(expiry);
            }
        }
        if (metaSet) {
            Map<String, Object> originalMeta = originalTarget.getMeta();
            if (null == meta && null != originalMeta ||
                null != meta && null == originalMeta ||
                null != meta && null != originalMeta && false == meta.equals(originalMeta)) {
                if (null == modifiedTarget) {
                    modifiedTarget = originalTarget.clone();
                }
                modifiedTarget.setMeta(meta);
            }
        }
        return modifiedTarget;
    }

    /**
     * Extracts all targets from the supplied shares.
     *
     * @param shareInfos The share infos
     * @return The extracted targets
     */
    private static List<ShareTarget> getTargets(List<ShareInfo> shareInfos) {
        if (null == shareInfos) {
            return null;
        }
        List<ShareTarget> targets = new ArrayList<ShareTarget>(shareInfos.size());
        for (ShareInfo share : shareInfos) {
            targets.add(share.getShare().getTarget());
        }
        return targets;
    }

    /**
     * Updates the guest user behind the anonymous recipient as needed, i.e. adjusts the defined password mechanism and the password
     * itself in case it differs from the updated recipient.
     *
     * @param connection A (writable) database connection
     * @param originalGuest The original guest
     * @return <code>true</code> if the user was updated, <code>false</code>, otherwise
     * @throws OXException
     */
    private boolean updateRecipient(Connection connection, GuestInfo originalGuest) throws OXException {
        if (null != this.recipient) {
            String password = recipient.getPassword();
            String originalPassword = originalGuest.getPassword();
            if (null == password && null != originalPassword ||
                null != password && null == originalPassword ||
                null != password && null != originalPassword && false == password.equals(originalPassword)) {
                Context context = getContextService().getContext(session.getContextId());
                UserService userService = getUserService();
                User guestUser = userService.getUser(connection, originalGuest.getGuestID(), context);
                UserImpl updatedUser = new UserImpl(guestUser);
                if (Strings.isEmpty(password)) {
                    updatedUser.setPasswordMech("");
                    updatedUser.setUserPassword(null);
                } else {
                    updatedUser.setUserPassword(services.getService(ShareCryptoService.class).encrypt(password));
                    updatedUser.setPasswordMech(ShareCryptoService.PASSWORD_MECH_ID);
                }
                userService.updateUser(connection, updatedUser, context);
                return true;
            }
        }
        return false;
    }

}
