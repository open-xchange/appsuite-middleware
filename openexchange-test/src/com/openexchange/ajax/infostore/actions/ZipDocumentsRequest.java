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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.file.storage.FileStorageFileAccess;

/**
 * {@link ZipDocumentsRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ZipDocumentsRequest extends AbstractInfostoreRequest<ZipDocumentsResponse> {

    class ZipDocumentsParser extends AbstractAJAXParser<ZipDocumentsResponse> {

        /**
         * Default constructor.
         */
        ZipDocumentsParser(final boolean failOnError) {
            super(failOnError);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected ZipDocumentsResponse createResponse(final Response response) throws JSONException {
            return new ZipDocumentsResponse(response);
        }
    }

    private final List<IdVersionPair> pairs;
    private final String folderId;
    private final boolean failOnError;

    /**
     * Initializes a new {@link ZipDocumentsRequest}.
     */
    public ZipDocumentsRequest(List<IdVersionPair> pairs, String folderId) {
        this(pairs, folderId, true);
    }

    /**
     * Initializes a new {@link ZipDocumentsRequest}.
     */
    public ZipDocumentsRequest(List<IdVersionPair> pairs, String folderId, boolean failOnError) {
        super();
        this.pairs = pairs;
        this.folderId = folderId;
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONArray ja = new JSONArray(pairs.size());
        for (final IdVersionPair pair : pairs) {
            final JSONObject jo = new JSONObject(3);
            jo.put(AJAXServlet.PARAMETER_FOLDERID, folderId);
            jo.put(AJAXServlet.PARAMETER_ID, pair.getIdentifier());
            final String version = pair.getVersion();
            if (null != version) {
                jo.put("version", version);
            }
            ja.put(jo);
        }
        return ja;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, "zipdocuments"), new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId) };
    }

    @Override
    public AbstractAJAXParser<ZipDocumentsResponse> getParser() {
        return new ZipDocumentsParser(failOnError);
    }

    /**
     * {@link IdVersionPair} - A pair of an identifier and a version.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    public static final class IdVersionPair {

        private final String identifier;
        private final String version;

        /**
         * Initializes a new {@link IdVersionPair}.
         *
         * @param identifier
         * @param version
         */
        public IdVersionPair(String identifier, String version) {
            super();
            this.identifier = identifier;
            this.version = version;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Gets the version
         *
         * @return The version or {@link FileStorageFileAccess#CURRENT_VERSION}
         */
        public String getVersion() {
            return version;
        }
    }

}
