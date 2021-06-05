/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.provider.xctx;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.osgi.Tools.requireService;
import static com.openexchange.share.core.tools.ShareLinks.extractHostName;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE_WITH_PASSWORD;
import static com.openexchange.share.subscription.ShareLinkState.CREDENTIALS_REFRESH;
import static com.openexchange.share.subscription.ShareLinkState.FORBIDDEN;
import static com.openexchange.share.subscription.ShareLinkState.REMOVED;
import static com.openexchange.share.subscription.ShareLinkState.SUBSCRIBED;
import static com.openexchange.share.subscription.ShareLinkState.UNRESOLVABLE;
import static com.openexchange.share.subscription.ShareLinkState.UNSUBSCRIBED;
import static com.openexchange.share.subscription.ShareLinkState.UNSUPPORTED;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
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
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
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
        if (null == targetPath || Module.CALENDAR.getFolderConstant() != targetPath.getModule() || false == targetPath.isFolder()) {
            return false;
        }
        try {
            return hasCapability(session) && null != resolveGuest(shareLink);
        } catch (OXException e) {
            LOG.warn("Error checking if share link is supported", e);
        }
        return false;
    }

    @Override
    public ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException {
        /*
         * re-check if supported & allowed
         */
        if (false == isSupported(session, shareLink)) {
            return new Builder().state(UNSUPPORTED).error(ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(shareLink)).build();
        }
        ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
        if (null == targetPath || Module.CALENDAR.getFolderConstant() != targetPath.getModule() || false == targetPath.isFolder()) {
            return new Builder().state(UNSUPPORTED).error(ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(shareLink)).build();
        }
        /*
         * check if account already exists for this guest user
         */
        CalendarAccount existingAccount = lookupExistingAccount(session, shareLink);
        if (null != existingAccount) {
            XctxCalendarAccess calendarAccess;
            try {
                calendarAccess = provider.connect(session, existingAccount, null);
            } catch (OXException e) {
                if (LoginExceptionCodes.INVALID_GUEST_PASSWORD.equals(e)) {
                    return new Builder().error(e).state(CREDENTIALS_REFRESH).infos(getSubscriptionInfo(existingAccount, targetPath)).build();
                }
                throw e;
            }
            /*
             * probe referenced folder
             */
            try {
                CalendarFolder folder = calendarAccess.getFolder(getRelativeFolderId(targetPath.getFolder()));
                ShareLinkState state = Boolean.FALSE.equals(folder.isSubscribed()) ? UNSUBSCRIBED : SUBSCRIBED;
                return new Builder().state(state).infos(getSubscriptionInfo(existingAccount, targetPath)).build();
            } catch (OXException e) {
                if (FolderExceptionErrorMessage.NOT_FOUND.equals(e) || FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e)) {
                    return new Builder().error(e).state(REMOVED).infos(getSubscriptionInfo(existingAccount, targetPath)).build();
                }
                throw e;
            } finally {
                calendarAccess.close();
            }
        }
        /*
         * check if and how the share link is resolvable, otherwise
         */
        GuestInfo guestInfo;
        try {
            guestInfo = resolveGuest(shareLink);
        } catch (OXException e) {
            return new Builder().state(UNRESOLVABLE).error(ShareSubscriptionExceptions.NOT_USABLE.create(shareLink, e)).build();
        }
        if (null != guestInfo) {
            if (false == RecipientType.GUEST.equals(guestInfo.getRecipientType()) || Strings.isEmpty(guestInfo.getEmailAddress())) {
                return new Builder().state(UNSUPPORTED).error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create()).build();
            }
            if (false == UserAliasUtility.isAlias(guestInfo.getEmailAddress(), getAliases(ServerSessionAdapter.valueOf(session).getUser()))) {
                return new Builder().state(FORBIDDEN).error(ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(shareLink)).build();
            }
        }
        return resolveState(shareLink, guestInfo).infos(new ShareSubscriptionInformation(null, Module.CALENDAR.getName(), null)).build();
    }

    @Override
    public ShareSubscriptionInformation subscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        /*
         * re-check if supported & allowed
         */
        if (false == isSupported(session, shareLink)) {
            throw ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(shareLink);
        }
        /*
         * update folder in existing account if account already exists for this guest user
         */
        CalendarAccount existingAccount = lookupExistingAccount(session, shareLink);
        if (null != existingAccount) {
            ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
            String folderId = getRelativeFolderId(targetPath.getFolder());
            DefaultCalendarFolder folderUpdate = new DefaultCalendarFolder();
            folderUpdate.setId(folderId);
            folderUpdate.setSubscribed(Boolean.TRUE);
            FolderCalendarAccess calendarAccess = provider.connect(session, existingAccount, null);
            try {
                calendarAccess.updateFolder(folderId, folderUpdate, existingAccount.getLastModified().getTime());
            } finally {
                calendarAccess.close();
            }
            return getSubscriptionInfo(existingAccount, targetPath);
        }
        /*
         * create new calendar account, otherwise
         */
        JSONObject userConfig = new JSONObject();
        userConfig.putSafe("url", shareLink);
        userConfig.putSafe("password", password);
        userConfig.putSafe("name", Strings.isEmpty(shareName) ? extractHostName(shareLink) : shareName);
        CalendarAccountService accountService = services.getServiceSafe(CalendarAccountService.class);
        CalendarAccount account = accountService.createAccount(session, provider.getId(), userConfig, null);
        return getSubscriptionInfo(account, ShareTool.getShareTarget(shareLink));
    }

    @Override
    public ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        /*
         * re-check if supported & allowed
         */
        if (false == isSupported(session, shareLink)) {
            throw ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(shareLink);
        }
        /*
         * get targeted account for share link
         */
        CalendarAccount existingAccount = lookupExistingAccount(session, shareLink);
        if (null == existingAccount) {
            throw ShareSubscriptionExceptions.MISSING_SUBSCRIPTION.create(shareLink);
        }
        /*
         * update account accordingly
         */
        JSONObject userConfig = null == existingAccount.getUserConfiguration() ? new JSONObject() : new JSONObject(existingAccount.getUserConfiguration());
        userConfig.putSafe("password", password);
        if (Strings.isNotEmpty(shareName)) {
            userConfig.putSafe("name", shareName);
        }
        CalendarAccountService accountService = services.getServiceSafe(CalendarAccountService.class);
        CalendarAccount updatedAccount = accountService.updateAccount(session, existingAccount.getAccountId(), userConfig, existingAccount.getLastModified().getTime(), null);
        return getSubscriptionInfo(updatedAccount, ShareTool.getShareTarget(shareLink));
    }

    @Override
    public boolean unsubscribe(Session session, String shareLink) throws OXException {
        return unsubscribe(session, shareLink, true);
    }

    public boolean unsubscribe(Session session, String shareLink, boolean ignoreWarnings) throws OXException {
        /*
         * update targeted folder in existing account
         */
        CalendarAccount existingAccount = lookupExistingAccount(session, shareLink);
        if (null == existingAccount) {
            return false;
        }
        ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
        String folderId = getRelativeFolderId(targetPath.getFolder());
        DefaultCalendarFolder folderUpdate = new DefaultCalendarFolder();
        folderUpdate.setId(folderId);
        folderUpdate.setSubscribed(Boolean.FALSE);
        CalendarParameters parameters = new DefaultCalendarParameters().set(CalendarParameters.PARAMETER_IGNORE_STORAGE_WARNINGS, B(ignoreWarnings));
        XctxCalendarAccess calendarAccess = provider.connect(session, existingAccount, parameters);
        String result;
        try {
            result = calendarAccess.updateFolder(folderId, folderUpdate, existingAccount.getLastModified().getTime());
        } finally {
            calendarAccess.close();
        }
        if (null != result) {
            return true;
        }
        if (null != calendarAccess.getWarnings() && 1 == calendarAccess.getWarnings().size() &&
            ShareSubscriptionExceptions.ACCOUNT_WILL_BE_REMOVED.equals(calendarAccess.getWarnings().get(0))) {
            throw calendarAccess.getWarnings().get(0); //TODO: or supply the warnings to the caller?
        }
        return false;
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
            return builder.state(UNRESOLVABLE).error(ShareSubscriptionExceptions.NOT_USABLE.create(shareLink));
        }
        AuthenticationMode mode = guestInfo.getAuthentication();
        switch (mode) {
            case ANONYMOUS:
            case ANONYMOUS_PASSWORD:
                /*
                 * Do not support anonymous shares
                 */
                return builder.state(UNSUPPORTED).error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create());
            case GUEST_PASSWORD:
                return builder.state(ADDABLE_WITH_PASSWORD);
            case GUEST:
                return builder.state(ADDABLE);
            default:
                return builder.state(UNRESOLVABLE).error(ShareSubscriptionExceptions.NOT_USABLE.create(shareLink));
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

    private static ShareSubscriptionInformation getSubscriptionInfo(CalendarAccount account, ShareTargetPath targetPath) throws OXException {
        if (null == targetPath || null == targetPath.getFolder()) {
            return new ShareSubscriptionInformation(String.valueOf(account.getAccountId()), Module.CALENDAR.getName(), null);
        }
        String foreignRelativeFolderId = getRelativeFolderId(targetPath.getFolder());
        String localUniqueFolderId = getUniqueFolderId(account, foreignRelativeFolderId);
        String qualifiedAccountId = com.openexchange.chronos.provider.composition.IDMangling.getQualifiedAccountId(account.getAccountId());
        return new ShareSubscriptionInformation(qualifiedAccountId, Module.CALENDAR.getName(), localUniqueFolderId);
    }

    private static String getUniqueFolderId(CalendarAccount account, String relativeFolderId) {
        return com.openexchange.chronos.provider.composition.IDMangling.getUniqueFolderId(account.getAccountId(), relativeFolderId, true);
    }

    private static String getRelativeFolderId(String uniqueFolderId) throws OXException {
        return com.openexchange.chronos.provider.composition.IDMangling.getRelativeFolderId(uniqueFolderId);
    }

}
