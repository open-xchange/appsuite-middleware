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

package com.openexchange.userfeedback;

import java.io.IOException;
import java.io.OutputStreamWriter;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.userfeedback.export.ExportResult;
import com.openexchange.userfeedback.export.ExportResultConverter;
import com.openexchange.userfeedback.export.ExportType;

/**
 * {@link ErrorResultConverter}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class ErrorResultConverter implements ExportResultConverter {

    private final JSONObject error;

    public ErrorResultConverter(String error) {
        JSONObject jsonError = new JSONObject();
        try {
            jsonError.put("error", error);
        } catch (JSONException e) {
            // won't happen
        }
        this.error = jsonError;
    }

    @Override
    public ExportResult get(final ExportType type) {
        final JSONObject lError = error;
        return new ExportResult() {

            @Override
            public Object getResult() {
                switch (type) {
                    case CSV:
                        return csvErrorResult();
                    case RAW:
                    default:
                        return lError;
                }
            }

            @SuppressWarnings("resource")
            private Object csvErrorResult() {
                ThresholdFileHolder sink = new ThresholdFileHolder();
                OutputStreamWriter writer = new OutputStreamWriter(sink.asOutputStream(), Charsets.UTF_8);

                try {
                    writer.write(lError.toString());
                    writer.flush();
                    return sink.getClosingStream();
                } catch (IOException | OXException e) {
                    sink.close();
                }
                return null;
            }
        };
    }

}
