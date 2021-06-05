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

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.LoginAwareFileStorageServiceExtension;
import com.openexchange.file.storage.MetadataAware;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.file.storage.SizeLimitedFileStorageAccountManager;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link XOXFileStorageService} - The file storage service to access OX shares on other installations
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXFileStorageService implements AccountAware, SharingFileStorageService, LoginAwareFileStorageServiceExtension, MetadataAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(XOXFileStorageService.class);

    private static final String FILESTORAGE_OXSHARE_CAPABILITY = "filestorage_xox";

    private static final Object LOCK = new Object();

    private final DynamicFormDescription formDescription;
    private final ServiceLookup services;
    private volatile FileStorageAccountManager accountManger;

    /**
     * Initializes a new {@link XOXFileStorageService}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public XOXFileStorageService(ServiceLookup services) {
        this.services = Objects.requireNonNull(services, "services must not be null");

        DynamicFormDescription description = new DynamicFormDescription();
        description.add(FormElement.link(XOXStorageConstants.SHARE_URL, FormStrings.SHARE_LINK_LABEL));
        description.add(FormElement.password(XOXStorageConstants.PASSWORD, FormStrings.PASSWORD, false, ""));
        formDescription = new ReadOnlyDynamicFormDescription(description);
    }

    /**
     * Gets the account manager
     *
     * @return The account manager
     * @throws OXException
     */
    private FileStorageAccountManager getAccountManager0() throws OXException {
        FileStorageAccountManager m = accountManger;
        if (null == m) {
            synchronized (LOCK) {
                m = accountManger;
                if (null == m) {
                    FileStorageAccountManagerLookupService lookupService = services.getService(FileStorageAccountManagerLookupService.class);
                    m = new SizeLimitedFileStorageAccountManager("xox8", this::getMaxAccounts, new XOXFileStorageAccountManager(services, lookupService.getAccountManagerFor(getId())));
                    accountManger = m;
                }
            }
        }
        return m;
    }

    /**
     * Gets a list of accounts for the given session
     *
     * @param session The session to get the accounts for
     * @return A list of accounts for the given session
     * @throws OXException
     */
    private List<FileStorageAccount> getAccounts0(Session session) throws OXException {
        return getAccountManager0().getAccounts(session);
    }

    /**
     * Checks if the given session has the appropriate capability for using this file storage
     *
     * @param session The session to check
     * @throws OXException in case the session does not have the appropriated capability to use this file storage
     */
    private void assertCapability(Session session) throws OXException {
        if (!hasCapability(session)) {
            throw XOXFileStorageExceptionCodes.MISSING_CAPABILITY.create(getId());
        }
    }

    /**
     * Returns the configured retryAfter value which indicates after which time access to an error afflicted account can be retried.
     *
     * @param session The session
     * @return The configured amount of time in seconds
     * @throws OXException
     */
    private int getRetryAfterError(Session session) throws OXException {
        LeanConfigurationService configuration = this.services.getServiceSafe(LeanConfigurationService.class);
        return configuration.getIntProperty(session.getUserId(), session.getContextId(), XOXFileStorageProperties.RETRY_AFTER_ERROR_INTERVAL);
    }

    /**
     * Returns the configured maxAccount value which indicates the amount of allowed XOX accounts
     *
     * @param session The session
     * @return The configured amount of allowed xox accounts
     * @throws OXException
     */
    private int getMaxAccounts(Session session) throws OXException {
        LeanConfigurationService configuration = this.services.getServiceSafe(LeanConfigurationService.class);
        return configuration.getIntProperty(session.getUserId(), session.getContextId(), XOXFileStorageProperties.MAX_ACCOUNTS);
    }

    @Override
    public String getId() {
        return XOXStorageConstants.ID;
    }

    @Override
    public String getDisplayName() {
        return XOXStorageConstants.DISPLAY_NAME;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.singleton(XOXStorageConstants.PASSWORD);
    }

    @Override
    public FileStorageAccountManager getAccountManager() throws OXException {
        return getAccountManager0();
    }

    @Override
    public boolean hasCapability(Session session) {
        try {
            CapabilityService capabilityService = services.getServiceSafe(CapabilityService.class);
            return capabilityService.getCapabilities(session).contains(FILESTORAGE_OXSHARE_CAPABILITY);
        } catch (OXException e) {
            LOGGER.error("Unable to get capability", e);
        }
        return false;
    }

    @Override
    public XOXAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        assertCapability(session);
        FileStorageAccountManager manager = getAccountManager();
        if (null == manager) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, getId(), I(session.getUserId()), I(session.getContextId()));
        }
        FileStorageAccount account = manager.getAccount(accountId, session);
        return new XOXAccountAccess(this, services, account, session, getRetryAfterError(session));
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return getAccounts0(session);
    }

    @Override
    public void testConnection(FileStorageAccount account, Session session) throws OXException {
        try {
            if (!getAccountAccess(account.getId(), session).ping()) {
                throw XOXFileStorageExceptionCodes.PING_FAILED.create();
            }
        } catch (Exception e) {
            throw XOXFileStorageExceptionCodes.PING_FAILED.create(e.getCause(), (Object[]) null);
        }
    }

    @Override
    public void resetRecentError(String accountId, Session session) throws OXException {
        XOXAccountAccess accountAccess = getAccountAccess(accountId, session);
        accountAccess.resetRecentError();
    }

    @Override
    public JSONObject getMetadata(Session session, FileStorageAccount account) throws OXException {
        XOXAccountAccess accountAccess = getAccountAccess(account.getId(), session);
        try {
            accountAccess.connect();
            return accountAccess.getMetadata();
        } finally {
            accountAccess.close();
        }
    }
}
