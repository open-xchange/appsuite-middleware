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

import static com.openexchange.java.Autoboxing.I2i;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.DefaultShare;
import com.openexchange.share.Guest;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

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
     * @param services The service lookup reference
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
            ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
            try {
                connectionHelper.start();
                removeShares(connectionHelper, Collections.singletonList(share));
                connectionHelper.commit();
            } finally {
                connectionHelper.finish();
            }
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
            /*
             * remove shares for folder / guest user(s)
             */
            int[] affectedGuestIDs = deleteSharesForFolder(
                connectionHelper.getParameters(), session.getContextId(), folder, module, guests);
            if (null == affectedGuestIDs || 0 == affectedGuestIDs.length) {
                LOG.info("No guest users affected for shares in folder {}.", folder);
                return new int[0];
            }
            /*
             * delete / adjust guest users as needed
             */
            int[] deletedGuestIDs = cleanupGuestUsers(connectionHelper, session.getContextId(), affectedGuestIDs);
            connectionHelper.commit();
            LOG.info("Shares to guest user(s) {} for folder {} in context {} deleted successfully.",
                Arrays.toString(guests), folder, session.getContextId());
            return deletedGuestIDs;
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public int[] deleteShares(Session session, List<String> tokens, Date clientTimestamp) throws OXException {
        if (null == tokens || 0 == tokens.size()) {
            return new int[0];
        }
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            /*
             * load & check shares, gather associated guest user IDs
             */
            List<Share> shares = services.getService(ShareStorage.class).loadShares(
                session.getContextId(), tokens, connectionHelper.getParameters());
            Set<Integer> affectedGuestIDs = new HashSet<Integer>(shares.size());
            for (String token : tokens) {
                Share share = ShareTool.findShare(shares, token);
                if (null == share || ShareTool.extractContextId(token) != session.getContextId()) {
                    throw ShareExceptionCodes.UNKNWON_SHARE.create(token);
                }
                if (session.getUserId() != share.getCreatedBy()) {
                    throw ShareExceptionCodes.NO_DELETE_PERMISSIONS.create(
                        Integer.valueOf(session.getUserId()), token, Integer.valueOf(session.getContextId()));
                }
                if (share.getLastModified().after(clientTimestamp)) {
                    throw ShareExceptionCodes.CONCURRENT_MODIFICATION.create(token);
                }
                affectedGuestIDs.add(Integer.valueOf(share.getGuest()));
            }
            /*
             * proceed with deletion
             */
            services.getService(ShareStorage.class).deleteShares(session.getContextId(), tokens, connectionHelper.getParameters());
            /*
             * delete / adjust guest users as needed
             */
            int[] deletedGuestIDs = cleanupGuestUsers(connectionHelper, session.getContextId(), I2i(affectedGuestIDs));
            connectionHelper.commit();
            return deletedGuestIDs;
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public Share updateShare(Session session, Share share, Date clientTimestamp) throws OXException {
        String token = share.getToken();
        if (Strings.isEmpty(token)) {
            throw ShareExceptionCodes.UNKNWON_SHARE.create(token);
        }
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            /*
             * load & check share
             */
            Share storedShare = services.getService(ShareStorage.class).loadShare(
                session.getContextId(), token, connectionHelper.getParameters());
            if (null == share || ShareTool.extractContextId(token) != session.getContextId()) {
                throw ShareExceptionCodes.UNKNWON_SHARE.create(token);
            }
            if (session.getUserId() != share.getCreatedBy()) {
                throw ShareExceptionCodes.NO_EDIT_PERMISSIONS.create(
                    Integer.valueOf(session.getUserId()), token, Integer.valueOf(session.getContextId()));
            }
            if (share.getLastModified().after(clientTimestamp)) {
                throw ShareExceptionCodes.CONCURRENT_MODIFICATION.create(token);
            }
            /*
             * prepare update
             */
            DefaultShare updatedShare = new DefaultShare(storedShare);
            updatedShare.setExpires(updatedShare.getExpires());
            updatedShare.setLastModified(new Date());
            updatedShare.setModifiedBy(session.getUserId());
            /*
             * proceed with update
             */
            services.getService(ShareStorage.class).updateShare(updatedShare, connectionHelper.getParameters());
            connectionHelper.commit();
            return updatedShare;
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public List<Share> createShares(Session session, String folder, int module, List<Guest> guests) throws OXException {
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
                User guestUser = getGuestUser(connectionHelper.getConnection(), context, sharingUser, permissionBits, guest);
                Share share = ShareTool.prepareShare(sharingUser, context.getContextId(), module, folder, guestUser.getId(),
                    guest.getExpires(), guest.getAuthenticationMode());
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
     * Gets all shares created in the supplied context.
     *
     * @param contextId The contextId
     * @return The shares
     * @throws OXException
     */
    public List<Share> getAllShares(int contextId) throws OXException {
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, false);
        try {
            connectionHelper.start();
            List<Share> shares = shareStorage.loadSharesForContext(contextId, connectionHelper.getParameters());
            connectionHelper.commit();
            return shares;
        } finally {
            connectionHelper.finish();
        }
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
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, false);
        try {
            connectionHelper.start();
            List<Share> shares = shareStorage.loadSharesCreatedBy(contextId, userId, connectionHelper.getParameters());
            connectionHelper.commit();
            return shares;
        } finally {
            connectionHelper.finish();
        }
    }

    /**
     * Remove all shares identified by supplied tokens.
     *
     * @param tokens The tokens
     * @throws OXException If removal fails
     */
    public void removeShares(List<String> tokens) throws OXException {
        /*
         * order tokens by context
         */
        Map<Integer, List<String>> tokensByContextID = new HashMap<Integer, List<String>>();
        for (String token : tokens) {
            Integer contextId = Integer.valueOf(ShareTool.extractContextId(token));
            List<String> tokensInContext = tokensByContextID.get(contextId);
            if (null == tokensInContext) {
                tokensInContext = new ArrayList<String>();
                tokensByContextID.put(contextId, tokensInContext);
            }
            tokensInContext.add(token);
        }
        /*
         * delete shares per context
         */
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        for (Map.Entry<Integer, List<String>> entry : tokensByContextID.entrySet()) {
            int contextID = entry.getKey().intValue();
            ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
            try {
                connectionHelper.start();
                List<Share> sharesToRemove = shareStorage.loadShares(contextID, entry.getValue(), connectionHelper.getParameters());
                removeShares(connectionHelper, sharesToRemove);
                connectionHelper.commit();
            } finally {
                connectionHelper.finish();
            }
        }
    }

    /**
     * Remove all shares in context identified by supplied tokens.
     *
     * @param contextId The contextId
     * @param tokens The tokens
     * @throws OXException If removal fails
     */
    public void removeShares(int contextId, List<String> tokens) throws OXException {
        for (String token : tokens) {
            if (ShareTool.extractContextId(token) != contextId) {
                throw ShareExceptionCodes.UNKNWON_SHARE.create(token);
            }
        }
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, true);
        try {
            connectionHelper.start();
            ShareStorage shareStorage = services.getService(ShareStorage.class);
            List<Share> sharesToRemove = shareStorage.loadShares(contextId, tokens, connectionHelper.getParameters());
            removeShares(connectionHelper, sharesToRemove);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
    }

    /**
     * Remove all shares in supplied context.
     *
     * @param contextId The contextId
     * @throws OXException If removal fails.
     */
    public void removeShares(int contextId) throws OXException {
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, true);
        try {
            connectionHelper.start();
            ShareStorage shareStorage = services.getService(ShareStorage.class);
            List<Share> shares = shareStorage.loadSharesForContext(contextId, connectionHelper.getParameters());
            removeShares(connectionHelper, shares);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
    }

    /**
     * Remove all shares created by supplied user in supplied context.
     *
     * @param contextId The contextId
     * @param userId The userId
     * @throws OXException If removal fails
     */
    public void removeShares(int contextId, int userId) throws OXException {
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, true);
        try {
            connectionHelper.start();
            ShareStorage shareStorage = services.getService(ShareStorage.class);
            List<Share> shares = shareStorage.loadSharesCreatedBy(contextId, userId, connectionHelper.getParameters());
            removeShares(connectionHelper, shares);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
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
            ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
            try {
                connectionHelper.start();
                removeShares(connectionHelper, expiredShares);
                connectionHelper.commit();
            } finally {
                connectionHelper.finish();
            }
        }
        return shares;
    }

    /**
     * Removes the supplied shares, i.e. deletes the share entries from the underlying storage along with the associated guest users.
     *
     * @param connectionHelper A (started) connection helper
     * @param shares The shares to delete
     * @throws OXException
     */
    private void removeShares(ConnectionHelper connectionHelper, List<Share> shares) throws OXException {
        if (null != shares && 0 < shares.size()) {
            /*
             * gather share tokens / guest user ids
             */
            Set<Integer> affectedGuestIDs = new HashSet<Integer>(shares.size());
            List<String> tokens = new ArrayList<String>(shares.size());
            for (int i = 0; i < shares.size(); i++) {
                Share share = shares.get(i);
                affectedGuestIDs.add(Integer.valueOf(share.getGuest()));
                tokens.add(share.getToken());
            }
            /*
             * proceed with deletion
             */
            services.getService(ShareStorage.class).deleteShares(
                connectionHelper.getContextID(), tokens, connectionHelper.getParameters());
            /*
             * delete / adjust guest users as needed
             */
            cleanupGuestUsers(connectionHelper, connectionHelper.getContextID(), I2i(affectedGuestIDs));
            connectionHelper.commit();
            LOG.info("Deleted {} share(s) in context {}: {}", tokens.size(), connectionHelper.getContextID(), tokens);
        }
    }

    /**
     * Gets a guest user for a new share. A new guest use is created if no matching one exists, the permission bits are applied as needed.
     *
     * @param connection A (writable) connection to the database
     * @param context The context
     * @param sharingUser The sharing user
     * @param permissionBits The permission bits to apply to the guest user
     * @param guest The guest description
     * @return The guest user
     * @throws OXException
     */
    private User getGuestUser(Connection connection, Context context, User sharingUser, int permissionBits, Guest guest) throws OXException {
        UserService userService = services.getService(UserService.class);
        if (AuthenticationMode.ANONYMOUS != guest.getAuthenticationMode() && services.getService(
            ConfigurationService.class).getBoolProperty("com.openexchange.share.aggregateShares", true)) {
            /*
             * re-use existing, non-anonymous guest user if possible
             */
            User existingGuestUser = null;
            try {
                existingGuestUser = userService.searchUser(guest.getMailAddress(), context, false, true, true);
            } catch (OXException e) {
                if (false == LdapExceptionCode.NO_USER_BY_MAIL.equals(e)) {
                    throw e;
                }
            }
            if (null != existingGuestUser) {
                /*
                 * combine permission bits with existing ones
                 */
                UserPermissionBits userPermissionBits = setPermissionBits(
                    connection, context, existingGuestUser.getId(), permissionBits, true);
                LOG.info("Using existing guest user {} with permissions {} in context {}: {}", existingGuestUser.getMail(),
                    userPermissionBits.getPermissionBits(), context.getContextId(), existingGuestUser.getId());
                return existingGuestUser;
            }
        }
        /*
         * create new guest user
         */
        UserImpl guestUser = ShareTool.prepareGuestUser(services, sharingUser, guest);
        int guestID = userService.createUser(connection, context, guestUser);
        guestUser.setId(guestID);
        services.getService(UserPermissionService.class).saveUserPermissionBits(
            connection, new UserPermissionBits(permissionBits, guestID, context.getContextId()));
        if (AuthenticationMode.ANONYMOUS == guest.getAuthenticationMode()) {
            LOG.info("Created anonymous guest user with permissions {} in context {}: {}", permissionBits, context.getContextId(), guestID);
        } else {
            LOG.info("Created guest user {} with permissions {} in context {}: {}",
                guestUser.getMail(), permissionBits, context.getContextId(), guestID);
        }
        return guestUser;
    }

    /**
     * Deletes guest users that are no longer needed after all shares to them has been revoked. Guest users that are referenced by
     * existing shares are skipped, while there module permissions are adjusted to reflect access to the remaining shares.
     *
     * @param connectionHelper A (started) connection helper
     * @param contextID The context ID
     * @param guestIDs The identifiers of the guest users to delete
     * @return The identifiers of the deleted guest users
     * @throws OXException
     */
    private int[] cleanupGuestUsers(ConnectionHelper connectionHelper, int contextID, int[] guestIDs) throws OXException {
        if (null == guestIDs || 0 == guestIDs.length) {
            return new int[0];
        }
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        UserService userService = services.getService(UserService.class);
        Context context = userService.getContext(contextID);
        /*
         * check which guest users can be deleted
         */
        List<Integer> deletedGuestIDs = new ArrayList<Integer>(guestIDs.length);
        for (int guestID : guestIDs) {
            List<Share> sharesForGuest = shareStorage.loadSharesForGuest(contextID, guestID, connectionHelper.getParameters());
            if (null != sharesForGuest && 0 < sharesForGuest.size()) {
                /*
                 * guest user still has shares, adjust permissions as needed
                 */
                int requiredPermissionBits = ShareTool.getUserPermissionBits(sharesForGuest);
                setPermissionBits(connectionHelper.getConnection(), context, guestID, requiredPermissionBits, true);
            } else {
                /*
                 * no shares remaining, delete permission bits & user
                 */
                services.getService(UserPermissionService.class).deleteUserPermissionBits(
                    connectionHelper.getConnection(), context, guestID);
                userService.deleteUser(connectionHelper.getConnection(), context, guestID);
                LOG.info("Deleted {} guest user(s) in context {}: {}", guestIDs.length, contextID, Arrays.toString(guestIDs));
                deletedGuestIDs.add(Integer.valueOf(guestID));
            }
        }
        return I2i(deletedGuestIDs);
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
        Set<Integer> guestIDs = new HashSet<Integer>(shares.size());
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
        return I2i(guestIDs);
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
        }
        return userPermissionBits;
    }

}
