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

package com.openexchange.ajax.folder.actions;

import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.json.FileMetadataFieldParser;
import com.openexchange.file.storage.meta.FileFieldSet;

/**
 * {@link VersionsParser}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class VersionsParser extends AbstractAJAXParser<VersionsResponse> {

    private static final String DATA = "data";
    private final int[] columns;

    protected VersionsParser(boolean failOnError, int[] columns) {
        super(failOnError);
        Arrays.sort(columns);
        this.columns = columns;
    }

    @Override
    protected VersionsResponse createResponse(Response response) throws JSONException {
        FileFieldSet fileFieldSet = new FileFieldSet();

        VersionsResponse versionsResponse = new VersionsResponse(response);
        JSONObject json = ResponseWriter.getJSON(response);
        if (json.has(DATA)) {
            JSONArray versions = (JSONArray) json.get(DATA);
            for (int i = 0; i < versions.length(); i++) {
                DefaultFile metadata = new DefaultFile();

                int columncount = 0;
                for (int column : columns) {
                    Field field = Field.get(column);
                    if (null != field) {
                        Object orig = ((JSONArray) versions.get(i)).get(columncount);
                        Object converted;
                        try {
                            converted = FileMetadataFieldParser.convert(field, orig);
                            field.doSwitch(fileFieldSet, metadata, converted);
                        } catch (OXException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        columncount++;
                    }
                }

                versionsResponse.addVersion(metadata);
            }
        }
        return versionsResponse;
    }
}
