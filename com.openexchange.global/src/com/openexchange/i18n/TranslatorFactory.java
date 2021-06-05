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
import com.openexchange.osgi.annotation.SingletonService;


/**
 * The {@link TranslatorFactory} creates {@link Translator} instances based for
 * given locales. Underneath the {@link I18nService}s are used.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@SingletonService
public interface TranslatorFactory {

    /**
     * Returns a translator for the given locale. If no {@link I18nService} for
     * the given locale is available or the locale is <code>null</code>,
     * a default translator is returned, that simply
     * returns the input string on {@link Translator#translate(String)}.
     *
     * @param locale The locale
     * @return A new translator instance
     */
    Translator translatorFor(Locale locale);

}
