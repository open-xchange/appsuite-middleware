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
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.chronos.ical.impl.ICalUtils;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;

/**
 * {@link ICal2AlarmDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICal2AlarmDataHandler extends ICal2ObjectDataHandler<Alarm> {

    private final ICalMapper mapper;

    /**
     * Initializes a new {@link ICal2AlarmDataHandler}.
     *
     * @param mapper The iCal mapper to use
     */
    public ICal2AlarmDataHandler(ICalMapper mapper) {
        super();
        this.mapper = mapper;
    }

    @Override
    protected Alarm parse(InputStream inputStream, ICalParameters parameters) throws OXException {
        ComponentList components = ICalUtils.parseComponents(inputStream, Component.VALARM, VEVENT_PREFIX, VEVENT_SUFFIX, parameters);
        if (null != components && 0 < components.size()) {
            List<Alarm> alarms = ICalUtils.importAlarms(components, mapper, parameters);
            if (null != alarms && 0 < alarms.size()) {
                return alarms.get(0);
            }
        }
        return null;
    }

}
