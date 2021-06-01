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

package com.openexchange.file.storage.xox;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.datahandler.DataHandlers;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.ErrorStateFileAccess;
import com.openexchange.file.storage.ErrorStateFolderAccess;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountErrorHandler;
import com.openexchange.file.storage.FileStorageAccountErrorHandler.Result;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageResult;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.SetterAwareFileStorageFolder;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.core.subscription.AccountMetadataHelper;
import com.openexchange.share.core.subscription.EntityMangler;
import com.openexchange.share.core.subscription.SubscribedHelper;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.tools.arrays.Collections;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.b;

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

    private final FileStorageService service;
    private final FileStorageAccountErrorHandler errorHandler;
    private final ServiceLookup services;

    private boolean isConnected;
    private ShareClient shareClient;

    protected final FileStorageAccount account;
    protected final Session session;

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
    public XOXAccountAccess(FileStorageService service,
                            ServiceLookup services,
                            FileStorageAccount account,
                            Session session,
                            int retryAfterError) throws OXException {
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
                .add((e) -> {
                    if(ApiClientExceptions.ACCESS_REVOKED.equals(e) && isAutoRemoveUnknownShares(session)) {
                        try {
                            LOG.info("Guest account for cross-ox share subscription no longer exists, removing file storage account {}.", account.getId(), e);
                            account.getFileStorageService().getAccountManager().deleteAccount(account, session);
                            return Boolean.FALSE; //handle by removing the account, abort upstream processing
                        }
                        catch(OXException e2) {
                            LOG.error("Unexpected error removing file storage account {}.", account.getId(), e2);
                        }
                    }
                    return Boolean.TRUE; //not handled, continue with upstream processing (persist error in account)
                })
            );
    }
    //@formatter:on

    /**
     * Gets a value indicating whether the automatic removal of accounts in the <i>cross-ox</i> file storage provider that refer to a no
     * longer existing guest user in the remote context is enabled or not.
     *
     * @param session The session to check the configuration for
     * @return <code>true</code> if unknown shares should be removed automatically, <code>false</code>, otherwise
     */
    private boolean isAutoRemoveUnknownShares(Session session) {
        Property property = XOXFileStorageProperties.AUTO_REMOVE_UNKNOWN_SHARES;
        try {
            return services.getServiceSafe(LeanConfigurationService.class).getBooleanProperty(session.getUserId(), session.getContextId(), property);
        } catch (OXException e) {
            LOG.error("Error getting {}, falling back to defaults.", property, e);
            return b(property.getDefaultValue(Boolean.class));
        }
    }

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
    public FileStorageFileAccess getFileAccess() throws OXException {
        assertConnected();
        OXException recentException = this.errorHandler.getRecentException();
        if (recentException != null) {
            //In case of an error state: we return an implementation which at least return empty objects on read access
            return new ErrorStateFileAccess(recentException, this);
        }
        return new XOXFileAccess(this, shareClient);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        assertConnected();
        OXException recentException = this.errorHandler.getRecentException();

        //In case of an error state: we return an implementation which will only allow to get the last known folders
        if (recentException != null) {
            return new ErrorStateFolderAccess(account, recentException) {

                @Override
                public FileStorageFolderStub[] getLastKnownSubFolders(String folderId) throws OXException {
                    return new AccountMetadataHelper(account, session).getLastKnownFolders(folderId);
                }

                @Override
                public FileStorageFolderStub getLastKnownFolder(String folderId) throws OXException {
                    return new AccountMetadataHelper(account, session).getLastKnownFolder(folderId);
                }

                @Override
                public FileStorageResult<String> updateLastKnownFolder(FileStorageFolder folder, boolean ignoreWarnings, FileStorageFolder toUpdate) throws OXException {
                    //Only able update the subscription flag in case of an error
                    if (SetterAwareFileStorageFolder.class.isInstance(toUpdate) && ((SetterAwareFileStorageFolder) toUpdate).containsSubscribed()) {

                        //Check for un-subscription and check if the last folder is going to be unscubscribed
                        if (false == toUpdate.isSubscribed()) {
                            List<FileStorageFolder> subscribedFolders = getVisibleRootFolders();
                            Optional<FileStorageFolder> folderToUnsubscibe = subscribedFolders.stream().filter(f -> f.getId().equals(folder.getId())).findFirst();
                            if (folderToUnsubscibe.isPresent()) {
                                subscribedFolders.removeIf(f -> f == folderToUnsubscibe.get());
                                if (subscribedFolders.isEmpty()) {
                                    //The last folder is going to be unsubscribed
                                    if (ignoreWarnings) {
                                        //Delete
                                        getService().getAccountManager().deleteAccount(getAccount(), session);
                                    } else {
                                        //Throw a warning
                                        String folderName = folderToUnsubscibe.get().getName();
                                        String accountName = getAccount().getDisplayName();
                                        return FileStorageResult.newFileStorageResult(null, Arrays.asList(ShareSubscriptionExceptions.ACCOUNT_WILL_BE_REMOVED.create(folderName, accountName)));
                                    }
                                    return FileStorageResult.newFileStorageResult(null, null);
                                }
                            }
                        }

                        getSubscribedHelper().setSubscribed(session, folder, B(toUpdate.isSubscribed()));
                        return FileStorageResult.newFileStorageResult(folder.getId(), null);
                    }
                    return FileStorageResult.newFileStorageResult(null, null);
                }
            };
        }

        return new XOXFolderAccess(this, new XOXFileAccess(this, shareClient), shareClient, session);
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
            //the client might just come from a cache so we ensure that we can access the remote by performing a ping
            shareClient.ping();
            isConnected = true;
        } catch (OXException e) {
            Result handlingResult = errorHandler.handleException(e);
            isConnected = handlingResult.isHandled();
        }
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
        Boolean supported;
        if (capability.isFileAccessCapability()) {
            supported = FileStorageCapabilityTools.supportsByClass(XOXFileAccess.class, capability);
        } else {
            supported = FileStorageCapabilityTools.supportsFolderCapabilityByClass(XOXFolderAccess.class, capability);
        }
        if (supported != null && Boolean.TRUE.equals(supported) && capability == FileStorageCapability.SEARCH_BY_TERM) {
            //The advanced search is only available on the remote side if the version is > 7.10.5
            try {
                connect();
                boolean hasKnownError = errorHandler.hasRecentException();
                if (hasKnownError) {
                    //We cannot perform the search by term in case of an error state
                    return Boolean.FALSE;
                }
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
