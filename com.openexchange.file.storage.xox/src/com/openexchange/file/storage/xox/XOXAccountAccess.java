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

package com.openexchange.file.storage.xox;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.api.client.Credentials;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.datahandler.DataHandlers;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.ErrorStateFolderAccess;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountErrorHandler;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.folderstorage.FederatedSharingFolders;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.core.subscription.AccountMetadataHelper;
import com.openexchange.share.core.subscription.EntityMangler;
import com.openexchange.share.core.subscription.SubscribedHelper;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.tools.arrays.Collections;

import static com.openexchange.java.Autoboxing.B;

/**
 * {@link XOXAccountAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXAccountAccess implements CapabilityAware {

    /** The identifiers of the parent folders where adjusting the subscribed flag is supported, which mark the entry points for shared and public files */
    private static final Set<String> SUBSCRIBE_PARENT_IDS = Collections.unmodifiableSet("10", "15");

    private static final Logger LOG = LoggerFactory.getLogger(XOXAccountAccess.class);

    private final FileStorageAccount account;
    private final FileStorageService service;
    private final Session session;
    private final FileStorageAccountErrorHandler errorHandler;
    private final ServiceLookup services;

    private boolean isConnected;
    private ShareClient shareClient;

    /**
     * Initializes a new {@link XOXAccountAccess}.
     *
     * @param service The {@link FileStorageService}
     * @param services The {@link ServiceLookup} to get the {@link ApiClientService} or the {@link ConversionService}
     * @param account The {@link FileStorageAccount}
     * @param session The {@link Session}
     * @param retryAfterError The amount of seconds after which accessing an error afflicted account should be retried.
     * @throws OXException If services are missing
     */
    //@formatter:off
    public XOXAccountAccess(
                            FileStorageService service,
                            ServiceLookup services,
                            FileStorageAccount account,
                            Session session,
                            int retryAfterError
                            ) throws OXException {
        this.service = Objects.requireNonNull(service, "service must not be null");
        this.account = Objects.requireNonNull(account, "account must not be null");
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.services = Objects.requireNonNull(services, "services must not be null");

        ConversionService conversionService = services.getServiceSafe(ConversionService.class);
        DataHandler ox2jsonDataHandler = conversionService.getDataHandler(DataHandlers.OXEXCEPTION2JSON);
        DataHandler json2oxDataHandler = conversionService.getDataHandler(DataHandlers.JSON2OXEXCEPTION);
        this.errorHandler = new FileStorageAccountErrorHandler(ox2jsonDataHandler,
            json2oxDataHandler,
            this,
            session,
            retryAfterError,
            new FileStorageAccountErrorHandler.CompositingFilter()
                .add(new FileStorageAccountErrorHandler.IgnoreExceptionPrefixes("SES"))
                .add(new FileStorageAccountErrorHandler.IgnoreExceptionCodes(ApiClientExceptions.SESSION_EXPIRED)));
    }
    //@formatter:on

    /**
     * Gets the {@link Session}
     *
     * @return The session
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Gets the associated {@link FileStorageAccount}
     *
     * @return The associated account
     */
    public FileStorageAccount getAccount() {
        return this.account;
    }

    /**
     * Gets a {@link SubscribedHelper} suitable for the connected file storage account.
     *
     * @return The subscribed helper
     */
    public SubscribedHelper getSubscribedHelper() {
        return new SubscribedHelper(account, SUBSCRIBE_PARENT_IDS);
    }

    /**
     * Gets a {@link JSONObject} providing additional arbitrary metadata of the account for clients.
     *
     * @return The metadata
     */
    public JSONObject getMetadata() throws OXException {
        assertConnected();
        this.errorHandler.assertNoRecentException();
        try {
            JSONObject metadata = new JSONObject();
            /*
             * add identifiers of guest user/context based on share token
             */
            ShareToken shareToken = new ShareToken(ShareLinks.extractBaseToken(getShareUrl()));
            metadata.put("guestContextId", shareToken.getContextID());
            metadata.put("guestUserId", shareToken.getUserID());
            metadata.put("guestUserIdentifier", new EntityMangler(getService().getId(), getAccountId()).mangleRemoteEntity(shareToken.getUserID()));
            /*
             * load & add capabilities of guest user
             */
            metadata.put("guestCapabilities", new AccountMetadataHelper(account, session).getCachedValue("capabilities", TimeUnit.DAYS.toMillis(1L), JSONArray.class, () -> {
                if (null == shareClient) {
                    throw new IllegalStateException("missing share client reference");
                }
                return new JSONArray(shareClient.getApiClient().execute(new com.openexchange.api.client.common.calls.capabilities.AllCall()));
            }));
            return metadata;
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Internal method to ensure that the account access is connected
     *
     * @throws OXException If the account access is not connected
     */
    private void assertConnected() throws OXException {
        if (!isConnected()) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
    }

    /**
     * Resets the last known, recent, error for this account
     */
    void resetRecentError() throws OXException {
        errorHandler.removeRecentException();
    }

    @Override
    public String getAccountId() {
        return account.getId();
    }

    @Override
    public XOXFileAccess getFileAccess() throws OXException {
        assertConnected();
        this.errorHandler.assertNoRecentException();
        return new XOXFileAccess(this, shareClient);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        assertConnected();

        OXException recentException = this.errorHandler.getRecentException();
        if (recentException != null) {
            //In case of an error state: we return an implementation which will only allow to get the last known folders
            //@formatter:off
            return new ErrorStateFolderAccess(
                recentException,
                (String folderId) -> FederatedSharingFolders.getLastKnownFolder(account, folderId, session));
            //@formatter:on
        }
        return new XOXFolderAccess(this, shareClient);
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageService getService() {
        return service;
    }

    @Override
    public void connect() throws OXException {
        if (isConnected()) {
            return;
        }

        boolean hasKnownError = errorHandler.hasRecentException();
        if (hasKnownError) {
            //We do not really connect because we have known errors,
            //but dummy folders should still be returned
            isConnected = true;
            return;
        }

        String shareUrl = getShareUrl();
        Credentials credentials = new Credentials("", (String) account.getConfiguration().get(XOXStorageConstants.PASSWORD));

        ApiClientService clientService = services.getServiceSafe(ApiClientService.class);
        try {
            shareClient = new ShareClient(session, account, clientService.getApiClient(session, shareUrl, credentials));
            final Credentials cachedCredentials = shareClient.getApiClient().getCredentials();
            if (!Objects.equals(cachedCredentials, credentials)) {
                //The credentials changed; we need to close the current client and create a new one
                clientService.close(shareClient.getApiClient());
                shareClient = new ShareClient(session, account, clientService.getApiClient(session, shareUrl, credentials));
            }
            //the client might just come from a cache so we ensure that we can access the remote by performing a ping
            shareClient.ping();
        } catch (OXException e) {
            errorHandler.handleException(e);
        }
        isConnected = true;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void close() {
        shareClient = null;
        isConnected = false;
    }

    @Override
    public boolean ping() throws OXException {
        try {
            connect();
            errorHandler.assertNoRecentException();
            shareClient.ping();
            return true;
        } catch (OXException e) {
            errorHandler.handleException(e);
            throw e;
        } finally {
            close();
        }
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        Boolean supported = FileStorageCapabilityTools.supportsByClass(XOXFileAccess.class, capability);
        if (supported != null && supported == Boolean.TRUE && capability == FileStorageCapability.SEARCH_BY_TERM) {
            //The advanced search is only available on the remote side if the version is > 7.10.5
            try {
                connect();
                return B(shareClient.supportsFederatedSharing());
            } catch (OXException e) {
                LOG.error("Error while checking if federated sharing functionality is available on the remote host: ", e);
                return B(false);
            }
        }
        return supported;
    }

    private String getShareUrl() throws OXException {
        Map<String, Object> configuration = account.getConfiguration();
        String shareUrl = (String) configuration.get(XOXStorageConstants.SHARE_URL);
        if (Strings.isEmpty(shareUrl)) {
            throw FileStorageExceptionCodes.INVALID_URL.create("not provided", "empty");
        }
        return shareUrl;
    }

}
