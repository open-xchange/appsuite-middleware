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

package com.openexchange.file.storage.oauth;

import static com.openexchange.file.storage.SecretAwareFileStorageAccountManager.newInstanceFor;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountDeleteListener;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.oauth.API;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractOAuthFileStorageService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractOAuthFileStorageService implements AccountAware, OAuthAccountDeleteListener, FileStorageAccountDeleteListener {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOAuthFileStorageService.class);

    private final DynamicFormDescription formDescription;
    private final ServiceLookup services;
    private final API api;
    private final String serviceId;
    private final String displayName;

    private volatile FileStorageAccountManager accountManager;
    private volatile CompositeFileStorageAccountManagerProvider compositeAccountManager;

    /**
     * Initializes a new {@link AbstractOAuthFileStorageService}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param api The {@link KnownApi}
     * @param displayName The display name of the service
     * @param serviceId The service identifier
     * @param compositeFileStorageAccountManagerProvider The {@link CompositeFileStorageAccountManagerProvider}
     */
    protected AbstractOAuthFileStorageService(ServiceLookup services, API api, String displayName, String serviceId, CompositeFileStorageAccountManagerProvider compositeFileStorageAccountManagerProvider) {
        this(services, api, displayName, serviceId);
        compositeAccountManager = compositeFileStorageAccountManagerProvider;
    }

    /**
     * Initializes a new {@link AbstractOAuthFileStorageService}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param api The {@link KnownApi}
     * @param displayName The display name of the service
     * @param serviceId The service identifier
     */
    protected AbstractOAuthFileStorageService(ServiceLookup services, API api, String displayName, String serviceId) {
        super();
        this.services = services;
        this.api = api;
        this.displayName = displayName;
        this.serviceId = serviceId;

        final DynamicFormDescription tmpDescription = new DynamicFormDescription();
        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", api.getServiceId());
        tmpDescription.add(oauthAccount);
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
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
    public void onBeforeOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        // Nothing
    }

    @Override
    public void onAfterOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        Integer iUserId = Integer.valueOf(user);
        Integer iContextId = Integer.valueOf(cid);
        try {
            List<FileStorageAccount> toDelete = new LinkedList<FileStorageAccount>();
            FakeSession session = new FakeSession(null, user, cid);
            for (FileStorageAccount account : getAccounts0(session, false)) {
                Object obj = account.getConfiguration().get("account");
                if (null != obj && Integer.toString(id).equals(obj.toString())) {
                    toDelete.add(account);
                }
            }

            // Acquire account manager
            FileStorageAccountManager accountManager = getAccountManager();
            OAuthAccessRegistryService registryService = services.getService(OAuthAccessRegistryService.class);
            OAuthAccessRegistry registry = registryService.get(api.getServiceId());

            // Pass the connection and the hint about the scopes to the FileStorageAccountManager
            // as a session parameter.
            session.setParameter(OAuthConstants.SESSION_PARAM_UPDATE_SCOPES, eventProps.get(OAuthConstants.SESSION_PARAM_UPDATE_SCOPES));
            session.setParameter("__file.storage.delete.connection", con);
            try {
                for (FileStorageAccount deleteMe : toDelete) {
                    accountManager.deleteAccount(deleteMe, session);
                    LOG.info("Deleted {} file storage account with id {} as OAuth account {} was deleted for user {} in context {}", deleteMe.getId(), api.getDisplayName(), deleteMe.getId(), iUserId, iContextId);
                    boolean purged = registry.purgeUserAccess(session.getContextId(), session.getUserId(), id);
                    if (purged) {
                        LOG.info("Removed {} OAuth accesses from registry for the deleted OAuth account with id '{}' for user '{}' in context '{}'", api.getDisplayName(), deleteMe.getId(), iUserId, iContextId);
                    }
                }
            } finally {
                session.setParameter("__file.storage.delete.connection", null);
                session.setParameter(OAuthConstants.SESSION_PARAM_UPDATE_SCOPES, null);
            }
        } catch (Exception e) {
            LOG.warn("Could not delete possibly existing {} accounts associated with deleted OAuth account {} for user {} in context {}", api.getDisplayName(), Integer.valueOf(id), iUserId, iContextId, e);
        }
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return getAccounts0(session, true);
    }

    @Override
    public void onBeforeFileStorageAccountDeletion(Session session, int id, Map<String, Object> eventProps, Connection con) throws OXException {
        // nothing to do

    }

    @Override
    public void onAfterFileStorageAccountDeletion(Session session, int id, Map<String, Object> eventProps, Connection con) throws OXException {
        try {
            getAccountManager().getAccount(Integer.toString(id), session);
        } catch (OXException e) {
            if (e.equalsCode(4, "FILE_STORAGE")) {
                // E.g. "File storage account 31 of service "xyz" could not be found for user 3 in context 1"
                return;
            }

            LOG.warn("Failed to load file storage account {} with service \"{}\".", Integer.valueOf(id), serviceId, e);
            return;
        }

        if (!updateScopes(session)) {
            return;
        }

        // Retrieve the OAuth account id that is linked with the file storage account that was deleted.
        Object value = eventProps.get("account");
        if (value == null) {
            LOG.debug("No OAuth account information was found");
            return;
        }

        int accountId;
        try {
            accountId = Integer.parseInt((String) value);
        } catch (NumberFormatException e) {
            LOG.debug("Not OAuth account information was found");
            return;
        }

        session.setParameter("__file.storage.delete.connection", con);
        try {
            OAuthService storage = services.getService(OAuthService.class);
            if (storage == null) {
                // Missing OAuth service
                return;
            }
            OAuthAccount account;
            try {
                account = storage.getAccount(session, accountId);
            } catch (OXException e) {
                if (OAuthExceptionCodes.ACCOUNT_NOT_FOUND.equals(e)) {
                    LOG.debug("The OAuth file storage account with id '{}' for the user '{}' in context '{}' does not exist anymore", I(accountId), I(session.getUserId()), I(session.getContextId()));
                    return;
                }
                throw e;
            }
            if (!account.getMetaData().getAPI().equals(this.api)) {
                // File storage account does not belong to this
                return;
            }
            // Get the enabled scopes...
            Set<OAuthScope> scopes = new HashSet<>();
            for (OAuthScope scope : account.getEnabledScopes()) {
                scopes.add(scope);
            }
            // ...and remove the 'drive' scope.
            scopes.remove(getScope());
            if (scopes.size() == 0) {
                storage.deleteAccount(session, accountId);
                return;
            }
            eventProps.put(OAuthConstants.ARGUMENT_SCOPES, scopes);
            // Update the account
            storage.updateAccount(session, accountId, eventProps);
        } finally {
            session.setParameter("__file.storage.delete.connection", null);
        }
    }

    /**
     * Fetches the optional updateScopes session parameter and evaluates it's value
     *
     * @param session the session
     * @return The value of the optional 'updateScopes' session parameter. Returns <code>true</code> as fall-back.
     */
    private boolean updateScopes(Session session) {
        Object updateScopesValue = session.getParameter(OAuthConstants.SESSION_PARAM_UPDATE_SCOPES);
        if (updateScopesValue == null) {
            return true;
        }
        if (updateScopesValue instanceof Boolean) {
            return ((Boolean) updateScopesValue).booleanValue();
        }
        if (updateScopesValue instanceof String) {
            return Boolean.parseBoolean((String) updateScopesValue);
        }
        return true;
    }

    /**
     * Returns the {@link CompositeFileStorageAccountManagerProvider}
     *
     * @return the {@link CompositeFileStorageAccountManagerProvider}
     */
    public CompositeFileStorageAccountManagerProvider getCompositeAccountManager() {
        return compositeAccountManager;
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
    private FileStorageAccountManager getAccountManager0() throws OXException {
        FileStorageAccountManager m = accountManager;
        if (null == m) {
            synchronized (this) {
                m = accountManager;
                if (null == m) {
                    FileStorageAccountManagerLookupService lookupService = services.getService(FileStorageAccountManagerLookupService.class);
                    m = newInstanceFor(lookupService.getAccountManagerFor(getId()));
                    accountManager = m;
                }
            }
        }
        return m;
    }

    /**
     *
     * @param secretAware
     * @return
     * @throws OXException
     */
    private FileStorageAccountManager getAccountManager0(boolean secretAware) throws OXException {
        if (secretAware) {
            return getAccountManager0();
        }
        FileStorageAccountManagerLookupService lookupService = services.getService(FileStorageAccountManagerLookupService.class);
        return lookupService.getAccountManagerFor(getId());
    }

    /**
     * Returns the {@link OAuthScope} for this provider
     *
     * @return the {@link OAuthScope} for this provider
     */
    protected abstract OAuthScope getScope();
}
