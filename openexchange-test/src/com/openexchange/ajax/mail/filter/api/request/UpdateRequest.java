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
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.parser.UpdateParser;
import com.openexchange.ajax.mail.filter.api.response.UpdateResponse;
import com.openexchange.java.Strings;

/**
 * Implements creating the necessary values for a rule update request. All
 * necessary values are read from the rule object.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class UpdateRequest extends AbstractMailFilterRequest<UpdateResponse> {

    private final Rule rule;
    private final String forUser;
    @SuppressWarnings("hiding")
    private final boolean failOnError;

    /**
     * Initialises a new {@link UpdateRequest}.
     */
    public UpdateRequest(Rule rule) {
        this(rule, null);
    }

    /**
     * Default constructor.
     *
     * @param rule Rule object with updated attributes.
     */
    public UpdateRequest(final Rule rule, final String forUser) {
        this(rule, forUser, true);
    }

    /**
     * Default constructor.
     *
     * @param rule
     *            Rule object with updated attributes.
     */
    public UpdateRequest(final Rule rule, final String forUser, boolean failOnError) {
        super();
        this.rule = rule;
        this.forUser = forUser;
        this.failOnError = failOnError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        return convert(rule);
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
        List<Parameter> parameters = new LinkedList<>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE));
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ID, rule.getId()));
        if (Strings.isNotEmpty(forUser)) {
            parameters.add(new Parameter("username", forUser));
        }
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateParser getParser() {
        return new UpdateParser(failOnError);
    }

    /**
     * @return the rule
     */
    protected Rule getRule() {
        return rule;
    }
}
