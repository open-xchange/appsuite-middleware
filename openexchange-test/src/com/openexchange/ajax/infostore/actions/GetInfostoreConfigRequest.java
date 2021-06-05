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

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * {@link GetInfostoreConfigRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class GetInfostoreConfigRequest extends AbstractInfostoreRequest<AbstractAJAXResponse> {

    private String name;

    public GetInfostoreConfigRequest(String name) {
        super();
        this.name = name;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Parameter[0];
    }

    @Override
    public AbstractAJAXParser<? extends AbstractAJAXResponse> getParser() {
        return new AbstractAJAXParser<AbstractAJAXResponse>(getFailOnError()) {

            @Override
            protected AbstractAJAXResponse createResponse(Response response) {
                return new AbstractAJAXResponse(response) { /* Empty */ };
            }
        };
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public String getServletPath() {
        return "/ajax/config" + name;
    }
}
