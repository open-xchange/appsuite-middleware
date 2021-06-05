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

package com.openexchange.chronos.ical.ical4j.handler;

import java.io.InputStream;
import java.util.TimeZone;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.ImportedTimeZone;
import com.openexchange.chronos.ical.impl.ICalUtils;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * {@link ICal2TimeZoneDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICal2TimeZoneDataHandler extends ICal2ObjectDataHandler<TimeZone> {

    /**
     * Initializes a new {@link ICal2TimeZoneDataHandler}.
     */
    public ICal2TimeZoneDataHandler() {
        super();
    }

    @Override
    protected TimeZone parse(InputStream inputStream, ICalParameters parameters) throws OXException {
        ComponentList components = ICalUtils.parseComponents(inputStream, Component.VTIMEZONE, VCALENDAR_PREFIX, VCALENDAR_SUFFIX, parameters);
        if (null == components || components.isEmpty()) {
            return null;
        }

        VTimeZone component = (VTimeZone) components.getComponent(Component.VTIMEZONE);
        if (null == component) {
            return null;
        }
        return new ImportedTimeZone(component);
    }

}
