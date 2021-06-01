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

package com.openexchange.chronos.ical.ical4j.mapping.event;

import com.openexchange.chronos.Event;
import com.openexchange.chronos.compat.ShownAsTransparency;
import com.openexchange.chronos.ical.ical4j.mapping.ICalTextMapping;
import net.fortuna.ical4j.extensions.outlook.BusyStatus;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Transp;

/**
 * {@link XMicrosoftBusyStatusMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class XMicrosoftBusyStatusMapping extends ICalTextMapping<VEvent, Event> {

    private static final String BUSY = BusyStatus.BUSY.getValue();
    private static final String OOF = "OOF";
    private static final String TENTATIVE = "TENTATIVE";
    private static final String FREE = "FREE";

    /**
     * Initializes a new {@link XMicrosoftBusyStatusMapping}.
     */
    public XMicrosoftBusyStatusMapping() {
        super(BusyStatus.PROPERTY_NAME);
    }

    @Override
    protected String getValue(Event object) {
        com.openexchange.chronos.Transp value = object.getTransp();
        if (null == value) {
            return null;
        }
        if (ShownAsTransparency.class.isInstance(value)) {
            switch ((ShownAsTransparency) value) {
                case ABSENT:
                    return OOF;
                case TEMPORARY:
                    return TENTATIVE;
                default:
                    break;
            }
        }
        return Transp.TRANSPARENT.getValue().equals(value.getValue()) ? FREE : BUSY;
    }

    @Override
    protected void setValue(Event object, String value) {
        if (null != value) {
            object.setTransp(getTransp(value));
        }
    }

    @Override
    protected Property createProperty() {
        return new BusyStatus(BusyStatus.FACTORY);
    }

    private static com.openexchange.chronos.Transp getTransp(String value) {
        if (null == value) {
            return null;
        }
        switch (value) {
            case FREE:
                return ShownAsTransparency.FREE;
            case OOF:
                return ShownAsTransparency.ABSENT;
            case TENTATIVE:
                return ShownAsTransparency.TEMPORARY;
            default:
                return ShownAsTransparency.RESERVED;
        }
    }

}
