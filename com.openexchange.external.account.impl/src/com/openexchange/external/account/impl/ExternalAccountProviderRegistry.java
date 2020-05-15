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

package com.openexchange.external.account.impl;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.ExternalAccountExceptionCodes;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;

/**
 * {@link ExternalAccountProviderRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class ExternalAccountProviderRegistry implements ServiceTrackerCustomizer<ExternalAccountProvider, ExternalAccountProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalAccountProviderRegistry.class);

    private final ConcurrentMap<ExternalAccountModule, ExternalAccountProvider> registry;
    private final BundleContext context;

    /**
     * Initializes a new {@link ExternalAccountProviderRegistry}.
     *
     * @param context The {@link BundleContext}
     */
    public ExternalAccountProviderRegistry(BundleContext context) {
        super();
        this.context = context;
        registry = new ConcurrentHashMap<>();
    }

    /**
     * Gets the external account provider for given module.
     *
     * @param module The module
     * @return The appropriate provider
     * @throws OXException If no such provider is available
     */
    public ExternalAccountProvider getProviderFor(ExternalAccountModule module) throws OXException {
        Optional<ExternalAccountProvider> optionalAccountProvider = optProviderFor(module);
        if (!optionalAccountProvider.isPresent()) {
            throw ExternalAccountExceptionCodes.PROVIDER_NOT_FOUND.create(module);
        }
        return optionalAccountProvider.get();
    }

    /**
     * Gets the external account provider for given module.
     *
     * @param module The module
     * @return The optional provider
     */
    public Optional<ExternalAccountProvider> optProviderFor(ExternalAccountModule module) {
        return module == null ? Optional.empty() : Optional.ofNullable(registry.get(module));
    }

    @Override
    public ExternalAccountProvider addingService(ServiceReference<ExternalAccountProvider> reference) {
        ExternalAccountProvider provider = context.getService(reference);
        if (registry.putIfAbsent(provider.getModule(), provider) == null) {
            LOG.info("Added external account provider with id '{}'", provider.getModule());
            return provider;
        }

        LOG.warn("There is already another provider registered with id '{}'", provider.getModule());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ExternalAccountProvider> reference, ExternalAccountProvider service) {
        // nope
    }

    @Override
    public void removedService(ServiceReference<ExternalAccountProvider> reference, ExternalAccountProvider provider) {
        ExternalAccountProvider removed = registry.remove(provider.getModule());
        if (removed != null) {
            context.ungetService(reference);
        }
    }
}
