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
