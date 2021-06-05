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

package com.openexchange.i18n.impl;

import java.util.Locale;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.parsing.Translations;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class TranslationsI18N implements I18nService {

    private final Translations translations;

    public TranslationsI18N(final Translations translations) {
        this.translations = translations;
    }

    @Override
    public String getLocalized(final String key) {
        final String translation = translations.translate(key);
        if (translation == null) {
            return key;
        }
        return translation;
    }

    @Override
    public String getL10NContextLocalized(String messageContext, String key) {
        String t = translations.translate(messageContext, key);
        if (t == null) {
            return key;
        }
        return t;
    }

    @Override
    public String getL10NPluralLocalized(String messageContext, String key, String keyPlural, int plural) {
        String t = translations.translate(messageContext, key, plural);
        if (t == null) {
            t = translations.translate(messageContext, keyPlural, plural);
        }
        if (t == null) {
            return key;
        }
        return t;

    }

    @Override
    public boolean hasKey(final String key) {
        return translations.getKnownStrings().contains(key);
    }

    @Override
    public boolean hasKey(String context, String key) {
        return translations.getKnownStrings(context).contains(key);
    }

    @Override
    public Locale getLocale() {
        return translations.getLocale();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append('{');
        if (translations != null) {
            final Locale locale = getLocale();
            if (null != locale) {
                builder.append("locale=").append(locale);
            }
            builder.append("translations=").append(translations);
        }
        builder.append('}');
        return builder.toString();
    }

    @Override
    public String getL10NLocalized(String key) {
        return getLocalized(key);
    }

}
