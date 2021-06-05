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

package com.openexchange.groupware.tasks;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.annotation.Nullable;
import com.openexchange.i18n.I18nService;

/**
 * {@link Translator} consumes {@link I18nService}s to translate task strings.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.2
 */
public final class Translator {

    private static final Translator SINGLETON = new Translator();

    private final Map<Locale, I18nService> services = new ConcurrentHashMap<Locale, I18nService>();

    private Translator() {
        super();
    }

    public static final Translator getInstance() {
        return SINGLETON;
    }

    public void addService(I18nService service) {
        services.put(service.getLocale(), service);
    }

    public void removeService(I18nService service) {
        services.remove(service.getLocale());
    }

    public String translate(@Nullable Locale locale, String toTranslate) {
        if (null == locale) {
            return toTranslate;
        }
        I18nService service = services.get(locale);
        if (null == service) {
            return toTranslate;
        }
        if (!service.hasKey(toTranslate)) {
            return toTranslate;
        }
        return service.getLocalized(toTranslate);
    }
}
