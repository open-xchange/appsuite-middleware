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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.xctx.subscription;

import static com.openexchange.share.subscription.ShareLinkState.ADDABLE;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE_WITH_PASSWORD;
import static com.openexchange.share.subscription.ShareLinkState.FORBIDDEN;
import static com.openexchange.share.subscription.ShareLinkState.UNRESOLVABLE;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.xctx.XctxFileStorageService;
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
import com.openexchange.share.core.subscription.AbstractFileStorageSubscriptionProvider;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult.Builder;
import com.openexchange.share.subscription.ShareLinkState;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link XctxShareSubscriptionProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XctxShareSubscriptionProvider extends AbstractFileStorageSubscriptionProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XctxShareSubscriptionProvider}.
     * 
     * @param services The services
     * @param fileStorageService The filestorage to operate with
     */
    public XctxShareSubscriptionProvider(ServiceLookup services, XctxFileStorageService fileStorageService) {
        super(fileStorageService, services.getService(UserPermissionService.class));
        this.services = services;
    }

    @Override
    public boolean isSupported(Session session, String shareLink) {
        if (false == ShareTool.isShare(shareLink)) {
            return false;
        }
        ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
        if (null == targetPath || Module.INFOSTORE.getFolderConstant() != targetPath.getModule()) {
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
            logExcpetionError(e);
        }
        return false;
    }

    @Override
    public int getRanking() {
        return 60;
    }

    @Override
    public ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException {
        /*
         * Check that the share can be subscribed
         */
        requireAccess(session);
        if (isSingleFileShare(shareLink)) {
            return new ShareLinkAnalyzeResult(FORBIDDEN, ShareExceptionCodes.NO_FILE_SUBSCRIBE.create(), getModuleInfo());
        }

        /*
         * Check for existing accounts
         */
        FileStorageAccountAccess accountAccess = getStorageAccountAccess(session, shareLink);
        if (null != accountAccess) {
            Builder builder = checkAccessible(accountAccess, shareLink);
            return builder.infos(generateInfos(accountAccess, shareLink)).build();
        }

        /*
         * Try to resolve the token to a guest
         */
        GuestInfo guestInfo = resolveGuest(shareLink);
        if (null == guestInfo) {
            return unknownGuest(shareLink);
        }
        if (false == RecipientType.GUEST.equals(guestInfo.getRecipientType()) || Strings.isEmpty(guestInfo.getEmailAddress())) {
            return forbidden(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create());
        }
        if (false == UserAliasUtility.isAlias(guestInfo.getEmailAddress(), getAliases(ServerSessionAdapter.valueOf(session).getUser()))) {
            return forbidden(ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(guestInfo.getEmailAddress()));
        }

        /*
         * Resolve the guest info to a state
         */
        Builder builder = resolveState(shareLink, guestInfo);
        builder.infos(getModuleInfo());
        return builder.build();
    }

    @Override
    public boolean isPasswordMissing(OXException e) {
        if (LoginExceptionCodes.prefix().equals(e.getPrefix())) {
            if (LoginExceptionCodes.INVALID_CREDENTIALS.getNumber() == e.getCode() || LoginExceptionCodes.INVALID_GUEST_PASSWORD.getNumber() == e.getCode()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isFolderRemoved(OXException e) {
        if (FolderExceptionErrorMessage.prefix().equals(e.getPrefix())) {
            if (FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.getNumber() == e.getCode()) {
                return true;
            }
        }
        return false;
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
                return builder.state(ShareLinkState.FORBIDDEN).error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create());
            case GUEST_PASSWORD:
                return builder.state(ADDABLE_WITH_PASSWORD);
            case GUEST:
                return builder.state(ADDABLE);
            default:
                return builder.state(UNRESOLVABLE).error(ShareSubscriptionExceptions.NOT_USABLE.create(shareLink));
        }
    }

    /**
     * Create response for a unknown guest.
     * <p>
     * Please note that the provider did find a guest with the given sharing link when {@link #isSupported(Session, String)}
     * was executed. However now the guest hasn't access anymore. Therefore behave like this provider was never called
     * and return the {@link ShareLinkState#UNRESOLVABLE} with no infos about this provider
     *
     * @return Appropriated response in case guest can't be resolved.
     */
    private ShareLinkAnalyzeResult unknownGuest(String shareLink) {
        return new Builder() // @formatter:off
            .state(UNRESOLVABLE)
            .error(ShareSubscriptionExceptions.NOT_USABLE.create(shareLink))
            .infos(null)
            .build(); // @formatter:on
    }

    private ShareLinkAnalyzeResult forbidden(OXException exception) {
        return new Builder() // @formatter:off
            .state(FORBIDDEN)
            .error(exception)
            .infos(getModuleInfo())
            .build(); // @formatter:on
    }

}
