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

/**
 * {@link NewCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class NewCall extends AbstractPostCall<String> {

    private final Boolean tryAddVersion;
    private final String pushToken;
    private final DefaultFile file;
    private final InputStream data;

    /**
     * Initializes a new {@link NewCall}.
     *
     * @param file The file meta data
     * @param data THe file data
     */
    public NewCall(DefaultFile file, InputStream data) {
        this(file, data, null);
    }

    /**
     * Initializes a new {@link NewCall}.
     *
     * @param file The file meta data
     * @param data THe file data
     * @param tryAddVersion Add new file version if file name exists
     */
    public NewCall(DefaultFile file, InputStream data, Boolean tryAddVersion) {
        this(file, data, tryAddVersion, null);
    }

    /**
     * Initializes a new {@link NewCall}.
     *
     * @param file The file meta data
     * @param data THe file data
     * @param tryAddVersion Add new file version if file name exists
     * @param pushToken The push token of the drive client
     */
    public NewCall(DefaultFile file, InputStream data, Boolean tryAddVersion, String pushToken) {
        this.file = Objects.requireNonNull(file, "file must not be null");
        this.data = Objects.requireNonNull(data, "data must not be null");
        this.tryAddVersion = tryAddVersion;
        this.pushToken = pushToken;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    protected String getAction() {
        return "new";
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        DefaultFileMapper mapper = new DefaultFileMapper();
        JSONObject json = mapper.serialize(file, mapper.getAssignedFields(file));
        return ApiClientUtils.createMultipartBody(json, data, file.getFileName(), file.getFileMIMEType());
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("force_json_response", "true");
        putIfPresent(parameters, "try_add_version", tryAddVersion);
        putIfPresent(parameters, "pushToken", pushToken);
    }

    @Override
    public HttpResponseParser<String> getParser() {
        return StringParser.getInstance();
    }
}
