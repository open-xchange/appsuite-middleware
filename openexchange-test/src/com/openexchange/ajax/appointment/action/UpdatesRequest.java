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
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;

/**
 * Request to get updated appointments.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class UpdatesRequest extends AbstractAppointmentRequest<AppointmentUpdatesResponse> {

    private final int folderId;
    private final int[] columns;
    private final Date timestamp;
    private final boolean recurrenceMaster;
    private final Ignore ignore;
    private Date end;
    private Date start;

    /**
     * Initializes a new {@link UpdatesRequest} that ignores updates for deleted.
     * 
     * @param folderId Folder id to use for the request
     * @param columns Columns to use for the request
     * @param timestamp Timestamp to use for the request
     * @param recurrenceMaster if true a recurring appointment isn't split into single occurrances but kept as one object
     */
    public UpdatesRequest(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster) {
        this(folderId, columns, timestamp, recurrenceMaster, Ignore.DELETED);
    }

    /**
     * Initializes a new {@link UpdatesRequest}.
     * 
     * @param folderId Folder id to use for the request
     * @param columns Columns to use for the request
     * @param timestamp Timestamp to use for the request
     * @param recurrenceMaster if true a recurring appointment isn't split into single occurrances but kept as one object
     * @param ignore What kind of updates should be ignored
     */
    public UpdatesRequest(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster, Ignore ignore) {
        this(folderId, columns, timestamp, recurrenceMaster, ignore, null, null);
    }

    public UpdatesRequest(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster, Ignore ignore, Date start, Date end) {
        this.folderId = folderId;
        this.columns = columns;
        this.timestamp = timestamp;
        this.recurrenceMaster = recurrenceMaster;
        this.ignore = ignore;
        this.start = start;
        this.end = end;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES));
        if (folderId != 0) {
            parameterList.add(new Parameter(AJAXServlet.PARAMETER_INFOLDER, folderId));
        }
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, timestamp));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_IGNORE, ignore.getValue()));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_RECURRENCE_MASTER, recurrenceMaster));
        if (start != null) {
            parameterList.add(new Parameter(AJAXServlet.PARAMETER_START, start));
        }
        if (end != null) {
            parameterList.add(new Parameter(AJAXServlet.PARAMETER_END, end));
        }
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    @Override
    public AbstractAJAXParser<AppointmentUpdatesResponse> getParser() {
        return new AppointmentUpdatesParser(columns);
    }
}
