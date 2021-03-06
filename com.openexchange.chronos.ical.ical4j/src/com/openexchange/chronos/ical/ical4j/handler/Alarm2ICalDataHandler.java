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

import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.chronos.ical.impl.ICalUtils;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.component.VAlarm;

/**
 * {@link Alarm2ICalDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Alarm2ICalDataHandler extends Object2ICalDataHandler<VAlarm, Alarm> {

    private final ICalMapper mapper;

    /**
     * Initializes a new {@link Alarm2ICalDataHandler}.
     *
     * @param mapper The iCal mapper to use
     */
    public Alarm2ICalDataHandler(ICalMapper mapper) {
        super(Alarm.class, Alarm[].class);
        this.mapper = mapper;
    }

    @Override
    protected VAlarm export(Alarm object, ICalParameters parameters, List<OXException> warnings) throws OXException {
        VAlarm vAlarm = mapper.exportAlarm(object, parameters, warnings);
        ICalUtils.removeProperties(vAlarm, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
        ICalUtils.removeParameters(vAlarm, parameters.get(ICalParameters.IGNORED_PROPERTY_PARAMETERS, String[].class));
        return vAlarm;
    }

}
