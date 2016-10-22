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

package com.openexchange.mail.oauth.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.mail.oauth.MailOAuthProvider;

/**
 * {@link MailOAuthProviderRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailOAuthProviderRegistry {

    private final ConcurrentMap<String, MailOAuthProvider> providers;

    /**
     * Initializes a new {@link MailOAuthProviderRegistry}.
     */
    public MailOAuthProviderRegistry() {
        super();
        providers = new ConcurrentHashMap<>(8, 9.0F, 1);
    }

    /**
     * Gets the providers currently held in this registry.
     *
     * @return The available providers
     */
    public Collection<MailOAuthProvider> getProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    /**
     * Gets the provider for given identifier.
     *
     * @param provderId The provider identifier
     * @return The associated provider instance or <code>null</code>
     */
    public MailOAuthProvider getProviderFor(String provderId) {
        return providers.get(provderId);
    }

    /**
     * Adds given provider to this registry.
     *
     * @param provider The provider to add
     * @return <code>true</code> if provider was successfully added; otherwise <code>false</code>
     */
    public boolean addProvider(MailOAuthProvider provider) {
        return null == provider ? false : null == providers.putIfAbsent(provider.getProviderId(), provider);
    }

    /**
     * Removes given provider from this registry.
     *
     * @param provider The provider to remove
     * @return <code>true</code> if provider was successfully removed; otherwise <code>false</code>
     */
    public boolean removeProvider(MailOAuthProvider provider) {
        return null == provider ? false : null != providers.remove(provider.getProviderId());
    }

}
