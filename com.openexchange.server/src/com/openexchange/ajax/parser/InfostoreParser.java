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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.ajax.parser;

import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;

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
            } catch (final NumberFormatException x) {
                throw new UnknownMetadataException(idString);
            }
            final Metadata m = Metadata.get(id);
            if(m == null) {
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
            if(m == null) {
                throw new UnknownMetadataException(key);
            }
            if(m == Metadata.FILENAME_LITERAL && (obj.optString(key) == null || obj.optString(key).equals(""))) {
                shrink = true;
            } else {
                metadata[i++] = m;
            }
        }
        if(shrink) {
            final Metadata[] shrunk = new Metadata[metadata.length-1];
            System.arraycopy(metadata, 0, shrunk, 0, shrunk.length);
            return shrunk;
        }
        return metadata;
    }
}
