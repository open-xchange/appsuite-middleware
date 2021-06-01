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

package com.openexchange.ajax.parser;

import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;

/**
 * Parses a <code>DocumentMetadata</code> from its JSON representation
 *
 * @deprecated Only used for testing
 */
@Deprecated
public class InfostoreParser {

    /**
     * TODO Error codes
     */
    public static class UnknownMetadataException extends Exception {

        private static final long serialVersionUID = 3260737756212968495L;
        private final String columnId;

        public UnknownMetadataException(final String id) {
            this.columnId = id;
        }

        public String getColumnId() {
            return columnId;
        }

    }

    public DocumentMetadata getDocumentMetadata(final String json) throws JSONException, OXException {

        final DocumentMetadata m = new JSONDocumentMetadata(json);
        return m;
    }

    public Metadata[] getColumns(final String[] parameterValues) throws UnknownMetadataException {
        final Metadata[] cols = new Metadata[parameterValues.length];
        int i = 0;
        for(final String idString : parameterValues) {
            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (NumberFormatException x) {
                throw new UnknownMetadataException(idString);
            }
            final Metadata m = Metadata.get(id);
            if (m == null) {
                throw new UnknownMetadataException(idString);
            }
            cols[i++] = m;
        }
        return cols;
    }

    public Metadata[] findPresentFields(final String updateBody) throws UnknownMetadataException, JSONException{
        final JSONObject obj = new JSONObject(updateBody);

        final Metadata[] metadata = new Metadata[obj.length()];
        int i = 0;
        boolean shrink = false;
        for(final Iterator iter = obj.keys(); iter.hasNext();) {
            final String key = (String) iter.next();
            final Metadata m = Metadata.get(key);
            if (m == null) {
                throw new UnknownMetadataException(key);
            }
            if (m == Metadata.FILENAME_LITERAL && (obj.optString(key) == null || obj.optString(key).equals(""))) {
                shrink = true;
            } else {
                metadata[i++] = m;
            }
        }
        if (shrink) {
            final Metadata[] shrunk = new Metadata[metadata.length-1];
            System.arraycopy(metadata, 0, shrunk, 0, shrunk.length);
            return shrunk;
        }
        return metadata;
    }
}
