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

package com.openexchange.api.client.common.calls.folders;

import java.util.List;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;


/**
 * {@link RemoteFolderParser}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class RemoteFolderParser extends AbstractHttpResponseParser<List<RemoteFolder>> {

    private final int[] columns;

    public RemoteFolderParser(int[] columns) {
        super();
        this.columns = columns;
    }

    @Override
    public List<RemoteFolder> parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
        JSONArray data = commonResponse.getJSONArray();
        RemoteFolderMapper mapper = new RemoteFolderMapper();
        List<RemoteFolder> folders = mapper.deserialize(data, mapper.getMappedFields(columns));
        return folders;
    }

}
