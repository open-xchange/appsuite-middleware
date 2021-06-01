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

package com.openexchange.chronos.json.action;

import static com.openexchange.chronos.json.fields.ChronosJsonFields.COMMENT;
import static com.openexchange.chronos.json.fields.ChronosJsonFields.ORGANIZER;
import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.Date;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.converter.CalendarResultConverter;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ChangeOrganizerAction} - "/chronos/changeOrganizer" endpoint for updating an organizer
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
@RestrictedAction(module = ChronosAction.MODULE, type = RestrictedAction.Type.WRITE)
public class ChangeOrganizerAction extends ChronosAction {

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_RANGE_START, PARAM_RANGE_END, PARAM_EXPAND, PARAM_FIELDS, PARAM_PUSH_TOKEN, PARAM_SCHEDULING);

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    private static final EventField[] ORGANIZER_FIELD = new EventField[] { EventField.ORGANIZER };

    /**
     * Initializes a new {@link ChangeOrganizerAction}.
     *
     * @param services The {@link ServiceLookup}
     */
    public ChangeOrganizerAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        long clientTimestamp = parseClientTimestamp(requestData);
        EventID eventId = parseIdParameter(requestData);

        JSONObject jsonObject = requestData.getData(JSONObject.class);
        if (null == jsonObject) {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }

        try {
            String comment = jsonObject.getString(COMMENT);
            if (Strings.isNotEmpty(comment)) {
                calendarAccess.set(CalendarParameters.PARAMETER_COMMENT, comment);
            }
        } catch (JSONException e) {
            LOG.debug("Unable to read comment parameter", e);
        }

        try {
            Event deserialize = EventMapper.getInstance().deserialize(jsonObject, ORGANIZER_FIELD);
            CalendarResult calendarResult = calendarAccess.changeOrganizer(eventId, deserialize.getOrganizer(), clientTimestamp);
            return new AJAXRequestResult(calendarResult, new Date(calendarResult.getTimestamp()), CalendarResultConverter.INPUT_FORMAT);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.MISSING_FIELD.create(ORGANIZER);
        }
    }

}
