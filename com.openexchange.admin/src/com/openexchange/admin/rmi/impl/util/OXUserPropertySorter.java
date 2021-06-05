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

package com.openexchange.admin.rmi.impl.util;

import java.util.Comparator;
import com.openexchange.admin.rmi.dataobjects.UserProperty;

/**
 * {@link OXUserPropertySorter} sorts the provided {@link UserProperty}s based on their name.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class OXUserPropertySorter implements Comparator<UserProperty> {

    private static final OXUserPropertySorter INSTANCE = new OXUserPropertySorter();

    /**
     * Gets the comparator instance.
     *
     * @return The instance
     */
    public static OXUserPropertySorter getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link OXUserPropertySorter}.
     */
    private OXUserPropertySorter() {
        super();
    }

    @Override
    public int compare(UserProperty userProperty1, UserProperty userProperty2) {
        return userProperty1.getName().compareToIgnoreCase(userProperty2.getName());
    }

}
