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

package com.openexchange.contact.provider;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ContactsProviderExceptionMessages}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsProviderExceptionMessages implements LocalizableStrings {

    /**
     * The requested contact account was not found.
     */
    public static final String ACCOUNT_NOT_FOUND_MSG = "The requested contact account was not found.";
    /**
     * The operation could not be completed due to missing capabilities.
     */
    public static final String MISSING_CAPABILITY_MSG = "The operation could not be completed due to missing capabilities.";
    /**
     * The supplied folder is not supported. Please select a valid folder and try again.
     */
    public static final String UNSUPPORTED_FOLDER_MSG = "The supplied folder is not supported. Please select a valid folder and try again.";
    /**
     * The contacts provider '%1$s' is not available.
     */
    public static final String PROVIDER_NOT_AVAILABLE_MSG = "The contacts provider '%1$s' is not available.";
    /**
     * The requested operation is not supported for contacts provider '%1$s'.
     */
    public static final String UNSUPPORTED_OPERATION_FOR_PROVIDER_MSG = "The requested operation is not supported for contacts provider '%1$s'.";
    /**
     * The operation could not be completed due to a concurrent modification. Please reload the data and try again.
     */
    public static final String CONCURRENT_MODIFICATION_MSG = "The operation could not be completed due to a concurrent modification. Please reload the data and try again.";
    /**
     * The requested contact was not found.
     */
    public static final String CONTACT_NOT_FOUND_MSG = "The requested contact was not found.";
    /**
     * The requested folder was not found.
     */
    public static final String FOLDER_NOT_FOUND_MSG = "The requested folder was not found.";
    /**
     * The field '%1$s' is mandatory. Please supply a valid value and try again.
     */
    public static final String MANDATORY_FIELD_MSG = "The field '%1$s' is mandatory. Please supply a valid value and try again.";
}
