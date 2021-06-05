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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link GetChangeExceptionsRequest}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class GetChangeExceptionsRequest extends AbstractAppointmentRequest<GetChangeExceptionsResponse> {

    private int folderId;

    private int objectId;

    private int[] columns;

    private boolean failOnError;

    public GetChangeExceptionsRequest(int folderId, int objectId, int[] columns) {
        this(folderId, objectId, columns, true);
    }

    public GetChangeExceptionsRequest(int folderId, int objectId, int[] columns, boolean failOnError) {
        this.folderId = folderId;
        this.objectId = objectId;
        this.columns = columns;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GETCHANGEEXCEPTIONS));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(folderId)));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ID, objectId));
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends GetChangeExceptionsResponse> getParser() {
        return new GetChangeExceptionsParser(failOnError, columns);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

}
