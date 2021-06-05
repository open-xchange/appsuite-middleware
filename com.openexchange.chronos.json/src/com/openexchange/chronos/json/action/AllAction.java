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

import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.json.converter.EventResultConverter;
import com.openexchange.chronos.json.converter.EventsPerFolderResultConverter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AllAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@RestrictedAction(module = ChronosAction.MODULE, type = RestrictedAction.Type.READ)
public class AllAction extends ChronosAction {

    private static final Set<String> REQUIRED_PARAMETERS = unmodifiableSet(PARAM_RANGE_START, PARAM_RANGE_END);

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_EXPAND, PARAM_ORDER_BY, PARAM_ORDER, PARAM_FIELDS, PARAM_UPDATE_CACHE);

    private static final String PARAMETER_FOLDERS = "folders";

    /**
     * Initializes a new {@link AllAction}.
     *
     * @param services A service lookup reference
     */
    protected AllAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        JSONObject jsonObject = requestData.getData(JSONObject.class);
        if (null != jsonObject) {
            /*
             * PUT with folder identifiers in body; get results for each requested folder & return appropriate result
             */
            List<String> folderIds;
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(PARAMETER_FOLDERS);
                folderIds = new ArrayList<String>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    folderIds.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
            }
            Map<String, EventsResult> eventsResults = calendarAccess.getEventsInFolders(folderIds);
            return new AJAXRequestResult(eventsResults, getMaximumTimestamp(eventsResults), EventsPerFolderResultConverter.INPUT_FORMAT);
        }
        /*
         * assume GET with single folder as optional parameter; get events in single folder or all events of user
         */
        List<Event> events;
        if (requestData.containsParameter(AJAXServlet.PARAMETER_FOLDERID)) {
            events = calendarAccess.getEventsInFolder(requestData.getParameter(AJAXServlet.PARAMETER_FOLDERID));
        } else {
            events = calendarAccess.getEventsOfUser();
        }
        return new AJAXRequestResult(events, CalendarUtils.getMaximumTimestamp(events), EventResultConverter.INPUT_FORMAT);
    }

    private static Date getMaximumTimestamp(Map<String, EventsResult> eventsResults) {
        if (null == eventsResults || eventsResults.isEmpty()) {
            return null;
        }
        long maximumTimestamp = 0L;
        for (Map.Entry<String, EventsResult> entry : eventsResults.entrySet()) {
            maximumTimestamp = Math.max(maximumTimestamp, entry.getValue().getTimestamp());
        }
        return new Date(maximumTimestamp);
    }

}
