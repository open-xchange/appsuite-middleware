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

import java.util.Dictionary;
import java.util.Locale;
import org.osgi.framework.BundleContext;

/**
 * Service for publishing implementations to translate texts into other languages.
 * <p>
 * Implementing services must carry the {@link BundleContext#registerService(Class, Object, Dictionary) property} named {@link #LANGUAGE} containing the string representation of the locale returned by the {@link #getLocale()} method.
 *
 * @author <a href="mailto:ben.pahne@open-xchange">Ben Pahne</a>
 */
public interface I18nService {

    static final String LANGUAGE = "language";

    /**
     * Localizes the given string
     *
     * @param key The string to localize
     * @return The localized string
     */
    String getLocalized(String key);

    /**
     * Checks if a translation is available for the given string
     *
     * @param key The string to check
     * @return true if a translation is available, false otherwise
     */
    boolean hasKey(String key);

    /**
     * Checks if a translation is available for the given string and context
     *
     * @param context The string context
     * @param key The string to check
     * @return true if a translation is available, false otherwise
     */
    boolean hasKey(String context, String key);

    /**
     * Localizes the given key.
     * This method is also a marker for xgettext to generate the translation templates.
     * For wrapping this with additional functionality, you must just keep the method signature identical and the call must contain the strings rather than variables.
     *
     * @param key
     * @return
     */
    String getL10NLocalized(String key);

    /**
     * Localizes the given key in the given context.
     * This method is also a marker for xgettext to generate the translation templates.
     * If you want something translated with a context you should use this method to avoid ambiguous keys which might have different translations depending on the context.
     * For wrapping this with additional functionality, you must just keep the method signature identical and the call must contain the strings rather than variables.
     *
     * For simple localization without context you can just use the com.openexchange.i18n.LocalizableStrings marker Interface.
     *
     * @param messageContext The context
     * @param key
     * @return
     */
    String getL10NContextLocalized(String messageContext, String key);

    /**
     * Localizes the given key in the given context depending on the number.
     * This method is also a marker for xgettext to generate the translation templates.
     * If you want something translated with a context you should use this method to avoid ambiguous keys which might have different translations depending on the context.
     * The plural form describes the number used for the translation. 0 means singular, everything >0 is a plural form. There should only be one english plural form, but other languages might have additional forms.
     * If a plural form is not available this defaults to the highest available form.
     * For wrapping this with additional functionality, you must just keep the method signature identical and the call must contain the strings rather than variables.
     *
     * For simple localization without context you can just use the com.openexchange.i18n.LocalizableStrings marker Interface.
     *
     * @param messageContext The context
     * @param key The english singular String
     * @param keyPlural The english plural String
     * @param plural The number form. 0 for singular, >0 for a plural form.
     * @return
     */
    String getL10NPluralLocalized(String messageContext, String key, String keyPlural, int plural);

    /**
     * Gets the locale for this {@link I18nService}
     *
     * @return The the {@link Locale}
     */
    Locale getLocale();

}
