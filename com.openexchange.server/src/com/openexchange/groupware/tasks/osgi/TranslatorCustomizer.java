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

package com.openexchange.groupware.tasks.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.groupware.tasks.Translator;
import com.openexchange.i18n.I18nService;

/**
 * {@link TranslatorCustomizer} adds {@link I18nService}s found by the service tracker to the task translator.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.2
 */
public final class TranslatorCustomizer implements ServiceTrackerCustomizer<I18nService, I18nService> {

    private final BundleContext context;

    public TranslatorCustomizer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public I18nService addingService(ServiceReference<I18nService> reference) {
        final I18nService service = context.getService(reference);
        Translator.getInstance().addService(service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<I18nService> reference, I18nService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<I18nService> reference, I18nService service) {
        Translator.getInstance().removeService(service);
        context.ungetService(reference);
    }
}
