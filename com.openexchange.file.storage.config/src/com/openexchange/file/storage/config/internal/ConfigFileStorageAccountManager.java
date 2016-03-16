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

package com.openexchange.file.storage.config.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.config.ConfigFileStorageAccount;
import com.openexchange.file.storage.config.ConfigFileStorageAuthenticator;
import com.openexchange.session.Session;

/**
 * {@link ConfigFileStorageAccountManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class ConfigFileStorageAccountManager implements FileStorageAccountManager {

    /**
     * The identifier of associated file storage service.
     */
    private final String serviceId;

    /**
     * The file storage service.
     */
    private final FileStorageService service;

    /**
     * The authenticators' map.
     */
    private final ConcurrentMap<ConfigFileStorageAuthenticator, ConfigFileStorageAuthenticator> authenticators;

    /**
     * Initializes a new {@link ConfigFileStorageAccountManager}.
     */
    public ConfigFileStorageAccountManager(final FileStorageService service) {
        super();
        serviceId = service.getId();
        this.service = service;
        for (final ConfigFileStorageAccountImpl account : ConfigFileStorageAccountParser.getInstance().getAccountsFor(serviceId).values()) {
            account.setFileStorageService(service);
        }
        authenticators = ConfigFileStorageAccountParser.getInstance().getAuthenticators();
    }

    @Override
    public String addAccount(final FileStorageAccount account, final Session session) throws OXException {
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(serviceId);
    }

    @Override
    public void updateAccount(final FileStorageAccount account, final Session session) throws OXException {
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(serviceId);
    }

    @Override
    public void deleteAccount(final FileStorageAccount account, final Session session) throws OXException {
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(serviceId);
    }

    @Override
    public List<FileStorageAccount> getAccounts(final Session session) throws OXException {
        final Map<String, ConfigFileStorageAccountImpl> accounts = ConfigFileStorageAccountParser.getInstance().getAccountsFor(serviceId);
        if (null == accounts || accounts.isEmpty()) {
            return Collections.<FileStorageAccount> emptyList();
        }
        final List<FileStorageAccount> ret = new ArrayList<FileStorageAccount>(accounts.size());
        for (final ConfigFileStorageAccountImpl account : accounts.values()) {
            ret.add(cloneAndApplyService(account, session));
        }
        return ret;
    }

    @Override
    public FileStorageAccount getAccount(final String id, final Session session) throws OXException {
        final Map<String, ConfigFileStorageAccountImpl> accounts = ConfigFileStorageAccountParser.getInstance().getAccountsFor(serviceId);
        if (null == accounts) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(
                id,
                serviceId,
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }
        final ConfigFileStorageAccountImpl account = accounts.get(id);
        if (null == account) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(
                id,
                serviceId,
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }
        return cloneAndApplyService(account, session);
    }

    private ConfigFileStorageAccount cloneAndApplyService(final ConfigFileStorageAccountImpl account, final Session session) throws OXException {
        final ConfigFileStorageAccountImpl ret = (ConfigFileStorageAccountImpl) account.clone();
        ret.setFileStorageService(service);
        /*-
         * Set login/password if authenticator is absent
         *
         * Check for an appropriate authenticator
         */
        final ConfigFileStorageAuthenticator authenticator = getAuthenticator(serviceId);
        if (null == authenticator) {
            // Set login/password obtained from session
            final Map<String, Object> configuration = ret.getConfiguration();
            {
                final String tmp = (String) configuration.get(CONF_PROPERTY_LOGIN);
                if (null == tmp) {
                    configuration.put(CONF_PROPERTY_LOGIN, session.getLogin());
                }
            }
            {
                final String tmp = (String) configuration.get(CONF_PROPERTY_PASSWORD);
                if (null == tmp) {
                    configuration.put(CONF_PROPERTY_PASSWORD, session.getPassword());
                }
            }
        } else {
            // Set login/password through authenticator
            authenticator.setAuthenticationProperties(ret, session);
        }
        return ret;
    }

    /**
     * Gets the appropriate and highest-ranked authenticator for given service identifier.
     *
     * @param serviceId The service identifier
     * @return The appropriate and highest-ranked authenticator or <code>null</code> if none available
     */
    private ConfigFileStorageAuthenticator getAuthenticator(final String serviceId) {
        ConfigFileStorageAuthenticator candidate = null;
        for (final ConfigFileStorageAuthenticator authenticator : authenticators.keySet()) {
            if (authenticator.handles(serviceId) && ((null == candidate) || (candidate.getRanking() < authenticator.getRanking()))) {
                candidate = authenticator;
            }
        }
        return candidate;
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) {
        // Nothing to do
    }

    @Override
    public void cleanUp(final String secret, final Session session) throws OXException {
        // Nothing to do
    }
    
    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean hasEncryptedItems(final Session session) throws OXException {
        return false;
    }

}
