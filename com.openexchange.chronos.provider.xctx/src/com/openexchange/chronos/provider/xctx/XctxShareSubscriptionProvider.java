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

package com.openexchange.chronos.provider.xctx;

import static com.openexchange.osgi.Tools.requireService;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE_WITH_PASSWORD;
import static com.openexchange.share.subscription.ShareLinkState.FORBIDDEN;
import static com.openexchange.share.subscription.ShareLinkState.UNRESOLVABLE;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.tools.alias.UserAliasUtility;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult.Builder;
import com.openexchange.share.subscription.ShareLinkState;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 * {@link XctxShareSubscriptionProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XctxShareSubscriptionProvider implements ShareSubscriptionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(XctxShareSubscriptionProvider.class);

    private final ServiceLookup services;
    private final XctxCalendarProvider provider;

    /**
     * Initializes a new {@link XctxShareSubscriptionProvider}.
     *
     * @param services A service lookup reference
     */
    public XctxShareSubscriptionProvider(ServiceLookup services, XctxCalendarProvider provider) {
        super();
        this.services = services;
        this.provider = provider;
    }

    @Override
    public int getRanking() {
        return 12;
    }

    @Override
    public boolean isSupported(Session session, String shareLink) {
        if (false == ShareTool.isShare(shareLink)) {
            return false;
        }
        ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
        if (null == targetPath || Module.CALENDAR.getFolderConstant() != targetPath.getModule()) {
            return false;
        }
        try {
            GuestInfo guestInfo = resolveGuest(shareLink);
            if (null != guestInfo) {
                /*
                 * Skip checking if context or user are enabled. This provider is responsible for handling the link
                 */
                return hasCapability(session);
            }
        } catch (OXException e) {
            LOG.warn("Error checking if share link is supported", e);
        }
        return false;
    }

    @Override
    public ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException {

        //TODO: re-check if allowed

        /*
         * check if account already exists for this guest user
         */
        CalendarAccount existingAccount = lookupExistingAccount(session, shareLink);
        if (null != existingAccount) {
            FolderCalendarAccess calendarAccess = provider.connect(session, existingAccount, null);
            ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
            CalendarFolder folder = calendarAccess.getFolder(targetPath.getFolder());
            ShareLinkState state = Boolean.FALSE.equals(folder.isSubscribed()) ? ShareLinkState.UNSUBSCRIBED : ShareLinkState.SUBSCRIBED;
            return new ShareLinkAnalyzeResult.Builder().state(state).infos(getSubscriptionInfo(existingAccount, targetPath)).build();
        }
        /*
         * check if and how the share link is resolvable, otherwise
         */
        GuestInfo guestInfo;
        try {
            guestInfo = resolveGuest(shareLink);
        } catch (OXException e) {
            return new Builder().state(UNRESOLVABLE).error(ShareExceptionCodes.INVALID_LINK.create(e, shareLink)).build();
        }

        if (false == RecipientType.GUEST.equals(guestInfo.getRecipientType()) || Strings.isEmpty(guestInfo.getEmailAddress())) {
            return new Builder().state(FORBIDDEN).error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create()).build();
        }
        if (false == UserAliasUtility.isAlias(guestInfo.getEmailAddress(), getAliases(ServerSessionAdapter.valueOf(session).getUser()))) {
            return new Builder().state(FORBIDDEN).error(ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create()).build();
        }
        
        return resolveState(shareLink, guestInfo).infos(new ShareSubscriptionInformation(null, Module.CALENDAR.getName(), null)).build();
    }

    @Override
    public ShareSubscriptionInformation subscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        
        //TODO: re-check if allowed
        //TODO: re-check if exists
        
        /*
         * update folder in existing account if account already exists for this guest user
         */
        CalendarAccount existingAccount = lookupExistingAccount(session, shareLink);
        if (null != existingAccount) {
            FolderCalendarAccess calendarAccess = provider.connect(session, existingAccount, null);
            ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
            DefaultCalendarFolder folderUpdate = new DefaultCalendarFolder();
            folderUpdate.setId(targetPath.getFolder());
            folderUpdate.setSubscribed(Boolean.TRUE);
            calendarAccess.updateFolder(targetPath.getFolder(), folderUpdate, CalendarUtils.DISTANT_FUTURE);
            return getSubscriptionInfo(existingAccount, targetPath);
        }
        /*
         * create new calendar account
         */
        JSONObject userConfig = new JSONObject();
        userConfig.putSafe("url", shareLink);
        userConfig.putSafe("password", password); //TODO: crypt
        userConfig.putSafe("name", shareName);
        
        CalendarAccountService accountService = services.getServiceSafe(CalendarAccountService.class);
        CalendarAccount account = accountService.createAccount(session, Constants.PROVIDER_ID, userConfig, null);
        
        // TODO: ensure folder is subscribed / all others are unsubscribed?

        return getSubscriptionInfo(account, ShareTool.getShareTarget(shareLink));
    }


    @Override
    public ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean unsubscribe(Session session, String shareLink) throws OXException {
        /*
         * update targeted folder in existing account
         */
        CalendarAccount existingAccount = lookupExistingAccount(session, shareLink);
        if (null == existingAccount) {
            return false;
        }
        FolderCalendarAccess calendarAccess = provider.connect(session, existingAccount, null);
        ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
        DefaultCalendarFolder folderUpdate = new DefaultCalendarFolder();
        folderUpdate.setSubscribed(Boolean.FALSE);
        calendarAccess.updateFolder(targetPath.getFolder(), folderUpdate, CalendarUtils.DISTANT_FUTURE);
        return true;
    }
    
    protected static Set<String> getAliases(User user) {
        Set<String> possibleAliases = new HashSet<String>();
        if (Strings.isNotEmpty(user.getMail())) {
            possibleAliases.add(user.getMail());
        }
        if (null != user.getAliases()) {
            for (String alias : user.getAliases()) {
                if (Strings.isNotEmpty(alias)) {
                    possibleAliases.add(alias);
                }
            }
        }
        return possibleAliases;
    }

    private boolean hasCapability(Session session) throws OXException {
        String capabilityName = CalendarProviders.getCapabilityName(provider);
        CapabilitySet capabilities = requireService(CapabilityService.class, services).getCapabilities(session);
        return capabilities.contains(capabilityName);
    }

    /**
     * Resolves a share link to a specific guest of this system
     *
     * @param shareLink The share link to get the guest info for
     * @return The guest info or <code>null</code> if no guest can be found for the link
     * @throws OXException In case {@link ShareService} is missing or token is invalid
     */
    private GuestInfo resolveGuest(String shareLink) throws OXException {
        String baseToken = ShareTool.getBaseToken(shareLink);
        if (Strings.isEmpty(baseToken)) {
            return null;
        }
        return services.getServiceSafe(ShareService.class).resolveGuest(baseToken);
    }

    /**
     * Map the {@link AuthenticationMode} of the guest to a fitting share state
     *
     * @param shareLink The share link for logging
     * @param guestInfo The guest info to map
     * @return A {@link Builder} holding the information
     */
    private Builder resolveState(String shareLink, GuestInfo guestInfo) {
        Builder builder = new Builder();
        if (null == guestInfo || null == guestInfo.getAuthentication()) {
            return builder.state(UNRESOLVABLE).error(ShareExceptionCodes.INVALID_LINK.create(shareLink));
        }
        AuthenticationMode mode = guestInfo.getAuthentication();
        switch (mode) {
            case ANONYMOUS:
            case ANONYMOUS_PASSWORD:
                /*
                 * Do not support anonymous shares
                 */
                return builder.state(ShareLinkState.FORBIDDEN).error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create());
            case GUEST_PASSWORD:
                return builder.state(ADDABLE_WITH_PASSWORD);
            case GUEST:
                return builder.state(ADDABLE);
            default:
                return builder.state(UNRESOLVABLE).error(ShareExceptionCodes.INVALID_LINK.create(shareLink));
        }
    }

    private CalendarAccount lookupExistingAccount(Session session, String shareLink) throws OXException {
        String baseToken = ShareTool.getBaseToken(shareLink);
        if (Strings.isEmpty(baseToken)) {
            return null;
        }
        CalendarAccountService accountService = services.getServiceSafe(CalendarAccountService.class);
        for (CalendarAccount account : accountService.getAccounts(session, provider.getId(), null)) {
            JSONObject userConfig = account.getUserConfiguration();
            if (null != userConfig && baseToken.equals(ShareTool.getBaseToken(userConfig.optString("url", null)))) {
                return account;
            }
        }
        return null;
    }

    private static ShareSubscriptionInformation getSubscriptionInfo(CalendarAccount account, ShareTargetPath targetPath) {
        String uniqueFolderId = null != targetPath ? getUniqueFolderId(account, targetPath.getFolder()) : null;
        return new ShareSubscriptionInformation(String.valueOf(account.getAccountId()), Module.CALENDAR.getName(), uniqueFolderId);
    }

    private static String getUniqueFolderId(CalendarAccount account, String relativeFolderId) {
        return com.openexchange.chronos.provider.composition.IDMangling.getUniqueFolderId(account.getAccountId(), relativeFolderId);
    }

}
