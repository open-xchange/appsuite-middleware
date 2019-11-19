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

package com.openexchange.chronos.impl;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;

/**
 * {@link JSONPrintableEvent} - Extends the {@link DelegatingEvent} and rewrites the {@link #toString()} method
 * to get a event representation in JSON style
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */

public class JSONPrintableEvent extends DelegatingEvent {

    /* See also com.openexchange.chronos.json.converter.mapper.Event2JSONDataHandler */
    private final static String FIELDS = DataHandlers.EVENT2JSON + ".fields";
    private final static String TIMEZONE = DataHandlers.EVENT2JSON + ".timeZone";

    private final CalendarSession session;
    private final EventField[] fields;
    private final String timeZone;

    /**
     * Initializes a new {@link JSONPrintableEvent}.
     * 
     * @param session The calendar session
     * @param delegate The delegating event
     */
    public JSONPrintableEvent(CalendarSession session, Event delegate) {
        this(session, delegate, null, null);
    }

    /**
     * Initializes a new {@link JSONPrintableEvent}.
     * 
     * @param session The calendar session
     * @param delegate The delegating event
     * @param fields EventFields to log
     * @param timeZone The time zone to use
     */
    public JSONPrintableEvent(CalendarSession session, Event delegate, EventField[] fields, String timeZone) {
        super(delegate);
        this.session = session;
        this.fields = fields;
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        if (null == delegate) {
            return "no event";
        }

        try {
            /*
             * Get data handler to convert the event
             */
            ConversionService conversionService = Services.getService(ConversionService.class, true);
            DataHandler handler = conversionService.getDataHandler(DataHandlers.EVENT2JSON);
            if (null == handler) {
                return delegate.toString();
            }

            /*
             * Prepare data for handler
             */
            SimpleData<Event> data = new SimpleData<>(delegate, null);
            Map<String, String> args = new HashMap<>(3);
            args.put(FIELDS, getFields());
            args.put(TIMEZONE, timeZone);

            /*
             * Convert to JSON and use the special JSONObject.toSting() implementation
             */
            ConversionResult result = handler.processData(data, new DataArguments(args), session.getSession());
            if (null != result && null != result.getData() && JSONObject.class.isAssignableFrom(result.getData().getClass())) {
                return ((JSONObject) result.getData()).toString();
            }
        } catch (OXException e) {
            // Ignore
        }
        return delegate.toString();
    }

    /**
     * Convert given fields to comma separated list
     */
    private String getFields() {
        if (null == fields || fields.length < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (EventField field : fields) {
            sb.append(field.name()).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
