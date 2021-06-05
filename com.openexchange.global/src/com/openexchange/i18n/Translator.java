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
 * Simple interface for passing translations.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface Translator {

    /** The empty translator */
    Translator EMPTY = new Translator() {

        @Override
        public String translate(final String toTranslate) {
            return toTranslate;
        }

        @Override
        public Locale getLocale() {
            return LocaleTools.DEFAULT_LOCALE;
        }
    };

    /**
     * Translates specified string
     *
     * @param toTranslate The string to translate
     * @return The translated string (if translation available) or the passed string
     */
    String translate(String toTranslate);

    /**
     * Gets the underlying locale.
     * 
     * @return The locale
     */
    Locale getLocale();

}
