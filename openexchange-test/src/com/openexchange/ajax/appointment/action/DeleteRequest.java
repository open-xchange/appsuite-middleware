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

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * Stores parameters for the delete request.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class DeleteRequest extends AbstractAppointmentRequest<CommonDeleteResponse> {

    private final int objectId;

    private final int[] objectIds;

    private final int inFolder;

    private final int recurrencePosition;

    private final Date lastModified;

    private boolean failOnError = true;
    private Date recurrenceDatePosition;

    public DeleteRequest(final int objectId, final int inFolder, final Date lastModified, final boolean failOnError) {
        this(objectId, inFolder, 0, lastModified, failOnError);
    }

    public DeleteRequest(final int[] objectIds, final int inFolder, final Date lastModified, final boolean failOnError) {
        this(objectIds, inFolder, 0, lastModified, failOnError);
    }

    /**
     * Default constructor. Deletes the complete appointment and even a series.
     */
    public DeleteRequest(final int objectId, final int inFolder, final Date lastModified) {
        this(objectId, inFolder, 0, lastModified, true);
    }

    public DeleteRequest(final Appointment appointment) {
        this(appointment.getObjectID(), appointment.getParentFolderID(), appointment.getLastModified());
    }

    public DeleteRequest(final Appointment appointment, boolean failOnError) {
        this(appointment.getObjectID(), appointment.getParentFolderID(), appointment.getLastModified(), failOnError);
    }

    /**
     * Deletes an occurrence of a series appointment by position.
     */
    public DeleteRequest(final int objectId, final int inFolder, final int recurrencePosition, final Date lastModified) {
        this(objectId, inFolder, recurrencePosition, lastModified, true);
    }

    /**
     * Deletes an occurrence of a series appointment by position.
     */
    public DeleteRequest(final int objectId, final int inFolder, final int recurrencePosition, final Date lastModified, final boolean failOnError) {
        super();
        this.objectId = objectId;
        this.objectIds = null;
        this.inFolder = inFolder;
        this.recurrencePosition = recurrencePosition;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    public DeleteRequest(final int[] objectIds, final int inFolder, final int recurrencePosition, final Date lastModified, final boolean failOnError) {
        super();
        this.objectId = 0;
        this.objectIds = objectIds;
        this.inFolder = inFolder;
        this.recurrencePosition = recurrencePosition;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    /**
     * Deletes an occurrence of a series appointment by date.
     */
    public DeleteRequest(final int objectId, final int inFolder, final Date recurrenceDatePosition, final Date lastModified) {
        this(objectId, inFolder, recurrenceDatePosition, lastModified, true);
    }

    /**
     * Deletes an occurrence of a series appointment by date.
     */
    public DeleteRequest(final int objectId, final int inFolder, final Date recurrenceDatePosition, final Date lastModified, final boolean failOnError) {
        super();
        this.objectId = objectId;
        this.objectIds = null;
        this.inFolder = inFolder;
        this.recurrencePosition = -1;
        this.recurrenceDatePosition = recurrenceDatePosition;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    public DeleteRequest(final int[] objectIds, final int inFolder, final Date recurrenceDatePosition, final Date lastModified, final boolean failOnError) {
        super();
        this.objectId = 0;
        this.objectIds = objectIds;
        this.inFolder = inFolder;
        this.recurrencePosition = -1;
        this.recurrenceDatePosition = recurrenceDatePosition;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        if (objectIds == null) {
            final JSONObject json = new JSONObject();
            json.put(DataFields.ID, objectId);
            json.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
            if (recurrencePosition > 0) {
                json.put(CalendarFields.RECURRENCE_POSITION, recurrencePosition);
            } else if (recurrenceDatePosition != null) {
                json.put(CalendarFields.RECURRENCE_DATE_POSITION, recurrenceDatePosition.getTime());
            }
            return json;
        }
        final JSONArray jsonArray = new JSONArray();
        for (final int id : objectIds) {
            final JSONObject json = new JSONObject();
            json.put(DataFields.ID, id);
            json.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
            if (recurrencePosition > 0) {
                json.put(CalendarFields.RECURRENCE_POSITION, recurrencePosition);
            } else if (recurrenceDatePosition != null) {
                json.put(CalendarFields.RECURRENCE_DATE_POSITION, recurrenceDatePosition.getTime());
            }
            jsonArray.put(json);
        }
        return jsonArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(lastModified == null ? System.currentTimeMillis() : lastModified.getTime())) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteParser getParser() {
        return new DeleteParser(failOnError);
    }
}
