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

import static com.openexchange.chronos.common.CalendarUtils.getMaximumTimestamp;
import static com.openexchange.chronos.json.fields.ChronosJsonFields.COMMENT;
import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.chronos.json.converter.CalendarResultsPerEventIdConverter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.ErrorAwareCalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RestrictedAction(module = ChronosAction.MODULE, type = RestrictedAction.Type.WRITE)
public class DeleteAction extends ChronosAction {

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_RANGE_START, PARAM_RANGE_END, PARAM_EXPAND, PARAM_FIELDS, PARAM_PUSH_TOKEN, PARAM_SCHEDULING);

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    /**
     * Initializes a new {@link DeleteAction}.
     *
     * @param services A service lookup reference
     */
    protected DeleteAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        Object data = requestData.getData();
        if (data == null) {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }
        JSONArray array = null;
        if (data instanceof JSONArray) {
            array = (JSONArray) data;
        } else if (data instanceof JSONObject) {
            JSONObject temp = (JSONObject) data;
            if (!temp.has(EVENTS)) {
                throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
            }

            try {
                array = temp.getJSONArray(EVENTS);
                if (temp.has(COMMENT)) {
                    calendarAccess.set(COMMENT, temp.get(COMMENT));
                }
            } catch (JSONException e) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
            }
        } else {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }
        Map<EventID, ErrorAwareCalendarResult> results = calendarAccess.deleteEvents(parseEventIDs(array), parseClientTimestamp(requestData));
        return new AJAXRequestResult(results, getMaximumTimestamp(results), CalendarResultsPerEventIdConverter.INPUT_FORMAT);
    }

}
