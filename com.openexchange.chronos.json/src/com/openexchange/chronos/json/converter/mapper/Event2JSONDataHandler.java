/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.json.converter.mapper;

import java.util.ArrayList;
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
        ArrayList<EventField> fields = new ArrayList<>();
        if (null != dataArguments && Strings.isNotEmpty(dataArguments.get(FIELDS))) {
            String sFields = dataArguments.get(FIELDS);
            for (String field : sFields.split(",")) {
                fields.add(EventField.valueOf(field));
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
            EventMapper.getInstance().serialize(convertee, out, fields.isEmpty() ? EventMapper.getInstance().getMappedFields(): fields.toArray(new EventField[fields.size()]), timeZone, session);
        } catch (JSONException e) {
            LOGGER.debug("Unable to convert event to JSON", e);
            result.addWarning(new OXException(e));
        }

        result.setData(out);
        return result;
    }

}
