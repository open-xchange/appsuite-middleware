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
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import com.openexchange.i18n.I18nService;

/**
 * @author <a href="mailto:ben.pahne@open-xchange">Ben Pahne</a>
 */
public class I18nImpl implements I18nService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(I18nImpl.class);

    private final ResourceBundle serverBundle;

    /**
     * Initializes a new {@link I18nImpl}.
     *
     * @param bundle The resource bundle
     */
    public I18nImpl(final ResourceBundle bundle) {
        serverBundle = bundle;
    }

    @Override
    public String getLocalized(final String key) {
        if (serverBundle == null) {
            return key;
        }
        try {
            return serverBundle.getString(key);
        } catch (MissingResourceException x) {
            LOG.info("Missing key {} for locale {}. Using default.", key, getLocale());
            return key;
        }
    }

    @Override
    public boolean hasKey(final String key) {
        try {
            serverBundle.getString(key);
            return true;
        } catch (MissingResourceException x) {
            return false;
        }
    }

    @Override
    public Locale getLocale() {
        if (null == serverBundle) {
            return null;
        }

        return serverBundle.getLocale();
    }

    @Override
    public boolean hasKey(String context, String key) {
        return context == null ? hasKey(key) : false;
    }

    @Override
    public String getL10NContextLocalized(String messageContext, String key) {
        return messageContext == null ? getLocalized(key) : key;
    }

    @Override
    public String getL10NPluralLocalized(String messageContext, String key, String keyPlural, int plural) {
        return messageContext == null && plural == 0 && keyPlural == null ? getLocalized(key) : key;
    }

    @Override
    public String getL10NLocalized(String key) {
        return getLocalized(key);
    }
}
