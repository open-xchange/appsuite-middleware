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

package com.openexchange.i18n.internal;

import java.util.Locale;
import com.openexchange.i18n.I18nService;

/**
 * {@link NOOPI18nService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class NOOPI18nService implements I18nService {

    private final Locale locale;

    /**
     * Initializes a new {@link NOOPI18nService}.
     */
    public NOOPI18nService(Locale locale) {
        super();
        this.locale = locale;

    }

    @Override
    public String getLocalized(String key) {
        return key;
    }

    @Override
    public boolean hasKey(String key) {
        return true;
    }

    @Override
    public boolean hasKey(String context, String key) {
        return true;
    }

    @Override
    public String getL10NLocalized(String key) {
        return key;
    }

    @Override
    public String getL10NContextLocalized(String messageContext, String key) {
        return key;
    }

    @Override
    public String getL10NPluralLocalized(String messageContext, String key, String keyPlural, int plural) {
        return key;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

}
