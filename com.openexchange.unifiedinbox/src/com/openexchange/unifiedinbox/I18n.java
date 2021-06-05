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

package com.openexchange.unifiedinbox;

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
        services = new ConcurrentHashMap<Locale, I18nService>();
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
        final I18nService service = services.get(locale);
        if (null != service) {
            return service.getLocalized(translateMe);
        }
        return translateMe;
    }

}
