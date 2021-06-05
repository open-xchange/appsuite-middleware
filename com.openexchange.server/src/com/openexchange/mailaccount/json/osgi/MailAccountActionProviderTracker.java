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

package com.openexchange.mailaccount.json.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.MailAccountActionProvider;
import com.openexchange.osgi.util.RankedService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailAccountActionProviderTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailAccountActionProviderTracker implements ServiceTrackerCustomizer<MailAccountActionProvider, MailAccountActionProvider>, ActiveProviderDetector {

    private final BundleContext context;
    private final SortableConcurrentList<RankedService<MailAccountActionProvider>> trackedProviders;

    /**
     * Initializes a new {@link MailAccountActionProviderTracker}.
     */
    public MailAccountActionProviderTracker(BundleContext context) {
        super();
        this.context = context;
        this.trackedProviders = new SortableConcurrentList<RankedService<MailAccountActionProvider>>();
    }

    @Override
    public MailAccountActionProvider getActiveProvider(ServerSession session) throws OXException {
        for (RankedService<MailAccountActionProvider> rankedService : trackedProviders) {
            MailAccountActionProvider provider = rankedService.service;
            if (provider.isApplicableFor(session)) {
                return provider;
            }
        }

        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    @Override
    public synchronized MailAccountActionProvider addingService(ServiceReference<MailAccountActionProvider> reference) {
        MailAccountActionProvider provider = context.getService(reference);
        trackedProviders.addAndSort(new RankedService<MailAccountActionProvider>(provider, RankedService.getRanking(reference)));
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<MailAccountActionProvider> reference, MailAccountActionProvider provider) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<MailAccountActionProvider> reference, MailAccountActionProvider provider) {
        trackedProviders.remove(new RankedService<MailAccountActionProvider>(provider, RankedService.getRanking(reference)));
        context.ungetService(reference);
    }

}
