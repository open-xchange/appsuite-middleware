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

package com.openexchange.ajax.importexport.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.ajax.parser.ResponseParser;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class VCardImportParser extends AbstractUploadParser<VCardImportResponse> {

    /**
     * @param failOnError
     */
    public VCardImportParser(final boolean failOnError) {
        super(failOnError);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected VCardImportResponse createResponse(final Response response) {
        final VCardImportResponse retval = new VCardImportResponse(response);
        final JSONArray array = (JSONArray) response.getData();
        if (null != array) {
            final List<Response> tmp = new ArrayList<Response>();
            for (int i = 0; i < array.length(); i++) {
                try {
                    tmp.add(ResponseParser.parse(array.getJSONObject(i)));
                } catch (JSONException e) {
                    if (isFailOnError()) {
                        fail(e.getMessage());
                    }
                }
            }
            retval.setResponses(tmp.toArray(new Response[tmp.size()]));
        }
        return retval;
    }

}
