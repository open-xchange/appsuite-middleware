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

package com.openexchange.ajax.mail.filter.api.parser;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.filter.api.response.InsertResponse;
import com.openexchange.ajax.writer.ResponseWriter;

/**
 * {@link InsertParser}
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class InsertParser extends AbstractAJAXParser<InsertResponse> {

    /**
     * Remembers if this parser fails out with an error.
     */
    private final boolean failOnError;

    /**
     * Default constructor.
     */
    public InsertParser(final boolean failOnError) {
        super(failOnError);
        this.failOnError = failOnError;
    }

    @Override
    protected InsertResponse createResponse(final Response response) throws JSONException {
        final InsertResponse retval = new InsertResponse(response);
        final JSONObject jsonRespones = ResponseWriter.getJSON(response);
        if (failOnError) {
            if (jsonRespones.has("data")) {
                final int objectId = jsonRespones.getInt("data");
                retval.setId(objectId);
            } else {
                fail(response.getErrorMessage());
            }
        }
        return retval;
    }
}
