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

package com.openexchange.ajax.contact.action;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractInsertParser;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.ajax.parser.ResponseParser;

/**
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class InsertParser extends AbstractInsertParser<InsertResponse> {

    private final boolean withImage;

    /**
     * Default constructor.
     */
    InsertParser(final boolean failOnError, final boolean withImage) {
        super(failOnError);
        this.withImage = withImage;
    }

    @Override
    public InsertResponse parse(String body) throws JSONException {
        final Response response;
        if (withImage) {
            JSONObject tmp = new JSONObject(AbstractUploadParser.extractFromCallback(body));
            response = ResponseParser.parse(tmp);
        } else {
            response = getResponse(body);
        }
        return createResponse(response);
    }

    @Override
    protected InsertResponse instantiateResponse(final Response response) {
        return new InsertResponse(response);
    }
}
