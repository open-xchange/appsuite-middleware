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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.mail.filter.api.parser.AllParser;
import com.openexchange.ajax.mail.filter.api.response.AllResponse;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link AllRequest}. Contains the data for a mail filter all request.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AllRequest extends AbstractMailFilterRequest<AllResponse> {

    public static final int[] GUI_COLUMNS = new int[] { Task.OBJECT_ID };

    @SuppressWarnings("hiding")
    private final boolean failOnError;

    private String userName = null;

    /**
     * Default constructor.
     */
    public AllRequest() {
        this(true);
    }

    /**
     * Default constructor.
     */
    public AllRequest(final boolean failOnError) {
        super();
        this.failOnError = failOnError;
    }

    public AllRequest(final String userName, final boolean failOnError) {
        super();
        this.userName = userName;
        this.failOnError = failOnError;
    }

    public AllRequest(final String userName) {
        this(userName, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
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
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST));
        if (userName != null) {
            params.add(new Parameter("username", userName));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllParser getParser() {
        return new AllParser(failOnError);
    }
}
