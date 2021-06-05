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

import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.writer.ResponseWriter;

/**
 * {@link UpdatesInfostoreParser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdatesInfostoreParser extends AbstractAJAXParser<UpdatesInfostoreResponse> {

    public UpdatesInfostoreParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected UpdatesInfostoreResponse createResponse(Response response) throws JSONException {
        Set<String> deleted = new HashSet<String>();
        Set<JSONArray> newAndModified = new HashSet<JSONArray>();
        JSONObject json = ResponseWriter.getJSON(response);
        JSONArray data = json.getJSONArray(ResponseFields.DATA);
        for (int i = 0; i < data.length(); i++) {
            Object obj = data.get(i);
            if (obj instanceof String) {
                deleted.add((String) obj);
            } else {
                newAndModified.add((JSONArray) obj);
            }
        }

        return new UpdatesInfostoreResponse(response, deleted, newAndModified);
    }

}
