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

package com.openexchange.ajax.infostore.actions;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * {@link CheckNameRequest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class CheckNameRequest extends AbstractInfostoreRequest<CheckNameResponse> {

    private final String name;
    private final boolean failOnError;

    public CheckNameRequest(String name) {
        this(name, true);
    }

    public CheckNameRequest(String name, boolean failOnError) {
        super();
        this.name = name;
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Params(AJAXServlet.PARAMETER_ACTION, "checkname", "name", name).toArray();
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public AbstractAJAXParser<? extends CheckNameResponse> getParser() {
        return new CheckNameParser(failOnError);
    }

    private static class CheckNameParser extends AbstractAJAXParser<CheckNameResponse> {

        public CheckNameParser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected CheckNameResponse createResponse(Response response) {
            return new CheckNameResponse(response);
        }

    }

}
