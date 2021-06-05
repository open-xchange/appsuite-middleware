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

package com.openexchange.ajax.mail.filter.api.request;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.filter.api.parser.DeleteParser;
import com.openexchange.ajax.mail.filter.api.response.DeleteResponse;

/**
 * {@link DeleteRequest}. Stores parameters for the delete request.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeleteRequest extends AbstractMailFilterRequest<DeleteResponse> {

    private final int ruleId;

    @SuppressWarnings("hiding")
    private boolean failOnError = true;

    /**
     * Default constructor.
     */
    public DeleteRequest(final int ruleId) {
        this(ruleId, true);
    }

    /**
     * Initialises a new {@link DeleteRequest}.
     *
     * @param ruleId The rule identifier
     * @param failOnError
     */
    public DeleteRequest(final int ruleId, final boolean failOnError) {
        super();
        this.ruleId = ruleId;
        this.failOnError = failOnError;
    }

    /**
     * Sets the fail on error flag
     *
     * @param failOnError
     */
    @Override
    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(DataFields.ID, ruleId);

        return json;
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
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter(AJAXServlet.PARAMETER_ID, ruleId)
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractAJAXParser<DeleteResponse> getParser() {
        return new DeleteParser(failOnError);
    }
}
