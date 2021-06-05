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

package com.openexchange.ajax.mail.actions;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link ExamineRequest}
 *
 * @author <a href="mailto:joshua.wirtz@open-xchange.com">Joshua Wirtz</a>
 */

public class ExamineRequest extends AbstractMailRequest<ExamineResponse> {
	
	String folder;
	boolean failOnError;

	public ExamineRequest(String folder, boolean failOnError) {
		this.folder = folder;
		this.failOnError = failOnError;
	}

	@Override
	public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
		return com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
	}

	@Override
	public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
		return new Parameter[] {
	            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_EXAMINE), new Parameter("folder", folder) };
	    }

	@Override
	public AbstractAJAXParser<? extends ExamineResponse> getParser() {
		return new AbstractAJAXParser<ExamineResponse>(this.failOnError) {

            @Override
            protected ExamineResponse createResponse(final Response response) {
                return new ExamineResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    public ExamineRequest ignoreError() {
        failOnError = false;
        return this;
    }

    public ExamineRequest failOnError() {
        failOnError = true;
        return this;
    }

}
