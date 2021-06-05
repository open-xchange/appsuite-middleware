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

package com.openexchange.find.json.osgi;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.find.json.converters.StringTranslator;
import com.openexchange.i18n.I18nService;


/**
 * {@link I18nTracker}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class I18nTracker implements ServiceTrackerCustomizer<I18nService, I18nService>, StringTranslator {

    private final ConcurrentMap<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>();

    private final BundleContext context;

    public I18nTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public I18nService addingService(ServiceReference<I18nService> reference) {
        I18nService service = context.getService(reference);
        services.put(service.getLocale(), service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<I18nService> reference, I18nService service) {
        services.put(service.getLocale(), service);
    }

    @Override
    public void removedService(ServiceReference<I18nService> reference, I18nService service) {
        services.remove(service.getLocale());
    }

    @Override
    public String translate(Locale locale, String localizable) {
        if (localizable == null) {
            return null;
        }

        I18nService i18nService = services.get(locale);
        if (i18nService == null) {
            return localizable;
        }

        String localized = i18nService.getLocalized(localizable);
        if (localized == null) {
            return localizable;
        }

        return localized;
    }

}
