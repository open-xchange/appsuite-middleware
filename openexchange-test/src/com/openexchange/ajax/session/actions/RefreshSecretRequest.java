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

package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link RefreshSecretRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class RefreshSecretRequest extends AbstractRequest<RefreshSecretResponse> {

    private final boolean failOnError;

    public RefreshSecretRequest(final boolean failOnError) {
        super(new Parameter[] { new URLParameter(LoginServlet.PARAMETER_ACTION, LoginServlet.ACTION_REFRESH_SECRET)
        });
        this.failOnError = failOnError;
    }

    @Override
    public AbstractAJAXParser<RefreshSecretResponse> getParser() {
        return new AbstractAJAXParser<RefreshSecretResponse>(failOnError) {

            @Override
            protected RefreshSecretResponse createResponse(Response response) {
                return new RefreshSecretResponse(response);
            }
        };
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }
}
