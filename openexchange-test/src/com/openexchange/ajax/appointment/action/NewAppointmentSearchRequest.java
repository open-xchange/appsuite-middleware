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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.appointment.action;

import org.json.JSONException;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest.Method;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class NewAppointmentSearchRequest extends AbstractAppointmentRequest {

    /**
     * The start range
     */
    private final Date start;

    /**
     * The end range
     */
    private final Date end;
	
	
	/**
	 * The max count of returned appointments
	 */
	private final int limit;
	
	private TimeZone timeZone;
	
	private int[] columns = {
		DataObject.OBJECT_ID,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		AppointmentObject.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL
	};
	
	/**
     * Default constructor.
     */
    public NewAppointmentSearchRequest(final Date start, final Date end, final int limit, final TimeZone timeZone) {
		super();
        this.start = start;
        this.end = end;
		this.limit = limit;
		this.timeZone = timeZone;
    }
	
    public NewAppointmentSearchRequest(final Date start, final Date end, final int limit, final TimeZone timeZone, final int[] columns) {
        super();
        this.start = start;
        this.end = end;
		this.limit = limit;
		this.timeZone = timeZone;
		this.columns = columns;
    }

    /**
     * {@inheritDoc}
     */
    public Object getBody() throws JSONException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return Method.GET;
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getParameters() {
		final List parameterList = new ArrayList();
		parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW_APPOINTMENTS));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_START, String.valueOf(start.getTime())));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_END, String.valueOf(end.getTime())));
		parameterList.add(new Parameter("limit", String.valueOf(limit)));
		parameterList.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));

		return (Parameter[])parameterList.toArray(new Parameter[parameterList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public AbstractAJAXParser getParser() {
        return new NewAppointmentSearchParser(columns, timeZone);
    }
}
