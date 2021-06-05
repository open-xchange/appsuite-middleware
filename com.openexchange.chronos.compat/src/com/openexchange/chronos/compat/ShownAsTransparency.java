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

package com.openexchange.chronos.compat;

import com.openexchange.chronos.Transp;

/**
 * {@link ShownAsTransparency}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum ShownAsTransparency implements Transp {

    /**
     * com.openexchange.groupware.container.Appointment.RESERVED = 1
     */
    RESERVED(Transp.OPAQUE, 1),

    /**
     * com.openexchange.groupware.container.Appointment.TEMPORARY = 2
     */
    TEMPORARY(Transp.OPAQUE, 2),

    /**
     * com.openexchange.groupware.container.Appointment.ABSENT = 3
     */
    ABSENT(Transp.OPAQUE, 3),

    /**
     * com.openexchange.groupware.container.Appointment.FREE = 4
     */
    FREE(Transp.TRANSPARENT, 4),

    ;

    /**
     * Gets a shown as transparency for the supplied legacy "shown as" constant.
     *
     * @param shownAs The legacy "shown as" constant
     * @return The corresponding shown as transparency, or {@link ShownAsTransparency#RESERVED} if not mappable
     */
    public static ShownAsTransparency getTransparency(int shownAs) {
        for (ShownAsTransparency transparency : values()) {
            if (shownAs == transparency.shownAs) {
                return transparency;
            }
        }
        return ShownAsTransparency.RESERVED;
    }

    private final String value;
    private final int shownAs;

    /**
     * Initializes a new {@link ShownAsTransparency}.
     *
     * @param value The transparency value
     * @param shownAs The legacy "shown as" constant
     */
    private ShownAsTransparency(String value, int shownAs) {
        this.value = value;
        this.shownAs = shownAs;
    }

    @Override
    public String getValue() {
        return value;
    }

    /**
     * Gets the legacy "shown as" constant.
     *
     * @return The shown as constant
     */
    public int getShownAs() {
        return shownAs;
    }

}
