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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.CreateRequest;
import com.openexchange.share.DefaultShare;
import com.openexchange.share.Guest;
import com.openexchange.share.Share;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link CreateHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class CreateHandler extends RequestHandler<CreateRequest, List<Share>> {

    /**
     * Initializes a new {@link CreateHandler}.
     * @param createRequest
     * @param entity
     * @param session
     */
    public CreateHandler(CreateRequest createRequest, ServerSession session, ServiceLookup services) {
        super(createRequest, session, services);
    }

    @Override
    protected List<Share> processRequest() throws OXException {
        DatabaseService dbService = getDatabaseService();
        UserService userService = getUserService();
        ShareStorage shareStorage = getShareStorage();

        Context context = session.getContext();
        boolean ownsConnection = false;
        Connection con = request.getConnection();
        if (con == null) {
            con = dbService.getWritable(context);
            ownsConnection = true;
        }

        List<Guest> guests = request.getGuests();
        List<Share> shares = new ArrayList<Share>(guests.size());
        try {
            if (ownsConnection) {
                Databases.startTransaction(con);
            }
            StorageParameters parameters = StorageParameters.newInstance(Connection.class.getName(), con);
            for (Guest guest : guests) {
                User guestUser = prepareGuestUser(guest);
                int guestUserId = userService.createUser(con, context, guestUser);
                UserPermissionBitsStorage.getInstance().saveUserPermissionBits(con, getUserPermissionBits(), guestUserId, context); // FIXME: to service layer
                if (request.getItem() == null) {
                    Share share = createShare(guest, guestUserId);
                    shareStorage.storeShare(share, parameters);
                    shares.add(share);
                } else {
                    // TODO
                }
            }

            if (ownsConnection) {
                con.commit();
            }
            return shares;
        } catch (Exception e) {
            if (ownsConnection) {
                Databases.rollback(con);
            }
            throw new OXException(e); // TODO
        } finally {
            if (ownsConnection) {
                Databases.autocommit(con);
                dbService.backWritable(context, con);
            }
        }
    }

    private int getUserPermissionBits() {
        Set<Permission> perms = new HashSet<Permission>();
        perms.add(Permission.DENIED_PORTAL);
        perms.add(Permission.READ_CREATE_SHARED_FOLDERS);
        Permission modulePermission = Module.getForFolderConstant(request.getModule()).getPermission();
        if (modulePermission != null) {
            perms.add(modulePermission);
        }
        return Permission.toBits(perms);
    }

    private Share createShare(Guest guest, int guestUserId) {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        Date created = new Date();
        DefaultShare share = new DefaultShare();
        share.setToken(ShareTool.generateToken(contextId));
        share.setAuthentication(guest.getAuthenticationMode().getID());
        share.setExpires(guest.getExpires());
        share.setContextID(contextId);
        share.setCreated(created);
        share.setLastModified(created);
        share.setCreatedBy(userId);
        share.setModifiedBy(userId);
        share.setGuest(guestUserId);
        share.setModule(request.getModule());
        share.setFolder(request.getFolder());

        return share;
    }

    private User prepareGuestUser(Guest entity) throws OXException {
        User user = session.getUser();
        UserImpl guest = new UserImpl();
        guest.setCreatedBy(session.getUserId());
        guest.setPreferredLanguage(user.getPreferredLanguage());
        guest.setTimeZone(user.getTimeZone());
        guest.setDisplayName(entity.getDisplayName());
        guest.setMailEnabled(true);
        guest.setPasswordMech("{CRYPTO_SERVICE}");
        AuthenticationMode authenticationMode = entity.getAuthenticationMode();
        if (authenticationMode != null && authenticationMode != AuthenticationMode.ANONYMOUS) {
            guest.setMail(entity.getMailAddress());
            guest.setUserPassword(getShareCryptoService().encrypt(entity.getPassword()));
        } else {
            guest.setMail(""); // not null
        }
        if (false == Strings.isEmpty(entity.getContactID()) && false == Strings.isEmpty(entity.getContactFolderID())) {
            Map<String, Set<String>> attributes = guest.getAttributes();
            if (null == attributes) {
                attributes = new HashMap<String, Set<String>>(2);
                attributes.put("com.openexchange.user.guestContactFolderID", Collections.singleton(entity.getContactFolderID()));
                attributes.put("com.openexchange.user.guestContactID", Collections.singleton(entity.getContactID()));
            }
            guest.setAttributes(attributes);
        }
        return guest;
    }

}
