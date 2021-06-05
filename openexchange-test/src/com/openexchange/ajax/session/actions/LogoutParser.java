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

import java.io.IOException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LogoutParser extends AbstractAJAXParser<LogoutResponse> {

    private final boolean ignoreMissingSession;

    /**
     * Default constructor.
     */
    LogoutParser(boolean ignoreMissingSession) {
        super(true);
        this.ignoreMissingSession = ignoreMissingSession;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public LogoutResponse parse(final String body) throws JSONException {
        return createResponse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LogoutResponse createResponse(final Response response) throws JSONException {
        return new LogoutResponse();
    }

    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        if (HttpStatus.SC_FORBIDDEN == resp.getStatusLine().getStatusCode()) {
            // No such session
            if (ignoreMissingSession) {
                return EntityUtils.toString(resp.getEntity());
            }
        }
        return super.checkResponse(resp, request);
    }
}
