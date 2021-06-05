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
 * {@link SupportedCalendarComponentSets}
 *
 * Enumerates the sets of component restrictions the server is willing to allow the client to specify in MKCALENDAR or extended
 * MKCOL requests.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SupportedCalendarComponentSets extends SingleXMLPropertyMixin {

    public static final String VEVENT = "VEVENT";
    public static final String VTODO = "VTODO";

    private final String[] components;

    public SupportedCalendarComponentSets() {
        this(VEVENT);
    }

    /**
     * Initializes a new {@link SupportedCalendarComponentSets}.
     *
     * @param components The supported calendar components
     */
    public SupportedCalendarComponentSets(String...components) {
        super(CaldavProtocol.CAL_NS.getURI(), "supported-calendar-component-sets");
        this.components = components;
    }

    @Override
    protected String getValue() {
        if (null != this.components) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String component : components) {
                stringBuilder.append("<CAL:supported-calendar-component-set><CAL:comp name=\"")
                    .append(component).append("\"/></CAL:supported-calendar-component-set>");
            }
            return stringBuilder.toString();
        }
        return null;
    }

}
