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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.ajax.appointment.action;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class GetRequest extends AbstractAppointmentRequest<GetResponse> {

    /**
     * Appointment is requested through this folder.
     */
    private final int folderId;

    /**
     * Unique identifier of the appointment.
     */
    private final int objectId;

    /**
     * Recurrence position
     */
    private final int recurrencePosition;

    private final boolean failOnError;

    /**
     * Default constructor.
     */
    public GetRequest(final int folderId, final int objectId) {
        this(folderId, objectId, 0, true);
    }

    public GetRequest(final int folderId, final int objectId, final boolean failOnError) {
        this(folderId, objectId, 0, failOnError);
    }

    public GetRequest(final int folderId, final int objectId, final int recurrencePosition) {
        this(folderId, objectId, recurrencePosition, true);
    }

    public GetRequest(final int folderId, final int objectId, final int recurrencePosition, final boolean failOnError) {
        super();
        this.folderId = folderId;
        this.objectId = objectId;
        this.recurrencePosition = recurrencePosition;
        this.failOnError = failOnError;
    }

    public GetRequest(final int folderId, final CommonInsertResponse insert) {
        this(folderId, insert.getId());
    }

    public GetRequest(Appointment appointment) {
        this(appointment.getParentFolderID(), appointment.getObjectID());
    }

    public GetRequest(Appointment appointment, final boolean failOnError) {
        this(appointment.getParentFolderID(), appointment.getObjectID(), failOnError);
    }

    @Override
    public Object getBody() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.GET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        final List<Parameter> parameterList = new ArrayList<Parameter>(4);
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(folderId)));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(objectId)));
        if (recurrencePosition > 0) {
            parameterList.add(new Parameter(CalendarFields.RECURRENCE_POSITION, String.valueOf(recurrencePosition)));
        }
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetParser getParser() {
        return new GetParser(failOnError);
    }
}
