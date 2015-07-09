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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.guest.GuestService;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.CreatedShare;
import com.openexchange.share.CreatedShares;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.CreatedSharesImpl;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.share.impl.cleanup.GuestCleaner;
import com.openexchange.share.impl.cleanup.GuestLastModifiedMarker;
import com.openexchange.share.impl.groupware.ShareModuleMapping;
import com.openexchange.share.impl.groupware.ShareQuotaProvider;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
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
     * @param guestCleaner An initialized guest cleaner to work with
     */
    public DefaultShareService(ServiceLookup services, GuestCleaner guestCleaner) {
        super();
        this.services = services;
        this.guestCleaner = guestCleaner;
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
        shareToken.verifyGuest(contextID, guest);
        List<Share> shares = services.getService(ShareStorage.class).loadSharesForGuest(contextID, guest.getId(), StorageParameters.NO_PARAMETERS);
        shares = removeExpired(contextID, shares);
        return 0 == shares.size() ? null : new ResolvedGuestShare(services, contextID, guest, shares, true);
    }

    @Override
    public List<ShareInfo> getShares(Session session, String token) throws OXException {
        /*
         * resolve share token & get associated guest user
         */
        ShareToken shareToken = new ShareToken(token);
        int contextID = session.getContextId();
        User guest = services.getService(UserService.class).getUser(shareToken.getUserID(), contextID);
        shareToken.verifyGuest(contextID, guest);
        /*
         * get shares for guest and filter results as needed
         */
        List<Share> shares = services.getService(ShareStorage.class).loadSharesForGuest(contextID, guest.getId(), StorageParameters.NO_PARAMETERS);
        shares = removeExpired(contextID, shares);
        if (guest.getId() == session.getUserId()) {
            /*
             * implicitly adjust share targets if the session's user is the guest himself
             */
            return ShareTool.toShareInfos(services, contextID, shares, true);

        }
        /*
         * filter share targets not accessible for the session's user before returning results
         */
        DefaultGuestInfo guestInfo = new DefaultGuestInfo(services, guest, shareToken);
        if (false == RecipientType.ANONYMOUS.equals(guestInfo.getRecipientType()) || session.getUserId() != guestInfo.getCreatedBy()) {
            shares = removeInaccessible(session, shares);
        }
        return ShareTool.toShareInfos(services, contextID, shares, false);
    }

    @Override
    public List<ShareInfo> getShares(Session session, String module, String folder, String item) throws OXException {
        int moduleId = null == module ? -1 : ShareModuleMapping.moduleMapping2int(module);
        List<Share> shares = services.getService(ShareStorage.class).loadSharesForTarget(session.getContextId(), moduleId, folder, item, StorageParameters.NO_PARAMETERS);
        shares = removeExpired(session.getContextId(), shares);
        shares = removeInaccessible(session, shares);
        return ShareTool.toShareInfos(services, session.getContextId(), shares);
    }

    @Override
    public ShareInfo getShare(Session session, String token, String path) throws OXException {
        List<ShareInfo> shareInfos = getShares(session, token);
        for (ShareInfo shareInfo : shareInfos) {
            ShareTarget target = shareInfo.getShare().getTarget();
            if (null != target && path.equals(target.getPath())) {
                return shareInfo;
            }
        }
        return null;
    }

    @Override
    public List<ShareInfo> getAllShares(Session session) throws OXException {
        return getAllShares(session, null);
    }

    @Override
    public List<ShareInfo> getAllShares(Session session, String module) throws OXException {
        int moduleId = null == module ? -1 : ShareModuleMapping.moduleMapping2int(module);
        List<Share> shares = services.getService(ShareStorage.class).loadSharesCreatedBy(session.getContextId(), session.getUserId(), moduleId, StorageParameters.NO_PARAMETERS);
        shares = removeExpired(session.getContextId(), shares);
        return ShareTool.toShareInfos(services, session.getContextId(), shares);
    }

    @Override
    public Set<Integer> getSharingUsersFor(int contextId, int guestId) throws OXException {
        return services.getService(ShareStorage.class).getSharingUsers(contextId, guestId, StorageParameters.NO_PARAMETERS);
    }

    @Override
    public CreatedShares addTarget(Session session, ShareTarget target, List<ShareRecipient> recipients) throws OXException {
        CreatedShares created = addTargets(session, Collections.singletonList(target), recipients);
        for (ShareRecipient recipient : recipients) {
            CreatedShare share = created.getShare(recipient);
            if (null == share || 1 != share.size()) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Unexpected number of shares created for recipient " + recipient);
            }
        }
        return created;
    }

    @Override
    public CreatedShares addTargets(Session session, List<ShareTarget> targets, List<ShareRecipient> recipients) throws OXException {
        if (null == targets || 0 == targets.size() || null == recipients || 0 == recipients.size()) {
            return new CreatedSharesImpl(Collections.<ShareRecipient, List<ShareInfo>>emptyMap());
        }

        ShareTool.validateTargets(targets);
        int contextID = session.getContextId();
        LOG.info("Adding share target(s) {} for recipients {} in context {}...", targets, recipients, I(contextID));
        Map<ShareRecipient, List<ShareInfo>> sharesPerRecipient = new HashMap<ShareRecipient, List<ShareInfo>>(recipients.size());
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        Context context = services.getService(ContextService.class).getContext(session.getContextId());
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            /*
             * check quota restrictions
             */
            int expectedShares = targets.size() * recipients.size();
            /*
             * prepare guest users and resulting shares
             */
            Connection connection = connectionHelper.getConnection();
            User sharingUser = services.getService(UserService.class).getUser(connection, session.getUserId(), context);
            List<Share> sharesToStore = new ArrayList<Share>(expectedShares);
            for (ShareRecipient recipient : recipients) {
                if (recipient.isInternal()) {
                    InternalRecipient internal = recipient.toInternal();
                    if (!internal.isGroup() && internal.getEntity() == session.getUserId()) {
                        throw ShareExceptionCodes.NO_SHARING_WITH_YOURSELF.create();
                    }
                }
                int permissionBits = ShareTool.getRequiredPermissionBits(recipient, targets);
                User guestUser = getGuestUser(connection, context, sharingUser, permissionBits, recipient);
                List<ShareInfo> sharesForGuest = new ArrayList<ShareInfo>(targets.size());
                /*
                 * prepare shares for each target, remember new guest shares for storing
                 */
                for (ShareTarget target : targets) {
                    Share share = ShareTool.prepareShare(context.getContextId(), sharingUser, guestUser.getId(), target);
                    if (guestUser.isGuest()) {
                        sharesForGuest.add(new DefaultShareInfo(services, contextID, guestUser, share, false));
                        sharesToStore.add(share);
                    } else {
                        sharesForGuest.add(new InternalUserShareInfo(contextID, guestUser, share));
                    }
                }
                sharesPerRecipient.put(recipient, sharesForGuest);
            }
            /*
             * Check quota for real shares only, i.e. don't count internals that have accidently been added via their email addresses
             */
            checkQuota(connectionHelper, session, sharesToStore.size() * targets.size());
            /*
             * store shares & trigger collection of e-mail addresses
             */
            if (0 < sharesToStore.size()) {
                shareStorage.storeShares(contextID, sharesToStore, connectionHelper.getParameters());
            }
            connectionHelper.commit();
            LOG.info("Share target(s) {} for recipients {} in context {} added successfully.", targets, recipients, I(contextID));
            collectAddresses(session, recipients);
            return new CreatedSharesImpl(sharesPerRecipient);
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public GuestShare updateTargets(Session session, List<ShareTarget> targets, int guestID, Date clientTimestamp) throws OXException {
        if (null == targets || 0 == targets.size()) {
            return null;
        }
        ShareTool.validateTargets(targets);
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
    public void deleteTargets(Session session, List<ShareTarget> targets, boolean includeItems) throws OXException {
        if (null == targets || 0 == targets.size()) {
            return;
        }
        /*
         * delete targets from storage
         */
        int[] affectedGuests;
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            affectedGuests = services.getService(ShareStorage.class).deleteTargets(session.getContextId(), targets, includeItems, connectionHelper.getParameters());
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (null != affectedGuests && 0 < affectedGuests.length) {
            scheduleGuestCleanup(session.getContextId(), affectedGuests);
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
        int[] affectedGuests;
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            affectedGuests = removeTargets(connectionHelper, targets, guestIDs);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (null != affectedGuests && 0 < affectedGuests.length) {
            scheduleGuestCleanup(session.getContextId(), affectedGuests);
        }
    }

    @Override
    public void deleteShares(Session session, List<String> tokens) throws OXException {
        removeShares(session, session.getContextId(), tokens);
    }

    @Override
    public GuestInfo resolveGuest(String token) throws OXException {
        ShareToken shareToken = new ShareToken(token);
        int contextID = shareToken.getContextID();
        User guestUser;
        try {
            guestUser = services.getService(UserService.class).getUser(shareToken.getUserID(), contextID);
            shareToken.verifyGuest(contextID, guestUser);
        } catch (OXException e) {
            if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.debug("Guest user for share token {} not found, unable to resolve token.", shareToken, e);
                return null;
            }
            throw e;
        }
        return new DefaultGuestInfo(services, guestUser, shareToken);
    }

    @Override
    public GuestInfo getGuest(int contextId, int guestId) throws OXException {
        User guestUser = services.getService(UserService.class).getUser(guestId, contextId);
        if (false == guestUser.isGuest()) {
            throw ShareExceptionCodes.UNKNOWN_GUEST.create(I(guestId));
        }
        return new DefaultGuestInfo(services, contextId, guestUser);
    }

    /**
     * Gets all shares created in a specific context.
     *
     * @param contextID The context identifier
     * @return The shares, or an empty list if there are none
     */
    public List<ShareInfo> getAllShares(int contextID) throws OXException {
        return ShareTool.toShareInfos(services, contextID, services.getService(ShareStorage.class).loadSharesForContext(contextID, StorageParameters.NO_PARAMETERS));
    }

    /**
     * Gets all shares created in a specific context that were created by a specific user.
     *
     * @param contextID The context identifier
     * @param userID The user identifier
     * @return The shares, or an empty list if there are none
     */
    public List<ShareInfo> getAllShares(int contextID, int userID) throws OXException {
        return ShareTool.toShareInfos(services, contextID, services.getService(ShareStorage.class).loadSharesCreatedBy(contextID, userID, -1, StorageParameters.NO_PARAMETERS));
    }

    /**
     * Removes all shares in a context.
     * <P/>
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled as
     * needed.
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
                removeTargetPermissions(null, connectionHelper, shares);
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
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled as
     * needed.
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
            shares = shareStorage.loadSharesCreatedBy(contextID, userID, -1, connectionHelper.getParameters());
            if (0 < shares.size()) {
                shareStorage.deleteShares(contextID, shares, connectionHelper.getParameters());
                removeTargetPermissions(null, connectionHelper, shares);
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
     * path), as well as in their base format only, which in turn leads to all share targets associated with the base token being removed.
     * <P/>
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled as
     * needed.
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
            affectedShares += removeShares(null, entry.getKey().intValue(), entry.getValue());
        }
        return affectedShares;
    }

    public int removeShares(List<String> tokens, int contextID) throws OXException {
        for (String token : tokens) {
            if (contextID != new ShareToken(token).getContextID()) {
                throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
            }
        }
        return removeShares(tokens);
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
        int[] affectedGuests;
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            affectedGuests = removeTargets(connectionHelper, targets, guestIDs);
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (null != affectedGuests && 0 < affectedGuests.length) {
            scheduleGuestCleanup(contextID, affectedGuests);
        }
    }

    /**
     * Deletes a list of share targets for all shares that belong to a certain list of guests.
     *
     * @param connectionHelper A (started) connection helper
     * @param targets The share targets to delete
     * @param guestIDs The guest IDs to consider, or <code>null</code> to delete all shares of all guests referencing the targets
     * @return The identifiers of the affected guest users, or an empty array if no shares were deleted
     */
    private int[] removeTargets(ConnectionHelper connectionHelper, List<ShareTarget> targets, List<Integer> guestIDs) throws OXException {
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
            int affectedShares = shareStorage.deleteShares(contextId, shares, connectionHelper.getParameters());
            return 0 < affectedShares ? I2i(guestIDs) : new int[0];
        }
    }

    /**
     * Removes all shares identified by the supplied tokens. The tokens might either be in their absolute format (i.e. base token plus
     * path), as well as in their base format only, which in turn leads to all share targets associated with the base token being removed.
     * <P/>
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled as
     * needed.
     * <p/>
     * Depending on the session, the removal is done in terms of an administrative update with no further permission checks, or regular
     * update as performed by the session's user, checking permissions on the share targets implicitly.
     *
     * @param session The session, or <code>null</code> to perform an administrative update
     * @param contextID The context ID
     * @param tokens The tokens to delete the shares for
     * @return The number of affected shares
     */
    private int removeShares(Session session, int contextID, List<String> tokens) throws OXException {
        /*
         * prepare a token collection to distinguish between base tokens only or base token with specific paths
         */
        TokenCollection tokenCollection = new TokenCollection(services, contextID, tokens);
        List<Share> shares;
        ConnectionHelper connectionHelper = null != session ? new ConnectionHelper(session, services, true) : new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            /*
             * load all shares referenced by the supplied tokens
             */
            shares = tokenCollection.loadShares(connectionHelper.getParameters());
            /*
             * delete the shares in storage, removing the associated target permissions as well
             */
            if (0 < shares.size()) {
                services.getService(ShareStorage.class).deleteShares(contextID, shares, connectionHelper.getParameters());
                removeTargetPermissions(session, connectionHelper, shares);
            }
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        /*
         * schedule cleanup tasks as needed
         */
        if (0 < shares.size()) {
            scheduleGuestCleanup(contextID, tokenCollection.getGuestUserIDs());
        }
        return shares.size();
    }

    /**
     * Filters expired shares from the supplied list of shares and triggers their final deletion, adjusting target permissions as well as
     * cleaning up guest users as needed.
     *
     * @param contextID The context identifier
     * @param shares The shares
     * @return The filtered shares, which may be an empty list if all shares were expired
     * @throws OXException
     */
    private List<Share> removeExpired(int contextID, List<Share> shares) throws OXException {
        List<Share> expiredShares = ShareTool.filterExpiredShares(shares);
        if (null != expiredShares && 0 < expiredShares.size()) {
            int affectedShares;
            ShareStorage shareStorage = services.getService(ShareStorage.class);
            ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
            try {
                connectionHelper.start();
                affectedShares = shareStorage.deleteShares(contextID, expiredShares, connectionHelper.getParameters());
                removeTargetPermissions(null, connectionHelper, expiredShares);
                connectionHelper.commit();
            } finally {
                connectionHelper.finish();
            }
            /*
             * schedule cleanup tasks as needed
             */
            if (0 < affectedShares) {
                scheduleGuestCleanup(contextID, I2i(ShareTool.getGuestIDs(expiredShares)));
            }
        }
        return shares;
    }

    /**
     * Filters out those shares from the supplied list where the session's user has no access to the targets.
     *
     * @param session The session
     * @param shares The shares
     * @return The filtered shares, which may be an empty list if all shares were expired
     */
    private List<Share> removeInaccessible(Session session, List<Share> shares) throws OXException {
        if (null != shares && 0 < shares.size()) {
            ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
            Iterator<Share> iterator = shares.iterator();
            while (iterator.hasNext()) {
                Share share = iterator.next();
                ShareTarget target = share.getTarget();
                if (target.getOwnedBy() == session.getUserId()) {
                    continue;
                }
                if (false == moduleSupport.exists(target, session)) {
                    removeTargets(session.getContextId(), Collections.singletonList(target), Collections.singletonList(Integer.valueOf(share.getGuest())));
                    iterator.remove();
                } else if (false == moduleSupport.isVisible(target, session)) {
                    iterator.remove();
                }
            }
        }
        return shares;
    }

    /**
     * Removes any permissions that are directly associated with the supplied shares, i.e. the permissions in the share targets for the
     * guest entities. Depending on the session, the removal is done in an administrative or regular update.
     *
     * @param session The session, or <code>null</code> to perform an administrative update
     * @param connectionHelper A (started) connection helper
     * @param shares The shares to remove the associated permissions for
     * @throws OXException
     */
    private void removeTargetPermissions(Session session, ConnectionHelper connectionHelper, List<Share> shares) throws OXException {
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        TargetUpdate targetUpdate;
        if (null == session) {
            targetUpdate = moduleSupport.prepareAdministrativeUpdate(connectionHelper.getContextID(), connectionHelper.getConnection());
        } else {
            targetUpdate = moduleSupport.prepareUpdate(session, connectionHelper.getConnection());
        }
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
     * Gets a guest user for a new share. A new guest user is created if no matching one exists, the permission bits are applied as needed.
     * In case the guest recipient denotes an already existing, internal user, this user is returned.
     *
     * @param connection A (writable) connection to the database
     * @param context The context
     * @param sharingUser The sharing user
     * @param permissionBits The permission bits to apply to the guest user
     * @param recipient The recipient description
     * @return The guest user
     */
    private User getGuestUser(Connection connection, Context context, User sharingUser, int permissionBits, ShareRecipient recipient) throws OXException {
        UserService userService = services.getService(UserService.class);
        if (GuestRecipient.class.isInstance(recipient)) {
            /*
             * re-use existing, non-anonymous guest user from this context if possible
             */
            GuestRecipient guestRecipient = (GuestRecipient) recipient;
            User existingUser = null;
            try {
                existingUser = userService.searchUser(guestRecipient.getEmailAddress(), context, true, true, false);
            } catch (OXException e) {
                if (false == LdapExceptionCode.NO_USER_BY_MAIL.equals(e)) {
                    throw e;
                }
            }
            if (null != existingUser) {
                if (existingUser.isGuest()) {
                    /*
                     * combine permission bits with existing ones, reset any last modified marker if present
                     */
                    UserPermissionBits userPermissionBits = ShareTool.setPermissionBits(services, connection, context, existingUser.getId(), permissionBits, true);
                    GuestLastModifiedMarker.clearLastModified(services, context, existingUser);
                    LOG.debug("Using existing guest user {} with permissions {} in context {}: {}", existingUser.getMail(), userPermissionBits.getPermissionBits(), context.getContextId(), existingUser.getId());
                    /*
                     * As the recipient already belongs to an existing user, its password must be set to null, to avoid wrong notification
                     * messages
                     */
                    guestRecipient.setPassword(null);
                } else {
                    /*
                     * guest recipient points to internal user
                     */
                    LOG.debug("Guest recipient {} points to internal user {} in context {}: {}",
                        guestRecipient.getEmailAddress(), existingUser.getLoginInfo(), context.getContextId(), existingUser.getId());
                }
                return existingUser;
            }
        } else if (InternalRecipient.class.isInstance(recipient)) {
            InternalRecipient internalRecipient = (InternalRecipient) recipient;
            User user = userService.getUser(internalRecipient.getEntity(), context);
            return user;
        }
        /*
         * create new guest user & contact in this context
         */
        ContactUserStorage contactUserStorage = services.getService(ContactUserStorage.class);
        UserImpl guestUser = ShareTool.prepareGuestUser(services, context.getContextId(), sharingUser, recipient);
        Contact contact = ShareTool.prepareGuestContact(services, context.getContextId(), sharingUser, guestUser);
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
            GuestService guestService = services.getService(GuestService.class);
            if (guestService == null) {
                LOG.error("Required service GuestService absent");
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create("GuestService");
            }
            String groupId = services.getService(ConfigViewFactory.class).getView(sharingUser.getId(), context.getContextId()).opt("com.openexchange.context.group", String.class, "default");

            guestService.addGuest(guestUser.getMail(), groupId, context.getContextId(), guestID, guestUser.getUserPassword(), guestUser.getPasswordMech());

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

    /**
     * Recognizes the email addresses that should be collected and adds them to the ContactCollector.
     *
     * @param session - the {@link Session} of the user to collect the addresses for
     * @param shareRecipients - List of {@link ShareRecipient}s to collect addresses for
     * @throws OXException
     */
    private void collectAddresses(final Session session, final List<ShareRecipient> shareRecipients) throws OXException {
        final ContactCollectorService ccs = services.getService(ContactCollectorService.class);
        if (null != ccs) {
            final Set<InternetAddress> addrs = getEmailAddresses(shareRecipients);
            if (!addrs.isEmpty()) {
                ccs.memorizeAddresses(new ArrayList<InternetAddress>(addrs), session);
            }
        }
    }

    /**
     * Returns a <code>Set</code> of <code>InternetAddress</code>es that should be collected by the {@link ContactCollectorService}
     *
     * @param shareRecipients - a list of {@link ShareRecipient}s to get addresses from
     * @return <code>Set</code> of <code>InternetAddress</code>es for further processing
     * @throws OXException
     */
    private Set<InternetAddress> getEmailAddresses(List<ShareRecipient> shareRecipients) throws OXException {
        Set<InternetAddress> addrs = new HashSet<InternetAddress>();
        for (ShareRecipient shareRecipient : shareRecipients) {
            if (RecipientType.GUEST.equals(RecipientType.of(shareRecipient))) {
                String emailAddress = ((GuestRecipient) shareRecipient).getEmailAddress();
                if (emailAddress != null) {
                    try {
                        addrs.add(new InternetAddress(emailAddress));
                    } catch (final AddressException addressException) {
                        LOG.warn("Unable to add address to ContactCollector.", addressException);
                    }
                }
            }
        }
        return addrs;
    }

}
