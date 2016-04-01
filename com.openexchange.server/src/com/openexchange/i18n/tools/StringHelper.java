/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.i18n.tools;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.server.services.I18nServices;

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
            I18nService service = I18nServices.getInstance().getService(loc);
            if (null == service) {
                // Do not cache if no such I18nService is available
                return Translator.EMPTY;
            }
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
            final I18nService tool = I18nServices.getInstance().getService(locale);
            if (tool == null) {
                LOG.debug("No service for {}  found. Using default for bundle ", locale);
                return key;
            }
            return tool.getLocalized(key);
        } catch (final MissingResourceException x) {
            LOG.debug("MissingResource for {}. Using default for bundle ", locale, x);
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
