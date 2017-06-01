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

package com.openexchange.server.services;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;

/**
 * Registry for all found {@link I18nService} instances.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class I18nServices {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(I18nServices.class);
    private static final Locale DEFAULT_LOCALE = Locale.US;
    private static final I18nServices SINGLETON = new I18nServices();

    private final Map<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>(32, 0.9F, 1);

    private I18nServices() {
        super();
    }

    public boolean addService(final I18nService service) {
        if (null != services.put(service.getLocale(), service)) {
            LOG.warn("Another i18n translation service found for {}", service.getLocale());
            return false;
        }
        return true;
    }

    public void removeService(final I18nService service) {
        if (null == services.remove(service.getLocale())) {
            LOG.warn("Unknown i18n translation service shut down for {}", service.getLocale());
        }
    }

    public static I18nServices getInstance() {
        return SINGLETON;
    }

    /**
     * Gets the i18n service for a specific locale.
     *
     * @param locale The locale to get the i18n service for, or <code>null</code> for the default locale
     */
    public I18nService getService(final Locale locale) {
        return getService(locale, true);
    }

    /**
     * Gets the i18n service for a specific locale.
     *
     * @param locale The locale to get the i18n service for, or <code>null</code> for the default locale
     * @param warn <code>true</code> to log a warning when no i18n service for the locale is available, <code>false</code>, otherwise
     * @return The i18n service for the locale, or <code>null</code> if no suitable i18n service available
     */
    public I18nService getService(final Locale locale, final boolean warn) {
        return getService(locale, warn, true);
    }

    /**
     * Gets the i18n service for a specific locale.
     *
     * @param locale The locale to get the i18n service for, or <code>null</code> for the default locale
     * @param warn <code>true</code> to log a warning when no i18n service for the locale is available, <code>false</code>, otherwise
     * @param tryLanguageAlternatives <code>true</code> to allow falling back to other locales with the same language when no i18n service
     *        for requested locale is available, <code>false</code>, otherwise
     * @return The i18n service for the locale, or <code>null</code> if no suitable i18n service available
     */
    public I18nService getService(final Locale locale, final boolean warn, boolean tryLanguageAlternatives) {
        final Locale loc = null == locale ? DEFAULT_LOCALE : locale;
        final I18nService retval = services.get(loc);
        if (null == retval && !"en".equalsIgnoreCase(loc.getLanguage())) {
            if (tryLanguageAlternatives && Strings.isNotEmpty(loc.getLanguage())) {
                I18nService altService = getService(loc.getLanguage());
                if (null != altService) {
                    LOG.debug("No i18n service for locale {} - falling back to {}.", loc, retval);
                    return altService;
                }
            }
            if (warn) {
                LOG.warn("No i18n service for locale {}.", loc);
            }
        }
        return retval;
    }

    public String translate(final Locale locale, final String toTranslate) {
        return translate(locale, toTranslate, true);
    }

    public String translate(final Locale locale, final String toTranslate, final boolean warn) {
        final Locale loc = null == locale ? DEFAULT_LOCALE : locale;
        final I18nService service = getService(loc, warn);
        if (null == service) {
            return toTranslate;
        }
        if (warn && !service.hasKey(toTranslate)) {
            LOG.debug("I18n service for locale {} has no translation for \"{}\".", loc, toTranslate);
            return toTranslate;
        }
        return service.getLocalized(toTranslate);
    }

    public String translate(final String localeId, final String toTranslate) {
        return translate(LocaleTools.getLocale(localeId), toTranslate);
    }

    public void clear() {
        services.clear();
    }

    private I18nService getService(String language) {
        for (Entry<Locale, I18nService> entry : services.entrySet()) {
            if (language.equalsIgnoreCase(entry.getKey().getLanguage())) {
                return entry.getValue();
            }
        }
        return null;
    }

}
