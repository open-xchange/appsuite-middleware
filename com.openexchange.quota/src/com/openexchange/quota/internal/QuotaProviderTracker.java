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

package com.openexchange.quota.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaService;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class QuotaProviderTracker implements QuotaService, ServiceTrackerCustomizer<QuotaProvider, QuotaProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(QuotaProviderTracker.class);

    private final ConcurrentMap<String, QuotaProvider> providers = new ConcurrentHashMap<String, QuotaProvider>();

    private final BundleContext context;

    public QuotaProviderTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public List<QuotaProvider> getAllProviders() {
        return new ArrayList<QuotaProvider>(providers.values());
    }

    @Override
    public QuotaProvider getProvider(String module) {
        return providers.get(module);
    }

    @Override
    public QuotaProvider addingService(ServiceReference<QuotaProvider> reference) {
        QuotaProvider provider = context.getService(reference);
        if (provider == null) {
            return null;
        }

        String moduleID = provider.getModuleID();
        LOG.info("Adding QuotaProvider for module {}.", moduleID);
        QuotaProvider existing = providers.put(moduleID, provider);
        if (existing != null) {
            LOG.warn("Detected a duplicate QuotaProvider for module {}. Service {} ({}) was overwritten by {} ({})!",
                moduleID,
                existing.toString(),
                existing.getClass().getName(),
                provider.toString(),
                provider.getClass().getName());
        }
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<QuotaProvider> reference, QuotaProvider provider) {
        //
    }

    @Override
    public void removedService(ServiceReference<QuotaProvider> reference, QuotaProvider provider) {
        String moduleID = provider.getModuleID();
        if (providers.remove(moduleID, provider)) {
            LOG.info("Removed QuotaProvider for module {}.", moduleID);
        }
    }

}
