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

package com.openexchange.resource;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ResourceExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ResourceExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link ResourceExceptionMessage}.
     */
    private ResourceExceptionMessage() {
        super();
    }

    // No resource group found for identifier \"%1$d\".
    public final static String RESOURCEGROUP_NOT_FOUND_MSG_DISPLAY = "No resource group found for identifier \"%1$d\".";

    // Found resource groups with the same identifier %1$d.
    public final static String RESOURCEGROUP_CONFLICT_MSG_DISPLAY = "Found resource groups with the same identifier \"%1$d\".";

    // No resource found with identifier \"%1$d\".
    public final static String RESOURCE_NOT_FOUND_MSG_DISPLAY = "No resource found with identifier \"%1$d\".";

    // Found resource(s) with same identifier \"%1$s\".
    public final static String RESOURCE_CONFLICT_MSG_DISPLAY = "Found resource(s) with same identifier \"%1$s\".";

    // You do not have the appropriate permissions to modify resources.
    public final static String PERMISSION_MSG_DISPLAY = "You do not have the appropriate permissions to modify resources.";

    // The provided resource identifier \"%1$s\" contains invalid characters.
    public final static String INVALID_RESOURCE_IDENTIFIER_MSG_DISPLAY = "The provided resource identifier \"%1$s\" contains invalid characters.";

    // The provided E-Mail address \"%1$s\" for resource is invalid.
    public final static String INVALID_RESOURCE_MAIL_MSG_DISPLAY = "The provided E-Mail address \"%1$s\" for resource is invalid.";

    // There is already a resource with E-Mail address "%1$s". Please choose another one.
    public final static String RESOURCE_CONFLICT_MAIL_MSG_DISPLAY = "There is already a resource with E-Mail address \"%1$s\". Please choose another one.";

    // The selected resource \"%1$s\" has been changed in the meantime.
    public final static String CONCURRENT_MODIFICATION_MSG_DISPLAY = "The selected resource \"%1$s\" has been changed in the meantime.";

    // Missing mandatory field(s) in given resource.
    public static final String MANDATORY_FIELD_MSG_DISPLAY = "Missing mandatory field(s) in given resource.";

    // Missing mandatory name in given resource.
    public static final String MANDATORY_FIELD_NAME_MSG_DISPLAY = "Missing mandatory name in given resource.";

    // Missing mandatory display name in given resource.
    public static final String MANDATORY_FIELD_DISPLAY_NAME_MSG_DISPLAY = "Missing mandatory display name in given resource.";

    // Missing mandatory E-Mail address in given resource.
    public static final String MANDATORY_FIELD_MAIL_MSG_DISPLAY = "Missing mandatory E-Mail address in given resource.";

}
