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

package com.openexchange.chronos.json.converter.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link Event2JSONDataHandler}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class Event2JSONDataHandler implements DataHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(Event2JSONDataHandler.class);

    /** The {@link EventField}s to convert. Must be a comma-separated list */
    public final static String FIELDS = DataHandlers.EVENT2JSON + ".fields";

    /** The time zone to use when converting. Must be a String */
    public final static String TIMEZONE = DataHandlers.EVENT2JSON + ".timeZone";

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { Event.class };
    }

    @Override
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException {
        Object object = data.getData();
        if (null == object || false == Event.class.isAssignableFrom(object.getClass())) {
            return null;
        }
        /*
         * Get data to serialize
         */
        Event convertee = (Event) object;
        Set<EventField> requestedFields = null;
        if (null != dataArguments && Strings.isNotEmpty(dataArguments.get(FIELDS))) {
            requestedFields = new HashSet<EventField>();
            String sFields = dataArguments.get(FIELDS);
            for (String field : sFields.split(",")) {
                requestedFields.add(EventField.valueOf(field));
            }
        }

        List<EventField> fields = new ArrayList<EventField>();
        for (Entry<EventField, ? extends JsonMapping<? extends Object, Event>> entry : EventMapper.getInstance().getMappings().entrySet()) {
            JsonMapping<? extends Object, Event> mapping = entry.getValue();
            if ((null == requestedFields || requestedFields.contains(entry.getKey())) && mapping.isSet(convertee) && null != mapping.get(convertee)) {
                fields.add(entry.getKey());
            }
        }

        String timeZone = null;
        if (null != dataArguments) {
            timeZone = dataArguments.get(TIMEZONE);
        }

        /*
         * Convert data via mapper
         */
        ConversionResult result = new ConversionResult();
        JSONObject out = new JSONObject();
        try {
            EventMapper.getInstance().serialize(convertee, out, fields.toArray(new EventField[fields.size()]), timeZone, session);
        } catch (JSONException e) {
            LOGGER.debug("Unable to convert event to JSON", e);
            result.addWarning(new OXException(e));
        }

        result.setData(out);
        return result;
    }

}
