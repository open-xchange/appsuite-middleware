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
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.groupware.importexport.ImportResult;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ImportExportParser {

    /**
     * Prevent instantiation.
     */
    private ImportExportParser() {
        super();
    }

    public static final ImportResult parse(final String data) throws JSONException {
        final Response response = ResponseParser.parse(data);
        final ImportResult retval;
        final JSONObject json = new JSONObject(data);
        final String id = json.optString(CommonFields.ID);
        final String folderId = json.optString(CommonFields.FOLDER_ID);
        final long lastModified = json.optLong(CommonFields.LAST_MODIFIED);
        retval = new ImportResult(id, folderId, lastModified);
        if (response.getWarnings() != null && response.getWarnings().size() > 0) {
            retval.setException(response.getWarnings().get(0));
        }
        if (response.hasError()) {
            retval.setException(response.getException());
        }

        JSONArray warnings = json.optJSONArray("warnings");
        List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>();

        if (warnings != null) {
            for (int i = 0, size = warnings.length(); i < size; i++) {
                String code = warnings.getJSONObject(i).getString("code");
                String message = warnings.getJSONObject(i).getString("error");
                int number = Integer.valueOf(code.substring(code.indexOf('-') + 1, code.length())).intValue();
                ConversionWarning warning = new ConversionWarning(i, getCode(number), message);
                conversionWarnings.add(warning);
            }
            retval.addWarnings(conversionWarnings);
        }

        return retval;
    }

    private static Code getCode(int number) {
        for (Code code : Code.values()) {
            if (code.getNumber() == number) {
                return code;
            }
        }
        return null;
    }
}
