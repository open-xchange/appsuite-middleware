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

package com.openexchange.file.storage.xctx;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.LoginAwareFileStorageServiceExtension;
import com.openexchange.file.storage.MetadataAware;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.file.storage.SizeLimitedFileStorageAccountManager;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;

/**
 * {@link XctxFileStorageService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFileStorageService implements SharingFileStorageService, LoginAwareFileStorageServiceExtension, MetadataAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(XctxFileStorageService.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XctxFileStorageService}
     *
     * @param services A service lookup reference
     */
    public XctxFileStorageService(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return getAccountManager().getAccounts(session);
    }

    @NonNull
    @Override
    public String getId() {
        return "xctx" + Module.INFOSTORE.getFolderConstant();
    }

    @Override
    public String getDisplayName() {
        return "Cross-Context";
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return new ReadOnlyDynamicFormDescription(new DynamicFormDescription()
            .add(FormElement.custom("url", "url", "Share Link:", true, ""))
            .add(FormElement.custom("password", "password", "Password:"))
        );
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.singleton("password");
    }

    @Override
    public JSONObject getMetadata(Session session, FileStorageAccount account) throws OXException {
        XctxAccountAccess accountAccess = getAccountAccess(account.getId(), session);
        try {
            accountAccess.connect();
            return accountAccess.getMetadata();
        } finally {
            accountAccess.close();
        }
    }

    @Override
    public boolean hasCapability(Session session) {
        try {
            CapabilitySet capabilities = services.getServiceSafe(CapabilityService.class).getCapabilities(session);
            return capabilities.contains("filestorage_xctx");
        } catch (OXException e) {
            LOGGER.error("Unable to get capability", e);
        }
        return false;
    }

    @Override
    public FileStorageAccountManager getAccountManager() throws OXException {
        return new SizeLimitedFileStorageAccountManager(getId(), this::getMaxAccounts, services.getServiceSafe(FileStorageAccountManagerLookupService.class).getAccountManagerFor(getId()));
    }

    @Override
    public XctxAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        assertCapability(session);
        FileStorageAccount account = getAccountManager().getAccount(accountId, session);
        return new XctxAccountAccess(services, account, session, getRetryAfterError(session));
    }

    @Override
    public void testConnection(FileStorageAccount account, Session session) throws OXException {
        assertCapability(session);
        XctxAccountAccess accountAccess = new XctxAccountAccess(services, account, session, getRetryAfterError(session));
        accountAccess.connect();
        accountAccess.close();
    }

    /**
     * Checks if the given session has the appropriate capability for using this file storage
     *
     * @param session The session to check
     * @throws OXException in case the session does not have the appropriated capability to use this file storage
     */
    private void assertCapability(Session session) throws OXException {
        if (!hasCapability(session)) {
            throw ShareExceptionCodes.NO_SUBSCRIBE_SHARE_PERMISSION.create();
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
        return configuration.getIntProperty(session.getUserId(), session.getContextId(), XctxFileStorageProperties.RETRY_AFTER_ERROR_INTERVAL);
    }

    /**
     * Returns the configured maxAccount value which indicates the amount of allowed xctx accounts
     *
     * @param session The session
     * @return The configured amount of allowed xctx accounts
     * @throws OXException
     */
    private int getMaxAccounts(Session session) throws OXException {
        LeanConfigurationService configuration = this.services.getServiceSafe(LeanConfigurationService.class);
        return configuration.getIntProperty(session.getUserId(), session.getContextId(), XctxFileStorageProperties.MAX_ACCOUNTS);
    }

    @Override
    public void resetRecentError(String accountId, Session session) throws OXException {
        XctxAccountAccess accountAccess = getAccountAccess(accountId, session);
        accountAccess.resetRecentError();
    }

}
