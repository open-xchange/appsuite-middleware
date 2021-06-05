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
