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

package com.openexchange.contact.storage.rdb.internal;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.i18n.I18nService;

/**
 * {@link Translator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Translator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Translator.class);
    private static final Translator INSTANCE = new Translator();

    private final Map<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>();

    /**
     * Gets the translator instance.
     *
     * @return The instance.
     */
    public static Translator getInstance() {
        return INSTANCE;
    }

    /**
     * Translates a string.
     *
     * @param locale The target locale
     * @param toTranslate The string to translate
     * @return The translated string
     */
    public String translate(Locale locale, String toTranslate) {
        Locale loc = null == locale ? Locale.US : locale;
        I18nService service = services.get(loc);
        if (null == service) {
            return toTranslate;
        }
        if (!service.hasKey(toTranslate)) {
            LOG.debug("I18n service for locale {} has no translation for \"{}\".", loc, toTranslate);
            return toTranslate;
        }
        return service.getLocalized(toTranslate);
    }

    /**
     * Adds an {@link I18nService}.
     *
     * @param service The service to add
     */
    public void addService(I18nService service) {
        if (null != services.put(service.getLocale(), service)) {
            LOG.warn("Another i18n translation service found for {}", service.getLocale());
        }
    }

    /**
     * Removes a previously added {@link I18nService}.
     *
     * @param service The service to remove
     */
    public void removeService(I18nService service) {
        if (null == services.remove(service.getLocale())) {
            LOG.warn("Unknown i18n translation service shut down for {}", service.getLocale());
        }
    }

    /**
     * Initializes a new {@link Translator}.
     */
    private Translator() {
        super();
    }

}
