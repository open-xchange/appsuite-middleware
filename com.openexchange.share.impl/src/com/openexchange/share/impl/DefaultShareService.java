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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ResolvedShare;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link DefaultShareService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
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
    public ResolvedShare resolveToken(String token) throws OXException {
        int contextID = ShareTool.extractContextId(token);
        int guestID = ShareTool.extractUserId(token);
        String baseToken = ShareTool.extractBaseToken(token);
        if (contextID < 0 || guestID < 0 || baseToken == null) {
            throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
        }

        User guest = services.getService(UserService.class).getUser(guestID, contextID);
        if (!baseToken.equals(ShareTool.getBaseToken(guest))) {
            throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
        }

        List<Share> shares = services.getService(ShareStorage.class).loadShares(contextID, guestID, StorageParameters.NO_PARAMETERS);
        List<ShareTarget> targets = new ArrayList<ShareTarget>();
        for (Share share : shares) {
            targets.add(share.getTarget());
        }
        ResolvedShare resolvedShare = new ResolvedShare();
        resolvedShare.setContextID(contextID);
        resolvedShare.setGuestID(guestID);
        resolvedShare.setTargets(targets);
        return resolvedShare;
    }

//    @Override
//    public Share resolveToken(String token, String path) throws OXException {
//        return null; //TODO
//    }

    @Override
    public List<Share> getAllShares(Session session) throws OXException {
        List<Share> shares = services.getService(ShareStorage.class).loadSharesCreatedBy(
            session.getContextId(), session.getUserId(), StorageParameters.NO_PARAMETERS);
        return removeExpired(shares);
    }

    @Override
    public List<Share> addTarget(Session session, ShareTarget shareTarget, List<ShareRecipient> recipients) throws OXException {
        return addTargets(session, Collections.singletonList(shareTarget), recipients).get(shareTarget);
    }

    @Override
    public Map<ShareTarget, List<Share>> addTargets(Session session, List<ShareTarget> targets, List<ShareRecipient> recipients) throws OXException {
        if (null == targets || 0 == targets.size() || null == recipients || 0 == recipients.size()) {
            return Collections.emptyMap();
        }
        Map<ShareTarget, List<Share>> sharesByTarget = new HashMap<ShareTarget, List<Share>>();
        for (ShareTarget target : targets) {
            sharesByTarget.put(target, new ArrayList<Share>(recipients.size()));
        }
        List<Share> allShares = new ArrayList<Share>(recipients.size() * targets.size());
        int contextID = session.getContextId();
        LOG.info("Adding share target(s) {} for recipients {} in context {}...", targets, recipients, I(contextID));
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        Context context = services.getService(ContextService.class).getContext(session.getContextId());
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            /*
             * prepare guest users and resulting shares
             */
            User sharingUser = services.getService(UserService.class).getUser(connectionHelper.getConnection(), session.getUserId(), context);
            List<Integer> guestIDs = new ArrayList<Integer>(recipients.size());
            for (ShareRecipient recipient : recipients) {
                int permissionBits = ShareTool.getUserPermissionBitsForTargets(recipient, targets);
                User guestUser = getGuestUser(connectionHelper.getConnection(), context, sharingUser, permissionBits, recipient);
                guestIDs.add(I(guestUser.getId()));
                for (ShareTarget target : targets) {
                    Share share = ShareTool.prepareShare(context.getContextId(), sharingUser, guestUser.getId(), target);
                    sharesByTarget.get(target).add(share);
                    allShares.add(share);
                }
            }
            /*
             * store shares
             */
            shareStorage.storeShares(contextID, allShares, connectionHelper.getParameters());
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        LOG.info("Share target(s) {} for recipients {} in context {} added successfully.", targets, recipients, I(contextID));
        return sharesByTarget;
    }

    @Override
    public void deleteTarget(Session session, ShareTarget target, List<Integer> guestIDs) throws OXException {
        deleteTargets(session, Collections.singletonList(target), guestIDs);
    }

    @Override
    public void deleteTargets(Session session, List<ShareTarget> targets, List<Integer> guestIDs) throws OXException {
        if (null == targets || 0 == targets.size() || null == guestIDs || 0 == guestIDs.size()) {
            return;
        }
        LOG.info("Deleting share target(s) {} for guest users {} in context {}...", targets, guestIDs, session.getContextId());
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            shareStorage.deleteShares(session.getContextId(), targets, I2i(guestIDs), connectionHelper.getParameters());
            // TODO: adjust user permission bits, delete user if last share deleted
//            shareStorage.
            /*
             * perform updates & adjust user permission bits
             */
//            if (0 < sharesToUpdate.size()) {
//                updateShares(connectionHelper, sharesToUpdate);
//            }
//            /*
//             * perform deletes & delete guest users
//             */
//            if (0 < sharesToDelete.size()) {
//                removeShares(connectionHelper, sharesToDelete);
//            }
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        LOG.info("Share target(s) {} for guest users {} in context {} deleted successfully.", targets, guestIDs, session.getContextId());
    }

