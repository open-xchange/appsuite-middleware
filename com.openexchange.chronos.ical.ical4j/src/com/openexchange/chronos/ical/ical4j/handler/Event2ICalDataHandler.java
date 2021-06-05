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
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.chronos.ical.impl.ICalUtils;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * {@link Event2ICalDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Event2ICalDataHandler extends Object2ICalDataHandler<VEvent, Event> {

    private final ICalMapper mapper;

    /**
     * Initializes a new {@link Event2ICalDataHandler}.
     *
     * @param mapper The iCal mapper to use
     */
    public Event2ICalDataHandler(ICalMapper mapper) {
        super(Event.class, Event[].class);
        this.mapper = mapper;
    }

    @Override
    protected VEvent export(Event object, ICalParameters parameters, List<OXException> warnings) throws OXException {
        VEvent vEvent = mapper.exportEvent(object, parameters, warnings);
        ICalUtils.removeProperties(vEvent, parameters.get(ICalParameters.IGNORED_PROPERTIES, String[].class));
        ICalUtils.removeParameters(vEvent, parameters.get(ICalParameters.IGNORED_PROPERTY_PARAMETERS, String[].class));
        return vEvent;
    }

}
