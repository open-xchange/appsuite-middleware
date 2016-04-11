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
