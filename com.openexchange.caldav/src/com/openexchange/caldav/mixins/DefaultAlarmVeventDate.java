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
 * The {@link DefaultAlarmVeventDate}
 *
 * A default alarm applied to "VEVENT" components whose "DTSTART" property
 * value type is "DATE"
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DefaultAlarmVeventDate extends SingleXMLPropertyMixin {

    public DefaultAlarmVeventDate() {
        super(CaldavProtocol.CAL_NS.getURI(), "default-alarm-vevent-date");
    }

    @Override
    protected String getValue() {
        return "";
    }

}
