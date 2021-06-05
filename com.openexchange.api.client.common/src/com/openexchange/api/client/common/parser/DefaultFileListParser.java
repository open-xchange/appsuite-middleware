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

package com.openexchange.api.client.common.parser;

import java.util.List;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.api.client.common.calls.infostore.mapping.DefaultFileMapper;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;

/**
 * {@link DefaultFileListParser}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class DefaultFileListParser extends AbstractHttpResponseParser<List<DefaultFile>> {

    private final int[] columns;

    /**
     * Initializes a new {@link DefaultFileListParser}.
     *
     * @param columns The columns to parse
     */
    public DefaultFileListParser(int[] columns) {
        super();
        this.columns = columns;
    }

    @Override
    public List<DefaultFile> parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
        JSONArray data = commonResponse.getJSONArray();
        DefaultFileMapper mapper = new DefaultFileMapper();
        List<DefaultFile> files = mapper.deserialize(data, mapper.getMappedFields(columns));
        return files;
    }

}
