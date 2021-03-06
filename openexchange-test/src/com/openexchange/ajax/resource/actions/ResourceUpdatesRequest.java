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

package com.openexchange.ajax.resource.actions;

import java.util.Date;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * {@link ResourceUpdatesRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ResourceUpdatesRequest extends AbstractResourceRequest<ResourceUpdatesResponse> {

    private final boolean failOnError;
    private final Date lastModified;

    @Override
    public Object getBody() {
        return null;
    }

    public ResourceUpdatesRequest(Date since, boolean failOnError) {
        this.failOnError = failOnError;
        this.lastModified = since;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES, AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(this.lastModified.getTime())).toArray();
    }

    @Override
    public AbstractAJAXParser<? extends ResourceUpdatesResponse> getParser() {
        return new AbstractAJAXParser<ResourceUpdatesResponse>(this.failOnError) {

            @Override
            protected ResourceUpdatesResponse createResponse(Response response) {
                return new ResourceUpdatesResponse(response);
            }

        };
    }

}
