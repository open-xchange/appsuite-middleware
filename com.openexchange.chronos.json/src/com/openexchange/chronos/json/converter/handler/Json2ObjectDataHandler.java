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

package com.openexchange.chronos.json.converter.handler;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.JsonMapper;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link Json2ObjectDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Json2ObjectDataHandler<O, E extends Enum<E>> implements DataHandler {

    private final JsonMapper<O, E> mapper;

    /**
     * Initializes a new {@link Json2ObjectDataHandler}.
     *
     * @param mapper The underlying JSON mapper
     */
    public Json2ObjectDataHandler(JsonMapper<O, E> mapper) {
        super();
        this.mapper = mapper;
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { JSONArray.class, JSONObject.class };
    }

    @Override
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException {
        ConversionResult result = new ConversionResult();
        Object sourceData = data.getData();
        if (null == sourceData || JSONObject.NULL.equals(sourceData)) {
            result.setData(null);
        } else if (JSONObject.class.isInstance(sourceData)) {
            result.setData(deserialize((JSONObject) sourceData, optTimeZoneID(dataArguments, session)));
        } else if (JSONArray.class.isInstance(sourceData)) {
            result.setData(deserialize((JSONArray) sourceData, optTimeZoneID(dataArguments, session)));
        } else {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(sourceData.getClass().toString());
        }
        return result;
    }

    private O deserialize(JSONObject jsonObject, String timeZoneID) throws OXException {
        try {
            return mapper.deserialize(jsonObject, mapper.getMappedFields(), timeZoneID);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private List<O> deserialize(JSONArray jsonArray, String timeZoneID) throws OXException {
        try {
            List<O> objects = new ArrayList<O>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                objects.add(deserialize(jsonArray.getJSONObject(i), timeZoneID));
            }
            return objects;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static String optTimeZoneID(DataArguments dataArguments, Session session) {
        String timeZoneID = dataArguments.get(CalendarParameters.PARAMETER_TIMEZONE);
        if (null == timeZoneID && null != session) {
            try {
                timeZoneID = ServerSessionAdapter.valueOf(session).getUser().getTimeZone();
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(Json2ObjectDataHandler.class).warn("Error getting user timezone", e);
            }
        }
        return timeZoneID;
    }

}
