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

package com.openexchange.publish.json;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.i18n.I18nService;

/**
 * Singleton for keeping references to {@link I18nService}s and for translating texts.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class I18n {

    private static final I18n SINGLETON = new I18n();

    private final Map<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>();

    /**
     * Initializes a new {@link I18n}.
     */
    private I18n() {
        super();
    }

    public static final I18n getInstance() {
        return SINGLETON;
    }

    public void addI18nService(final I18nService service) {
        final Locale locale = service.getLocale();
        if (null == locale) {
            return;
        }
        services.put(locale, service);
    }

    public void removeI18nService(final I18nService service) {
        final Locale locale = service.getLocale();
        if (null == locale) {
            return;
        }
        services.remove(locale);
    }

    public I18nService get(final Locale locale) {
        return services.get(getLocale(locale));
    }

    public String translate(final Locale locale, final String translateMe) {
        String retval = translateMe;
        final I18nService service = services.get(getLocale(locale));
        if (null != service) {
            retval = service.getLocalized(translateMe);
        }
        return retval;
    }

    private static final Locale DEFAULT_LOCALE = Locale.US;

    private static Locale getLocale(final Locale locale) {
        return null == locale ? DEFAULT_LOCALE : locale;
    }
}
