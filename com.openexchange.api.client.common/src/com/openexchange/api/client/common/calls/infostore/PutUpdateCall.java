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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractPutCall;
import com.openexchange.api.client.common.calls.infostore.mapping.DefaultFileMapper;
import com.openexchange.api.client.common.parser.StringParser;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File.Field;

/**
 * {@link PutUpdateCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class PutUpdateCall extends AbstractPutCall<String> {

    private final String id;
    private final DefaultFile file;
    private final long timestamp;
    private final int[] columns;
    private final String pushToken;

    public PutUpdateCall(DefaultFile file, long timestamp, int[] columns) {
        this(file.getId(), file, timestamp, columns, null);
    }

    /**
     * Initializes a new {@link PutUpdateCall}.
     *
     * @param id The ID of the item to update
     * @param file The file to update
     * @param timestamp the last known timestamp/sequencenumber
     * @param columns the column IDs of the file's fields to update
     */
    public PutUpdateCall(String id, DefaultFile file, long timestamp, int[] columns) {
        this(id, file, timestamp, columns, null);
    }

    /**
     * Initializes a new {@link PutUpdateCall}.
     *
     * @param id The ID of the item to update
     * @param file The file to update containing the new data
     * @param timestamp the last known timestamp/sequencenumber
     * @param columns the column IDs of the file's fields to update
     * @param pushToken The drive push token
     */
    public PutUpdateCall(String id, DefaultFile file, long timestamp, int[] columns, String pushToken) {
        this.id = id;
        this.file = Objects.requireNonNull(file, "file must not be null");
        this.timestamp = timestamp;
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
        JSONObject json = new JSONObject();
        DefaultFileMapper mapper = new DefaultFileMapper();
        Field[] fields = columns != null ? mapper.getMappedFields(columns) : mapper.getAssignedFields(file);
        JSONObject fileObject = mapper.serialize(file, Arrays.stream(fields).filter(Objects::nonNull).toArray(Field[]::new));
        json.put("file", fileObject);
        return ApiClientUtils.createJsonBody(json);
    }

    @Override
    public HttpResponseParser<String> getParser() {
        return new StringParser();
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("timestamp", String.valueOf(timestamp));
        putIfNotEmpty(parameters,"pushToken", pushToken);
    }

    @Override
    protected String getAction() {
        return "update";
    }
}
