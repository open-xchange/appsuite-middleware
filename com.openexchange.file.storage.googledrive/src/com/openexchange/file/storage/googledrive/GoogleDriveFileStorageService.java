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

package com.openexchange.file.storage.googledrive;

import static com.openexchange.file.storage.SecretAwareFileStorageAccountManager.newInstanceFor;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.googledrive.osgi.Services;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.session.Session;

/**
 * {@link GoogleDriveFileStorageService} - The Google Drive file storage service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveFileStorageService implements AccountAware, OAuthAccountDeleteListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GoogleDriveFileStorageService.class);

    private static final String SERVICE_ID = GoogleDriveConstants.ID;

    /**
     * Creates a new Google Drive file storage service.
     *
     * @return A new Google Drive file storage service
     */
    public static GoogleDriveFileStorageService newInstance() {
        return new GoogleDriveFileStorageService();
    }

    /**
     * Creates a new Google Drive file storage service.
     *
     * @param compositeAccountManager The composite account manager
     * @return A new Google Drive file storage service
     */
    public static GoogleDriveFileStorageService newInstance(final CompositeFileStorageAccountManagerProvider compositeAccountManager) {
        final GoogleDriveFileStorageService newInst = new GoogleDriveFileStorageService();
        newInst.applyCompositeAccountManager(compositeAccountManager);
        return newInst;
    }

    /**
     * The attribute expiration in millis.
     */
    public static final int DEFAULT_ATTR_EXPIRATION_PERIOD = 300000;

    private final DynamicFormDescription formDescription;
    private volatile FileStorageAccountManager accountManager;
    private volatile CompositeFileStorageAccountManagerProvider compositeAccountManager;

    /**
     * Initializes a new {@link GoogleDriveFileStorageService}.
     */
    private GoogleDriveFileStorageService() {
        super();
        final DynamicFormDescription tmpDescription = new DynamicFormDescription();
        /*
         * API & secret key
         */
        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", "com.openexchange.oauth.google");
        tmpDescription.add(oauthAccount);
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
    }

    private FileStorageAccountManager getAccountManager0() throws OXException {
        FileStorageAccountManager m = accountManager;
        if (null == m) {
            synchronized (this) {
                m = accountManager;
                if (null == m) {
                    final FileStorageAccountManagerLookupService lookupService = Services.getService(FileStorageAccountManagerLookupService.class);
                    m = newInstanceFor(lookupService.getAccountManagerFor(SERVICE_ID));
                    accountManager = m;
                }
            }
        }
        return m;
    }

    private FileStorageAccountManager getAccountManager0(boolean secretAware) throws OXException {
        if (secretAware) {
            return getAccountManager0();
        }
        FileStorageAccountManagerLookupService lookupService = Services.getService(FileStorageAccountManagerLookupService.class);
        return lookupService.getAccountManagerFor(SERVICE_ID);
    }

    private void applyCompositeAccountManager(final CompositeFileStorageAccountManagerProvider compositeAccountManager) {
        this.compositeAccountManager = compositeAccountManager;
    }

    /**
     * Gets the composite account manager.
     *
     * @return The composite account manager
     */
    public CompositeFileStorageAccountManagerProvider getCompositeAccountManager() {
        return compositeAccountManager;
    }

    // --------------------------------------------------------------------------------------------------------------------------------- //

    @Override
    public void onBeforeOAuthAccountDeletion(int oauthAccountId, Map<String, Object> eventProps, int user, int cid, Connection con) {
        // Nothing
    }

    @Override
    public void onAfterOAuthAccountDeletion(int oauthAccountId, Map<String, Object> eventProps, int user, int cid, Connection con) {
        try {
            List<FileStorageAccount> toDelete = new LinkedList<FileStorageAccount>();
            FakeSession session = new FakeSession(null, user, cid);
            for (FileStorageAccount account : getAccounts0(session, false)) {
                Object obj = account.getConfiguration().get("account");
                if (null != obj && Integer.toString(oauthAccountId).equals(obj.toString())) {
                    toDelete.add(account);
                }
            }

            // Acquire account manager
            FileStorageAccountManager accountManager = getAccountManager();
            OAuthAccessRegistryService registryService = Services.getService(OAuthAccessRegistryService.class);
            OAuthAccessRegistry registry = registryService.get(API.GOOGLE.getFullName());
            for (FileStorageAccount deleteMe : toDelete) {
                accountManager.deleteAccount(deleteMe, session);
                LOG.info("Deleted Google Drive file storage account with ID {} as OAuth account {} was deleted for user {} in context {}", deleteMe.getId(), oauthAccountId, user, cid);
                boolean purged = registry.purgeUserAccess(session.getContextId(), session.getUserId());
                if (purged) {
                    LOG.info("Removed Google Drive OAuth accesses from registry for the deleted OAuth account with id '{}' for user '{}' in context '{}'", deleteMe.getId(), user, cid);
                }
            }

        } catch (Exception e) {
            LOG.warn("Could not delete possibly existing Google Drive accounts associated with deleted OAuth account {} for user {} in context {}", oauthAccountId, user, cid, e);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------- //

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getDisplayName() {
        return "Google Drive File Storage Service";
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    private static final class FileStorageAccountInfo {

        protected final FileStorageAccount account;
        protected final int ranking;

        protected FileStorageAccountInfo(FileStorageAccount account, int ranking) {
            super();
            this.account = account;
            this.ranking = ranking;
        }
    }

    /**
     * Gets all service's accounts associated with session user.
     *
     * @param session The session providing needed user data
     * @return All accounts associated with session user.
     * @throws OXException If listing fails
     */
    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return getAccounts0(session, true);
    }

    private List<FileStorageAccount> getAccounts0(Session session, boolean secretAware) throws OXException {
        CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
        if (null == compositeAccountManager) {
            return getAccountManager0(secretAware).getAccounts(session);
        }

        Map<String, FileStorageAccountInfo> accountsMap = new LinkedHashMap<String, FileStorageAccountInfo>(8);
        for (FileStorageAccountManagerProvider provider : compositeAccountManager.providers()) {
            for (FileStorageAccount account : newInstanceFor(provider.getAccountManagerFor(SERVICE_ID)).getAccounts(session)) {
                FileStorageAccountInfo info = new FileStorageAccountInfo(account, provider.getRanking());
                FileStorageAccountInfo prev = accountsMap.get(account.getId());
                if (null == prev || prev.ranking < info.ranking) {
                    // Replace with current
                    accountsMap.put(account.getId(), info);
                }
            }
        }
        List<FileStorageAccount> ret = new ArrayList<FileStorageAccount>(accountsMap.size());
        for (FileStorageAccountInfo info : accountsMap.values()) {
            ret.add(info.account);
        }
        return ret;
    }

    @Override
    public FileStorageAccountManager getAccountManager() throws OXException {
        final CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
        if (null == compositeAccountManager) {
            return getAccountManager0();
        }
        try {
            return newInstanceFor(compositeAccountManager.getAccountManagerFor(SERVICE_ID));
        } catch (final OXException e) {
            LOG.warn("", e);
            return getAccountManager0();
        }
    }

    @Override
    public FileStorageAccountAccess getAccountAccess(final String accountId, final Session session) throws OXException {
        final FileStorageAccount account;
        {
            final CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
            if (null == compositeAccountManager) {
                account = getAccountManager0().getAccount(accountId, session);
            } else {
                account = compositeAccountManager.getAccountManager(accountId, session).getAccount(accountId, session);
            }
        }
        return new GoogleDriveAccountAccess(this, account, session);
    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    private static final class FakeSession implements Session, Serializable {

        private static final long serialVersionUID = -4527564586038651789L;

        private final String password;
        private final int userId;
        private final int contextId;
        private final ConcurrentMap<String, Object> parameters;

        FakeSession(final String password, final int userId, final int contextId) {
            super();
            this.password = password;
            this.userId = userId;
            this.contextId = contextId;
            parameters = new ConcurrentHashMap<String, Object>(8, 0.9f, 1);
        }

        @Override
        public int getContextId() {
            return contextId;
        }

        @Override
        public String getLocalIp() {
            return null;
        }

        @Override
        public void setLocalIp(final String ip) {
            // Nothing to do
        }

        @Override
        public String getLoginName() {
            return null;
        }

        @Override
        public boolean containsParameter(final String name) {
            return parameters.containsKey(name);
        }

        @Override
        public Object getParameter(final String name) {
            return parameters.get(name);
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getRandomToken() {
            return null;
        }

        @Override
        public String getSecret() {
            return null;
        }

        @Override
        public String getSessionID() {
            return null;
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public String getUserlogin() {
            return null;
        }

        @Override
        public String getLogin() {
            return null;
        }

        @Override
        public void setParameter(final String name, final Object value) {
            if (null == value) {
                parameters.remove(name);
            } else {
                parameters.put(name, value);
            }
        }

        @Override
        public Set<String> getParameterNames() {
            return parameters.keySet();
        }

        @Override
        public String getAuthId() {
            return null;
        }

        @Override
        public String getHash() {
            return null;
        }

        @Override
        public void setHash(final String hash) {
            // Nope
        }

        @Override
        public String getClient() {
            return null;
        }

        @Override
        public void setClient(final String client) {
            // Nothing to do
        }

        @Override
        public boolean isTransient() {
            return false;
        }
    }

}
