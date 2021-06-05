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

import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.json.FileMetadataWriter;
import com.openexchange.file.storage.json.actions.files.TestFriendlyInfostoreRequest;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractInfostoreRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    private boolean failOnError;
    private TimeZone timeZone;

    public static final String INFOSTORE_URL = "/ajax/infostore";

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean getFailOnError() {
        return failOnError;
    }

    /**
     * Gets the timeZone
     *
     * @return The timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the timeZone
     *
     * @param timeZone The timeZone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String getServletPath() {
        return INFOSTORE_URL;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    public JSONObject writeJSON(File data) {
        return writeJSON(data, null);
    }

    public JSONObject writeJSON(File data, Field[] fields) {
        return convertToJSON(data, fields, getTimeZone());
    }

    public static JSONObject convertToJSON(File data, Field[] fields) {
        return convertToJSON(data, fields, null);
    }

    public static JSONObject convertToJSON(File data, Field[] fields, TimeZone timeZone) {
        String timezoneId = null != timeZone ? timeZone.getID() : "UTC";
        FileMetadataWriter writer = new com.openexchange.file.storage.json.FileMetadataWriter(null);
        if (fields == null) {
            return writer.write(new TestFriendlyInfostoreRequest(timezoneId), data);
        }

        return writer.writeSpecific(new TestFriendlyInfostoreRequest(timezoneId), data, fields, null);
    }

    public JSONArray writeFolderAndIDList(List<String> ids, List<String> folders) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0, length = ids.size(); i < length; i++) {
            JSONObject tuple = new JSONObject();
            tuple.put(AJAXServlet.PARAMETER_ID, ids.get(i));
            tuple.put(AJAXServlet.PARAMETER_FOLDERID, folders.get(i));
            array.put(tuple);
        }
        return array;
    }
}
