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

package com.openexchange.i18n.tools;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link StringHelper} - Helper class to translate strings.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StringHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StringHelper.class);

    private static final ConcurrentMap<Locale, StringHelper> CACHE = new ConcurrentHashMap<Locale, StringHelper>(16, 0.9F, 1);
    private static final ConcurrentMap<Locale, Translator> TRANSLATOR_CACHE = new ConcurrentHashMap<Locale, Translator>(16, 0.9F, 1);

    private static final Locale DEFAULT_LOCALE = Locale.US;

    /**
     * Gets the {@link StringHelper} instance for specified locale.
     *
     * @param locale The locale
     * @return The associated {@link StringHelper} instance
     */
    public static StringHelper valueOf(final Locale locale) {
        final Locale loc = null == locale ? DEFAULT_LOCALE : locale;
        StringHelper sh = CACHE.get(loc);
        if (null == sh) {
            final StringHelper newHelper = new StringHelper(loc);
            sh = CACHE.putIfAbsent(loc, newHelper);
            if (null == sh) {
                sh = newHelper;
            }
        }
        return sh;
    }

    /**
     * Convenience method for retrieving the translator associated with specified locale
     *
     * @param locale The locale
     * @return The associated translator
     */
    public static Translator translatorFor(final Locale locale) {
        Locale loc = null == locale ? DEFAULT_LOCALE : locale;
        Translator t = TRANSLATOR_CACHE.get(loc);
        if (null == t) {
            I18nServiceRegistry registry = ServerServiceRegistry.getServize(I18nServiceRegistry.class);
            if (registry == null) {
                return Translator.EMPTY;
            }
            I18nService service = registry.getI18nService(locale);
            Translator nt = new I18nTranslator(service);
            t = TRANSLATOR_CACHE.putIfAbsent(loc, nt);
            if (null == t) {
                t = nt;
            }
        }
        return t;
    }

    // -----------------------------------------------------------------------------------------------------------------------

    private final Locale locale;

    /**
     * Initializes a string replacer using the given locale.
     *
     * @param locale The locale to translate string to. If <code>null</code> is
     *            given, no replacement takes place.
     */
    private StringHelper(final Locale locale) {
        super();
        this.locale = locale;
    }

    /**
     * Tries to load a String under key for the given locale in the resource
     * bundle. If either the resource bundle or the String is not found the key
     * is returned instead. This makes most sense for ResourceBundles created
     * with the gettext tools.
     */
    public final String getString(final String key) {
        if (null == locale) {
            return key;
        }
        try {
            I18nServiceRegistry registry = ServerServiceRegistry.getServize(I18nServiceRegistry.class);
            if (registry == null) {
                LOG.debug("No such i18n service found for {}. Using default.", locale);
                return key;
            }
            I18nService service = registry.getI18nService(locale);
            return service.getLocalized(key);
        } catch (MissingResourceException x) {
            LOG.debug("Missing resource for {}. Using default.", locale, x);
            return key;
        }
    }

    @Override
    public int hashCode() {
        return (locale == null) ? 0 : locale.getClass().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof StringHelper) {
            final StringHelper sh = (StringHelper) o;
            if (locale == null && sh.locale == null) {
                return true;
            }
            if (locale == null && sh.locale != null) {
                return false;
            }
            if (locale != null && sh.locale == null) {
                return false;
            }

            return sh.locale.equals(locale);
        }
        return false;
    }
}
