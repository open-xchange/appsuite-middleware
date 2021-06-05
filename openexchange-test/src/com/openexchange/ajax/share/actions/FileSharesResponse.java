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

package com.openexchange.ajax.share.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;

/**
 * {@link FileSharesResponse}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileSharesResponse extends AbstractAJAXResponse {

    private final int[] columns;
    private final JSONArray data;

    /**
     * Initializes a new {@link FileSharesResponse}.
     *
     * @param response The underlying response
     * @param columns The requested columns
     */
    public FileSharesResponse(Response response, int[] columns) {
        super(response);
        this.columns = columns;
        this.data = response.hasError() ? null : (JSONArray) response.getData();
    }

    /**
     * Gets the shared files.
     *
     * @param timeZone The client timezone to consider
     * @return The shared files
     */
    public List<FileShare> getShares(TimeZone timeZone) throws JSONException, OXException {
        if (null != data) {
            return parse(data, columns, timeZone);
        }
        return null;
    }

    private static List<FileShare> parse(JSONArray jsonArray, int[] columns, TimeZone timeZone) throws JSONException, OXException {
        List<FileShare> shares = new ArrayList<FileShare>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            shares.add(FileShare.parse(jsonArray.getJSONArray(i), columns, timeZone));
        }
        return shares;
    }

}
