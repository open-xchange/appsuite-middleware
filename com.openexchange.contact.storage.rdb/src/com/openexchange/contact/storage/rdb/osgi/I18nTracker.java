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

package com.openexchange.contact.storage.rdb.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.contact.storage.rdb.internal.Translator;
import com.openexchange.i18n.I18nService;

/**
 * {@link I18nTracker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
final class I18nTracker implements ServiceTrackerCustomizer<I18nService, I18nService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link I18nTracker}.
     *
     * @param context The bundle context
     */
    public I18nTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public I18nService addingService(ServiceReference<I18nService> reference) {
        I18nService i18nService = context.getService(reference);
        Translator.getInstance().addService(i18nService);
        return i18nService;
    }

    @Override
    public void modifiedService(ServiceReference<I18nService> reference, I18nService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<I18nService> reference, I18nService service) {
        I18nService i18nService = service;
        Translator.getInstance().removeService(i18nService);
        context.ungetService(reference);
    }

}
