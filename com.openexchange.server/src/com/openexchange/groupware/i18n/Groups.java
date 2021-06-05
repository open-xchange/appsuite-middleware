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

package com.openexchange.groupware.i18n;

import com.openexchange.i18n.LocalizableStrings;

/**
 * I18n strings for groups.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Groups implements LocalizableStrings {

    /**
     * Default constructor for reading fields with reflection.
     */
    public Groups() {
        super();
    }

    // Display name of group with identifier 0. This group always contains all existing users of a context but no guests.
    public static final String ALL_USERS = "All users";

    // Display name of group for guest users.
    public static final String GUEST_GROUP = "Guests";

    // Display name of group with identifier 1. Every created user will be added to this group but they can be removed.
    public static final String STANDARD_GROUP = "Standard group";

}
