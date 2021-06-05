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

package com.openexchange.i18n.tools.replacement;

import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link LocationReplacement} - The replacement for location.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class LocationReplacement extends FormatLocalizedStringReplacement {

    /**
     * Initializes a new {@link LocationReplacement}.
     *
     * @param location The location as a string.
     */
    public LocationReplacement(final String location) {
        super(TemplateToken.LOCATION, Notifications.FORMAT_LOCATION, location);
    }

    @Override
    public String getReplacement() {
        final String soleRepl = getSoleReplacement();
        if (null == soleRepl || soleRepl.length() == 0) {
            return "";
        }
        final String repl = super.getReplacement();
        return new StringBuilder(repl.length() + 1).append(repl).append('\n').toString();
    }
}
