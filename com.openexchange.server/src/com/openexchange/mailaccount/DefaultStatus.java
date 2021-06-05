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

package com.openexchange.mailaccount;

import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link DefaultStatus} - The default immutable status implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultStatus implements Status {

    private final String identifier;
    private final String message;
    private final OXException error;

    /**
     * Initializes a new {@link DefaultStatus}.
     * 
     * @param identifier The status identifier
     * @param message The status message
     */
    public DefaultStatus(String identifier, String message) {
        this(identifier, message, null);
    }

    /**
     * Initializes a new {@link DefaultStatus}.
     * 
     * @param identifier The status identifier
     * @param message The status message
     * @param error The optional error
     */
    public DefaultStatus(String identifier, String message, OXException error) {
        super();
        this.identifier = identifier;
        this.message = message;
        this.error = error;
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public String getMessage(Locale locale) {
        return StringHelper.valueOf(null == locale ? Locale.US : locale).getString(message);
    }

    @Override
    public OXException getError() {
        return error;
    }

}
