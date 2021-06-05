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

package com.openexchange.mailaccount.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.mailaccount.MailAccountDeleteListener;

/**
 * {@link DeleteListenerServiceTracker} - The {@link ServiceTrackerCustomizer} for mail account delete listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteListenerServiceTracker implements ServiceTrackerCustomizer<MailAccountDeleteListener,MailAccountDeleteListener> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteListenerServiceTracker.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link DeleteListenerServiceTracker}.
     */
    public DeleteListenerServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public MailAccountDeleteListener addingService(final ServiceReference<MailAccountDeleteListener> reference) {
        final MailAccountDeleteListener addedService = context.getService(reference);
        if (DeleteListenerRegistry.getInstance().addDeleteListener(addedService)) {
            return addedService;
        }
        LOG.warn("Duplicate delete listener \"{}\" is not be added to registry.", addedService.getClass().getName());
        // This service needs not to be tracked, thus return null
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<MailAccountDeleteListener> reference, final MailAccountDeleteListener service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<MailAccountDeleteListener> reference, final MailAccountDeleteListener service) {
        if (null != service) {
            try {
                DeleteListenerRegistry.getInstance().removeDeleteListener(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
