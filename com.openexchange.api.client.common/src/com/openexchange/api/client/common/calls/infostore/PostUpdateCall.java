/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