//    @Override
//    public int[] deleteShares(Session session, List<String> tokens, Date clientTimestamp) throws OXException {
//        if (null == tokens || 0 == tokens.size()) {
//            return new int[0];
//        }
//        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
//        try {
//            connectionHelper.start();
//            /*
//             * load & check shares, gather associated guest user IDs
//             */
//            List<ShareList> shares = services.getService(ShareStorage.class).loadShares(session.getContextId(), tokens, connectionHelper.getParameters());
//            for (String token : tokens) {
//                ShareList share = ShareTool.findShare(shares, token);
//                if (null == share || ShareTool.extractContextId(token) != session.getContextId()) {
//                    throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
//                }
//                if (session.getUserId() != share.getCreatedBy()) {
//                    throw ShareExceptionCodes.NO_DELETE_PERMISSIONS.create(I(session.getUserId()), token, I(session.getContextId()));
//                }
//                if (share.getLastModified().after(clientTimestamp)) {
//                    throw ShareExceptionCodes.CONCURRENT_MODIFICATION.create(token);
//                }
//            }
//            /*
//             * proceed with deletion
//             */
//            int[] guestIDs = removeShares(connectionHelper, shares);
//            connectionHelper.commit();
//            return guestIDs;
//        } finally {
//            connectionHelper.finish();
//        }
//    }


    @Override
    public void updateShare(Session session, Share share, Date clientTimestamp) throws OXException {
//        String token = share.getToken();
//        if (Strings.isEmpty(token)) {
//            throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
//        }
//        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
//        try {
//            connectionHelper.start();
//            /*
//             * load & check share
//             */
//            ShareStorage storage = services.getService(ShareStorage.class);
//            ShareList storedShare = storage.loadShare(session.getContextId(), token, connectionHelper.getParameters());
//            if (null == share || ShareTool.extractContextId(token) != session.getContextId()) {
//                throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
//            }
//            if (session.getUserId() != share.getCreatedBy()) {
//                throw ShareExceptionCodes.NO_EDIT_PERMISSIONS.create(I(session.getUserId()), token, I(session.getContextId()));
//            }
//            if (share.getLastModified().after(clientTimestamp)) {
//                throw ShareExceptionCodes.CONCURRENT_MODIFICATION.create(token);
//            }
//            /*
//             * prepare update
//             */
//            //TODO
//            DefaultShareList updatedShare = new DefaultShareList(storedShare);
//            updatedShare.setLastModified(new Date());
//            updatedShare.setModifiedBy(session.getUserId());
//
//
//
//
//
//
//
//            /*
//             * proceed with update
//             */
//            storage.updateShare(updatedShare, connectionHelper.getParameters());
//            connectionHelper.commit();
//            return updatedShare;
//        } finally {
//            connectionHelper.finish();
//        }
    }

    @Override
    public List<String> generateShareURLs(int contextId, List<Share> shares, String protocol, String fallbackHostname) throws OXException {
        UserService userService = services.getService(UserService.class);
        List<String> urls = new ArrayList<String>(shares.size());
        for (Share share : shares) {
            User guest = userService.getUser(share.getGuest(), contextId);
            String hostname = getHostname(share.getCreatedBy(), contextId, fallbackHostname);
            String prefix = getServletPrefix();
            urls.add(protocol + hostname + prefix + ShareTool.SHARE_SERVLET + '/' + ShareTool.generateShareToken(contextId, guest));
        }
        return urls;
    }

    private String getHostname(int userID, int contextID, String fallbackHostname) {
        HostnameService hostnameService = services.getService(HostnameService.class);
        if (hostnameService == null) {
            return fallbackHostname;
        }

        return hostnameService.getHostname(userID, contextID);
    }

    private String getServletPrefix() {
        DispatcherPrefixService prefixService = services.getService(DispatcherPrefixService.class);
        if (prefixService == null) {
            return DispatcherPrefixService.DEFAULT_PREFIX;
        }

        return prefixService.getPrefix();
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
//            List<Share> shares = shareStorage.loadSharesForContext(contextId, connectionHelper.getParameters());
            connectionHelper.commit();
//            return shares;
            return null;
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
            Integer contextId = I(ShareTool.extractContextId(token));
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
        for (Map.Entry<Integer, List<String>> entry : tokensByContextID.entrySet()) {
            removeShares(entry.getKey().intValue(), entry.getValue());
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
                throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
            }
        }
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, true);
        try {
            connectionHelper.start();
//            List<Share> sharesToRemove = shareStorage.loadShares(contextId, tokens, connectionHelper.getParameters());
//            removeShares(connectionHelper, sharesToRemove);
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
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, true);
        try {
            connectionHelper.start();
//            List<ShareList> shares = shareStorage.loadSharesForContext(contextId, connectionHelper.getParameters());
//            removeShares(connectionHelper, shares);
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
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, true);
        try {
            connectionHelper.start();
//            List<ShareList> shares = shareStorage.loadSharesCreatedBy(contextId, userId, connectionHelper.getParameters());
//            removeShares(connectionHelper, shares);
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
        //TODO : remove expired targets, remove parent share if all targets expired
//        List<Share> expiredShares = ShareTool.filterExpiredShares(shares);
//        if (null != expiredShares && 0 < expiredShares.size()) {
//            if (LOG.isInfoEnabled()) {
//                for (Share share : expiredShares) {
//                    LOG.info("Detected expired share ({}): {}", share.getExpiryDate(), share);
//                }
//            }
//            ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
//            try {
//                connectionHelper.start();
//                removeShares(connectionHelper, expiredShares);
//                connectionHelper.commit();
//            } finally {
//                connectionHelper.finish();
//            }
//        }
        return shares;
    }

    /**
     * Removes the share in case it is expired.
     *
     * @param share The share
     * @return The share, if it is not expired, or <code>null</code>, otherwise
     * @throws OXException
     */
    private List<Share> removeExpired(List<Share> shares) throws OXException {
//        if (null != share && null != share.getTargets()) {
//            boolean updateNeeded = false;
//            Iterator<Share> iterator = share.getTargets().iterator();
//            while (iterator.hasNext()) {
//                Share target = iterator.next();
//                if (target.isExpired()) {
//                    iterator.remove();
//                    updateNeeded = true;
//                }
//            }
//            if (updateNeeded) {
//                ConnectionHelper connectionHelper = new ConnectionHelper(share.getContextID(), services, true);
//                try {
//                    connectionHelper.start();
//                    if (0 == share.getTargets().size()) {
//                        removeShares(connectionHelper, Collections.singletonList(share));
//                        share = null;
//                    } else {
//                        updateShares(connectionHelper, Collections.singletonList(share));
//                    }
//                    connectionHelper.commit();
//                } finally {
//                    connectionHelper.finish();
//                }
//            }
//        }
        return shares;
    }

    /**
     * Removes the supplied shares, i.e. deletes the share entries from the underlying storage along with the associated guest users.
     *
     * @param connectionHelper A (started) connection helper
     * @param shares The shares to delete
     * @return The identifiers of the guest users that have been removed through the removal of the shares
     * @throws OXException
     */
    private int[] removeShares(ConnectionHelper connectionHelper, List<Share> shares) throws OXException {
        if (null == shares || 0 == shares.size()) {
            return new int[0];
        }
        Context context = services.getService(ContextService.class).getContext(connectionHelper.getContextID());
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        UserPermissionService userPermissionService = services.getService(UserPermissionService.class);
        //ContactUserStorage contactUserStorage = services.getService(ContactUserStorage.class);
        UserService userService = services.getService(UserService.class);
        /*
         * delete shares
         */
//        shareStorage.deleteShares(context.getContextId(), ShareTool.extractTokens(shares), connectionHelper.getParameters());
        /*
         * delete affected guest users
         */
        List<Integer> deletedGuestIDs = new ArrayList<Integer>(shares.size());
        for (Share share : shares) {
            userPermissionService.deleteUserPermissionBits(connectionHelper.getConnection(), context, share.getGuest());
            //TODO: delete by user ID
            // contactUserStorage.deleteGuestContact(session.getContextId(), share.getGuest(), null, connectionHelper.getConnection());
            userService.deleteUser(connectionHelper.getConnection(), context, share.getGuest());
            deletedGuestIDs.add(I(share.getGuest()));
        }
        LOG.info("Deleted {} guest user(s) in context {}: {}", deletedGuestIDs.size(), context.getContextId(), deletedGuestIDs);
        return I2i(deletedGuestIDs);
    }

    /**
     * Updates the supplied shares, i.e. updates the share entries from the underlying storage along with updating associated guest
     * permissions as needed.
     *
     * @param connectionHelper A (started) connection helper
     * @param shares The shares to update
     * @throws OXException
     */
    private void updateShares(ConnectionHelper connectionHelper, List<Share> shares) throws OXException {
        if (null == shares || 0 == shares.size()) {
            return;
        }
        Context context = services.getService(ContextService.class).getContext(connectionHelper.getContextID());
        ShareStorage shareStorage = services.getService(ShareStorage.class);
//        shareStorage.updateShares(context.getContextId(), shares, connectionHelper.getParameters());
        for (Share share : shares) {
            int requiredPermissionBits = ShareTool.getUserPermissionBits(share);
            setPermissionBits(connectionHelper.getConnection(), context, share.getGuest(), requiredPermissionBits, false);
        }
    }

    /**
     * Gets a guest user for a new share. A new guest use is created if no matching one exists, the permission bits are applied as needed.
     *
     * @param connection A (writable) connection to the database
     * @param context The context
     * @param sharingUser The sharing user
     * @param permissionBits The permission bits to apply to the guest user
     * @param recipient The recipient description
     * @return The guest user
     * @throws OXException
     */
    private User getGuestUser(Connection connection, Context context, User sharingUser, int permissionBits, ShareRecipient recipient) throws OXException {
        UserService userService = services.getService(UserService.class);
        ContactUserStorage contactUserStorage = services.getService(ContactUserStorage.class);
        if (GuestRecipient.class.isInstance(recipient) && services.getService(ConfigurationService.class).getBoolProperty(
            "com.openexchange.share.aggregateShares",
            true)) {
            /*
             * re-use existing, non-anonymous guest user if possible
             */
            GuestRecipient guestRecipient = (GuestRecipient) recipient;
            User existingGuestUser = null;
            try {
                existingGuestUser = userService.searchUser(guestRecipient.getEmailAddress(), context, false, true, true);
            } catch (OXException e) {
                if (false == LdapExceptionCode.NO_USER_BY_MAIL.equals(e)) {
                    throw e;
                }
            }
            if (null != existingGuestUser) {
                /*
                 * combine permission bits with existing ones
                 */
                UserPermissionBits userPermissionBits = setPermissionBits(connection, context, existingGuestUser.getId(), permissionBits, true);
                LOG.info(
                    "Using existing guest user {} with permissions {} in context {}: {}",
                    existingGuestUser.getMail(),
                    userPermissionBits.getPermissionBits(),
                    context.getContextId(),
                    existingGuestUser.getId());
                return existingGuestUser;
            }
        }
        /*
         * create new guest user
         */
        UserImpl guestUser;
        if (AnonymousRecipient.class.isInstance(recipient)) {
            guestUser = ShareTool.prepareGuestUser(services, sharingUser, (AnonymousRecipient) recipient);
        } else if (GuestRecipient.class.isInstance(recipient)) {
            GuestRecipient guestRecipient = (GuestRecipient) recipient;
            if (Strings.isEmpty(guestRecipient.getPassword())) {
                guestRecipient.setPassword(PasswordUtility.generate());
            }
            guestUser = ShareTool.prepareGuestUser(services, sharingUser, guestRecipient);
        } else {
            throw new UnsupportedOperationException("unsupported share recipient: " + recipient);
        }
        Contact contact = new Contact();
        contact.setParentFolderID(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID);
        contact.setCreatedBy(sharingUser.getId());
        contact.setDisplayName(guestUser.getDisplayName());
        contact.setEmail1(guestUser.getMail());
        int contactId = contactUserStorage.createGuestContact(context.getContextId(), contact, connection);
        guestUser.setContactId(contactId);
        int guestID = userService.createUser(connection, context, guestUser);
        guestUser.setId(guestID);
        contact.setInternalUserId(guestID);
        contactUserStorage.updateGuestContact(context.getContextId(), guestID, contactId, contact, new Date(), connection);
        services.getService(UserPermissionService.class).saveUserPermissionBits(
            connection,
            new UserPermissionBits(permissionBits, guestID, context.getContextId()));
        if (AnonymousRecipient.class.isInstance(recipient)) {
            LOG.info("Created anonymous guest user with permissions {} in context {}: {}", permissionBits, context.getContextId(), guestID);
        } else {
            LOG.info("Created guest user {} with permissions {} in context {}: {}", guestUser.getMail(), permissionBits, context.getContextId(), guestID);
        }
        return guestUser;
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

}
