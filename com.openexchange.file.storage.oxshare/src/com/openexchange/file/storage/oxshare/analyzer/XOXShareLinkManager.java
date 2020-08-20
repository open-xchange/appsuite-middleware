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

package com.openexchange.file.storage.oxshare.analyzer;

import static com.openexchange.api.client.common.OXExceptionParser.matches;
import static com.openexchange.file.storage.oxshare.OXShareStorageConstants.SHARE_URL;
import static com.openexchange.share.federated.ShareLinkState.ADDABLE;
import static com.openexchange.share.federated.ShareLinkState.ADDABLE_WITH_PASSWORD;
import static com.openexchange.share.federated.ShareLinkState.CREDENTIALS_REFRESH;
import static com.openexchange.share.federated.ShareLinkState.INACCESSIBLE;
import static com.openexchange.share.federated.ShareLinkState.SUBSCRIBED;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.oxshare.OXShareFileStorageService;
import com.openexchange.file.storage.oxshare.OXShareStorageConstants;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.federated.ShareLinkManager;
import com.openexchange.share.federated.ShareLinkState;

/**
 * {@link XOXShareLinkManager}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XOXShareLinkManager implements ShareLinkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(XOXShareLinkManager.class);

    private final ServiceLookup services;

    private OXShareFileStorageService fileStorageService;

    /**
     * Initializes a new {@link XOXShareLinkManager}.
     * 
     * @param services The services
     * @param fileStorageService The storage
     */
    public XOXShareLinkManager(ServiceLookup services, OXShareFileStorageService fileStorageService) {
        super();
        this.services = services;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public int getSupportedModule() {
        return Module.INFOSTORE.getFolderConstant();
    }

    @Override
    public int getRanking() {
        return XOX_REMOTE_RANK;
    }

    @Override
    @NonNull
    public String getId() {
        return OXShareStorageConstants.ID;
    }

    @Override
    public ShareLinkState analyzeLink(Session session, String shareLink) throws OXException {
        /*
         * Check if account exists and still accessible
         */
        FileStorageAccountAccess accountAccess = getStorageAccountAccess(session, shareLink);
        if (null != accountAccess) {
            return checkAccessible(accountAccess, shareLink);
        }

        /*
         * The share is unknown. Try to login to the remote server
         */
        ApiClientService apiClientService = services.getServiceSafe(ApiClientService.class);
        ApiClient apiClient = null;
        try {
            /*
             * If creation of the client throws no error, the share has been access successfully
             */
            apiClient = apiClientService.getApiClient(session, shareLink, null);
            return ADDABLE;
        } catch (OXException e) {
            /*
             * Check if credentials are missing
             */
            if (isPasswordMissing(e)) {
                return ADDABLE_WITH_PASSWORD;
            }
            handleExcpetion(e);
        } finally {
            /*
             * Close to avoid initialized client in cache
             */
            apiClientService.close(apiClient);
        }
        return INACCESSIBLE;
    }

    @Override
    public String bindShare(Session session, String shareLink, String password, String shareName) throws OXException {
        XOXFileStorageAccount account = new XOXFileStorageAccount(fileStorageService, shareLink, password, shareName, null);
        return fileStorageService.getAccountManager().addAccount(account, session);
    }

    @Override
    public void unbindShare(Session session, String shareLink) throws OXException {
        FileStorageAccount storageAccount = getStorageAccount(session, shareLink);
        if (null != storageAccount) {
            clearRemoteSessions(session, shareLink, storageAccount);
            fileStorageService.getAccountManager().deleteAccount(storageAccount, session);
        }
    }

    @Override
    public void updateShare(Session session, String shareLink, String password) throws OXException {
        FileStorageAccount storageAccount = getStorageAccount(session, shareLink);
        if (null != storageAccount) {
            XOXFileStorageAccount updated = new XOXFileStorageAccount(storageAccount, password);
            fileStorageService.getAccountManager().updateAccount(updated, session);
            clearRemoteSessions(session, shareLink, storageAccount);
        }
    }

    /*
     * =============== HELPER ===============
     */
    private void handleExcpetion(OXException e) {
        LOGGER.debug("Resource is not accessible: {}", e.getMessage(), e);
    }

    /**
     * Check if there is an account for this share with the same base token.
     *
     * @param session The user session
     * @param shareLink The share link
     * @param baseToken The base token of the share link
     * @return A {@link FileStorageAccount} or <code>null</code> if no account was found
     * @throws OXException
     */
    private FileStorageAccount getStorageAccount(Session session, String shareLink) throws OXException {
        URL shareUrl = getUrl(shareLink);
        if (null == shareUrl) {
            return null;
        }
        String baseToken = ShareTool.getBaseToken(shareLink);
        for (FileStorageAccount account : fileStorageService.getAccountManager().getAccounts(session)) {
            String url = String.valueOf(account.getConfiguration().get(SHARE_URL));
            if (Strings.isNotEmpty(url) && baseToken.equals(ShareTool.getBaseToken(url)) && compareHost(shareUrl, url)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Check if there is an account for this share with the same base token.
     *
     * @param session The user session
     * @param shareLink The share link
     * @param baseToken The base token of the share link
     * @return A {@link FileStorageAccount} or <code>null</code> if no account was found
     * @throws OXException
     */
    private FileStorageAccountAccess getStorageAccountAccess(Session session, String shareLink) throws OXException {
        FileStorageAccount storageAccount = getStorageAccount(session, shareLink);
        if (null != storageAccount) {
            return fileStorageService.getAccountAccess(storageAccount.getId(), session);
        }
        return null;
    }

    /**
     * Compares two URLs if they have the same host
     * 
     * @param actual The actual URL
     * @param url The expected URL as saved in the account access
     * @return <code>true</code> if both URL share the same host/domain part
     */
    private static boolean compareHost(URL actual, String expected) {
        URL expectedUrl = getUrl(expected);
        if (null == expectedUrl || null == expectedUrl.getHost()) {
            /*
             * This should never happen ...
             */
            LOGGER.debug("The shrae URL within the account is broken.");
            return false;
        }
        return expectedUrl.getHost().equals(actual.getHost());
    }

    /**
     * Transforms the string into an {@link URL}
     *
     * @param url THe URL to transform
     * @return A {@link URL} or <code>null</code>
     */
    private static URL getUrl(String url) {
        try {
            if (false == url.startsWith("http")) { // includes 'https'
                return new URL("https://" + url);
            }
            return new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.trace("Maleformed URL", e);
        }
        return null;
    }

    /**
     * Check if the share is still accessible
     * <p>
     * Closes the account access
     * 
     * @param accountAccess The access to the account
     * @param shareLink The share link
     * @return A state fitting the accessibility of the share
     */
    private ShareLinkState checkAccessible(FileStorageAccountAccess accountAccess, String shareLink) {
        try {
            /*
             * Connect and access to check accessibility
             */
            accountAccess.connect();

            // Folder ID is also always set for single item share
            ShareTargetPath path = ShareTool.getShareTarget(shareLink);
            if (null != path && Strings.isNotEmpty(path.getFolder())) {
                FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
                folderAccess.getFolder(path.getFolder());
                return SUBSCRIBED;
            }
        } catch (OXException e) {
            if (isPasswordMissing(e)) {
                /*
                 * Client needs new credentials
                 */
                return CREDENTIALS_REFRESH;
            }
            handleExcpetion(e);
        } finally {
            accountAccess.close();
        }
        return INACCESSIBLE;
    }

    /**
     * Gets a value indicating whether exception is about a missing
     * password or not
     *
     * @param e The exception to analyze
     * @return <code>true</code> if the exception has anything to do with a missing password, <code>false</code> otherwise
     */
    private boolean isPasswordMissing(OXException e) {
        return matches(e, ApiClientExceptions.MISSING_CREDENTIALS, LoginExceptionCodes.INVALID_CREDENTIALS, LoginExceptionCodes.INVALID_GUEST_PASSWORD);
    }

    /**
     * Clear any remote session so follow up calls will work as expected
     *
     * @param session The user session
     * @param shareLink The share link
     * @param storageAccount The account to close
     * @throws OXException In case service is missing
     */
    private void clearRemoteSessions(Session session, String shareLink, FileStorageAccount storageAccount) throws OXException {
        fileStorageService.getAccountAccess(storageAccount.getId(), session).close();
        services.getServiceSafe(ApiClientService.class).close(session.getContextId(), session.getUserId(), Optional.of(shareLink));
    }

}
