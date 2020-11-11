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

package com.openexchange.file.storage.xox.subscription;

import static com.openexchange.api.client.common.OXExceptionParser.matches;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE_WITH_PASSWORD;
import static com.openexchange.share.subscription.ShareLinkState.FORBIDDEN;
import static com.openexchange.share.subscription.ShareLinkState.REMOVED;
import static com.openexchange.share.subscription.ShareLinkState.UNRESOLVABLE;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.api.client.LoginInformation;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.xox.XOXFileStorageService;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.tools.alias.UserAliasUtility;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.subscription.AbstractFileStorageSubscriptionProvider;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult.Builder;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link XOXShareSubscriptionProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XOXShareSubscriptionProvider extends AbstractFileStorageSubscriptionProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XOXShareSubscriptionProvider}.
     * 
     * @param services The services
     * @param fileStorageService The storage
     */
    public XOXShareSubscriptionProvider(ServiceLookup services, XOXFileStorageService fileStorageService) {
        super(fileStorageService, services.getService(UserPermissionService.class));
        this.services = services;
    }

    @Override
    public boolean isSupported(Session session, String shareLink) {
        ShareTargetPath shareTargetPath = ShareTool.getShareTarget(shareLink);
        if (null == shareTargetPath) {
            return false;
        }
        if (Strings.isNotEmpty(shareTargetPath.getItem())) {
            /*
             * Single files aren't supported
             */
            return false;
        }
        int module = shareTargetPath.getModule();
        if (Module.INFOSTORE.getFolderConstant() != module) {
            return false;
        }
        String folder = shareTargetPath.getFolder();
        if (Strings.isEmpty(folder)) {
            return false;
        }
        return hasCapability(session);
    }

    @Override
    public int getRanking() {
        return 55;
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
         * Check if account exists and still accessible
         */
        FileStorageAccountAccess accountAccess = getStorageAccountAccess(session, shareLink);
        if (null != accountAccess) {
            Builder builder = checkAccessible(accountAccess, shareLink);
            return builder.infos(generateInfos(accountAccess, shareLink)).build();
        }

        /*
         * The share is unknown. Try to login to the remote server
         */
        ApiClientService apiClientService = services.getServiceSafe(ApiClientService.class);
        ApiClient apiClient = null;
        Builder builder = new Builder();
        try {
            /*
             * If creation of the client throws no error, the share has been access successfully
             */
            apiClient = apiClientService.getApiClient(session, shareLink, null);
            LoginInformation loginInformation = apiClient.getLoginInformation();
            if (null != loginInformation) {
                builder.state(ADDABLE);
                if (Strings.isNotEmpty(loginInformation.getLoginType())) {
                    if (false == loginInformation.getLoginType().startsWith("guest")) {
                        builder.state(FORBIDDEN);
                        builder.error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create());
                    } else if (false == UserAliasUtility.isAlias(loginInformation.getRemoteMailAddress(), getAliases(ServerSessionAdapter.valueOf(session).getUser()))) {
                        /*
                         * Share is not for the current user
                         */
                        builder.state(FORBIDDEN);
                        builder.error(ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(loginInformation.getRemoteMailAddress()));
                    }
                }
            } else {
                builder.state(UNRESOLVABLE).error(ShareSubscriptionExceptions.NOT_USABLE.create(shareLink));
            }
        } catch (OXException e) {
            /*
             * Check if credentials are missing
             */
            if (isPasswordMissing(e)) {
                builder.state(ADDABLE_WITH_PASSWORD);
            } else if (isFolderRemoved(e)) {
                builder.state(REMOVED);
            } else {
                builder.state(UNRESOLVABLE).error(ShareSubscriptionExceptions.NOT_USABLE.create(shareLink, e));
            }
            logExcpetionDebug(e);
        } finally {
            /*
             * Close to avoid initialized client in cache
             */
            apiClientService.close(apiClient);
        }
        builder.infos(getModuleInfo());
        return builder.build();
    }

    @Override
    public ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        clearRemoteSessions(session, shareLink);
        ShareSubscriptionInformation information = super.resubscribe(session, shareLink, shareName, password);
        return information;
    }

    @Override
    public boolean unsubscribe(Session session, String shareLink) throws OXException {
        if (super.unsubscribe(session, shareLink)) {
            clearRemoteSessions(session, shareLink);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPasswordMissing(OXException e) {
        return matches(e, ApiClientExceptions.MISSING_CREDENTIALS, LoginExceptionCodes.INVALID_CREDENTIALS, LoginExceptionCodes.INVALID_GUEST_PASSWORD);
    }

    @Override
    protected boolean isFolderRemoved(OXException e) {
        return matches(e, ApiClientExceptions.ACCESS_REVOKED, FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE);
    }

    /**
     * Clear any remote session so follow up calls will work as expected
     *
     * @param session The user session
     * @param shareLink The share link
     * @param storageAccount The account to close
     * @throws OXException In case service is missing
     */
    private void clearRemoteSessions(Session session, String shareLink) throws OXException {
        services.getServiceSafe(ApiClientService.class).close(session.getContextId(), session.getUserId(), shareLink);
    }
}
