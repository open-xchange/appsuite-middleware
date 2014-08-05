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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.CreateRequest;
import com.openexchange.share.DefaultShare;
import com.openexchange.share.Entity;
import com.openexchange.share.Share;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link ShareCreator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ShareCreator extends SharePerformer<List<Share>> {

    protected final CreateRequest createRequest;

    /**
     * Initializes a new {@link ShareCreator}.
     * @param createRequest
     * @param entity
     * @param session
     */
    public ShareCreator(ServiceLookup services, CreateRequest createRequest, ServerSession session) {
        super(services, session);
        this.createRequest = createRequest;
    }

    @Override
    protected List<Share> perform() throws OXException {
        DatabaseService dbService = getDatabaseService();
        UserService userService = getUserService();
        ShareStorage shareStorage = getShareStorage();

        Context context = session.getContext();
        boolean ownsConnection = false;
        Connection con = createRequest.getConnection();
        if (con == null) {
            con = dbService.getWritable(context);
            ownsConnection = true;
        }

        List<Entity> entities = createRequest.getEntities();
        List<Share> shares = new ArrayList<Share>(entities.size());
        // TODO: can possibly removed if OXFolderManagerImpl doesn't try to commit foreign connections anymore...
//        ResilientConnection con = new ResilientConnection(dbService.getWritable(context));
        try {
            if (ownsConnection) {
                Databases.startTransaction(con);
            }

            for (Entity entity : entities) {
                User guest = prepareGuest(entity);
                int guestId = userService.createUser(con, context, guest);
                UserPermissionBitsStorage.getInstance().saveUserPermissionBits(con, getUserPermissionBits(entity), guestId, context); // FIXME: to service layer
                if (createRequest.getItem() == null) {
                    Share share = createShare(guestId);
                    StorageParameters parameters = new StorageParameters()
                        .put(Connection.class.getName(), con);
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

    private int getUserPermissionBits(Entity entity) {
        Set<com.openexchange.groupware.userconfiguration.Permission> perms = new HashSet<com.openexchange.groupware.userconfiguration.Permission>();
        perms.add(com.openexchange.groupware.userconfiguration.Permission.DENIED_PORTAL);
        perms.add(com.openexchange.groupware.userconfiguration.Permission.READ_CREATE_SHARED_FOLDERS);
        Permission modulePermission = Module.getForFolderConstant(createRequest.getModule()).getPermission();
        if (modulePermission != null) {
            perms.add(modulePermission);
        }
        return com.openexchange.groupware.userconfiguration.Permission.toBits(perms);
    }

    private Share createShare(int guestId) {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        Date created = new Date();
        DefaultShare share = new DefaultShare();
        share.setToken(ShareTool.generateToken(contextId));
        share.setAuthentication(AuthenticationMode.ANONYMOUS.getID()); // TODO
        share.setContextID(contextId);
        share.setCreated(created);
        share.setLastModified(created);
        share.setCreatedBy(userId);
        share.setModifiedBy(userId);
        share.setGuest(guestId);
        share.setModule(createRequest.getModule());
        share.setFolder(createRequest.getFolder());

        return share;
    }

    private User prepareGuest(Entity entity) throws OXException {
        User user = session.getUser();
        UserImpl guest = new UserImpl();
        guest.setCreatedBy(session.getUserId());
        guest.setPreferredLanguage(user.getPreferredLanguage());
        guest.setTimeZone(user.getTimeZone());
        guest.setDisplayName(entity.getMailAddress());
        guest.setMail(entity.getMailAddress());
        guest.setMailEnabled(true);
        guest.setPasswordMech("{CRYPTO_SERVICE}");
        AuthenticationMode authenticationMode = entity.getAuthenticationMode();
        if (authenticationMode != null && authenticationMode != AuthenticationMode.ANONYMOUS) {
            guest.setUserPassword(encrypt(entity.getPassword()));
        }
        return guest;
    }

    // FIXME: centralize, see Authenticator in JSON bundle
    private static String encrypt(String value) throws OXException {
        CryptoService cryptoService = ShareServiceLookup.getService(CryptoService.class, true);
        String cryptKey = ShareServiceLookup.getService(ConfigurationService.class, true).getProperty(
            "com.openexchange.share.cryptKey",
            "erE2e8OhAo71");
        return cryptoService.encrypt(value, cryptKey);
    }

}
