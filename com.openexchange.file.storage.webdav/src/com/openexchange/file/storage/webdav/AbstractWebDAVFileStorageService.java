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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.file.storage.webdav;

import static com.openexchange.file.storage.HttpClientAwareAccountManager.newInstanceFor;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.LoginAwareFileStorageServiceExtension;
import com.openexchange.file.storage.webdav.exception.WebdavExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.webdav.client.WebDAVClientFactory;

/**
 * {@link AbstractWebDAVFileStorageService} - The WebDAV file storage service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractWebDAVFileStorageService implements AccountAware, LoginAwareFileStorageServiceExtension {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractWebDAVFileStorageService.class);
    public static final String CAPABILITY_PREFIX = "filestorage_";

    private final DynamicFormDescription formDescription;
    protected final ServiceLookup services;
    private final String serviceId;
    private final String displayName;

    private volatile FileStorageAccountManager accountManager;
    private volatile CompositeFileStorageAccountManagerProvider compositeAccountManager;

    /**
     * Initializes a new {@link AbstractWebDAVFileStorageService}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param displayName The display name of the service
     * @param serviceId The service identifier
     * @param compositeFileStorageAccountManagerProvider The {@link CompositeFileStorageAccountManagerProvider}
     */
    protected AbstractWebDAVFileStorageService(ServiceLookup services, String displayName, String serviceId, CompositeFileStorageAccountManagerProvider compositeFileStorageAccountManagerProvider) {
        this(services, displayName, serviceId);
        compositeAccountManager = compositeFileStorageAccountManagerProvider;
    }

    /**
     * Initializes a new {@link AbstractWebDAVFileStorageService}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param displayName The display name of the service
     * @param serviceId The service identifier
     */
    protected AbstractWebDAVFileStorageService(ServiceLookup services, String displayName, String serviceId) {
        super();
        this.services = services;
        this.displayName = displayName;
        this.serviceId = serviceId;

        DynamicFormDescription tmpDescription = new DynamicFormDescription();
        FormElement login = FormElement.custom("login", "login", FormStrings.LOGIN_LABEL);
        tmpDescription.add(login);
        FormElement password = FormElement.custom("password", "password", FormStrings.PASSWORD_LABEL);
        tmpDescription.add(password);
        FormElement url = FormElement.custom(WebDAVFileStorageConstants.WEBDAV_URL, WebDAVFileStorageConstants.WEBDAV_URL, FormStrings.URL_LABEL);
        tmpDescription.add(url);

        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
    }

    public WebDAVClientFactory getClientFactory() throws OXException {
        return services.getServiceSafe(WebDAVClientFactory.class);
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.singleton("password");
    }

    @Override
    public String getId() {
        return serviceId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public FileStorageAccountManager getAccountManager() throws OXException {
        final CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
        if (null == compositeAccountManager) {
            return getAccountManager0();
        }
        try {
            return newInstanceFor(compositeAccountManager.getAccountManagerFor(getId()));
        } catch (OXException e) {
            LOG.warn("{}", e.getMessage(), e);
            return getAccountManager0();
        }
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return getAccounts0(session, true);
    }

    @Override
    public void testConnection(FileStorageAccount account, Session session) throws OXException {
        boolean ping = getAccountAccess(account.getId(), session).ping();
        if (!ping) {
            throw WebdavExceptionCodes.PING_FAILED.create();
        }
    }

    /**
     * Retrieves the {@link FileStorageAccount} with the specified identifier for the specified {@link Session}
     *
     * @param session The {@link Session}
     * @param accountId The account identifier
     * @return The {@link FileStorageAccount}
     * @throws OXException if an error is occurred
     */
    protected FileStorageAccount getAccountAccess(Session session, String accountId) throws OXException {
        checkCapability(session);
        final CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
        if (null == compositeAccountManager) {
            FileStorageAccountManager manager = getAccountManager0();
            if (null == manager) {
                throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, serviceId, I(session.getUserId()), I(session.getContextId()));
            }
            return manager.getAccount(accountId, session);
        }

        FileStorageAccountManager manager = compositeAccountManager.getAccountManager(accountId, session);
        if (null == manager) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, serviceId, I(session.getUserId()), I(session.getContextId()));
        }
        return manager.getAccount(accountId, session);
    }

    /**
     * Gets the accounts
     *
     * @param session
     * @param secretAware
     * @return
     * @throws OXException
     */
    private List<FileStorageAccount> getAccounts0(Session session, boolean secretAware) throws OXException {
        CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
        if (null == compositeAccountManager) {
            return getAccountManager0(secretAware).getAccounts(session);
        }

        Map<String, FileStorageAccountInfo> accountsMap = new LinkedHashMap<String, FileStorageAccountInfo>(8);
        for (FileStorageAccountManagerProvider provider : compositeAccountManager.providers()) {
            for (FileStorageAccount account : newInstanceFor(provider.getAccountManagerFor(getId())).getAccounts(session)) {
                FileStorageAccountInfo info = new FileStorageAccountInfo(account, provider.getRanking());
                FileStorageAccountInfo prev = accountsMap.get(account.getId());
                if (null == prev || prev.getRanking() < info.getRanking()) {
                    // Replace with current
                    accountsMap.put(account.getId(), info);
                }
            }
        }

        List<FileStorageAccount> ret = new ArrayList<FileStorageAccount>(accountsMap.size());
        for (FileStorageAccountInfo info : accountsMap.values()) {
            ret.add(info.getAccount());
        }

        return ret;
    }

    /**
     * Get the FileStorageAccountManager
     *
     * @return the FileStorageAccountManager
     * @throws OXException
     */
    @SuppressWarnings("null")
    private FileStorageAccountManager getAccountManager0() throws OXException {
        FileStorageAccountManager m = accountManager;
        if (null == m) {
            synchronized (this) {
                m = accountManager;
                if (null == m) {
                    FileStorageAccountManagerLookupService lookupService = services.getService(FileStorageAccountManagerLookupService.class);
                    m = newInstanceFor(lookupService.getAccountManagerFor(getId()));
                    Optional<String> optCap = getCapability();
                    if (optCap.isPresent()) {
                        m = new CapabilityAwareAccountManager(optCap.get(), getId(), services.getServiceSafe(CapabilityService.class), m);
                    }
                    accountManager = m;
                }
            }
        }
        return m;
    }

    /**
     * Checks if the user has the appropriate capability for this file storage
     *
     * @param session The user session
     * @throws OXException in case the user has no capability for this file storage
     */
    protected void checkCapability(Session session) throws OXException {
        Optional<String> optCap = getCapability();
        if (optCap.isPresent()) {
            CapabilityService capabilityService = services.getServiceSafe(CapabilityService.class);
            if (false == capabilityService.getCapabilities(session).contains(optCap.get())) {
                throw WebdavExceptionCodes.MISSING_CAPABILITY.create(getId());
            }
        }
    }

    /**
     * Returns a list of blacklisted hosts (names or addresses) for a session to which a connection is not allowed
     *
     * @param session The session to get the blacklisted hosts for
     * @return A {@link HostList} of blacklisted hosts
     * @throws OXException If the {@link LeanConfigurationService} service is missing
     * @throws IllegalArgumentException If hosts list is invalid, see {@link HostList#valueOf(String)}
     */
    public HostList getBlackListedHosts(Session session) throws OXException {
        final LeanConfigurationService leanConfiguration = services.getServiceSafe(LeanConfigurationService.class);
        String blacklist = leanConfiguration.getProperty(session.getUserId(), session.getContextId(), WebDAVFileStorageProperties.BLACKLISTED_HOSTS);
        return HostList.valueOf(blacklist.trim());
    }

    /**
     * Returns an optional set of allowed ports for a session to which a connection is allowed in case ports are restricted.
     *
     * @param session The session to get the allowed ports for
     * @return An optional set of allowed ports.
     * @throws OXException In case of missing service
     */
    public Optional<Set<Integer>> getAllowedPorts(Session session) throws OXException {
        final LeanConfigurationService leanConfiguration = services.getServiceSafe(LeanConfigurationService.class);
        String portString = leanConfiguration.getProperty(session.getUserId(), session.getContextId(), WebDAVFileStorageProperties.ALLOWED_PORTS);
        return parsePortString(portString);
    }

    /**
     * Parses the given port string which contains the allowed ports as a comma separated list to a {@link Set} of ports.
     *
     * @param portString The comma separated ports
     * @return An optional {@link Set} of ports
     */
    private Optional<Set<Integer>> parsePortString(String portString) {
        if (Strings.isEmpty(portString)) {
            return Optional.empty();
        }
        String[] ports = Strings.splitByComma(portString);
        HashSet<Integer> ret = new HashSet<Integer>(ports.length);
        for (String port : ports) {
            if (Strings.isNotEmpty(port)) {
                try {
                    ret.add(Integer.valueOf(port.trim()));
                } catch (NumberFormatException e) {
                    LOG.error("Ignored unkown port number " + port, e);
                }
            }
        }
        return ret.isEmpty() ? Optional.empty() : Optional.of(ret);
    }

    /**
     * Gets the optional capability for this file store
     *
     * @return The optional capability
     */
    public Optional<String> getCapability() {
        return Optional.empty();
    }

    /**
     * Gets the {@link FileStorageAccountManager}
     *
     * @param secretAware Whether the account manager is secret aware or not
     * @return The {@link FileStorageAccountManager}
     * @throws OXException
     */
    private FileStorageAccountManager getAccountManager0(boolean secretAware) throws OXException {
        if (secretAware) {
            return getAccountManager0();
        }
        FileStorageAccountManagerLookupService lookupService = services.getService(FileStorageAccountManagerLookupService.class);
        return lookupService.getAccountManagerFor(getId());
    }

}
