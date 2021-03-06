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
 * {@link PostCopyCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class PostCopyCall extends AbstractPostCall<String> {

    private final String id;
    private final DefaultFile file;
    private final String pushToken;
    private final int[] columns;
    private final InputStream data;

    /**
     * Initializes a new {@link PostCopyCall}.
     *
     * @param id The ID of the file to copy
     * @param file The file containing the modified fields of the destination infoitem.
     * @param data The binary data to apply
     */
    public PostCopyCall(String id, DefaultFile file, InputStream data) {
        this(id, file, data, null, null);
    }

    /**
     * Initializes a new {@link PostCopyCall}.
     *
     * @param id The ID of the file to copy
     * @param file The file to copy
     * @param data The binary data to apply
     * @param columns the modified columns to update, or null to update all
     */
    public PostCopyCall(String id, DefaultFile file, InputStream data, int[] columns) {
        this(id, file, data, columns, null);
    }

    /**
     * Initializes a new {@link PostCopyCall}.
     *
     * @param id The ID of the file to copy
     * @param file The file to copy
     * @param data The binary data to apply
     * @param columns the modified columns to update, or null to update all
     * @param pushToken The drive push token
     */
    public PostCopyCall(String id, DefaultFile file, InputStream data, int[] columns, String pushToken) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.file = Objects.requireNonNull(file, "file must not be null");
        this.data = data;
        this.columns = columns;
        this.pushToken = pushToken;
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
        parameters.put("id", id);
        putIfNotEmpty(parameters, "pushToken", pushToken);
    }

    @Override
    protected String getAction() {
        return "copy";
    }

}
