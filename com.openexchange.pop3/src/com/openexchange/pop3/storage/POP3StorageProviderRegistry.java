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

package com.openexchange.pop3.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link POP3StorageProviderRegistry} - The registry for {@link POP3StorageProvider POP3 storage providers}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3StorageProviderRegistry {

    private static final POP3StorageProviderRegistry instance = new POP3StorageProviderRegistry();

    /**
     * Gets the POP3 storage provider registry.
     *
     * @return The POP3 storage provider registry
     */
    public static POP3StorageProviderRegistry getInstance() {
        return instance;
    }

    /*-
     * Member section
     */

    private final ConcurrentMap<String, POP3StorageProvider> registryMap;

    /**
     * Initializes a new {@link POP3StorageProviderRegistry}.
     */
    private POP3StorageProviderRegistry() {
        super();
        registryMap = new ConcurrentHashMap<String, POP3StorageProvider>();
    }

    /**
     * Gets the provider from this registry which is bound to specified provider name.
     *
     * @param providerName The provider name
     * @return The provider bound to specified provider name or <code>null</code> if none found
     */
    public POP3StorageProvider getPOP3StorageProvider(final String providerName) {
        return registryMap.get(providerName);
    }

    /**
     * Adds given provider to this registry bound to name obtained by {@link POP3StorageProvider#getPOP3StorageName()}.
     *
     * @param provider The provider
     * @return <code>true</code> if provider could be successfully added; otherwise <code>false</code>
     */
    public boolean addPOP3StorageProvider(final POP3StorageProvider provider) {
        return addPOP3StorageProvider(provider.getPOP3StorageName(), provider);
    }

    /**
     * Adds given provider to this registry bound to specified provider name.
     *
     * @param providerName The provider name
     * @param provider The provider
     * @return <code>true</code> if provider could be successfully added; otherwise <code>false</code>
     */
    public boolean addPOP3StorageProvider(final String providerName, final POP3StorageProvider provider) {
        return (null == registryMap.putIfAbsent(providerName, provider));
    }

    /**
     * Removes the provider from this registry which is bound to specified provider name.
     *
     * @param providerName The provider name
     * @return The removed provider or <code>null</code> if none removed
     */
    public POP3StorageProvider removePOP3StorageProvider(final String providerName) {
        return registryMap.remove(providerName);
    }

    /**
     * Clears this registry.
     */
    public void clear() {
        registryMap.clear();
    }
}
