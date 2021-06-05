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

package com.openexchange.pgp.core.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link PGPCoreExceptionMessages}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v8.0.0
 */
public final class PGPCoreExceptionMessages implements LocalizableStrings {

    // No encrypted data was found for decryption
    public static final String NO_PGP_DATA_FOUND = "No encrypted items found.";

    // The right private key wasn't found to perform the action.  Key identity is listed
    public static final String PRIVATE_KEY_NOT_FOUND = "The private key for the identity '%1$s' could not be found.";

    // Bad password used
    public static final String BAD_PASSWORD = "Bad password.";

    // No items that contain a signature were found
    public static final String NO_PGP_SIGNATURE_FOUND = "No signature items found";

    // Generic IO error with error specified
    public static final String IO_EXCEPTION = "An I/O error occurred: '%1$s'";

    // Generic PGP error with error specified
    public static final String PGP_EXCEPTION = "A PGP error occurred: '%1$s'";

    private PGPCoreExceptionMessages() {}

}
