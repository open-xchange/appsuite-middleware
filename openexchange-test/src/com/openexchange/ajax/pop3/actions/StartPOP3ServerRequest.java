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

package com.openexchange.ajax.pop3.actions;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;

/**
 * Depends on bundle com.openexchange.test.pop3, contained in backend-test.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class StartPOP3ServerRequest implements AJAXRequest<StartPOP3ServerResponse> {

    private final boolean failOnConnect;

    private final boolean failOnAuth;

    private final int acceptedConnects;

    public StartPOP3ServerRequest(final boolean failOnConnect, final boolean failOnAuth) {
        this(failOnConnect, failOnAuth, 0);
    }

    public StartPOP3ServerRequest(final boolean failOnConnect, final boolean failOnAuth, final int acceptedConnects) {
        super();
        this.failOnConnect = failOnConnect;
        this.failOnAuth = failOnAuth;
        this.acceptedConnects = acceptedConnects;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/pop3test";
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Parameter[] { new URLParameter("action", "startServer"), new URLParameter("failOnConnect", failOnConnect), new URLParameter("failOnAuth", failOnAuth), new URLParameter("acceptedConnects", acceptedConnects),
        };
    }

    @Override
    public AbstractAJAXParser<? extends StartPOP3ServerResponse> getParser() {
        return new Parser(true);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    private static final class Parser extends AbstractAJAXParser<StartPOP3ServerResponse> {

        /**
         * Initializes a new {@link Parser}.
         * 
         * @param failOnError
         */
        protected Parser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected StartPOP3ServerResponse createResponse(Response response) throws JSONException {
            return new StartPOP3ServerResponse(response);
        }
    }
}
