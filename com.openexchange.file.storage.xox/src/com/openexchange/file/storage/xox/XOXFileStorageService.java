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

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.LoginAwareFileStorageServiceExtension;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link XOXFileStorageService} - The file storage service to access OX shares on other installations
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXFileStorageService implements AccountAware, SharingFileStorageService, LoginAwareFileStorageServiceExtension {

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
                    m = lookupService.getAccountManagerFor(getId());
                    accountManger = m;
                }
            }
        }
        return m;
    }

    /**
     * Gets the client {@link ApiClientService} for accessing the remote server
     *
     * @return The {@link ApiClientService}
     * @throws OXException
     */
    private ApiClientService getApiClientService() throws OXException {
        return this.services.getServiceSafe(ApiClientService.class);
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
    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        assertCapability(session);
        FileStorageAccountManager manager = getAccountManager();
        if (null == manager) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, getId(), I(session.getUserId()), I(session.getContextId()));
        }
        FileStorageAccount account = manager.getAccount(accountId, session);
        return new XOXAccountAccess(this, getApiClientService(), account, session);
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
        } catch (OXException e) {
            throw XOXFileStorageExceptionCodes.PING_FAILED.create(e.getCause(), (Object[]) null);
        }
    }
}
