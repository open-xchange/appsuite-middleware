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

package com.openexchange.share.core.performer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link UpdateLinkPerformer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class UpdateLinkPerformer extends AbstractPerformer<Void> {

    private final ShareInfo originalShare;
    private final Date clientLastModified;

    private Map<String, Object> meta;
    private boolean metaSet;
    private String password;
    private boolean passwordSet;
    private Date expiryDate;
    private boolean expiryDateSet;

    /**
     * Initializes a new {@link UpdateLinkPerformer}.
     *
     * @param session The session
     * @param services A service lookup reference
     * @param originalShare The original share link
     * @param clientLastModified The last client timestamp to catch concurrent modifications
     */
    public UpdateLinkPerformer(ServerSession session, ServiceLookup services, ShareInfo originalShare, Date clientLastModified) {
        super(session, services);
        this.originalShare = originalShare;
        this.clientLastModified = clientLastModified;
    }

    public UpdateLinkPerformer setPasword(String password) {
        this.password = password;
        this.passwordSet = true;
        return this;
    }

    public UpdateLinkPerformer setEypiryDate(Date expiryDate) {
        this.expiryDate= expiryDate;
        this.expiryDateSet = true;
        return this;
    }

    public UpdateLinkPerformer setMeta(Map<String, Object> meta) {
        this.meta = meta;
        this.metaSet = true;
        return this;
    }

    @Override
    public Void perform() throws OXException {
        if (false == metaSet && false == passwordSet && false == expiryDateSet) {
            return null;
        }
        /*
         * check "share_links" capability
         */
        CapabilityService capabilityService = services.getService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }
        CapabilitySet capabilities = capabilityService.getCapabilities(session);
        if (null == capabilities || !capabilities.contains("share_links")) {
            throw ShareExceptionCodes.NO_SHARE_LINK_PERMISSION.create();
        }
        /*
         * prepare transaction
         */
        DatabaseService dbService = services.getService(DatabaseService.class);
        Context context = session.getContext();
        Connection connection = dbService.getWritable(context);
        session.setParameter(Connection.class.getName(), connection);
        try {
            Databases.startTransaction(connection);
            /*
             * update password for anonymous guest user as needed
             */
            boolean guestUserUpdated = updatePassword(connection);
            /*
             * update share expiry & meta as needed
             */
            updateShare();
            /*
             * commit changes, invalidate guest user afterwards if modified
             */
            connection.commit();
            if (guestUserUpdated) {
                getUserService().invalidateUser(context, originalShare.getGuest().getGuestID());
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

    /**
     * Updates meta & expiry date for the share as needed.
     *
     * @return A share info as result of the update, or <code>null</code> if no update was necessary
     */
    private ShareInfo updateShare() throws OXException {
        if (metaSet || expiryDateSet) {
            Share toUpdate = new Share(originalShare.getGuest().getGuestID(), originalShare.getShare().getTarget());
            if (metaSet) {
                toUpdate.setMeta(meta);
            }
            if (expiryDateSet) {
                toUpdate.setExpiryDate(expiryDate);
            }
            return getShareService().updateShare(session, toUpdate, clientLastModified);
        }
        return null;
    }

    /**
     * Updates the guest user behind the anonymous recipient as needed, i.e. adjusts the defined password mechanism and the password
     * itself in case it differs from the updated recipient.
     *
     * @param connection A (writable) database connection
     * @return <code>true</code> if the user was updated, <code>false</code>, otherwise
     */
    private boolean updatePassword(Connection connection) throws OXException {
        if (passwordSet) {
            String originalPassword = originalShare.getGuest().getPassword();
            if (null == password && null != originalPassword ||
                null != password && null == originalPassword ||
                null != password && null != originalPassword && false == password.equals(originalPassword)) {
                Context context = getContextService().getContext(session.getContextId());
                UserService userService = getUserService();
                User guestUser = userService.getUser(connection, originalShare.getGuest().getGuestID(), context);
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
