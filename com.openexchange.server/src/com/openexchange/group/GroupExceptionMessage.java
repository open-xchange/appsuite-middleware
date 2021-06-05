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

package com.openexchange.group;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link GroupExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class GroupExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link GroupExceptionMessage}.
     */
    private GroupExceptionMessage() {
        super();
    }

    // No group given.
    public final static String NULL_MSG = "No group given.";

    // The mandatory field %1$s is not defined.
    public final static String MANDATORY_MISSING_MSG = "Required value \"%1$s\" was not supplied.";

    // The simple name contains invalid characters: "%1$s".
    public final static String NOT_ALLOWED_SIMPLE_NAME_MSG = "The name contains invalid characters: \"%1$s\".";

    // Another group with the same identifier name exists: %1$d.
    public final static String DUPLICATE_MSG = "Another group with the same identifier name exists.";

    // Group contains a not existing member %1$d.
    public final static String NOT_EXISTING_MEMBER_MSG = "Group contains a not existing member.";

    // Group contains invalid data: "%1$s".
    public final static String INVALID_DATA_MSG = "Group contains invalid data.";

    // You are not allowed to create groups.
    public final static String NO_CREATE_PERMISSION_MSG = "You are not allowed to create groups.";

    // Edit Conflict. Your change cannot be completed because somebody else has made a conflicting change to the same item. Please refresh
    // or synchronize and try again.
    public final static String MODIFIED_MSG = "Edit Conflict. Your change cannot be completed because somebody else has made a conflicting" +
    		" change to the same item. Please refresh or synchronize and try again.";

    // You are not allowed to change groups.
    public final static String NO_MODIFY_PERMISSION_MSG = "You are not allowed to change groups.";

    // You are not allowed to delete groups.
    public final static String NO_DELETE_PERMISSION_MSG = "You are not allowed to delete groups.";

    // Group "%1$s" can not be deleted.
    public final static String NO_GROUP_DELETE_MSG = "Group \"%1$s\" can not be deleted.";

    // Group "%1$s" can not be changed.
    public final static String NO_GROUP_UPDATE_MSG = "Group \"%1$s\" can not be changed.";

    // The display name "%1$s" is reserved. Please choose another one.
    public static final String RESERVED_DISPLAY_NAME_MSG = "The display name \"%1$s\" is reserved. Please choose another one.";

    // Group contains a guest user %1$d.
    public static final String NO_GUEST_USER_IN_GROUP_MSG = "Group contains a guest user %1$d.";

}
