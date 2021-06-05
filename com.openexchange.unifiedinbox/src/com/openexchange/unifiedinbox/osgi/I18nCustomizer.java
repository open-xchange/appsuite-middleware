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

package com.openexchange.unifiedinbox.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.i18n.I18nService;
import com.openexchange.unifiedinbox.I18n;

/**
 * {@link I18nCustomizer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class I18nCustomizer implements ServiceTrackerCustomizer<I18nService,I18nService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link I18nCustomizer}.
     *
     * @param context The bundle context
     */
    public I18nCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public I18nService addingService(final ServiceReference<I18nService> reference) {
        final I18nService service = context.getService(reference);
        if (I18n.getInstance().addI18nService(service)) {
            return service;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<I18nService> reference, final I18nService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<I18nService> reference, final I18nService service) {
        if (null != service) {
            final I18nService i18nService = service;
            I18n.getInstance().removeI18nService(i18nService);
            context.ungetService(reference);
        }
    }
}
