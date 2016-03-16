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

package com.openexchange.exception.internal;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.i18n.I18nService;

/**
 * {@link I18n} - Singleton for keeping references to {@link I18nService}s and for translating texts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class I18n {

    private static final I18n SINGLETON = new I18n();

    private final ConcurrentMap<Locale, I18nService> services;

    /**
     * Initializes a new {@link I18n}.
     */
    private I18n() {
        super();
        services = new ConcurrentHashMap<Locale, I18nService>(4, 0.9f, 1);
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static final I18n getInstance() {
        return SINGLETON;
    }

    /**
     * Add specified i18n service.
     *
     * @param service The service to add
     * @return <code>true</code> on successful insertion to registry; otherwise <code>false</code>
     */
    public boolean addI18nService(final I18nService service) {
        return (null == services.putIfAbsent(service.getLocale(), service));
    }

    /**
     * Removes specified i18n service.
     *
     * @param service The service to remove
     */
    public void removeI18nService(final I18nService service) {
        services.remove(service.getLocale());
    }

    /**
     * Gets the i18n service for specified locale.
     *
     * @param locale The locale
     * @return The i18n service for specified locale or <code>null</code> if absent
     */
    public I18nService get(final Locale locale) {
        return services.get(locale);
    }

    /**
     * Translates specified string to given locale.
     *
     * @param locale The locale
     * @param translateMe The string to translate
     * @return The translated string
     */
    public String translate(final Locale locale, final String translateMe) {
        if (null == translateMe) {
            return null;
        }
        final I18nService service = services.get(locale);
        if (null != service) {
            return service.getLocalized(translateMe);
        }
        return translateMe;
    }

}
