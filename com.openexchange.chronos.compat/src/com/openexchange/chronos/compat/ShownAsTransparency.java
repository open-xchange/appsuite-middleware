/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
