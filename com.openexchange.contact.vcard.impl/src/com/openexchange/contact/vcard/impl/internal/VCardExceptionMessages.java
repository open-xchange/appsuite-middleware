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

package com.openexchange.contact.vcard.impl.internal;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link VCardExceptionMessages}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class VCardExceptionMessages implements LocalizableStrings {

    public static final String NO_VCARD_FOUND_MSG = "No vCard could be found in the supplied file. Please use a valid vCard file and try again.";

    public static final String VALIDATION_FAILED_MSG = "Validation failed for property \"%1$s\": %2$s";

    public static final String PARSER_ERROR_MSG = "Error reading vCard: %1$s";

    public static final String CONVERSION_FAILED_MSG = "Conversion failed for property \"%1$s\": %2$s";

    public static final String MAXIMUM_SIZE_EXCEEDED_MSG = "The vCard exceeds the maximum allowed size and can't be imported.";

    /**
     * Initializes a new {@link VCardExceptionMessages}.
     */
    private VCardExceptionMessages() {
        super();
    }
}
