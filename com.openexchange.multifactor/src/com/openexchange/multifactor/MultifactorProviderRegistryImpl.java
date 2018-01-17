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

package com.openexchange.multifactor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link MultifactorAuthenticationProviderRegistryImpl}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorProviderRegistryImpl implements MultifactorProviderRegistry{

    private final ConcurrentHashMap<String /* key: provider-name */ , MultifactorProvider> providers = new ConcurrentHashMap<>();

    @Override
    public void registerProvider(MultifactorProvider provider) {
        providers.put(provider.getName(), provider);
    }

    @Override
    public void unRegisterProvider(MultifactorProvider provider) {
        providers.remove(provider.getName());
    }

    @Override
    public Optional<MultifactorProvider> getProvider(String name) {
        return Optional.ofNullable(providers.get(name));
    }

    @Override
    public Collection<MultifactorProvider> getProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    @Override
    public Collection<MultifactorProvider> getProviders(MultifactorRequest multifactorRequest) {
        ArrayList<MultifactorProvider> ret = new ArrayList<MultifactorProvider>();
        Iterator<MultifactorProvider> iterator = getProviders().iterator();
        while (iterator.hasNext()) {
            final MultifactorProvider provider = iterator.next();
            if (provider.isEnabled(multifactorRequest)) {
                ret.add(provider);
            }
        }
        return Collections.unmodifiableCollection(ret);
    }

    @Override
    public Collection<MultifactorProvider> getProviders(MultifactorRequest multifactorRequest, String[] nameFilters) {
        return (nameFilters == null || nameFilters.length == 0) ?
            getProviders(multifactorRequest) :
            getProviders(multifactorRequest).stream().filter(p -> Arrays.contains(nameFilters,p.getName())).collect(Collectors.toList());
    }
}
