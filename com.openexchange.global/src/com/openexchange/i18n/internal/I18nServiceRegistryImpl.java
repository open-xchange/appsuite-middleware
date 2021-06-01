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

package com.openexchange.i18n.internal;

import static com.openexchange.i18n.LocaleTools.DEFAULT_LOCALE;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.java.Strings;

/**
 * {@link I18nServiceRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class I18nServiceRegistryImpl implements I18nServiceRegistry {

    /** The concurrent map contain all currently tracked <code>I18nService</code> instances */
    private final ConcurrentMap<Locale, I18nService> services;

    /** The special locale for <code>"en"</code> language */
    private final Locale en_Locale;

    /** The special locale for <code>"ja"</code> language */
    private final Locale ja_Locale;

    private static final I18nService NOOP_I18N = new NOOPI18nService(Locale.US);

    private static final I18nServiceRegistryImpl INSTANCE = new I18nServiceRegistryImpl();

    public static I18nServiceRegistryImpl getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link I18nServiceRegistryImpl}.
     */
    private I18nServiceRegistryImpl() {
        super();
        services = new ConcurrentHashMap<Locale, I18nService>(32, 0.9F, 1);
        en_Locale = new Locale("en");
        ja_Locale = new Locale("ja");
    }

    /**
     * Adds specified i18n service to this registry.
     *
     * @param service The service to add
     * @return <code>true</code> if successfully added; otherwise <code>false</code>
     */
    public boolean addI18nService(I18nService service) {
        return null != service && null == services.putIfAbsent(service.getLocale(), service);
    }

    /**
     * Removes specified i18n service from this registry.
     *
     * @param service The service to remove
     * @return <code>true</code> if successfully removed; otherwise <code>false</code>
     */
    public boolean removeI18nService(I18nService service) {
        return null != service && null != services.remove(service.getLocale());
    }

    @Override
    public Collection<I18nService> getI18nServices() throws OXException {
        return Collections.unmodifiableCollection(services.values());
    }

    @Override
    public I18nService getI18nService(Locale locale) {
        return getI18nService(locale, false);
    }

    @Override
    public I18nService getI18nService(Locale locale, boolean strict) {
        if (strict) {
            return null == locale ? NOOP_I18N : services.get(locale);
        }
        return getBestFittingI18nService(locale);
    }

    public I18nService getBestFittingI18nService(Locale locale) {
        if (null == locale) {
            return NOOP_I18N;
        }

        // Direct look-up
        I18nService service = services.get(locale);
        if (null != service) {
            return service;
        }

        String bestFit = Locale2LanguageMapping.getLanguageForLocale(locale.toLanguageTag());
        if (Strings.isNotEmpty(bestFit)) {
            Locale bestFitLocale = Locale.forLanguageTag(bestFit);
            service = services.get(bestFitLocale);
            if (null != service) {
                return service;
            }
        }

        // Grab language identifier
        String language = locale.getLanguage();
        if (null == language) {
            // Huh...?
            return NOOP_I18N;
        }

        /*-
         * As per JavaDoc:
         *
         * Don't do:
         *   if (locale.getLanguage().equals("he")) // BAD!
         *     ...
         *
         * Instead, do:
         *   if (locale.getLanguage().equals(new Locale("he").getLanguage()))
         *     ...
         */
        if (en_Locale.getLanguage().equals(language)) {
            I18nService i18nService = services.get(DEFAULT_LOCALE);
            if (i18nService != null) {
                return i18nService;
            }
            return NOOP_I18N;
        } else if (ja_Locale.getLanguage().equals(language)) {
            I18nService i18nService = services.get(Locale.JAPAN);
            if (i18nService != null) {
                return i18nService;
            }
        }

        // Guess best fit...
        I18nService firstMatch = null;
        for (Map.Entry<Locale, I18nService> entry : services.entrySet()) {
            Locale loc = entry.getKey();
            if (language.equals(loc.getLanguage())) {
                // Does language match country part?
                if (language.equalsIgnoreCase(loc.getCountry())) {
                    return entry.getValue();
                }

                // Is it the first candidate with equal language?
                if (null == firstMatch) {
                    firstMatch = entry.getValue();
                }
            }
        }
        return firstMatch == null ? NOOP_I18N : firstMatch;
    }

    /**
     * Removes all services from this registry
     */
    public void clear() {
        services.clear();
    }
}
