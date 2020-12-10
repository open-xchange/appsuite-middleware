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

package com.openexchange.share.core.subscription;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.share.core.tools.ShareLinks.extractHostName;
import static com.openexchange.share.subscription.ShareLinkState.CREDENTIALS_REFRESH;
import static com.openexchange.share.subscription.ShareLinkState.INACCESSIBLE;
import static com.openexchange.share.subscription.ShareLinkState.REMOVED;
import static com.openexchange.share.subscription.ShareLinkState.SUBSCRIBED;
import static com.openexchange.share.subscription.ShareLinkState.UNRESOLVABLE;
import static com.openexchange.share.subscription.ShareLinkState.UNSUBSCRIBED;
import static com.openexchange.share.subscription.ShareLinkState.UNSUPPORTED;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult.Builder;
import com.openexchange.share.subscription.ShareLinkState;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.user.User;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link AbstractFileStorageSubscriptionProvider} - Abstract class that takes care to add/update/close operations for
 * an account belonging to the given generic filestorage.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public abstract class AbstractFileStorageSubscriptionProvider implements ShareSubscriptionProvider {

    private static final String SYSTEM_USER_INFOSTORE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
    private static final String SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileStorageSubscriptionProvider.class);

    private static final String URL = "url";
    private static final String PASSWORD = "password";

    protected final SharingFileStorageService fileStorageService;

    private final UserPermissionService userPermissionService;

    /**
     * Initializes a new {@link AbstractFileStorageSubscriptionProvider}.
     *
     * @param fileStorageService The storage to operate on
     * @param userPermissionService The {@link UserPermissionService}
     */
    public AbstractFileStorageSubscriptionProvider(SharingFileStorageService fileStorageService, UserPermissionService userPermissionService) {
        super();
        this.fileStorageService = Objects.requireNonNull(fileStorageService);
        this.userPermissionService = Objects.requireNonNull(userPermissionService);
    }

    @Override
    public ShareSubscriptionInformation subscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        requireAccess(session);
        FileStorageAccount existingAccount = getStorageAccount(session, shareLink);
        if (null != existingAccount) {
            LOGGER.debug("Found existing account with ID {}", existingAccount.getId());
            FileStorageAccountAccess accountAccess = fileStorageService.getAccountAccess(existingAccount.getId(), session);
            try {
                accountAccess.connect();
                setSubscribed(accountAccess, shareLink, true);
                return testAndGenerateInfos(accountAccess, shareLink);
            } finally {
                accountAccess.close();
            }
        }
        /*
         * Setup new account
         */
        DefaultFileStorageAccount storageAccount = new DefaultFileStorageAccount();
        storageAccount.setServiceId(fileStorageService.getId());
        storageAccount.setFileStorageService(fileStorageService);
        storageAccount.setId(null);
        storageAccount.setDisplayName(Strings.isNotEmpty(shareName) ? shareName : extractHostName(shareLink));

        /*
         * Set share specific configuration
         */
        Map<String, Object> configuration = new HashMap<>(3, 0.9f);
        if (Strings.isEmpty(shareLink)) {
            ShareSubscriptionExceptions.MISSING_LINK.create(shareLink);
        }
        configuration.put(URL, shareLink);
        if (Strings.isNotEmpty(password)) {
            configuration.put(PASSWORD, password);
        }
        storageAccount.setConfiguration(configuration);

        /*
         * Create new account and access it
         */
        FileStorageAccountAccess accountAccess = null;
        String accountId = null;
        try {
            accountId = fileStorageService.getAccountManager().addAccount(storageAccount, session);
            accountAccess = fileStorageService.getAccountAccess(accountId, session);
            accountAccess.connect();
            setSubscribed(accountAccess, shareLink, true);
            return testAndGenerateInfos(accountAccess, shareLink);
        } catch (OXException e) {
            /**
             * Account can't be created, connect or subscribed therefore delete it
             */
            deleteAccount(session, accountId);
            throw e;
        } finally {
            if (null != accountAccess) {
                accountAccess.close();
            }
        }
    }

    @Override
    public boolean unsubscribe(Session session, String shareLink) throws OXException {
        FileStorageAccount storageAccount = getStorageAccount(session, shareLink);
        if (null == storageAccount) {
            LOGGER.trace("No account found for {} in filestorage {}.", shareLink, fileStorageService.getClass().getSimpleName());
            return false;
        }
        if (false == hasAccess(session)) {
            LOGGER.info("User {} in context {} tried to unmount share without appropriated permissions.", I(session.getUserId()), I(session.getContextId()));
            return false;
        }
        FileStorageAccountAccess accountAccess = null;
        try {
            accountAccess = fileStorageService.getAccountAccess(storageAccount.getId(), session);
            accountAccess.connect();
            String folderId = setSubscribed(accountAccess, shareLink, false);
            if (Strings.isEmpty(folderId)) {
                /*
                 * Last folder was unsubscribed, account was deleted. Skip further checks
                 */
                return true;
            }
            FileStorageFolder rootFolder = getShareRootFolder(accountAccess, getFolderFrom(shareLink));
            if (rootFolder.isSubscribed()) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Unsubscribe was not successful");
            }
        } finally {
            if (null != accountAccess) {
                accountAccess.close();
            }
        }
        return true;
    }

    @Override
    public ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        requireAccess(session);
        /*
         * Check preconditions before update
         */
        if (Strings.isEmpty(shareLink)) {
            ShareSubscriptionExceptions.MISSING_LINK.create(shareLink);
        }
        FileStorageAccount storageAccount = getStorageAccount(session, shareLink);
        if (null == storageAccount) {
            throw ShareSubscriptionExceptions.MISSING_SUBSCRIPTION.create(shareLink);
        }

        /*
         * Prepare account to update
         */
        DefaultFileStorageAccount updated = new DefaultFileStorageAccount();
        updated.setServiceId(fileStorageService.getId());
        updated.setFileStorageService(fileStorageService);
        updated.setId(storageAccount.getId());
        updated.setDisplayName(Strings.isEmpty(shareName) ? storageAccount.getDisplayName() : shareName);
        Map<String, Object> configuration = null == storageAccount.getConfiguration() ? new HashMap<>(3, 0.9f) : new HashMap<>(storageAccount.getConfiguration());
        configuration.put(URL, shareLink);
        configuration.put(PASSWORD, password);
        updated.setConfiguration(configuration);

        /*
         * Close current access, update afterwards and formulate result
         */
        fileStorageService.getAccountAccess(storageAccount.getId(), session).close();
        fileStorageService.getAccountManager().updateAccount(updated, session);

        FileStorageAccountAccess accountAccess = null;
        try {
            accountAccess = fileStorageService.getAccountAccess(storageAccount.getId(), session);
            accountAccess.connect();
            return testAndGenerateInfos(accountAccess, shareLink);
        } finally {
            if (null != accountAccess) {
                accountAccess.close();
            }
        }
    }

    /**
     * Gets a value indicating whether an {@link OXException} is about a missing
     * password or not. This is used to determine the correct {@link ShareLinkState} when
     * accessing the share via {@link #checkAccessible(FileStorageAccountAccess, String)} fails
     *
     * @param e The exception to analyze
     * @return <code>true</code> if the exception has anything to do with a missing password, <code>false</code> otherwise
     */
    protected abstract boolean isPasswordMissing(OXException e);

    /**
     * Gets a value indicating whether an {@link OXException} is about a removed
     * folder or not. This is used to determine the correct {@link ShareLinkState} when
     * accessing the share via {@link #checkAccessible(FileStorageAccountAccess, String)} fails
     *
     * @param The exception to analyze
     * @return <code>true</code> if the exception has anything to do with a removed folder, <code>false</code> otherwise
     */
    protected abstract boolean isFolderRemoved(OXException e);

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Logs the given {@link OXException}
     *
     * @param e The exception to log
     */
    protected void logExcpetionError(OXException e) {
        LOGGER.debug("Resource is not accessible: {}", e.getMessage(), e);
    }

    /**
     * Logs the given {@link OXException}
     *
     * @param e The exception to log
     */
    protected void logExcpetionDebug(OXException e) {
        LOGGER.debug("Resource is not accessible: {}", e.getMessage(), e);
    }

    /**
     * Check if there is an account for this share with the same base token.
     *
     * @param session The user session
     * @param shareLink The share link
     * @param baseToken The base token of the share link
     * @return A {@link FileStorageAccount} or <code>null</code> if no account was found
     * @throws OXException If account listing fails
     */
    protected FileStorageAccount getStorageAccount(Session session, String shareLink) throws OXException {
        URL shareUrl = getUrl(shareLink);
        String baseToken = ShareTool.getBaseToken(shareLink);
        if (null == shareUrl || Strings.isEmpty(baseToken)) {
            return null;
        }
        ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
        if (null != targetPath && Module.INFOSTORE.getFolderConstant() != targetPath.getModule()) {
            return null;
        }
        for (FileStorageAccount account : fileStorageService.getAccountManager().getAccounts(session)) {
            String url = String.valueOf(account.getConfiguration().get(URL));
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
     * @throws OXException If account access cannot be returned for given account identifier
     */
    protected FileStorageAccountAccess getStorageAccountAccess(Session session, String shareLink) throws OXException {
        FileStorageAccount storageAccount = getStorageAccount(session, shareLink);
        if (null != storageAccount) {
            return fileStorageService.getAccountAccess(storageAccount.getId(), session);
        }
        return null;
    }

    /**
     * Deletes the account. Any exception on deletion is logged.
     *
     * @param session The session of the user
     * @param accountId The account to delete
     */
    private void deleteAccount(Session session, String accountId) {
        if (Strings.isEmpty(accountId)) {
            return;
        }
        try {
            FileStorageAccountManager accountManager = fileStorageService.getAccountManager();
            FileStorageAccount account = accountManager.getAccount(accountId, session);
            accountManager.deleteAccount(account, session);
        } catch (OXException e) {
            LOGGER.warn("Unable to remove account {} for user {} in context {}", accountId, I(session.getUserId()), I(session.getContextId()), e);
        }
    }

    /**
     * Compares two URLs if they have the same host
     *
     * @param actual The actual URL
     * @param url The expected URL as saved in the account access
     * @return <code>true</code> if both URL share the same host/domain part
     */
    protected static boolean compareHost(URL actual, String expected) {
        URL expectedUrl = getUrl(expected);
        if (null == expectedUrl || null == expectedUrl.getHost()) {
            /*
             * This should never happen ...
             */
            LOGGER.debug("The share URL within the account is broken.");
            return false;
        }
        return expectedUrl.getHost().equals(actual.getHost());
    }

    /**
     * Transforms the string into an {@link URL}
     *
     * @param url The URL to transform
     * @return A {@link URL} or <code>null</code>
     */
    protected static URL getUrl(String url) {
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
     * <p>
     * Uses {@link #isPasswordMissing(OXException)} to check a {@link OXException} that might occur while
     * accessing the share. If is <code>true</code> the {@link ShareLinkState#CREDENTIALS_REFRESH}
     * is used as a return value instead of {@link ShareLinkState#INACCESSIBLE}
     *
     * @param accountAccess The access to the account
     * @param shareLink The share link
     * @param session The session
     * @return A builder holding the state fitting the accessibility of the share
     */
    protected Builder checkAccessible(FileStorageAccountAccess accountAccess, String shareLink, Session session) {
        Builder builder = new Builder();
        try {
            /**
             * Clear recent errors of the account
             */
            fileStorageService.resetRecentError(accountAccess.getAccountId(), session);

            /*
             * Connect and access to check accessibility
             */
            accountAccess.connect();

            String folderId = getFolderFrom(shareLink);
            if (Strings.isEmpty(folderId)) {
                throw ShareSubscriptionExceptions.NOT_USABLE.create(shareLink);
            }
            FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
            folderAccess.getPath2DefaultFolder(folderId);
            if (isSubscribed(accountAccess, shareLink)) {
                return builder.state(SUBSCRIBED);
            }
            return builder.state(UNSUBSCRIBED).error(ShareSubscriptionExceptions.UNSUBSCRIEBED_FOLDER.create(folderId));
        } catch (OXException e) {
            if (isPasswordMissing(e)) {
                /*
                 * Client needs new credentials
                 */
                return builder.state(CREDENTIALS_REFRESH).error(e);
            }
            if (isFolderRemoved(e)) {
                return builder.state(REMOVED).error(e);
            }
            logExcpetionDebug(e);
            return builder.state(INACCESSIBLE).error(e);
        } finally {
            accountAccess.close();
        }
    }

    /**
     * Generates the information about the account, test connection before
     *
     * @param session The session
     * @param accountId The account ID
     * @param shareLink The share link
     * @return The {@link ShareSubscriptionInformation}
     * @throws OXException In case of error
     */
    protected ShareSubscriptionInformation testAndGenerateInfos(FileStorageAccountAccess accountAccess, String shareLink) throws OXException {
        String folderId = getFolderFrom(shareLink);
        accountAccess.getFolderAccess().getPath2DefaultFolder(folderId);
        return generateInfos(accountAccess, shareLink);
    }

    /**
     * Generates the information about the account
     *
     * @param account The account to get the infos from
     * @param shareLink The share link
     * @return The {@link ShareSubscriptionInformation}
     */
    protected ShareSubscriptionInformation generateInfos(FileStorageAccountAccess accountAccess, String shareLink) {
        return generateInfos(getFolderFrom(shareLink), accountAccess);
    }

    /**
     * Generates the information about the account
     *
     * @param folderId The folder ID
     * @param account The account to get the infos from
     * @return The {@link ShareSubscriptionInformation}
     */
    protected ShareSubscriptionInformation generateInfos(String folderId, FileStorageAccountAccess accountAccess) {
        return new ShareSubscriptionInformation( // @formatter:off
            accountAccess.getAccountId(),
            String.valueOf(Module.INFOSTORE.getName()),
            IDMangler.mangle(fileStorageService.getId(), accountAccess.getAccountId(), folderId)); // @formatter:on
    }

    /**
     * Gets a value indicating whether the user represented by the session is allowed
     * to subscribe to the share
     *
     * @param session The session of the user
     */
    protected boolean hasAccess(Session session) {
        return hasModuleAccess(session) && fileStorageService.hasCapability(session);
    }

    /**
     * Checks that the user represented by the session is allowed to subscribe
     * to a share
     *
     * @param session The session of the user
     * @throws OXException In case the permissions are sufficient
     */
    protected void requireAccess(Session session) throws OXException {
        if (false == hasAccess(session)) {
            throw ShareExceptionCodes.NO_SUBSCRIBE_SHARE_PERMISSION.create();
        }
    }

    /**
     * Gets a value indicating whether if the user has the permission to access the infostore
     *
     * @param userPermissionService The permission service
     * @param session The session
     * @return <code>true</code> if the user is allowed to use the infostore, <code>false</code> otherwise
     */
    private boolean hasModuleAccess(Session session) {
        try {
            UserPermissionBits userPermissionBits = userPermissionService.getUserPermissionBits(session.getUserId(), session.getContextId());
            if (null == userPermissionBits) {
                return false;
            }
            return userPermissionBits.hasInfostore();
        } catch (OXException e) {
            LOGGER.error("Unable to get permissions for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
        }
        return false;
    }

    /**
     * Gets a value indicating whether the user can access the filestorage or not
     *
     * @param session The user session
     * @return <code>true</code> if the user has the capability to access the file storage, <code>false</code> otherwise
     */
    protected boolean hasCapability(Session session) {
        return fileStorageService.hasCapability(session);
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

    /**
     * Gets a value indicating whether the share is for a single file or not.
     *
     * @param shareLink The share link
     * @return <code>true</code> if the link is about a single file, <code>false</code> if not, e.g. for a folder share
     */
    protected boolean isSingleFileShare(String shareLink) {
        ShareTargetPath path = ShareTool.getShareTarget(shareLink);
        return null != path && Strings.isNotEmpty(path.getItem());
    }

    /**
     * The module info
     *
     * @return {@link ShareSubscriptionInformation} with only the module set
     */
    protected ShareSubscriptionInformation getModuleInfo() {
        return new ShareSubscriptionInformation(null, Module.INFOSTORE.getName(), null);
    }

    /**
     * Extracts the folder ID from a share link
     *
     * @param shareLink The share link
     * @return The folder ID or <code>null</code>
     */
    protected String getFolderFrom(String shareLink) {
        ShareTargetPath path = ShareTool.getShareTarget(shareLink);
        if (null != path) {
            return path.getFolder();
        }
        return null;
    }

    /**
     * Set the subscribed flag to the root folder of the given folder
     *
     * @param session The session to use
     * @param shareLink The share link to set the subscribed flag for
     * @param accountId The file storage account ID
     * @param subscribed <code>true</code> to subscribe, <code>false</code> to unsubscribe
     * @return The folder ID of the updated folder
     * @throws OXException In case it can't be (un-)subscribed
     */
    private String setSubscribed(FileStorageAccountAccess accountAccess, String shareLink, boolean subscribed) throws OXException {
        String folderId = getFolderFrom(shareLink);

        /*
         * Search for share or public root folder
         */
        FileStorageFolder rootFolder = getShareRootFolder(accountAccess, folderId);
        if (null == rootFolder) {
            throw FolderExceptionErrorMessage.NOT_FOUND.create(folderId, "");
        }

        /*
         * Subscribe folder, root folder only at the moment
         */
        DefaultFileStorageFolder update = new DefaultFileStorageFolder();
        update.setId(rootFolder.getId());
        update.setSubscribed(subscribed);
        return accountAccess.getFolderAccess().updateFolder(rootFolder.getId(), update);
    }

    /**
     * Set the subscribed flag to the root folder of the given folder
     *
     * @param session The session to use
     * @param shareLink The share link to set the subscribed flag for
     * @param accountId The file storage account ID
     * @throws OXException In case parent folder can't be get
     */
    private boolean isSubscribed(FileStorageAccountAccess accountAccess, String shareLink) throws OXException {
        String folderId = getFolderFrom(shareLink);
        FileStorageFolder rootFolder = getShareRootFolder(accountAccess, folderId);
        return null != rootFolder && rootFolder.isSubscribed();
    }

    /**
     * Get the shares root folder. The parent of this folder might be the user or the public infostore folder
     *
     * @param accountAccess The access to use
     * @param folderId The folder to resolve the path to
     * @return The root folder or <code>null</code>
     * @throws OXException If path can't be resolved
     */
    private FileStorageFolder getShareRootFolder(FileStorageAccountAccess accountAccess, String folderId) throws OXException {
        for (FileStorageFolder folder : accountAccess.getFolderAccess().getPath2DefaultFolder(folderId)) {
            String id = folder.getParentId();
            if (Strings.isNotEmpty(id) && SYSTEM_USER_INFOSTORE_FOLDER_ID.equals(id) || SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID.equals(id)) {
                return folder;
            }
        }
        return null;
    }

    /**
     * Gets a state indicating whether a single file is already contained in a subscribed share or not.
     *
     * @param session The user session
     * @param shareLink The share link containing the file
     * @return {@link ShareLinkState#SUBSCRIBED} if the file belongs to a subscribed folder,
     *         {@link ShareLinkState#FORBIDDEN} if only the file is shared and the user has no further access
     * @throws OXException
     */
    protected ShareLinkAnalyzeResult checkSingleFileAccessible(Session session, String shareLink) throws OXException {
        FileStorageAccountAccess accountAccess = getStorageAccountAccess(session, shareLink);
        if (null == accountAccess) {
            /*
             * Unknown share
             */
            return new ShareLinkAnalyzeResult(UNSUPPORTED, ShareExceptionCodes.NO_FILE_SUBSCRIBE.create(), getModuleInfo());
        }
        ShareTargetPath path = ShareTool.getShareTarget(shareLink);
        if (null == path) {
            // Should not happen since checked by precondition in isSupported()
            return new ShareLinkAnalyzeResult(UNRESOLVABLE, ShareSubscriptionExceptions.UNEXPECTED_ERROR.create("Unable to get share path from link"), getModuleInfo());
        }

        /*
         * Check if the file is already in a known folder
         */
        try {
            String folderId = path.getFolder();
            accountAccess.connect();
            FileStorageFolder folder = accountAccess.getFolderAccess().getFolder(folderId);
            if (SYSTEM_USER_INFOSTORE_FOLDER_ID.equals(folder.getId()) || SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID.equals(folder.getId())) {
                /*
                 * Do not allow to subscribe to files under the root folder of "shared" or "public" folder
                 */
                return new ShareLinkAnalyzeResult(UNSUPPORTED, ShareExceptionCodes.NO_FILE_SUBSCRIBE.create(), getModuleInfo());
            }
            String item = getItemID(path);
            if (accountAccess.getFileAccess().exists(folder.getId(), item, FileStorageFileAccess.CURRENT_VERSION)) {
                return new ShareLinkAnalyzeResult(SUBSCRIBED, generateInfos(folderId, accountAccess));
            }
        } catch (OXException e) {
            return new ShareLinkAnalyzeResult(UNSUPPORTED, e, getModuleInfo());
        } finally {
            accountAccess.close();
        }
        return new ShareLinkAnalyzeResult(UNSUPPORTED, ShareExceptionCodes.NO_FILE_SUBSCRIBE.create(), getModuleInfo());
    }

    private String getItemID(ShareTargetPath path) {
        String folderId = path.getFolder();
        String item = path.getItem();
        if (item.startsWith(folderId) && item.length() > (folderId.length() + 1)) {
            item = item.substring(folderId.length() + 1); // "folderId/itemId"
        }
        return item;
    }

}
