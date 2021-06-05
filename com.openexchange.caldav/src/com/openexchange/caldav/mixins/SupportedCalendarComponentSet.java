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

package com.openexchange.caldav.mixins;

import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;


/**
 * {@link SupportedCalendarComponentSet}
 *
 * Specifies the calendar component types (e.g., VEVENT, VTODO, etc.) that
 * calendar object resources can contain in the calendar collection.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SupportedCalendarComponentSet extends SingleXMLPropertyMixin {

    public static final String VEVENT = "VEVENT";
    public static final String VTODO = "VTODO";
    public static final String VAVAILABILITY = "VAVAILABILITY";

    private final String[] components;

    public SupportedCalendarComponentSet() {
        this(VEVENT);
    }

    public SupportedCalendarComponentSet(String...components) {
        super(CaldavProtocol.CAL_NS.getURI(), "supported-calendar-component-set");
        this.components = components;
    }

    @Override
    protected String getValue() {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != this.components) {
            for (String component : components) {
                stringBuilder.append("<CAL:comp name=\"").append(component).append("\"/>");
            }
        }
        return stringBuilder.toString();
//        return "<CAL:comp name=\"VEVENT\"/><CAL:comp name=\"VTODO\"/>";
    }

}
