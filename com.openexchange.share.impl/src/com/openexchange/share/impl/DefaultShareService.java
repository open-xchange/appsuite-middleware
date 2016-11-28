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

package com.openexchange.share.impl;

import static com.openexchange.java.Autoboxing.*;
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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.passwordmechs.PasswordMechFactory;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaService;
import com.openexchange.quota.QuotaType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.CreatedShares;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.LinkUpdate;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.CreatedSharesImpl;
import com.openexchange.share.core.ShareConstants;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.share.impl.cleanup.GuestCleaner;
import com.openexchange.share.impl.cleanup.GuestLastModifiedMarker;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
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

    /** The default permission bits to use for anonymous link shares */
    private static final int LINK_PERMISSION_BITS = Permissions.createPermissionBits(
        Permission.READ_FOLDER, Permission.READ_ALL_OBJECTS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, false);

    private final ServiceLookup services;
    private final GuestCleaner guestCleaner;
    private final ShareUtils utils;

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
        this.utils = new ShareUtils(services);
    }

    @Override
    public DefaultGuestInfo resolveGuest(String token) throws OXException {
        ShareToken shareToken = new ShareToken(token);
        int contextID = shareToken.getContextID();
        User guestUser;
        try {
            guestUser = services.getService(UserService.class).getUser(shareToken.getUserID(), contextID);
            shareToken.verifyGuest(contextID, guestUser);

            GuestService guestService = services.getService(GuestService.class);
            if (guestService != null) {
                guestUser = guestService.alignUserWithGuest(guestUser, contextID);
            }
        } catch (OXException e) {
            if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.debug("Guest user for share token {} not found, unable to resolve token.", shareToken, e);
                return null;
            }
            throw e;
        }
        return removeExpired(new DefaultGuestInfo(services, guestUser, shareToken, getLinkTarget(contextID, guestUser)));
    }

    @Override
    public GuestInfo getGuestInfo(Session session, int guestID) throws OXException {
        User user = null;
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, false);
        try {
            user = services.getService(UserService.class).getUser(connectionHelper.getConnection(), guestID, utils.getContext(session));
            connectionHelper.commit();
        } catch (OXException e) {
            if (false == UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                throw e;
            }
        } finally {
            connectionHelper.finish();
        }
        if (null != user && user.isGuest()) {
            DefaultGuestInfo guestInfo = new DefaultGuestInfo(services, session.getContextId(), user, getLinkTarget(session.getContextId(), user));
            if (Boolean.TRUE.equals(session.getParameter("com.openexchange.share.administrativeUpdate"))) {
                return guestInfo; // don't remove expired shares during administrative updates to avoid recursions
            }
            return removeExpired(guestInfo);
        }
        return null;
    }

    @Override
    public Set<Integer> getSharingUsersFor(int contextID, int guestID) throws OXException {
        Set<Integer> userIDs = new HashSet<Integer>();
        User guestUser = services.getService(UserService.class).getUser(guestID, contextID);
        if (false == guestUser.isGuest()) {
            throw ShareExceptionCodes.UNKNOWN_GUEST.create(I(guestID));
        }
        /*
         * always add the user who created this guest
         */
        userIDs.add(I(guestUser.getCreatedBy()));
        /*
         * for invited guests, also add the user permission entities found in accessible targets
         */
        if (false == ShareTool.isAnonymousGuest(guestUser)) {
            List<TargetProxy> targets = services.getService(ModuleSupport.class).listTargets(contextID, guestID);
            for (TargetProxy target : targets) {
                List<TargetPermission> permissions = target.getPermissions();
                if (null != permissions && 0 < permissions.size()) {
                    for (TargetPermission permission : permissions) {
                        if (guestID != permission.getEntity() && false == permission.isGroup()) {
                            userIDs.add(I(permission.getEntity()));
                        }
                    }
                }
            }
        }
        return userIDs;
    }

    @Override
    public CreatedShares addTarget(Session session, ShareTarget target, List<ShareRecipient> recipients) throws OXException {
        ShareTool.validateTarget(target);
        LOG.info("Configuring accounts for {} at {} in context {}...", recipients, target, I(session.getContextId()));
        Map<ShareRecipient, ShareInfo> sharesByRecipient = new HashMap<ShareRecipient, ShareInfo>(recipients.size());
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            /*
             * Initial checks
             */
            checkRecipients(recipients, session);
            /*
             * prepare guest users and resulting shares
             */
            List<ShareInfo> sharesInfos = new ArrayList<ShareInfo>(recipients.size());
            for (ShareRecipient recipient : recipients) {
                /*
                 * prepare shares for this recipient
                 */
                ShareInfo shareInfo = prepareShare(connectionHelper, session, recipient, target);
                sharesInfos.add(shareInfo);
                sharesByRecipient.put(recipient, shareInfo);
            }
            /*
             * check quota restrictions & commit transaction
             * store shares
             */
            checkQuota(session, connectionHelper, sharesInfos);
            connectionHelper.commit();
            LOG.info("Accounts at {} in context {} configured: {}", target, I(session.getContextId()), sharesByRecipient.values());
            return new CreatedSharesImpl(sharesByRecipient);
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public ShareLink optLink(Session session, ShareTarget target) throws OXException {
        Context context = utils.getContext(session);
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        TargetProxy proxy = moduleSupport.load(target, session);
        DefaultShareInfo shareInfo = optLinkShare(session, context, proxy, null);
        return null != shareInfo ? new DefaultShareLink(shareInfo, proxy.getTimestamp(), false) : null;
    }

    @Override
    public ShareLink getLink(Session session, ShareTarget target) throws OXException {
        int count = 0;
        final int MAX_RETRIES = 5;
        do {
            try {
                return getOrCreateLink(session, target);
            } catch (OXException e) {
                /*
                 * try again in case of concurrent modifications
                 */
                if (++count < MAX_RETRIES && ("IFO-1302".equals(e.getErrorCode()) || "FLD-1022".equals(e.getErrorCode()))) {
                    LOG.info("Detected concurrent modification during link creation: \"{}\" - trying again ({}/{})...", e.getMessage(), I(count), I(MAX_RETRIES));
                    continue;
                }
                throw e;
            }
        } while (true);
    }

    @Override
    public ShareLink updateLink(Session session, ShareTarget target, LinkUpdate linkUpdate, Date clientTimestamp) throws OXException {
        /*
         * update password of anonymous user for this share link
         */
        UserService userService = services.getService(UserService.class);
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        Context context = utils.getContext(session);
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            TargetUpdate targetUpdate = moduleSupport.prepareUpdate(session, connectionHelper.getConnection());
            targetUpdate.fetch(Collections.singletonList(target));
            TargetProxy targetProxy = targetUpdate.get(target);
            if (false == targetProxy.mayAdjust()) {
                throw ShareExceptionCodes.NO_EDIT_PERMISSIONS.create(I(session.getUserId()), target, I(session.getContextId()));
            }
            if (clientTimestamp.before(targetProxy.getTimestamp())) {
                throw ShareExceptionCodes.CONCURRENT_MODIFICATION.create(target);
            }
            DefaultShareInfo shareInfo = optLinkShare(session, context, targetProxy, connectionHelper);
            if (null == shareInfo) {
                throw ShareExceptionCodes.INVALID_LINK_TARGET.create(I(target.getModule()), target.getFolder(), target.getItem());
            }
            User guest = userService.getUser(shareInfo.getGuest().getGuestID(), session.getContextId());
            if (false == guest.isGuest() || false == ShareTool.isAnonymousGuest(guest)) {
                throw ShareExceptionCodes.UNKNOWN_GUEST.create(I(guest.getId()));
            }

            boolean guestUserUpdated = false;
            if (linkUpdate.containsExpiryDate()) {
                String expiryDateValue = null != linkUpdate.getExpiryDate() ? String.valueOf(linkUpdate.getExpiryDate().getTime()) : null;
                userService.setAttribute(connectionHelper.getConnection(), ShareTool.EXPIRY_DATE_USER_ATTRIBUTE, expiryDateValue, guest.getId(), context);
                guestUserUpdated = true;
            }
            if (linkUpdate.containsPassword()) {
                if(updatePassword(connectionHelper, context, guest, linkUpdate.getPassword())){
                    guestUserUpdated=true;
                }
            }

            if (guestUserUpdated) {
                targetProxy.touch();
                targetUpdate.run();
            }
            connectionHelper.commit();
            if (guestUserUpdated) {
                userService.invalidateUser(context, guest.getId());
                return new DefaultShareLink(shareInfo, moduleSupport.load(target, session).getTimestamp(), false);
            }
            return new DefaultShareLink(shareInfo, targetProxy.getTimestamp(), false);
        } finally {
            connectionHelper.finish();
        }
    }

    @Override
    public void deleteLink(Session session, ShareTarget target, Date clientTimestamp) throws OXException {
        /*
         * delete anonymous guest user permission for this share link
         */
        int guestID;
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        Context context = utils.getContext(session);
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            TargetUpdate targetUpdate = moduleSupport.prepareUpdate(session, connectionHelper.getConnection());
            targetUpdate.fetch(Collections.singletonList(target));
            TargetProxy targetProxy = targetUpdate.get(target);
            if (false == targetProxy.mayAdjust()) {
                throw ShareExceptionCodes.NO_EDIT_PERMISSIONS.create(I(session.getUserId()), target, I(session.getContextId()));
            }
            if (clientTimestamp.before(targetProxy.getTimestamp())) {
                throw ShareExceptionCodes.CONCURRENT_MODIFICATION.create(target);
            }
            DefaultShareInfo shareInfo = optLinkShare(session, context, targetProxy, connectionHelper);
            if (null == shareInfo) {
                throw ShareExceptionCodes.INVALID_LINK_TARGET.create(I(target.getModule()), target.getFolder(), target.getItem());
            }
            guestID = shareInfo.getGuest().getGuestID();
            targetProxy.removePermissions(Collections.singletonList(new TargetPermission(guestID, false, 0)));
            targetUpdate.run();
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        scheduleGuestCleanup(session.getContextId(), new int[] { guestID });
    }

    @Override
    public void scheduleGuestCleanup(int contextID, int...guestIDs) throws OXException {
        if (null == guestIDs) {
            guestCleaner.scheduleContextCleanup(contextID);
        } else {
            guestCleaner.scheduleGuestCleanup(contextID, guestIDs);
        }
    }

    /**
     * Optionally gets an existing share link for a specific share target, i.e. a share to an anonymous guest user.
     *
     * @param connectionHelper A (started) connection helper, or <code>null</code> if not available
     * @param context The context
     * @param target The share target
     * @param proxy The target proxy
     * @return The share link, if one exists for the target, or <code>null</code>, otherwise
     */
    private DefaultShareInfo optLinkShare(Session session, Context context, TargetProxy proxy, ConnectionHelper connectionHelper) throws OXException {
        List<TargetPermission> permissions = proxy.getPermissions();
        if (null != permissions && 0 < permissions.size()) {
            List<Integer> entities = new ArrayList<Integer>(permissions.size());
            for (TargetPermission permission : permissions) {
                if (false == permission.isGroup() && LINK_PERMISSION_BITS == permission.getBits()) {
                    entities.add(I(permission.getEntity()));
                }
            }
            if (0 < entities.size()) {
                UserService userService = services.getService(UserService.class);
                for (Integer entity : entities) {
                    User user;
                    if (null != connectionHelper) {
                        user = userService.getUser(connectionHelper.getConnection(), entity.intValue(), context);
                    } else {
                        user = userService.getUser(entity.intValue(), context);
                    }
                    if (ShareTool.isAnonymousGuest(user)) {
                        ShareTarget dstTarget = services.getService(ModuleSupport.class).adjustTarget(proxy.getTarget(), session, user.getId());
                        return new DefaultShareInfo(services, context.getContextId(), user, proxy.getTarget(), dstTarget, proxy.getTargetPath());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Resolves the a share token to the referenced shares.
     *
     * @param token The token to resolve
     * @param path The path to a specific share target, or <code>null</code> to resolve all accessible shares
     * @return The shares
     */
    List<ShareInfo> getShares(String token, String path) throws OXException {
        DefaultGuestInfo guest = resolveGuest(token);
        if (null == guest) {
            return null;
        }
        int contextID = guest.getContextID();
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        List<ShareInfo> shareInfos;
        if (path == null) {
            List<TargetProxy> proxies = moduleSupport.listTargets(contextID, guest.getGuestID());
            shareInfos = new ArrayList<ShareInfo>(proxies.size());
            for (TargetProxy proxy : proxies) {
                ShareTargetPath targetPath = proxy.getTargetPath();
                ShareTarget srcTarget = new ShareTarget(targetPath.getModule(), targetPath.getFolder(), targetPath.getItem());
                shareInfos.add(new DefaultShareInfo(services, contextID, guest.getUser(), srcTarget, proxy.getTarget(), targetPath));
            }
        } else {
            ShareTargetPath targetPath = ShareTargetPath.parse(path);
            if (targetPath == null) {
                return Collections.emptyList();
            }

            shareInfos = new ArrayList<ShareInfo>(1);
            TargetProxy proxy = moduleSupport.resolveTarget(targetPath, contextID, guest.getGuestID());
            ShareTarget srcTarget = new ShareTarget(targetPath.getModule(), targetPath.getFolder(), targetPath.getItem());
            shareInfos.add(new DefaultShareInfo(services, contextID, guest.getUser(), srcTarget, proxy.getTarget(), proxy.getTargetPath()));
        }

        return removeExpired(contextID, shareInfos);
    }

    /**
     * Gets all shares created in a specific context.
     *
     * @param contextID The context identifier
     * @return The shares, or an empty list if there are none
     */
    List<ShareInfo> getAllShares(int contextID) throws OXException {
        List<ShareInfo> shareInfos = new ArrayList<ShareInfo>();
        UserService userService = services.getService(UserService.class);
        int[] guestIDs = userService.listAllUser(contextID, true, true);
        if (null != guestIDs && 0 < guestIDs.length) {
            Set<Integer> guestsWithoutShares = new HashSet<>(guestIDs.length);
            for (int guestID : guestIDs) {
                User guest = userService.getUser(guestID, contextID);
                if (guest.isGuest()) { // double check
                    List<TargetProxy> targets = services.getService(ModuleSupport.class).listTargets(contextID, guestID);
                    if (targets.isEmpty()) {
                        guestsWithoutShares.add(guestID);
                    } else {
                        for (TargetProxy proxy : targets) {
                            ShareTargetPath targetPath = proxy.getTargetPath();
                            ShareTarget srcTarget = new ShareTarget(targetPath.getModule(), targetPath.getFolder(), targetPath.getItem());
                            shareInfos.add(new DefaultShareInfo(services, contextID, guest, srcTarget, proxy.getTarget(), targetPath));
                        }
                    }
                }
            }

            if (!guestsWithoutShares.isEmpty()) {
                scheduleGuestCleanup(contextID, Autoboxing.Coll2i(guestsWithoutShares));
            }
        }
        return shareInfos;
    }

    /**
     * Gets all shares created in a specific context that were created for a specific user.
     *
     * @param contextID The context identifier
     * @param guestID The guest user identifier
     * @return The shares, or an empty list if there are none
     */
    List<ShareInfo> getAllShares(int contextID, int guestID) throws OXException {
        List<ShareInfo> shareInfos = new ArrayList<ShareInfo>();
        Context context = services.getService(ContextService.class).getContext(contextID);
        User guest = services.getService(UserService.class).getUser(guestID, context);
        if (false == guest.isGuest()) {
            throw ShareExceptionCodes.UNKNOWN_GUEST.create(I(guestID));
        }
        List<TargetProxy> targets = services.getService(ModuleSupport.class).listTargets(contextID, guest.getId());
        for (TargetProxy proxy : targets) {
            ShareTargetPath targetPath = proxy.getTargetPath();
            ShareTarget srcTarget = new ShareTarget(targetPath.getModule(), targetPath.getFolder(), targetPath.getItem());
            shareInfos.add(new DefaultShareInfo(services, contextID, guest, srcTarget, proxy.getTarget(), targetPath));
        }
        return shareInfos;
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
    int removeShares(int contextID) throws OXException {
        /*
         * load & delete all shares in the context, removing associated target permissions
         */
        List<ShareInfo> shares = getAllShares(contextID);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            if (0 < shares.size()) {
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
     * Removes all shares in a context that were created for a specific user.
     * <P/>
     * Associated guest permission entities from the referenced share targets are removed implicitly, guest cleanup tasks are scheduled as
     * needed.
     * <p/>
     * This method ought to be called in an administrative context, hence no session is required and no permission checks are performed.
     *
     * @param contextID The context identifier
     * @param guestID The identifier of the guest user to delete the shares for
     * @return The number of affected shares
     */
    int removeShares(int contextID, int guestID) throws OXException {
        /*
         * load & delete all shares in the context, removing associated target permissions
         */
        List<ShareInfo> shares = getAllShares(contextID, guestID);
        ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            if (0 < shares.size()) {
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
    int removeShares(List<String> tokens) throws OXException {
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

    int removeShares(List<String> tokens, int contextID) throws OXException {
        for (String token : tokens) {
            if (contextID != new ShareToken(token).getContextID()) {
                throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
            }
        }
        return removeShares(tokens);
    }

    /**
     * Checks that the sharing user doesn't try to share targets to himself and has sufficient permissions
     * to create links or invite guests.
     *
     * @param recipients The recipients
     * @param userId the sharing users ID
     * @throws OXException {@link ShareExceptionCodes#NO_SHARING_WITH_YOURSELF)
     */
    private void checkRecipients(List<ShareRecipient> recipients, Session session) throws OXException {
        boolean shareLinks = false;
        boolean inviteGuests = false;
        CapabilityService capabilityService = services.getService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }
        CapabilitySet capabilities = capabilityService.getCapabilities(session);
        if (null != capabilities && capabilities.contains("share_links")) {
            shareLinks = true;
        }
        if (null != capabilities && capabilities.contains("invite_guests")) {
            inviteGuests = true;
        }

        int userId = session.getUserId();
        for (ShareRecipient recipient : recipients) {
            if (recipient.isInternal()) {
                InternalRecipient internal = recipient.toInternal();
                if (!internal.isGroup() && internal.getEntity() == userId) {
                    throw ShareExceptionCodes.NO_SHARING_WITH_YOURSELF.create();
                }
            }
            if (RecipientType.ANONYMOUS.equals(recipient.getType())) {
                if (!shareLinks) {
                    throw ShareExceptionCodes.NO_SHARE_LINK_PERMISSION.create();
                }
            }
            if (RecipientType.GUEST.equals(recipient.getType())) {
                if (!inviteGuests) {
                    throw ShareExceptionCodes.NO_INVITE_GUEST_PERMISSION.create();
                }
            }
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
        List<ShareInfo> shares;
        ConnectionHelper connectionHelper = null != session ? new ConnectionHelper(session, services, true) : new ConnectionHelper(contextID, services, true);
        try {
            connectionHelper.start();
            /*
             * load all shares referenced by the supplied tokens
             */
            shares = tokenCollection.loadShares();
            /*
             * delete the shares by removing the associated target permissions
             */
            if (0 < shares.size()) {
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
    private List<ShareInfo> removeExpired(int contextID, List<ShareInfo> shares) throws OXException {
        List<ShareInfo> expiredShares = ShareTool.filterExpiredShares(shares);
        if (null != expiredShares && 0 < expiredShares.size()) {
            ConnectionHelper connectionHelper = new ConnectionHelper(contextID, services, true);
            try {
                connectionHelper.start();
                removeTargetPermissions(null, connectionHelper, expiredShares);
                connectionHelper.commit();
            } finally {
                connectionHelper.finish();
            }
            /*
             * schedule cleanup tasks
             */
            scheduleGuestCleanup(contextID, I2i(ShareTool.getGuestIDs(expiredShares)));
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
    private void removeTargetPermissions(Session session, ConnectionHelper connectionHelper, List<ShareInfo> shares) throws OXException {
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
     * Removes a guests permission from a share target.
     *
     * @param guestID The guests ID
     * @param target The share target; must ontain globally valid IDs
     * @param connectionHelper A (started) connection helper
     * @throws OXException
     */
    private void removeTargetPermission(int guestID, ShareTarget target, ConnectionHelper connectionHelper) throws OXException {
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        TargetUpdate targetUpdate = moduleSupport.prepareAdministrativeUpdate(connectionHelper.getContextID(), connectionHelper.getConnection());
        try {
            targetUpdate.fetch(Collections.singletonList(target));
            targetUpdate.get(target).removePermissions(Collections.singletonList(new TargetPermission(guestID, false, 0)));
            targetUpdate.run();
        } finally {
            targetUpdate.close();
        }
    }

    /**
     * Prepares a new individual share for a specific target based on the supplied recipient. This includes resolving the share recipient
     * to an internal permission entity, with new guest entities being provisioned as needed.
     *
     * @param connectionHelper A (started) connection helper
     * @param session The sharing users session
     * @param recipient The share recipient
     * @param target The share target from the sharing users point of view
     * @return The prepared share
     */
    private ShareInfo prepareShare(ConnectionHelper connectionHelper, Session session, ShareRecipient recipient, ShareTarget target) throws OXException {
        User sharingUser = utils.getUser(session);
        Context context = services.getService(ContextService.class).getContext(connectionHelper.getContextID());
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        ShareInfo shareInfo;
        if (RecipientType.GROUP.equals(recipient.getType())) {
            /*
             * prepare pseudo share infos for group recipient
             */
            Group group = services.getService(GroupService.class).getGroup(context, ((InternalRecipient) recipient).getEntity());
            int[] members = group.getMember();
            ShareTarget dstTarget;
            if (members.length == 0) {
                dstTarget = moduleSupport.adjustTarget(target, session, context.getMailadmin()); // fallback
            } else {
                dstTarget = moduleSupport.adjustTarget(target, session, members[0]);
            }
            shareInfo = new InternalGroupShareInfo(context.getContextId(), group, target, dstTarget);
        } else {
            /*
             * prepare guest or internal user shares for other recipient types
             */
            int permissionBits = utils.getRequiredPermissionBits(recipient, target);
            User targetUser = getGuestUser(connectionHelper.getConnection(), context, sharingUser, permissionBits, recipient, target);
            ShareTarget dstTarget = moduleSupport.adjustTarget(target, session, targetUser.getId());
            if (false == targetUser.isGuest()) {
                shareInfo = new InternalUserShareInfo(context.getContextId(), targetUser, target, dstTarget);
            } else {
                ShareTargetPath targetPath = moduleSupport.getPath(target, session);
                shareInfo = new DefaultShareInfo(services, context.getContextId(), targetUser, target, dstTarget, targetPath);
            }
        }
        return shareInfo;
    }

    /**
     * Stores one or more shares in the storage. This includes only share for external entities, i.e. for anonymous or named guest users.
     * Share infos pointing to internal users and groups are skipped implicitly.
     *
     * @param session The session
     * @param connectionHelper A (started) connection helper
     * @param shareInfos The shares to store
     */
    private void checkQuota(Session session, ConnectionHelper connectionHelper, List<ShareInfo> shareInfos) throws OXException {
        if (null == shareInfos || 0 == shareInfos.size()) {
            return;
        }
        /*
         * distinguish between links and invitations for quota checks
         */
        List<ShareInfo> anonymousShares = new ArrayList<ShareInfo>();
        List<ShareInfo> guestShares = new ArrayList<ShareInfo>();
        for (ShareInfo shareInfo : shareInfos) {
            if (RecipientType.ANONYMOUS.equals(shareInfo.getGuest().getRecipientType())) {
                anonymousShares.add(shareInfo);
            } else if (RecipientType.GUEST.equals(shareInfo.getGuest().getRecipientType())) {
                guestShares.add(shareInfo);
            }
        }
        if (0 < anonymousShares.size()) {
            /*
             * check quota restrictions & capability for anonymous links
             */
            CapabilitySet capabilities = services.getService(CapabilityService.class).getCapabilities(session);
            if (null == capabilities || false == capabilities.contains("share_links")) {
                throw ShareExceptionCodes.NO_SHARE_LINK_PERMISSION.create();
            }
            AccountQuota quota = services.getService(QuotaService.class).getProvider("share_links").getFor(session, "0");
            if (null != quota && quota.hasQuota(QuotaType.AMOUNT)) {
                Quota amountQuota = quota.getQuota(QuotaType.AMOUNT);
                if (amountQuota.isExceeded() || amountQuota.willExceed(anonymousShares.size())) {
                    throw QuotaExceptionCodes.QUOTA_EXCEEDED_SHARE_LINKS.create(amountQuota.getUsage(), amountQuota.getLimit());
                }
            }
        }
        if (0 < guestShares.size()) {
            /*
             * check quota restrictions & capability for inviting guests
             */
            CapabilitySet capabilities = services.getService(CapabilityService.class).getCapabilities(session);
            if (null == capabilities || false == capabilities.contains("invite_guests")) {
                throw ShareExceptionCodes.NO_INVITE_GUEST_PERMISSION.create();
            }
            AccountQuota quota = services.getService(QuotaService.class).getProvider("invite_guests").getFor(session, "0");
            if (null != quota && quota.hasQuota(QuotaType.AMOUNT)) {
                Quota amountQuota = quota.getQuota(QuotaType.AMOUNT);
                if (amountQuota.isExceeded() || amountQuota.willExceed(guestShares.size())) {
                    throw QuotaExceptionCodes.QUOTA_EXCEEDED_INVITE_GUESTS.create(amountQuota.getUsage(), amountQuota.getLimit());
                }
            }
        }
    }

    /**
     * Updates the guest user behind the anonymous recipient as needed, i.e. adjusts the defined password mechanism and the password
     * itself in case it differs from the updated recipient.
     *
     * @param connection A (writable) connection to the database
     * @param context The context
     * @param guestUser The guest user to update the password for
     * @param password The password to set for the anonymous guest user, or <code>null</code> to remove the password protection
     * @return <code>true</code> if the user was updated, <code>false</code>, otherwise
     */
    private boolean updatePassword(ConnectionHelper connectionHelper, Context context, User guestUser, String password) throws OXException {
        String originalPassword = guestUser.getUserPassword();
        if (null == password && null != originalPassword || null != password && null == originalPassword ||
            null != password && null != originalPassword && false == password.equals(originalPassword)) {
            if (false == ShareTool.isAnonymousGuest(guestUser)) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Can't change password for non-anonymous guest");
            }
            UserImpl updatedGuest = new UserImpl();
            updatedGuest.setId(guestUser.getId());
            updatedGuest.setCreatedBy(guestUser.getCreatedBy());
            if (Strings.isEmpty(password)) {
                updatedGuest.setPasswordMech("");
                updatedGuest.setUserPassword(null);
            } else {
                String encodePassword = services.getService(PasswordMechFactory.class).get(ShareConstants.PASSWORD_MECH_ID).encode(password);
                updatedGuest.setUserPassword(encodePassword);
                updatedGuest.setPasswordMech(ShareConstants.PASSWORD_MECH_ID);
            }
            services.getService(UserService.class).updatePassword(connectionHelper.getConnection(), updatedGuest, context);
            return true;
        }
        return false;
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
     * @param target The share target
     * @return The guest user
     */
    private User getGuestUser(Connection connection, Context context, User sharingUser, int permissionBits, ShareRecipient recipient, ShareTarget target) throws OXException {
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
                    UserPermissionBits userPermissionBits = utils.setPermissionBits(connection, context, existingUser.getId(), permissionBits, true);
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
        UserImpl guestUser = utils.prepareGuestUser(context.getContextId(), sharingUser, recipient, target);
        Contact contact = utils.prepareGuestContact(context.getContextId(), sharingUser, guestUser);
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
        services.getService(UserPermissionService.class).saveUserPermissionBits(connection, new UserPermissionBits(permissionBits, guestID, context));
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
     * Gets the link target for the given anonymous guest user. If no target is set (via an user attribute)
     * this method tries to restore the consistency by setting the attribute.
     *
     * @param contextId The context ID
     * @param guestUser The guest user
     * @return The target or <code>null</code> if the user is no guest at all or not anonymous
     * @throws OXException
     */
    private ShareTarget getLinkTarget(int contextId, User guestUser) throws OXException {
        if (guestUser.isGuest() && ShareTool.isAnonymousGuest(guestUser)) {
            try {
                String targetAttr = ShareTool.getUserAttribute(guestUser, ShareTool.LINK_TARGET_USER_ATTRIBUTE);
                if (targetAttr == null) {
                    scheduleGuestCleanup(contextId, guestUser.getId());
                    OXException e = ShareExceptionCodes.UNEXPECTED_ERROR.create("Anonymous guest " + guestUser.getId() + " in context " + contextId + " is in inconsistent state - no share target exists.");
                    LOG.warn("Scheduled clean up of broken guest user entity", e);
                    return null;
                }
                return ShareTool.jsonToTarget(new JSONObject(targetAttr));
            } catch (JSONException e) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Could not compile or resolve share target", e);
            }
        }
        return null;
    }

    private DefaultShareLink getOrCreateLink(Session session, ShareTarget target) throws OXException {
        Context context = utils.getContext(session);
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, true);
        try {
            connectionHelper.start();
            TargetUpdate targetUpdate = moduleSupport.prepareUpdate(session, connectionHelper.getConnection());
            targetUpdate.fetch(Collections.singletonList(target));
            TargetProxy targetProxy = targetUpdate.get(target);
            if (false == targetProxy.mayAdjust()) {
                throw ShareExceptionCodes.NO_EDIT_PERMISSIONS.create(I(session.getUserId()), target, I(session.getContextId()));
            }
            DefaultShareInfo existingLink = optLinkShare(session, context, targetProxy, connectionHelper);
            if (null != existingLink) {
                return new DefaultShareLink(existingLink, targetProxy.getTimestamp(), false);
            }
            /*
             * create new anonymous recipient for this target
             */
            AnonymousRecipient recipient = new AnonymousRecipient(LINK_PERMISSION_BITS, null, null);
            LOG.info("Adding new share link to {} for {} in context {}...", target, recipient, I(session.getContextId()));
            ShareInfo shareInfo = prepareShare(connectionHelper, session, recipient, target);
            checkQuota(session, connectionHelper, Collections.singletonList(shareInfo));
            /*
             * apply new permission entity for this target
             */
            TargetPermission targetPermission = new TargetPermission(shareInfo.getGuest().getGuestID(), false, recipient.getBits());
            targetProxy.applyPermissions(Collections.singletonList(targetPermission));
            /*
             * run target update, commit transaction & return created share link info
             */
            targetUpdate.run();
            connectionHelper.commit();
            LOG.info("Share link to {} for {} in context {} added successfully.", target, recipient, I(session.getContextId()));
            Date timestamp = moduleSupport.load(target, session).getTimestamp();
            return new DefaultShareLink(shareInfo, timestamp, true);
        } finally {
            connectionHelper.finish();
        }
    }

    /**
     * Evaluates an optionally set expiry date for the guest user prior returning it.
     *
     * @param guestInfo The guest to check the expiry date for
     * @return The passed guest, or <code>null</code> if the guest was expired
     */
    private DefaultGuestInfo removeExpired(DefaultGuestInfo guestInfo) throws OXException {
        Date expiryDate = guestInfo.getExpiryDate();
        if (null != expiryDate && expiryDate.before(new Date())) {
            LOG.info("Guest user {} in context {} expired, scheduling guest cleanup.", I(guestInfo.getGuestID()), I(guestInfo.getContextID()));
            ConnectionHelper connectionHelper = new ConnectionHelper(guestInfo.getContextID(), services, true);
            try {
                connectionHelper.start();
                removeTargetPermission(guestInfo.getGuestID(), guestInfo.getLinkTarget(), connectionHelper);
                connectionHelper.commit();
            } finally {
                connectionHelper.finish();
            }
            scheduleGuestCleanup(guestInfo.getContextID(), guestInfo.getGuestID());
            return null;
        }
        return guestInfo;
    }

}
