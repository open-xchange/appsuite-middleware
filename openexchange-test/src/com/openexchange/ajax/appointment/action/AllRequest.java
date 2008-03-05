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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonAllRequest;
import com.openexchange.ajax.request.AppointmentRequest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.search.Order;

/**
 * Contains the data for an appointment all request.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AllRequest extends CommonAllRequest {

    public static final int[] GUI_COLUMNS = new int[] {
        AppointmentObject.OBJECT_ID,
        AppointmentObject.FOLDER_ID
    };

    public static final int GUI_SORT = AppointmentObject.START_DATE;

    public static final Order GUI_ORDER = Order.ASCENDING;

    private final Date start;

    private final Date end;

    private final boolean recurrenceMaster;

    public AllRequest(final int folderId, final int[] columns, final Date start,
        final Date end) {
        this(folderId, columns, start, end, true);
    }

    /**
     * Default constructor.
     */
    public AllRequest(final int folderId, final int[] columns, final Date start,
        final Date end, final boolean recurrenceMaster) {
        super(AbstractAppointmentRequest.URL, folderId, addGUIColumns(columns),
            0, null, true);
        this.start = start;
        this.end = end;
        this.recurrenceMaster = recurrenceMaster;
    }

    private static int[] addGUIColumns(final int[] columns) {
        final List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < columns.length; i++) {
            list.add(Integer.valueOf(columns[i]));
        }
        // Move GUI_COLUMNS to end.
        for (int i = 0; i < GUI_COLUMNS.length; i++) {
            final Integer column = Integer.valueOf(GUI_COLUMNS[i]);
            list.remove(column);
            list.add(column);
        }
        final int[] retval = new int[list.size()];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = list.get(i).intValue();
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        final Parameter[] params = super.getParameters();
        final Parameter[] retval = new Parameter[params.length + 3];
        System.arraycopy(params, 0, retval, 0, params.length);
        retval[retval.length - 3] = new Parameter(AJAXServlet.PARAMETER_START, start);
        retval[retval.length - 2] = new Parameter(AJAXServlet.PARAMETER_END, end);
        retval[retval.length - 1] = new Parameter(AppointmentRequest.RECURRENCE_MASTER,
            recurrenceMaster);
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllParser getParser() {
        return new AllParser(isFailOnError(), getColumns());
    }
}
