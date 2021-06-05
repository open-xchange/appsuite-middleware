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

package com.openexchange.xing;

import com.openexchange.java.Strings;

/**
 * {@link Language} - Supported languages by XING API; <code>de en es fr it nl pl pt ru tr zh</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Language {

    DE("de"), EN("en"), ES("es"), FR("fr"), IT("it"), NL("nl"), PL("pl"), PT("pt"), RU("ru"), TR("tr"), ZH("zh"),

    ;

    private final String slang;

    private Language(final String slang) {
        this.slang = slang;
    }

    /**
     * Gets the language identifier
     *
     * @return The language identifier
     */
    public String getLangId() {
        return slang;
    }

    /**
     * Gets the language for given identifier.
     *
     * @param language The language identifier
     * @return The associated language or <code>null</code>
     */
    public static Language languageFor(final String language) {
        if (Strings.isEmpty(language)) {
            return null;
        }
        for (final Language l : Language.values()) {
            if (l.getLangId().equalsIgnoreCase(language)) {
                return l;
            }
        }
        return null;
    }

}
