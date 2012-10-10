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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.cmis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
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
import com.openexchange.session.Session;

/**
 * {@link CMISFileStorageService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CMISFileStorageService implements AccountAware {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CMISFileStorageService.class);

    /**
     * Creates a new CMIS file storage service.
     * 
     * @return A new CMIS file storage service
     * @throws OXException If creation fails
     */
    public static CMISFileStorageService newInstance() throws OXException {
        final CMISFileStorageService newInst = new CMISFileStorageService();
        newInst.applyAccountManager(CMISServices.getService(FileStorageAccountManagerLookupService.class));
        return newInst;
    }

    /**
     * Creates a new CMIS file storage service.
     * 
     * @param compositeAccountManager The composite account manager
     * @return A new CMIS file storage service
     */
    public static CMISFileStorageService newInstance(final CompositeFileStorageAccountManagerProvider compositeAccountManager) {
        final CMISFileStorageService newInst = new CMISFileStorageService();
        newInst.applyCompositeAccountManager(compositeAccountManager);
        return newInst;
    }

    private final DynamicFormDescription formDescription;
    private final Set<String> secretProperties;
    private volatile FileStorageAccountManager accountManager;
    private volatile CompositeFileStorageAccountManagerProvider compositeAccountManager;

    /**
     * Initializes a new {@link CMISFileStorageService}.
     */
    private CMISFileStorageService() {
        super();
        final DynamicFormDescription tmpDescription = new DynamicFormDescription();
        tmpDescription.add(FormElement.input(CMISConstants.CMIS_LOGIN, FormStrings.FORM_LABEL_LOGIN, true, ""));
        tmpDescription.add(FormElement.password(CMISConstants.CMIS_PASSWORD, FormStrings.FORM_LABEL_PASSWORD, true, ""));
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
        secretProperties = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(CMISConstants.CMIS_PASSWORD)));
    }

    private void applyAccountManager(final FileStorageAccountManagerLookupService accountManagerLookupService) throws OXException {
        accountManager = accountManagerLookupService.getAccountManagerFor(this);
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

    @Override
    public String getId() {
        return CMISConstants.ID;
    }

    @Override
    public String getDisplayName() {
        return "CMIS File Storage Service";
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public Set<String> getSecretProperties() {
        return secretProperties;
    }

    @Override
    public FileStorageAccountManager getAccountManager() {
        final CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
        if (null == compositeAccountManager) {
            return accountManager;
        }
        try {
            return compositeAccountManager.getAccountManagerFor(this);
        } catch (final OXException e) {
            LOG.warn(e.getMessage(), e);
            return accountManager;
        }
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
    public List<FileStorageAccount> getAccounts(final Session session) throws OXException {
        final CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
        if (null == compositeAccountManager) {
            return accountManager.getAccounts(session);
        }
        final Map<String, FileStorageAccountInfo> accountsMap = new LinkedHashMap<String, FileStorageAccountInfo>(8);
        for (final FileStorageAccountManagerProvider provider : compositeAccountManager.providers()) {
            for (final FileStorageAccount account : provider.getAccountManagerFor(this).getAccounts(session)) {
                final FileStorageAccountInfo info = new FileStorageAccountInfo(account, provider.getRanking());
                final FileStorageAccountInfo prev = accountsMap.get(account.getId());
                if (null == prev || prev.ranking < info.ranking) {
                    // Replace with current
                    accountsMap.put(account.getId(), info);
                }
            }
        }
        final List<FileStorageAccount> ret = new ArrayList<FileStorageAccount>(accountsMap.size());
        for (final FileStorageAccountInfo info : accountsMap.values()) {
            ret.add(info.account);
        }
        return ret;
    }

    @Override
    public FileStorageAccountAccess getAccountAccess(final String accountId, final Session session) throws OXException {
        final FileStorageAccount account;
        {
            final CompositeFileStorageAccountManagerProvider compositeAccountManager = this.compositeAccountManager;
            if (null == compositeAccountManager) {
                account = accountManager.getAccount(accountId, session);
            } else {
                account = compositeAccountManager.getAccountManager(accountId, session).getAccount(accountId, session);
            }
        }
        return new CMISAccountAccess(this, account, session);
    }

}
