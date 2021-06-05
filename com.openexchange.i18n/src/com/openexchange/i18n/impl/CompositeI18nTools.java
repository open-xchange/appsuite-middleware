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

import java.util.List;
import java.util.Locale;
import com.openexchange.i18n.I18nService;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CompositeI18nTools implements I18nService {

    private Locale locale;

    private final List<I18nService> tools;

    public CompositeI18nTools(final List<I18nService> i18n) {
        super();
        tools = i18n;
        for (final I18nService i18nTool : i18n) {
            if (null == locale) {
                locale = i18nTool.getLocale();
            } else if (!locale.equals(i18nTool.getLocale())) {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public String getLocalized(final String key) {
        for (final I18nService tool : tools) {
            if (tool.hasKey(key)) {
                return tool.getLocalized(key);
            }
        }
        return key;
    }

    @Override
    public String getL10NContextLocalized(String messageContext, String key) {
        for (I18nService tool : tools) {
            if (tool.hasKey(messageContext, key)) {
                return tool.getL10NContextLocalized(messageContext, key);
            }
        }
        return key;
    }

    @Override
    public String getL10NPluralLocalized(String messageContext, String key, String keyPlural, int plural) {
        for (I18nService tool : tools) {
            if (tool.hasKey(messageContext, key)) {
                return tool.getL10NPluralLocalized(messageContext, key, keyPlural, plural);
            }
        }
        return key;
    }

    @Override
    public boolean hasKey(final String key) {
        for (final I18nService tool : tools) {
            if (tool.hasKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasKey(String context, String key) {
        for (final I18nService tool : tools) {
            if (tool.hasKey(context, key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getL10NLocalized(String key) {
        return getLocalized(key);
    }

}
