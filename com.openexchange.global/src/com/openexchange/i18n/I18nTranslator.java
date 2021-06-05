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

package com.openexchange.i18n;

import java.util.Locale;

/**
 * Implementation of a {@link Translator} backed with an {@link I18nService}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class I18nTranslator implements Translator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(I18nTranslator.class);

    private final I18nService service;

    /**
     * Initializes a new {@link I18nTranslator}.
     *
     * @param service The i18n service
     */
    public I18nTranslator(final I18nService service) {
        super();
        this.service = service;
    }

    @Override
    public String translate(final String toTranslate) {
        if (!service.hasKey(toTranslate)) {
            LOG.debug("I18n service for locale {} has no translation for \"{}\".", service.getLocale(), toTranslate);
            return toTranslate;
        }
        return service.getLocalized(toTranslate);
    }

    @Override
    public Locale getLocale() {
        return service.getLocale();
    }

}
