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
import java.util.Collections;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.InputStreamParser;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link DocumentCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class DocumentCall extends AbstractGetCall<DocumentResponse> {

    /**
     * The If-None-Match header-name
     */
    private static final String HEADER_IF_NONE_MATCH = "If-None-Match";

    /**
     * The ETag header-name
     */
    private static final String HEADER_ETAG = "ETag";

    /**
     * The "download" delivery method
     */
    public static final String DELIVERY_METHOD_DOWNLOAD = "download";

    private final String folderId;
    private final String id;
    private final String version;
    private final String delivery;
    private final String eTag;

    /**
     * Initializes a new {@link DocumentCall}.
     *
     * @param folderId The ID of the folder
     * @param id The ID of the document
     */
    public DocumentCall(String folderId, String id) {
        this(folderId, id, null);
    }

    /**
     * Initializes a new {@link DocumentCall}.
     *
     * @param folderId The ID of the folder
     * @param id The ID of the document
     * @param version The version to fetch
     */
    public DocumentCall(String folderId, String id, String version) {
        this(folderId, id, version, null, null);
    }

    /**
     * Initializes a new {@link DocumentCall}.
     *
     * @param folderId The ID of the folder
     * @param id The ID of the document
     * @param version The version to fetch, or null to omit
     * @param delivery The delivery method to use, or null to omit
     */
    public DocumentCall(String folderId, String id, String version, String delivery) {
        this(folderId, id, version, delivery, null);
    }

    /**
     * Initializes a new {@link DocumentCall}.
     *
     * @param folderId The ID of the folder
     * @param id The ID of the document
     * @param version The version to fetch, or null to omit
     * @param delivery The delivery method to use, or null to omit
     * @param eTag an ETag to add to the request
     */
    public DocumentCall(String folderId, String id, String version, String delivery, String eTag) {
        this.folderId = folderId;
        this.id = id;
        this.version = version;
        this.delivery = delivery;
        this.eTag = eTag;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("folder", folderId);
        putIfPresent(parameters, "version", version);
        putIfPresent(parameters, "delivery", delivery);
    }

    @Override
    protected String getAction() {
        return "document";
    }

    @Override
    public HttpResponseParser<DocumentResponse> getParser() {
        return new HttpResponseParser<DocumentResponse>() {

            @SuppressWarnings("resource")
            @Override
            public DocumentResponse parse(HttpResponse response, HttpContext httpContext) throws OXException {
                DocumentResponse ret = new DocumentResponse();
                ret.setETag(ApiClientUtils.getHeaderValue(response, HEADER_ETAG));
                if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_MODIFIED) {
                    InputStream stream = new InputStreamParser().parse(response, httpContext);
                    if (stream != null) {
                        ret.setInputStream(stream);
                    }
                }
                return ret;
            }
        };
    }

    @SuppressWarnings("null")
    @Override
    @NonNull
    public Map<String, String> getHeaders() {
        return Strings.isEmpty(eTag) ? Collections.emptyMap() : Collections.singletonMap(HEADER_IF_NONE_MATCH, eTag);
    }
}
