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

import java.util.Collection;
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
 * {@link Object2JsonDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Object2JsonDataHandler<O, E extends Enum<E>> implements DataHandler {

    private final JsonMapper<O, E> mapper;
    private final Class<O> clazz;
    private final Class<O[]> arrayClass;

    /**
     * Initializes a new {@link Object2JsonDataHandler}.
     *
     * @param mapper The underlying JSON mapper
     * @param clazz The object class
     * @param arrayClass The array of objects class
     */
    public Object2JsonDataHandler(JsonMapper<O, E> mapper, Class<O> clazz, Class<O[]> arrayClass) {
        super();
        this.mapper = mapper;
        this.clazz = clazz;
        this.arrayClass = arrayClass;
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { clazz, arrayClass, Collection.class };
    }

    @Override
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException {
        ConversionResult result = new ConversionResult();
        Object sourceData = data.getData();
        if (null == sourceData) {
            result.setData(null);
        } else if (clazz.isInstance(sourceData)) {
            result.setData(serialize(clazz.cast(sourceData), optTimeZoneID(dataArguments, session)));
        } else if (arrayClass.isInstance(sourceData)) {
            result.setData(serialize(arrayClass.cast(sourceData), optTimeZoneID(dataArguments, session)));
        } else if (Collection.class.isInstance(sourceData)) {
            result.setData(serialize((Collection<?>) sourceData, optTimeZoneID(dataArguments, session)));
        } else {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(sourceData.getClass().toString());
        }
        return result;
    }

    private JSONObject serialize(O object, String timeZoneID) throws OXException {
        try {
            return mapper.serialize(object, mapper.getAssignedFields(object), timeZoneID);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONArray serialize(O[] objects, String timeZoneID) throws OXException {
        JSONArray jsonArray = new JSONArray(objects.length);
        for (O object : objects) {
            jsonArray.put(serialize(object, timeZoneID));
        }
        return jsonArray;
    }

    private JSONArray serialize(Collection<?> objects, String timeZoneID) throws OXException {
        JSONArray jsonArray = new JSONArray(objects.size());
        for (Object object : objects) {
            try {
                jsonArray.put(serialize(clazz.cast(object), timeZoneID));
            } catch (ClassCastException e) {
                throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(e, object.getClass().toString());
            }
        }
        return jsonArray;
    }

    private static String optTimeZoneID(DataArguments dataArguments, Session session) {
        String timeZoneID = dataArguments.get(CalendarParameters.PARAMETER_TIMEZONE);
        if (null == timeZoneID && null != session) {
            try {
                timeZoneID = ServerSessionAdapter.valueOf(session).getUser().getTimeZone();
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(Object2JsonDataHandler.class).warn("Error getting user timezone", e);
            }
        }
        return timeZoneID;
    }

}
