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

package com.openexchange.i18n.impl;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;


/**
 * If strings shall be translated, you need to use an appropriate {@link I18nService},
 * according to the desired locale. The {@link I18nTranslatorFactory} helps you to
 * keep track of all {@link I18nService} instances and creates new {@link Translator}
 * instances based on given locales.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class I18nTranslatorFactory extends ServiceTracker<I18nService, I18nService> implements TranslatorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(I18nTranslatorFactory.class);

    private final ConcurrentMap<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>();


    /**
     * Initializes a new {@link I18nTranslatorFactory}.
     *
     * @param context The bundle context
     */
    public I18nTranslatorFactory(BundleContext context) {
        super(context, I18nService.class, null);
    }

    @Override
    public Translator translatorFor(Locale locale) {
        if (locale == null) {
            return I18nTranslator.EMPTY;
        }

        I18nService service = services.get(locale);
        if (service == null) {
            return I18nTranslator.EMPTY;
        }

        return new I18nTranslator(service);
    }

    @Override
    public I18nService addingService(ServiceReference<I18nService> reference) {
        I18nService service = super.addingService(reference);
        if (service != null) {
            I18nService existing = services.putIfAbsent(service.getLocale(), service);
            if (existing != null) {
                LOG.warn("Ignoring duplicate I18nService for locale {}.", service.getLocale());
                context.ungetService(reference);
                return null;
            }
        }
        return service;
    }

    @Override
    public void removedService(ServiceReference<I18nService> reference, I18nService service) {
        if (services.remove(service.getLocale(), service)) {
            super.removedService(reference, service);
        }
    }

}
