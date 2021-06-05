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

package com.openexchange.ajax.user.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.ajax.parser.ResponseParser;

/**
 * {@link UpdateParser}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class UpdateParser extends AbstractAJAXParser<UpdateResponse> {

    private final boolean withImage;

    /**
     * Initializes a new {@link UpdateParser}.
     * 
     * @param failOnError
     * @param withImage
     */
    public UpdateParser(boolean failOnError, boolean withImage) {
        super(failOnError);
        this.withImage = withImage;
    }

    @Override
    public UpdateResponse parse(String body) throws JSONException {
        final Response response;
        if (withImage) {
            JSONObject tmp = new JSONObject(AbstractUploadParser.extractFromCallback(body));
            response = ResponseParser.parse(tmp);
        } else {
            response = getResponse(body);
        }
        return createResponse(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UpdateResponse createResponse(final Response response) throws JSONException {
        return new UpdateResponse(response);
    }
}
