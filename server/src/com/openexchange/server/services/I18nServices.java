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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.LocaleTools;

/**
 * Registry for all found {@link I18nService} instances.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class I18nServices {

    private static final Log LOG = LogFactory.getLog(I18nServices.class);

    private final ConcurrentHashMap<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>();

    private static final I18nServices instance = new I18nServices();

    private I18nServices() {
        super();
    }

    public int addService(final Locale l, final I18nService i18n) {
        services.put(l, i18n);
        return services.size();
    }

    public int removeService(final Locale l, final I18nService i18n) {
        services.remove(l, i18n);
        return services.size();

    }

    public static I18nServices getInstance() {
        return instance;
    }

    public I18nService getService(final Locale l) {
        if (null != services) {
            return services.get(l);
        }
        return null;
    }

    public String translate(Locale locale, String toTranslate) {
        I18nService service = services.get(locale);
        if (null == service) {
            if (!"en".equalsIgnoreCase(locale.getLanguage())) {
                LOG.warn("No i18n service for locale " + locale + ".");
            }
            return toTranslate;
        }
        if (!service.hasKey(toTranslate)) {
            LOG.warn("I18n service for locale " + locale + " has no translation for \"" + toTranslate + "\".");
            return toTranslate;
        }
        return service.getLocalized(toTranslate);
    }

    public String translate(String localeId, String toTranslate) {
        return translate(LocaleTools.getLocale(localeId), toTranslate);
    }
}
