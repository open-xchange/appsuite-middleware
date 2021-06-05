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

package com.openexchange.ajax.jslob.actions;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link SetRequest}
 *
 * @author <a href="mailto:markus.wagner@open-xchange.com">Markus Wagner</a>
 */
public class SetRequest extends AbstractJSlobRequest<SetResponse> {

    private final String identifier;

    private final String value;

    private final boolean failOnError;

    /**
     * Initializes a new {@link SetRequest}.
     */
    public SetRequest(final String identifier, final String value) {
        this(identifier, value, true);
    }

    /**
     * Initializes a new {@link SetRequest}.
     */
    public SetRequest(final String identifier, final String value, final boolean failOnError) {
        super();
        this.identifier = identifier;
        this.value = value;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "set"));
        list.add(new Parameter(AJAXServlet.PARAMETER_ID, identifier));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public Object getBody() {
        return value;
    }

    @Override
    public AbstractAJAXParser<? extends SetResponse> getParser() {
        return new SetParser(failOnError);
    }

}
