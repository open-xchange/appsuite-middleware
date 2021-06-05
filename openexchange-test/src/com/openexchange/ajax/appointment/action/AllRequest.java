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

package com.openexchange.ajax.appointment.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonAllRequest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.search.Order;

/**
 * Contains the data for an appointment all request.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AllRequest extends CommonAllRequest {

    public static final int[] COLUMNS_ALL_ALIAS = new int[] { 1, 20, 207, 206, 2 };

    public static final int[] COLUMNS_LIST_ALIAS = new int[] { 1, 20, 207, 206, 2, 200, 201, 202, 203, 209, 221, 401, 402, 102, 400, 101, 220, 215, 100 };

    public static final int[] GUI_COLUMNS = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID };

    public static final int GUI_SORT = Appointment.START_DATE;

    public static final Order GUI_ORDER = Order.ASCENDING;

    private final Date start;

    private final Date end;

    private final boolean recurrenceMaster;

    private String timeZoneId;

    public AllRequest(final int folderId, final int[] columns, final Date start, final Date end, final TimeZone tz) {
        this(folderId, columns, start, end, tz, true);
    }

    /**
     * Default constructor.
     */
    public AllRequest(final int folderId, final int[] columns, final Date start, final Date end, final TimeZone tz, final boolean recurrenceMaster) {
        super(AbstractAppointmentRequest.URL, folderId, addGUIColumns(columns), 0, null, true);
        this.start = start;
        this.end = end;
        this.recurrenceMaster = recurrenceMaster;
        this.timeZoneId = tz.getID();
    }

    public AllRequest(final int folderId, final String alias, final Date start, final Date end, final TimeZone tz) {
        this(folderId, alias, start, end, tz, true);
    }

    public AllRequest(final int folderId, final String alias, final Date start, final Date end, final TimeZone tz, final boolean recurrenceMaster) {
        super(AbstractAppointmentRequest.URL, folderId, alias, 0, null, true);
        // Add time zone's offset to simulate local time as passed by requests from GUI
        this.start = addTimeZone2Date(start, tz);
        this.end = addTimeZone2Date(end, tz);
//        this.start = start;
//        this.end = end;
        this.recurrenceMaster = recurrenceMaster;
        this.timeZoneId = tz.getID();
    }

    /**
     * Gets the time zone of the response.
     *
     * @return The time zone ID
     */
    public String getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * Sets the time zone of the response.
     *
     * @param timeZoneId The time zone ID to set
     */
    public void setTimeZoneId(final String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    private static int[] addGUIColumns(final int[] columns) {
        final List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < columns.length; i++) {
            list.add(Integer.valueOf(columns[i]));
        }
        // Move GUI_COLUMNS to end.
        for (int i = 0; i < GUI_COLUMNS.length; i++) {
            final Integer column = Integer.valueOf(GUI_COLUMNS[i]);
            if (!list.contains(column)) {
                list.add(column);
            }
        }
        final int[] retval = new int[list.size()];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = list.get(i).intValue();
        }
        return retval;
    }

    private static Date addTimeZone2Date(final Date d, final TimeZone tz) {
        return addTimeZone2Date(d.getTime(), tz);
    }

    private static Date addTimeZone2Date(final long timeMillis, final TimeZone tz) {
        return new Date(timeMillis + tz.getOffset(timeMillis));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        final List<Parameter> params = new LinkedList<Parameter>(Arrays.asList(super.getParameters()));

        if (null != timeZoneId) {
            params.add(new Parameter(AJAXServlet.PARAMETER_TIMEZONE, timeZoneId));
        }

        params.add(new Parameter(AJAXServlet.PARAMETER_START, start));
        params.add(new Parameter(AJAXServlet.PARAMETER_END, end));
        params.add(new Parameter(AJAXServlet.PARAMETER_RECURRENCE_MASTER, recurrenceMaster));

        return params.toArray(new Parameter[] {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllParser getParser() {
        if (getColumns() != null) {
            return new AllParser(isFailOnError(), getColumns());
        }
        if (getAlias().equals("all")) {
            return new AllParser(isFailOnError(), COLUMNS_ALL_ALIAS);
        }
        if (getAlias().equals("list")) {
            return new AllParser(isFailOnError(), COLUMNS_LIST_ALIAS);
        }
        return null;
    }
}
