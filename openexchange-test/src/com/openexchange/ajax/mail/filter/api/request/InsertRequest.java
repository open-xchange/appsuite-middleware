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

import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.parser.InsertParser;
import com.openexchange.ajax.mail.filter.api.response.InsertResponse;

/**
 * {@link InsertRequest}. Stores the parameters for inserting the appointment.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class InsertRequest extends AbstractMailFilterRequest<InsertResponse> {

    /**
     * Rule to insert.
     */
    final Rule rule;

    /**
     * The affected user
     */
    final String forUser;

    /**
     * Should the parser fail on error in server response.
     */
    @SuppressWarnings("hiding")
    final boolean failOnError;

    /**
     * Initialises a new {@link InsertRequest}.
     *
     * @param rule The {@link Rule} to insert
     */
    public InsertRequest(Rule rule) {
        this(rule, null, true);
    }

    /**
     * Initialises a new {@link InsertRequest}.
     *
     * @param rule The {@link Rule} to insert
     * @param failOnError The fail on error flag
     */
    public InsertRequest(Rule rule, boolean failOnError) {
        this(rule, null, failOnError);
    }

    /**
     * default constructor.
     *
     * @param rule Rule to insert. <code>true</code> to check the response for error messages.
     */
    public InsertRequest(final Rule rule, final String forUser) {
        this(rule, forUser, true);
    }

    /**
     * More detailed constructor.
     *
     * @param rule Rule to insert.
     * @param failOnError <code>true</code> to check the response for error messages.
     */
    public InsertRequest(final Rule rule, final String forUser, final boolean failOnError) {
        super();
        this.rule = rule;
        this.forUser = forUser;
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        return convert(rule);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> parameters = new LinkedList<Parameter>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        if (forUser != null) {
            parameters.add(new Parameter("username", forUser));
        }
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AbstractAJAXParser<InsertResponse> getParser() {
        return new InsertParser(failOnError);
    }
}
