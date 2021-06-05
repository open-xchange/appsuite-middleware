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

package com.openexchange.file.storage.xox.subscription;

import static com.openexchange.api.client.common.OXExceptionParser.matches;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE_WITH_PASSWORD;
import static com.openexchange.share.subscription.ShareLinkState.FORBIDDEN;
import static com.openexchange.share.subscription.ShareLinkState.UNRESOLVABLE;
import static com.openexchange.share.subscription.ShareLinkState.UNSUPPORTED;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.api.client.LoginInformation;
import com.openexchange.api.client.common.calls.folders.GetFolderCall;
import com.openexchange.api.client.common.calls.folders.RemoteFolder;
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
        if (false == ShareTool.isShare(shareLink)) {
            return false;
        }
        ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
        if (null == targetPath || (Module.INFOSTORE.getFolderConstant() != targetPath.getModule())) {
            return false;
        }
        String folder = targetPath.getFolder();
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
            return checkSingleFileAccessible(session, shareLink);
        }
        /*
         * Check if account exists and still accessible
         */
        FileStorageAccountAccess accountAccess = getStorageAccountAccess(session, shareLink);
        if (null != accountAccess) {
            Builder builder = checkAccessible(accountAccess, shareLink, session);
            return builder.infos(generateInfos(accountAccess, shareLink)).build();
        }

        /*
         * The share is unknown. Try to login to the remote server
         */
        ApiClientService apiClientService = services.getServiceSafe(ApiClientService.class);
        ApiClient apiClient = null;
        Builder builder = new Builder();
        builder.state(UNRESOLVABLE).error(ShareSubscriptionExceptions.NOT_USABLE.create(shareLink));
        try {
            /*
             * If creation of the client throws no error, the share has been access successfully
             */
            apiClient = apiClientService.getApiClient(session, shareLink, null);
            LoginInformation loginInformation = apiClient.getLoginInformation();
            if (null != loginInformation) {
                if (Strings.isEmpty(loginInformation.getRemoteMailAddress())) {
                    /*
                     * Only anonymous guests have no mail address
                     */
                    builder.state(UNSUPPORTED).error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create());
                } else if (Strings.isNotEmpty(loginInformation.getLoginType()) && false == loginInformation.getLoginType().startsWith("guest")) {
                    /*
                     * No guest
                     */
                    builder.state(UNSUPPORTED).error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create());
                } else if (false == UserAliasUtility.isAlias(loginInformation.getRemoteMailAddress(), getAliases(ServerSessionAdapter.valueOf(session).getUser()))) {
                    /*
                     * Share is not for the current user
                     */
                    builder.state(FORBIDDEN).error(ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(loginInformation.getRemoteMailAddress()));
                } else {
                    /*
                     * Try to access the folder
                     */
                    String folder = getFolderFrom(shareLink);
                    if (Strings.isNotEmpty(folder)) {
                        RemoteFolder remoteFolder = apiClient.execute(new GetFolderCall(folder));
                        if (folder.equals(remoteFolder.getID())) {
                            builder.state(ADDABLE).error(null);
                        }
                    }
                }
            }
        } catch (OXException e) {
            /*
             * Check if credentials are missing
             */
            if (isPasswordMissing(e)) {
                builder.state(ADDABLE_WITH_PASSWORD);
            } else if (matches(e, ApiClientExceptions.MISSING_CREDENTIALS)) {
                /*
                 * Only thrown for anonymous guest
                 */
                builder.state(UNSUPPORTED).error(ShareExceptionCodes.NO_SUBSCRIBE_SHARE_ANONYMOUS.create());
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
        return matches(e, LoginExceptionCodes.INVALID_CREDENTIALS, LoginExceptionCodes.INVALID_GUEST_PASSWORD);
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
        services.getServiceSafe(ApiClientService.class).close(session, shareLink);
    }
}
