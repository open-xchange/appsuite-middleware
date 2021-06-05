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

/**
 * {@link Status} - Represents a status for a mail account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface Status {

    /**
     * Gets the identifier; such as "ok" or "invalid_credentials"
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the accompanying human-readable message (optional) for given locale.
     *
     * @param locale The locale
     * @return The human-readable message or <code>null</code>
     */
    String getMessage(Locale locale);

    /**
     * Gets an optional error providing further details in case an erroneous status is represented.
     *
     * @return The error, or <code>null</code> if not available
     */
    OXException getError();

}
