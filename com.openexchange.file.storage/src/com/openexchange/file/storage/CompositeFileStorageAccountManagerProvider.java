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

package com.openexchange.file.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link CompositeFileStorageAccountManagerProvider} - Wrapping multiple providers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CompositeFileStorageAccountManagerProvider implements FileStorageAccountManagerProvider {

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<FileStorageAccountManagerProvider, Object> set;
    private volatile int ranking;

    /**
     * Initializes a new {@link CompositeFileStorageAccountManagerProvider}.
     */
    public CompositeFileStorageAccountManagerProvider() {
        super();
        set = new ConcurrentHashMap<FileStorageAccountManagerProvider, Object>(4, 0.9f, 1);
    }

    /**
     * Checks if this composite {@link FileStorageAccountManagerProvider provider} has any providers assigned.
     *
     * @return <code>true</code> if there are any providers; otherwise <code>false</code>
     */
    public boolean hasAnyProvider() {
        return !set.isEmpty();
    }

    /**
     * Gets a collection of known providers.
     *
     * @return The providers
     */
    public Collection<FileStorageAccountManagerProvider> providers() {
        return Collections.unmodifiableCollection(set.keySet());
    }

    /**
     * Adds specified provider.
     *
     * @param provider The provider
     * @return <code>true</code> if added; otherwise <code>false</code>
     */
    public synchronized boolean addProvider(final FileStorageAccountManagerProvider provider) {
        if (null == provider) {
            return false;
        }
        if (null != set.putIfAbsent(provider, PRESENT)) {
            return false;
        }
        this.ranking = Math.max(this.ranking, provider.getRanking());
        return true;
    }

    /**
     * Removes specified provider
     *
     * @param provider The provider
     */
    public synchronized void removeProvider(final FileStorageAccountManagerProvider provider) {
        if (null == provider) {
            return;
        }
        if (null != set.remove(provider)) {
            int ranking = DEFAULT_RANKING;
            for (final FileStorageAccountManagerProvider p : set.keySet()) {
                final int otherRanking = p.getRanking();
                if (otherRanking > ranking) {
                    ranking = otherRanking;
                }
            }
            this.ranking = ranking;
        }
    }

    @Override
    public boolean supports(final String serviceId) {
        for (final FileStorageAccountManagerProvider provider : set.keySet()) {
            if (provider.supports(serviceId)) {
                return true;
            }
        }
        return false;
    }

    private static final String PARAM_DEFAULT_ACCOUNT = "file.storage.compositeAccount";

    @Override
    public FileStorageAccountManager getAccountManager(final String accountId, final Session session) throws OXException {
        final String paramName = new StringBuilder(PARAM_DEFAULT_ACCOUNT).append('@').append(accountId).toString();
        FileStorageAccountManager accountManager = (FileStorageAccountManager) session.getParameter(paramName);
        if (null == accountManager) {
            FileStorageAccountManagerProvider candidate = null;
            for (final FileStorageAccountManagerProvider provider : set.keySet()) {
                if ((null == candidate) || (provider.getRanking() > candidate.getRanking())) {
                    final FileStorageAccountManager cAccountManager = provider.getAccountManager(accountId, session);
                    if (null != cAccountManager) {
                        candidate = provider;
                        accountManager = cAccountManager;
                    }
                }
            }
            if (null == accountManager) {
                return null;
            }
            session.setParameter(paramName, accountManager);
        }
        return accountManager;
    }

    @Override
    public FileStorageAccountManager getAccountManagerFor(final String serviceId) throws OXException {
        FileStorageAccountManagerProvider candidate = null;
        for (final FileStorageAccountManagerProvider provider : set.keySet()) {
            if (provider.supports(serviceId) && (null == candidate || candidate.getRanking() < provider.getRanking())) {
                candidate = provider;
            }
        }
        if (null == candidate) {
            throw FileStorageExceptionCodes.NO_ACCOUNT_MANAGER_FOR_SERVICE.create(serviceId);
        }
        return candidate.getAccountManagerFor(serviceId);
    }

    @Override
    public int getRanking() {
        return ranking;
    }

}
