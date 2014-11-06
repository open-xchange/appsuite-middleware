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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.GuestShare;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.share.impl.cleanup.GuestCleaner;
import com.openexchange.share.impl.groupware.ShareQuotaProvider;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.user.UserService;
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
    private final GuestCleaner guestCleaner;

    /**
     * Initializes a new {@link DefaultShareService}.
     *
     * @param services The service lookup reference
     */
    public DefaultShareService(ServiceLookup services) {
        super();
        this.services = services;
        this.guestCleaner = new GuestCleaner(services);
    }

    @Override
    public GuestShare resolveToken(String token) throws OXException {
        ShareToken shareToken = new ShareToken(token);
        int contextID = shareToken.getContextID();
        User guest;
        try {
            guest = services.getService(UserService.class).getUser(shareToken.getUserID(), contextID);
        } catch (OXException e) {
            if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.debug("Guest user for share token {} not found, unable to resolve token.", shareToken, e);
                return null;
            }
            throw e;
        }
        if (false == guest.isGuest() || false == shareToken.equals(new ShareToken(contextID, guest))) {
            LOG.warn("Token mismatch for guest user {} and share token {}, cancelling token resolve request.", guest, shareToken);
            throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
        }
        List<Share> shares = services.getService(ShareStorage.class).loadSharesForGuest(
            contextID, guest.getId(), StorageParameters.NO_PARAMETERS);
        shares = removeExpired(contextID, shares);
        return 0 == shares.size() ? null : new ResolvedGuestShare(services, contextID, guest, shares);
    }

    @Override
    public List<ShareInfo> getAllShares(Session session) throws OXException {
        List<Share> shares = services.getService(ShareStorage.class).loadSharesCreatedBy(
            session.getContextId(), session.getUserId(), StorageParameters.NO_PARAMETERS);
        shares = removeExpired(session.getContextId(), shares);
        return ShareTool.toShareInfos(services, session.getContextId(), shares);
    }

    @Override
    public Set<Integer> getSharingUsersFor(int contextId, int guestId) throws OXException {
        return services.getService(ShareStorage.class).getSharingUsers(contextId, guestId, StorageParameters.NO_PARAMETERS);
    }

    @Override
    public List<GuestShare> addTargets(Session session, List<ShareTarget> targets, List<ShareRecipient> recipients) throws OXException {
        if (null == targets || 0 == targets.size() || null == recipients || 0 == recipients.size()) {
            return Collections.emptyList();
        }

        int contextID = session.getContextId();
        LOG.info("Adding share target(s) {} for recipients {} in context {}...", targets, recipients, I(contextID));
        List<GuestShare> guestShares = new ArrayList<GuestShare>(recipients.size());
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        Context context = services.getService(ContextService.class).getContext(session.getContextId());
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();

            int newShares = targets.size() * recipients.size();
            checkQuota(connectionHelper, session, newShares);
            /*
             * prepare guest users and resulting shares
             */
            Connection connection = connectionHelper.getConnection();
            User sharingUser = services.getService(UserService.class).getUser(connection, session.getUserId(), context);
            List<Share> sharesToStore = new ArrayList<Share>(newShares);
            for (ShareRecipient recipient : recipients) {
                int permissionBits = ShareTool.getRequiredPermissionBits(recipient, targets);
                User guestUser = getGuestUser(connection, context, sharingUser, permissionBits, recipient);
                List<Share> sharesForGuest = new ArrayList<Share>(targets.size());
                for (ShareTarget target : targets) {
                    Share share = ShareTool.prepareShare(context.getContextId(), sharingUser, guestUser.getId(), target);
                    sharesForGuest.add(share);
                    sharesToStore.add(share);
                }
                guestShares.add(new ResolvedGuestShare(services, contextID, guestUser, sharesForGuest));
            }
            /*
             * store shares
             */
            shareStorage.storeShares(contextID, sharesToStore, connectionHelper.getParameters());
            connectionHelper.commit();
            LOG.info("Share target(s) {} for recipients {} in context {} added successfully.", targets, recipients, I(contextID));
            return guestShares;
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public GuestShare updateTargets(Session session, List<ShareTarget> targets, int guestID, Date clientTimestamp) throws OXException {
        if (null == targets || 0 == targets.size()) {
            return null;
        }
        /*
         * prepare shares to update
         */
        Date now = new Date();
        List<Share> shares = new ArrayList<Share>(targets.size());
        for (ShareTarget target : targets) {
            Share share = new Share(guestID, target);
            share.setModified(now);
            share.setModifiedBy(session.getUserId());
            shares.add(share);
        }
        /*
         * perform update
         */
        Context context = services.getService(ContextService.class).getContext(session.getContextId());
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            User guestUser = services.getService(UserService.class).getUser(connectionHelper.getConnection(), guestID, context);
            services.getService(ShareStorage.class).updateShares(session.getContextId(), shares, clientTimestamp, connectionHelper.getParameters());
            connectionHelper.commit();
            return new ResolvedGuestShare(services, session.getContextId(), guestUser, shares);
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public void deleteTargets(Session session, List<ShareTarget> targets, List<Integer> guestIDs) throws OXException {
        if (null == targets || 0 == targets.size() || null != guestIDs && 0 == guestIDs.size()) {
            return;
        }
        /*
         * delete targets from storage
         */
        int affectedShares = 0;
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            affectedShares = removeTargets(connectionHelper, targets, guestIDs);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (0 < affectedShares) {
            scheduleGuestCleanup(session.getContextId(), guestIDs == null ? null : I2i(guestIDs));
        }
    }

    /**
     * Gets all shares created in a specific context.
     *
     * @param contextID The context identifier
     * @return The shares, or an empty list if there are none
     */
    public List<Share> getAllShares(int contextID) throws OXException {
        return services.getService(ShareStorage.class).loadSharesForContext(contextID, StorageParameters.NO_PARAMETERS);
    }

    /**
     * Gets all shares created in a specific context that were created by a specific user.
     *
     * @param contextID The context identifier
     * @param userID The user identifier
     * @return The shares, or an empty list if there are none
     */
    public List<Share> getAllShares(int contextID, int userID) throws OXException {
        return services.getService(ShareStorage.class).loadSharesCreatedBy(contextID, userID, StorageParameters.NO_PARAMETERS);
    }

    /**
     * Removes all shares in a context.
     * <P/>
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled
     * as needed.
     * <p/>
     * This method ought to be called in an administrative context, hence no session is required and no permission checks are performed.
     *
     * @param contextID The context identifier
     * @return The number of affected shares
     */
    public int removeShares(int contextID) throws OXException {
        List<Share> shares;
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            /*
             * load & delete all shares in the context, removing associated target permissions
             */
            shares = shareStorage.loadSharesForContext(contextID, connectionHelper.getParameters());
            if (0 < shares.size()) {
                shareStorage.deleteShares(contextID, shares, connectionHelper.getParameters());
                removeTargetPermissions(connectionHelper, shares);
            }
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (0 < shares.size()) {
            scheduleGuestCleanup(contextID, I2i(ShareTool.getGuestIDs(shares)));
        }
        return shares.size();
    }

    /**
     * Removes all shares in a context that were created by a specific user.
     * <P/>
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled
     * as needed.
     * <p/>
     * This method ought to be called in an administrative context, hence no session is required and no permission checks are performed.
     *
     * @param contextID The context identifier
     * @param userID The identifier of the user to delete the shares for
     * @return The number of affected shares
     */
    public int removeShares(int contextID, int userID) throws OXException {
        List<Share> shares;
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            /*
             * load & delete all shares in the context, removing associated target permissions
             */
            shares = shareStorage.loadSharesForGuest(contextID, userID, connectionHelper.getParameters());
            if (0 < shares.size()) {
                shareStorage.deleteShares(contextID, shares, connectionHelper.getParameters());
                removeTargetPermissions(connectionHelper, shares);
            }
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (0 < shares.size()) {
            scheduleGuestCleanup(contextID, I2i(ShareTool.getGuestIDs(shares)));
        }
        return shares.size();
    }

    /**
     * Removes all shares identified by the supplied tokens. The tokens might either be in their absolute format (i.e. base token plus
     * path), as well as in their base format only, which in turn leads to all share targets associated with the base token being
     * removed.
     * <P/>
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled
     * as needed.
     * <p/>
     * This method ought to be called in an administrative context, hence no session is required and no permission checks are performed.
     *
     * @param tokens The tokens to delete the shares for
     * @return The number of affected shares
     */
    public int removeShares(List<String> tokens) throws OXException {
        /*
         * order tokens by context
         */
        Map<Integer, List<String>> tokensByContextID = new HashMap<Integer, List<String>>();
        for (String token : tokens) {
            Integer contextID = I(new ShareToken(token).getContextID());
            List<String> tokensInContext = tokensByContextID.get(contextID);
            if (null == tokensInContext) {
                tokensInContext = new ArrayList<String>();
                tokensByContextID.put(contextID, tokensInContext);
            }
            tokensInContext.add(token);
        }
        /*
         * delete shares per context
         */
        int affectedShares = 0;
        for (Map.Entry<Integer, List<String>> entry : tokensByContextID.entrySet()) {
            affectedShares += removeShares(entry.getKey().intValue(), entry.getValue());
        }
        return affectedShares;
    }

    /**
     * Removes share targets for specific guest users in a context.
     *
     * @param contextID The context identifier
     * @param targets The share targets
     * @param guestIDs The guest IDs to consider, or <code>null</code> to delete all shares of all guests referencing the targets
     * @throws OXException
     */
    public void removeTargets(int contextID, List<ShareTarget> targets, List<Integer> guestIDs) throws OXException {
        if (null == targets || 0 == targets.size() || null != guestIDs && 0 == guestIDs.size()) {
            return;
        }
        int affectedShares;
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            affectedShares = removeTargets(connectionHelper, targets, guestIDs);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (0 < affectedShares) {
            scheduleGuestCleanup(contextID, null == guestIDs ? null : I2i(guestIDs));
        }
    }

    /**
     * Deletes a list of share targets for all shares that belong to a certain list of guests.
     *
     * @param connectionHelper A (started) connection helper
     * @param targets The share targets to delete
     * @param guestIDs The guest IDs to consider, or <code>null</code> to delete all shares of all guests referencing the targets
     * @return The number of deleted shares in the storage
     */
    private int removeTargets(ConnectionHelper connectionHelper, List<ShareTarget> targets, List<Integer> guestIDs) throws OXException {
        int contextId = connectionHelper.getContextID();
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        if (null == guestIDs) {
            /*
             * delete all targets for all guest users
             */
            return shareStorage.deleteTargets(contextId, targets, connectionHelper.getParameters());
        } else {
            /*
             * delete targets for specific guests
             */
            List<Share> shares = new ArrayList<Share>(targets.size() * guestIDs.size());
            for (ShareTarget target : targets) {
                for (Integer guestID : guestIDs) {
                    shares.add(new Share(guestID.intValue(), target));
                }
            }
            return shareStorage.deleteShares(contextId, shares, connectionHelper.getParameters());
        }
    }

    /**
     * Removes all shares identified by the supplied tokens. The tokens might either be in their absolute format (i.e. base token plus
     * path), as well as in their base format only, which in turn leads to all share targets associated with the base token being
     * removed.
     * <P/>
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled
     * as needed.
     * <p/>
     * This method ought to be called in an administrative context, hence no session is required and no permission checks are performed.
     *
     * @param tokens The tokens to delete the shares for
     * @return The number of affected shares
     */
    private int removeShares(int contextID, List<String> tokens) throws OXException {
        /*
         * distinguish between base tokens only or base token with specific paths
         */
        Set<ShareToken> baseTokensOnly = new HashSet<ShareToken>();
        Map<ShareToken, Set<String>> pathsPerBaseToken = new HashMap<ShareToken, Set<String>>();
        for (String token : tokens) {
            ShareToken shareToken = new ShareToken(token);
            if (contextID != shareToken.getContextID()) {
                throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
            }
            String baseToken = shareToken.getToken();
            if (token.length() > baseToken.length() + 1 && '/' == token.charAt(baseToken.length())) {
                /*
                 * base token with path
                 */
                Set<String> paths = pathsPerBaseToken.get(baseToken);
                if (null == paths) {
                    paths = new HashSet<String>();
                    pathsPerBaseToken.put(shareToken, paths);
                }
                paths.add(token.substring(baseToken.length() + 1));
            } else {
                /*
                 * base token only
                 */
                baseTokensOnly.add(shareToken);
            }
        }
        /*
         * proceed with deletion
         */
        List<Share> shares = new ArrayList<Share>();
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            /*
             * gather all shares for guest users with base token only (removing redundant tokens with paths implicitly)
             */
            for (ShareToken baseToken : baseTokensOnly) {
                pathsPerBaseToken.remove(baseToken);
                shares.addAll(shareStorage.loadSharesForGuest(contextID, baseToken.getUserID(), connectionHelper.getParameters()));
            }
            /*
             * pick specific shares for guest users with base tokens and paths
             */
            for (Map.Entry<ShareToken, Set<String>> entry : pathsPerBaseToken.entrySet()) {
                List<Share> sharesForGuest = shareStorage.loadSharesForGuest(
                    contextID, entry.getKey().getUserID(), connectionHelper.getParameters());
                for (String path : entry.getValue()) {
                    for (Share share : sharesForGuest) {
                        if (null != share.getTarget() && path.equals(share.getTarget().getPath())) {
                            shares.add(share);
                            break;
                        }
                    }
                }
            }
            /*
             * delete the shares in storage, removing the associated target permissions as well
             */
            if (0 < shares.size()) {
                shareStorage.deleteShares(contextID, shares, connectionHelper.getParameters());
                removeTargetPermissions(connectionHelper, shares);
            }
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (0 < shares.size()) {
            scheduleGuestCleanup(contextID, I2i(ShareTool.getGuestIDs(shares)));
        }
        return shares.size();
    }

    /**
     * Filters expired shares from the supplied list of shares and triggers their final deletion, adjusting target permissions as well as
     * cleaning up guest users as needed.
     *
     * @param share The shares
     * @return The filtered shares, which may be an empty list if all shares were expired
     * @throws OXException
     */
    private List<Share> removeExpired(int contextID, List<Share> shares) throws OXException {
        List<Share> expiredShares = ShareTool.filterExpiredShares(shares);
        if (null != expiredShares && 0 < expiredShares.size()) {
            int affectedShares = 0;
            ShareStorage shareStorage = services.getService(ShareStorage.class);
            ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
            try {
                connectionHelper.start();
                affectedShares = shareStorage.deleteShares(contextID, expiredShares, connectionHelper.getParameters());
                removeTargetPermissions(connectionHelper, expiredShares);
                connectionHelper.commit();
            } finally {
                connectionHelper.finish();
            }
            /*
             * schedule cleanup tasks as needed
             */
            if (0 < affectedShares) {
                scheduleGuestCleanup(contextID, I2i(ShareTool.getGuestIDs(shares)));
            }
        }
        return shares;
    }

    /**
     * Removes any permissions that are directly associated with the supplied shares, i.e. the permissions in the share targets for the
     * guest entities.
     *
     * @param connectionHelper A (started) connection helper
     * @param shares The share to remove the associated permissions for
     * @throws OXException
     */
    private void removeTargetPermissions(ConnectionHelper connectionHelper, List<Share> shares) throws OXException {
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        TargetUpdate targetUpdate = moduleSupport.prepareAdministrativeUpdate(connectionHelper.getContextID(), connectionHelper.getConnection());
        try {
            Map<ShareTarget, Set<Integer>> guestsByTarget = ShareTool.mapGuestsByTarget(shares);
            targetUpdate.fetch(guestsByTarget.keySet());
            for (Entry<ShareTarget, Set<Integer>> entry : guestsByTarget.entrySet()) {
                Set<Integer> guestIDs = entry.getValue();
                List<TargetPermission> permissions = new ArrayList<TargetPermission>(guestIDs.size());
                for (Integer guestID : guestIDs) {
                    permissions.add(new TargetPermission(guestID.intValue(), false, 0));
                }
                targetUpdate.get(entry.getKey()).removePermissions(permissions);
            }
            targetUpdate.run();
        } finally {
            targetUpdate.close();
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
        if (GuestRecipient.class.isInstance(recipient)) {
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
                UserPermissionBits userPermissionBits = ShareTool.setPermissionBits(
                    services, connection, context, existingGuestUser.getId(), permissionBits, true);
                LOG.debug("Using existing guest user {} with permissions {} in context {}: {}",
                    existingGuestUser.getMail(), userPermissionBits.getPermissionBits(), context.getContextId(), existingGuestUser.getId());
                return existingGuestUser;
            }
        }
        /*
         * create new guest user & contact
         */
        UserImpl guestUser = ShareTool.prepareGuestUser(services, sharingUser, recipient);
        Contact contact = ShareTool.prepareGuestContact(sharingUser, guestUser);
        int contactId = contactUserStorage.createGuestContact(context.getContextId(), contact, connection);
        guestUser.setContactId(contactId);
        int guestID = userService.createUser(connection, context, guestUser);
        guestUser.setId(guestID);
        contact.setCreatedBy(guestID);
        contact.setModifiedBy(guestID);
        contact.setInternalUserId(guestID);
        contactUserStorage.updateGuestContact(context.getContextId(), contactId, contact, connection);
        /*
         * store permission bits
         */
        services.getService(UserPermissionService.class).saveUserPermissionBits(connection, new UserPermissionBits(permissionBits, guestID, context.getContextId()));
        if (AnonymousRecipient.class.isInstance(recipient)) {
            LOG.info("Created anonymous guest user with permissions {} in context {}: {}", permissionBits, context.getContextId(), guestID);
        } else {
            LOG.info("Created guest user {} with permissions {} in context {}: {}", guestUser.getMail(), permissionBits, context.getContextId(), guestID);
        }
        return guestUser;
    }

    /**
     * Schedules guest cleanup tasks in a context.
     *
     * @param contextID The context ID
     * @param guestIDs The guest IDs to consider, or <code>null</code> to cleanup all guest users in the context
     * @throws OXException
     */
    private void scheduleGuestCleanup(int contextID, int[] guestIDs) throws OXException {
        if (null == guestIDs) {
            guestCleaner.scheduleContextCleanup(contextID);
        } else {
            guestCleaner.scheduleGuestCleanup(contextID, guestIDs);
        }
    }

    /**
     * Checks the quota for the user associated to the session
     *
     * @param connectionHelper The ConnectionHelper
     * @param session The session
     * @param additionalQuotaUsage The quota that should be added to existing one
     * @throws OXException
     */
    protected void checkQuota(ConnectionHelper connectionHelper, Session session, int additionalQuotaUsage) throws OXException {
        QuotaService quotaService = services.getService(QuotaService.class);
        if (quotaService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(QuotaService.class.getName());
        }

        ShareQuotaProvider provider = (ShareQuotaProvider) quotaService.getProvider("share");
        if (provider == null) {
            LOG.warn("ShareQuotaProvider is not available. A share will be created without quota check!");
            return;
        }

        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        if (viewFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }

        Quota quota = provider.getAmountQuota(session, connectionHelper.getConnection(), connectionHelper.getParameters(), viewFactory);

        if (!quota.isUnlimited() && quota.willExceed(additionalQuotaUsage)) {
            long limit = quota.getLimit();
            long usage = quota.getUsage();
            throw QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.create(usage, limit);
        }
    }
}
