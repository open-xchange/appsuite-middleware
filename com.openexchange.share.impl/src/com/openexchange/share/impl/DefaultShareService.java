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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.userconfiguration.RdbUserPermissionBitsStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.Guest;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.user.UserService;

/**
 * {@link DefaultShareService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class DefaultShareService implements ShareService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultShareService.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DefaultShareService}.
     *
     * @param storage The underlying share storage
     */
    public DefaultShareService(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Share resolveToken(String token) throws OXException {
        int contextID = ShareTool.extractContextId(token);
        Share share = services.getService(ShareStorage.class).loadShare(contextID, token, StorageParameters.NO_PARAMETERS);
        if (null != share && share.isExpired()) {
            LOG.info("Detected expired share ({}): {}", share.getExpires(), share);
            removeShares(new ConnectionHelper(contextID, services, true), Collections.singletonList(share));
            return null;
        }
        return share;
    }

    @Override
    public List<Share> getAllShares(Session session) throws OXException {
        List<Share> shares = services.getService(ShareStorage.class).loadSharesCreatedBy(
            session.getContextId(), session.getUserId(), StorageParameters.NO_PARAMETERS);
        return removeExpired(session, shares);
    }

    @Override
    public int[] deleteSharesForFolder(Session session, String folder, int module, int[] guests) throws OXException {
        LOG.info("Deleting shares to guest user(s) {} for folder {} in context {}...",
            Arrays.toString(guests), folder, session.getContextId());
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            int[] guestIDs = deleteSharesForFolder(connectionHelper.getParameters(), session.getContextId(), folder, module, guests);
            deleteGuestUsers(connectionHelper.getConnection(), session.getContextId(), guestIDs);
            connectionHelper.commit();
            LOG.info("Shares to guest user(s) {} for folder {} in context {} deleted successfully.",
                Arrays.toString(guestIDs), folder, session.getContextId());
            return guestIDs;
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public List<Share> addSharesToFolder(Session session, String folder, int module, List<Guest> guests) throws OXException {
        LOG.info("Adding shares to guest user(s) {} for folder {} in context {}...", guests, folder, session.getContextId());
        List<Share> shares = new ArrayList<Share>(guests.size());
        Context context = services.getService(ContextService.class).getContext(session.getContextId());
        int permissionBits = ShareTool.getUserPermissionBits(module);
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            User sharingUser = services.getService(UserService.class).getUser(
                connectionHelper.getConnection(), session.getUserId(), context);
            for (Guest guest : guests) {
                User guestUser = createGuestUser(connectionHelper.getConnection(), context, sharingUser, permissionBits, guest);
                Share share = ShareTool.prepareShare(
                    context.getContextId(), guestUser, module, folder, guest.getExpires(), guest.getAuthenticationMode());
                services.getService(ShareStorage.class).storeShare(share, connectionHelper.getParameters());
                shares.add(share);
            }
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        LOG.info("Shares added successfully for folder {} in context {}: {}", folder, session.getContextId(), shares);
        return shares;
    }

    @Override
    public List<String> generateShareURLs(List<Share> shares, String protocol, String fallbackHostname) throws OXException {
        List<String> urls = new ArrayList<String>(shares.size());
        for (Share share : shares) {
            urls.add(ShareTool.getShareUrl(share, protocol, fallbackHostname));
        }

        return urls;
    }

    /**
     * Removes all expired shares from the supplied list.
     *
     * @param session The session
     * @param shares The shares
     * @return The shares, without the expired ones
     * @throws OXException
     */
    private List<Share> removeExpired(Session session, List<Share> shares) throws OXException {
        List<Share> expiredShares = ShareTool.filterExpiredShares(shares);
        if (null != expiredShares && 0 < expiredShares.size()) {
            if (LOG.isInfoEnabled()) {
                for (Share share : expiredShares) {
                    LOG.info("Detected expired share ({}): {}", share.getExpires(), share);
                }
            }
            removeShares(new ConnectionHelper(session, services, true), expiredShares);
        }
        return shares;
    }

    /**
     * Removes the supplied shares, i.e. deletes the share entries from the underlying storage along with the associated guest users.
     *
     * @param connectionHelper A connection helper
     * @param shares The shares to delete
     * @throws OXException
     */
    private void removeShares(ConnectionHelper connectionHelper, List<Share> shares) throws OXException {
        if (null != shares && 0 < shares.size()) {
            /*
             * gather share tokens / guest user ids
             */
            int[] guestIDs = new int[shares.size()];
            List<String> tokens = new ArrayList<String>(shares.size());
            for (int i = 0; i < shares.size(); i++) {
                Share share = shares.get(i);
                guestIDs[i] = share.getGuest();
                tokens.add(share.getToken());
            }
            try {
                connectionHelper.start();
                /*
                 * delete guest users and share entries
                 */
                deleteGuestUsers(connectionHelper.getConnection(), connectionHelper.getContextID(), guestIDs);
                ShareStorage shareStorage = services.getService(ShareStorage.class);
                shareStorage.deleteShares(connectionHelper.getContextID(), tokens, connectionHelper.getParameters());
                LOG.info("Deleted {} share(s) in context {}: {}", tokens.size(), connectionHelper.getContextID(), tokens);
                connectionHelper.commit();
            } finally {
                connectionHelper.finish();
            }
        }
    }

    /**
     * Creates a guest user.
     *
     * @param connection A (writable) connection to the database
     * @param context The context
     * @param sharingUser The sharing user
     * @param permissionBits The permission bits to apply to the guest user
     * @param guest The guest description
     * @return The created guest user
     * @throws OXException
     */
    private User createGuestUser(Connection connection, Context context, User sharingUser, int permissionBits, Guest guest) throws OXException {
        UserImpl guestUser = ShareTool.prepareGuestUser(services, sharingUser, guest);
        int guestID = services.getService(UserService.class).createUser(connection, context, guestUser);
        guestUser.setId(guestID);
        UserPermissionBitsStorage.getInstance().saveUserPermissionBits(connection, permissionBits, guestID, context); // FIXME: to service layer
        if (AuthenticationMode.ANONYMOUS == guest.getAuthenticationMode()) {
            LOG.info("Created anonymous guest user with permissions {} in context {}: {}",
                permissionBits, context.getContextId(), guestID);
        } else {
            LOG.info("Created guest user {} with permissions {} in context {}: {}",
                guestUser.getMail(), permissionBits, context.getContextId(), guestID);
        }
        return guestUser;
    }

    /**
     * Deletes guest users.
     *
     * @param connection A (writable) database connection
     * @param contextID The context ID
     * @param guestIDs The identifiers of the guest users to delete
     * @throws OXException
     */
    private void deleteGuestUsers(Connection connection, int contextID, int[] guestIDs) throws OXException {
        if (null != guestIDs && 0 < guestIDs.length) {
            UserService userService = services.getService(UserService.class);
            Context context = userService.getContext(contextID);
            try {
                for (int guestID : guestIDs) {
                    /*
                     * delete permission bits & user
                     */
                    RdbUserPermissionBitsStorage.deleteUserPermissionBits(guestID, connection, context); // TODO: service layer
                    userService.deleteUser(connection, context, guestID);
                }
                LOG.info("Deleted {} guest user(s) in context {}: {}", guestIDs.length, contextID, Arrays.toString(guestIDs));
            } catch (SQLException e) {
                throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
            }
        }
    }

    /**
     * Deletes shares for a specific folder that were bound to one of the supplied guest user IDs.
     *
     * @param storageParameters The storage parameters
     * @param contextID The context ID
     * @param folder The folder ID
     * @param module The module ID
     * @param guests The guest user IDs
     * @return The guest IDs that were bound to the deleted shares
     * @throws OXException
     */
    private int[] deleteSharesForFolder(StorageParameters storageParameters, int contextID, String folder, int module, int[] guests) throws OXException {
        /*
         * load known shares for folder
         */
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        List<Share> shares = shareStorage.loadSharesForFolder(contextID, folder, storageParameters);
        /*
         * collect share tokens for matching guest users
         */
        List<String> tokens = new ArrayList<String>(shares.size());
        List<Integer> guestIDs = new ArrayList<Integer>(shares.size());
        for (Share share : shares) {
            for (int i = 0; i < guests.length; i++) {
                if (guests[i] == share.getGuest()) {
                    guestIDs.add(Integer.valueOf(share.getGuest()));
                    tokens.add(share.getToken());
                    break;
                }
            }
        }
        /*
         * delete shares
         */
        if (0 < tokens.size()) {
            shareStorage.deleteShares(contextID, tokens, storageParameters);
            LOG.info("Deleted {} share(s) for folder {} in context {}: {}", tokens.size(), folder, contextID, tokens);
        }
        int[] guestUserIDs = new int[guestIDs.size()];
        for (int i = 0; i < guestIDs.size(); i++) {
            guestUserIDs[i] = guestIDs.get(i).intValue();
        }
        return guestUserIDs;
    }

    /**
     * Gets all shares created in the supplied context.
     *
     * @param contextId The contextId
     * @return The shares
     * @throws OXException
     */
    public List<Share> getAllShares(int contextId) throws OXException {
        List<Share> result = new ArrayList<Share>();
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper helper = new ConnectionHelper(contextId, services, false);
        result.addAll(shareStorage.loadSharesForContext(contextId, helper.getParameters()));
        return result;
    }

    /**
     * Gets all shares created in the supplied context by supplied user.
     *
     * @param contextId The contextId
     * @param userId The userId
     * @return The shares
     * @throws OXException
     */
    public List<Share> getAllShares(int contextId, int userId) throws OXException {
        List<Share> result = new ArrayList<Share>();
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper helper = new ConnectionHelper(contextId, services, false);
        result.addAll(shareStorage.loadSharesCreatedBy(contextId, userId, helper.getParameters()));
        return result;
    }

    /**
     * Remove all shares identified by supplied tokens.
     *
     * @param tokens The tokens
     * @throws OXException If removal fails
     */
    public void removeShares(String[] tokens) throws OXException {
        Map<Integer, List<Share>> shareMap = new HashMap<Integer, List<Share>>();
        for (String token : tokens) {
            int contextId = ShareTool.extractContextId(token);
            List<Share> shares = shareMap.get(contextId);
            if (null == shares) {
                shares = new ArrayList<Share>();
                shareMap.put(contextId, shares);
            }
            shares.add(resolveToken(token));
        }
        for (int contextId : shareMap.keySet()) {
            ConnectionHelper helper = new ConnectionHelper(contextId, services, false);
            List<Share> shares = shareMap.get(contextId);
            removeShares(helper, shares);
        }
    }

    /**
     * Remove all shares in supplied context.
     *
     * @param contextId The contextId
     * @throws OXException If removal fails.
     */
    public void removeShares(int contextId) throws OXException {
        List<Share> shares = getAllShares(contextId);
        ConnectionHelper helper = new ConnectionHelper(contextId, services, false);
        removeShares(helper, shares);
    }

    /**
     * Remove all shares created by supplied user in supplied context.
     *
     * @param contextId The contextId
     * @param userId The userId
     * @throws OXException If removal fails
     */
    public void removeShares(int contextId, int userId) throws OXException {
        List<Share> shares = getAllShares(contextId, userId);
        ConnectionHelper helper = new ConnectionHelper(contextId, services, false);
        removeShares(helper, shares);
    }

}
