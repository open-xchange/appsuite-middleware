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
import static com.openexchange.share.subscription.ShareLinkState.CREDENTIALS_REFRESH;
import static com.openexchange.share.subscription.ShareLinkState.INACCESSIBLE;
import static com.openexchange.share.subscription.ShareLinkState.SUBSCRIBED;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.subscription.ShareLinkState;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link AbstractFileStorageSubscriptionProvider} - Abstract class that takes care to add/update/close operations for
 * an account belonging to the given generic filestorage.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public abstract class AbstractFileStorageSubscriptionProvider implements ShareSubscriptionProvider {

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
    public ShareSubscriptionInformation mount(Session session, String shareLink, String shareName, String password) throws OXException {
        requireAccess(session);
        FileStorageAccount existingAccount = getStorageAccount(session, shareLink);
        if (null != existingAccount) {
            LOGGER.debug("Found existing account with ID {}", existingAccount.getId());
            return generateInfos(session, existingAccount.getId(), shareLink);
        }
        /*
         * Setup new account
         */
        DefaultFileStorageAccount storageAccount = new DefaultFileStorageAccount();
        storageAccount.setServiceId(fileStorageService.getId());
        storageAccount.setFileStorageService(fileStorageService);
        storageAccount.setId(null);
        storageAccount.setDisplayName(shareName);

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
         * Create new account and formulate a result
         */
        String accountId = fileStorageService.getAccountManager().addAccount(storageAccount, session);
        return generateInfos(session, accountId, shareLink);
    }

    @Override
    public boolean unmount(Session session, String shareLink) throws OXException {
        FileStorageAccount storageAccount = getStorageAccount(session, shareLink);
        if (null != storageAccount) {
            if (false == hasAccess(session)) {
                LOGGER.info("User {} in context {} tried to unmount share without appropriated permissions.", I(session.getUserId()), I(session.getContextId()));
                return false;
            }
            /*
             * Close access and remove account, announce successful closing
             */
            fileStorageService.getAccountAccess(storageAccount.getId(), session).close();
            fileStorageService.getAccountManager().deleteAccount(storageAccount, session);
            return true;
        }
        LOGGER.trace("No account found for {} in filestorage {}.", shareLink, fileStorageService.getClass().getSimpleName());
        return false;
    }

    @Override
    public ShareSubscriptionInformation remount(Session session, String shareLink, String shareName, String password) throws OXException {
        requireAccess(session);
        /*
         * Check preconditions before update
         */
        if (Strings.isEmpty(shareLink)) {
            ShareSubscriptionExceptions.MISSING_LINK.create(shareLink);
        }
        if (Strings.isEmpty(password)) {
            ShareSubscriptionExceptions.MISSING_CREDENTIALS.create();
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
        return generateInfos(session, storageAccount.getId(), shareLink);
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
     * Get the filestorage ID
     *
     * @return The ID
     */
    protected String getId() {
        return fileStorageService.getId();
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
    protected FileStorageAccount getStorageAccount(Session session, String shareLink) throws OXException {
        URL shareUrl = getUrl(shareLink);
        if (null == shareUrl) {
            return null;
        }
        String baseToken = ShareTool.getBaseToken(shareLink);
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
     * @throws OXException
     */
    protected FileStorageAccountAccess getStorageAccountAccess(Session session, String shareLink) throws OXException {
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
     * @return A state fitting the accessibility of the share
     */
    protected ShareLinkState checkAccessible(FileStorageAccountAccess accountAccess, String shareLink) {
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
            logExcpetionDebug(e);
        } finally {
            accountAccess.close();
        }
        return INACCESSIBLE;
    }

    /**
     * Generates the information about the account
     *
     * @param session The session
     * @param accountId The account ID
     * @param shareLink The share link
     * @return The {@link ShareSubscriptionInformation}
     * @throws OXException In case of error
     */
    protected ShareSubscriptionInformation generateInfos(Session session, String accountId, String shareLink) throws OXException {
        if (Strings.isEmpty(accountId)) {
            throw ShareSubscriptionExceptions.MISSING_SUBSCRIPTION.create(shareLink);
        }

        FileStorageAccountAccess accountAccess = fileStorageService.getAccountAccess(accountId, session);
        try {
            accountAccess.connect();
            return generateInfos(accountAccess, shareLink);
        } finally {
            accountAccess.close();
        }
    }

    /**
     * Generates the information about the account
     *
     * @param account The account to get the infos from
     * @return The {@link ShareSubscriptionInformation}
     */
    protected ShareSubscriptionInformation generateInfos(FileStorageAccountAccess accountAccess, String shareLink) {
        String folderId = ShareTool.getShareTarget(shareLink).getFolder();
        return new ShareSubscriptionInformation( // @formatter:off
            getId(),
            accountAccess.getAccountId(),
            String.valueOf(Module.INFOSTORE.getFolderConstant()),
            IDMangler.mangle(getId(), accountAccess.getAccountId(), folderId)); // @formatter:on
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

}
