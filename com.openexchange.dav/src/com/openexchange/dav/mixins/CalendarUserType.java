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

package com.openexchange.dav.mixins;

import com.openexchange.dav.DAVProtocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;


/**
 * {@link CalendarUserType}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarUserType extends SingleXMLPropertyMixin {

    private final String userType;

    /**
     * Initializes a new {@link CalendarUserType}.
     *
     * @param userType The user type
     */
    public CalendarUserType(String userType) {
        super(DAVProtocol.CAL_NS.getURI(), "calendar-user-type");
        this.userType = userType;
    }

    /**
     * Initializes a new {@link CalendarUserType}.
     *
     * @param userType The user type
     */
    public CalendarUserType(com.openexchange.chronos.CalendarUserType type) {
        this(type.toString());
    }

    @Override
    protected String getValue() {
        return userType;
    }

}
