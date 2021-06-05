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

package com.openexchange.api.client.common.calls.infostore;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractPostCall;
import com.openexchange.api.client.common.calls.infostore.mapping.DefaultFileMapper;
import com.openexchange.api.client.common.parser.StringParser;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File.Field;

/**
 * {@link PostUpdateCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class PostUpdateCall extends AbstractPostCall<String> {

    private final DefaultFile file;
    private final InputStream data;
    private final long timestamp;
    private final int offset;
    private final String pushToken;
    private final int[] columns;

    /**
     * Initializes a new {@link PostUpdateCall}.
     *
     * @param file The file to update
     * @param data The binary data to update
     * @param timestamp The last known timestamp/sequencenumber
     */
    public PostUpdateCall(DefaultFile file, InputStream data, long timestamp) {
        this(file, data, timestamp, -1, null, null);
    }

    /**
     * Initializes a new {@link PostUpdateCall}.
     *
     * @param file The file to update
     * @param data The binary data to update
     * @param timestamp The last known timestamp/sequencenumber
     * @param columns The column IDs of the file's fields to update
     */
    public PostUpdateCall(DefaultFile file, InputStream data, long timestamp, int[] columns) {
        this(file, data, timestamp, -1, null, columns);
    }

    /**
     * Initializes a new {@link PostUpdateCall}.
     *
     * @param file The file to update
     * @param data The binary data to update
     * @param timestamp The last known timestamp/sequencenumber
     * @param offset The start offset in bytes where to append the data to the document, must be equal to the actual document's length. Only available if the underlying File storage account supports the "RANDOM_FILE_ACCESS" capability.
     * @param pushToken The drive push token
     * @param columns The columns to update or null in order to update
     */
    public PostUpdateCall(DefaultFile file, InputStream data, long timestamp, int offset, String pushToken, int[] columns) {
        super();
        this.file = Objects.requireNonNull(file, "file must not be null");
        this.data = data;
        this.timestamp = timestamp;
        this.offset = offset;
        this.pushToken = pushToken;
        this.columns = columns;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        DefaultFileMapper mapper = new DefaultFileMapper();
        Field[] fields = columns != null ? mapper.getMappedFields(columns) : mapper.getAssignedFields(file);
        JSONObject json = mapper.serialize(file, fields);
        return ApiClientUtils.createMultipartBody(json, data, file.getFileName(), file.getFileMIMEType());
    }

    @Override
    public HttpResponseParser<String> getParser() {
        return StringParser.getInstance();
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("force_json_response", "true");
        parameters.put("id", file.getId());
        parameters.put("timestamp", String.valueOf(timestamp));
        if (offset > -1) {
            parameters.put("offset", String.valueOf(timestamp));
        }
        putIfNotEmpty(parameters, "pushToken", pushToken);
    }

    @Override
    protected String getAction() {
        return "update";
    }
}
