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

package com.openexchange.snippet.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.SnippetExceptionCodes;

/**
 * {@link SnippetJsonParser} - The JSON parser for snippets.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SnippetJsonParser {

    /**
     * Initializes a new {@link SnippetJsonParser}.
     */
    private SnippetJsonParser() {
        super();
    }

    /**
     * Parses specified JSON into given snippet.
     *
     * @param jsonSnippet The JSON snippet
     * @param snippet The snippet
     * @throws JSONException If a JSON error occurs
     * @throws OXException If received JSON data is invalid
     */
    public static void parse(final JSONObject jsonSnippet, final DefaultSnippet snippet) throws JSONException, OXException {
        parse(jsonSnippet, snippet, null);
    }

    /**
     * Parses specified JSON into given snippet.
     *
     * @param jsonSnippet The JSON snippet
     * @param snippet The snippet
     * @throws JSONException If a JSON error occurs
     * @throws OXException If received JSON data is invalid
     */
    public static void parse(final JSONObject jsonSnippet, final DefaultSnippet snippet, final Set<Property> set) throws JSONException, OXException {
        String key = Property.ACCOUNT_ID.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            snippet.setAccountId(jsonSnippet.getInt(key));
            if (null != set) {
                set.add(Property.ACCOUNT_ID);
            }
        }
        key = "content";
        if (jsonSnippet.hasAndNotNull(key)) {
            snippet.setContent(jsonSnippet.getString(key));
            if (null != set) {
                set.add(Property.CONTENT);
            }
        }
        key = Property.CREATED_BY.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            snippet.setCreatedBy(jsonSnippet.getInt(key));
            if (null != set) {
                set.add(Property.CREATED_BY);
            }
        }
        key = Property.DISPLAY_NAME.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            String displayName = jsonSnippet.getString(key);
            if (displayName.length() > 255) {
                throw SnippetExceptionCodes.DISPLAY_NAME_TOO_LONG.create();
            }
            snippet.setDisplayName(displayName);
            if (null != set) {
                set.add(Property.DISPLAY_NAME);
            }
        }
        key = Property.ID.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            String id = jsonSnippet.getString(key);
            if (id.length() > 64) {
                throw SnippetExceptionCodes.ID_TOO_LONG.create();
            }
            snippet.setId(id);
            if (null != set) {
                set.add(Property.ID);
            }
        }
        key = Property.MISC.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            snippet.setMisc(jsonSnippet.get(key));
            if (null != set) {
                set.add(Property.MISC);
            }
        }
        key = Property.MODULE.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            String module = jsonSnippet.getString(key);
            if (module.length() > 255) {
                throw SnippetExceptionCodes.MODULE_TOO_LONG.create();
            }
            snippet.setModule(module);
            if (null != set) {
                set.add(Property.MODULE);
            }
        }
        key = Property.SHARED.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            snippet.setShared(jsonSnippet.getBoolean(key));
            if (null != set) {
                set.add(Property.SHARED);
            }
        }
        key = Property.TYPE.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            String type = jsonSnippet.getString(key);
            if (type.length() > 255) {
                throw SnippetExceptionCodes.TYPE_TOO_LONG.create();
            }
            snippet.setType(type);
            if (null != set) {
                set.add(Property.TYPE);
            }
        }
        key = "props";
        if (jsonSnippet.hasAndNotNull(key)) {
            final JSONObject jsonUnnamedProperties = jsonSnippet.getJSONObject(key);
            final Map<String, Object> up = new HashMap<String, Object>(jsonUnnamedProperties.length());
            for (final Map.Entry<String, Object> entry : jsonUnnamedProperties.entrySet()) {
                up.put(entry.getKey(), entry.getValue());
            }
            snippet.putUnnamedProperties(up);
            if (null != set) {
                set.add(Property.PROPERTIES);
            }
        }
        key = "files";
        if (jsonSnippet.hasAndNotNull(key)) {
            final JSONArray jsonAttachments = jsonSnippet.getJSONArray(key);
            final int len = jsonAttachments.length();
            final List<Attachment> list = new ArrayList<Attachment>(len);
            for (int i = 0; i < len; i++) {
                final DefaultAttachment attachment = new DefaultAttachment();
                parse(jsonAttachments.getJSONObject(i), attachment);
                list.add(attachment);
            }
            snippet.setAttachments(list);
            if (null != set) {
                set.add(Property.ATTACHMENTS);
            }
        }
    }

    /**
     * Parses specified JSON into given attachments.
     *
     * @param jsonAttachment The JOSN attachment
     * @param attachment The attachment
     * @throws JSONException If a JSON error occurs
     */
    public static void parse(final JSONObject jsonAttachment, final DefaultAttachment attachment) throws JSONException {
        String key = "filename";
        if (jsonAttachment.hasAndNotNull(key)) {
            final ContentDisposition cd = new ContentDisposition();
            cd.setAttachment();
            cd.setFilenameParameter(jsonAttachment.getString(key));
            attachment.setContentDisposition(cd.toString());
        }
        key = "id";
        if (jsonAttachment.hasAndNotNull(key)) {
            attachment.setId(jsonAttachment.getString(key));
        }
        key = "mimetype";
        if (jsonAttachment.hasAndNotNull(key)) {
            attachment.setContentType(jsonAttachment.getString(key));
        }
        key = "contentid";
        if (jsonAttachment.hasAndNotNull(key)) {
            attachment.setContentId(jsonAttachment.getString(key));
        }
        key = "size";
        if (jsonAttachment.hasAndNotNull(key)) {
            attachment.setSize(jsonAttachment.getLong(key));
        }
        key = "streamprovider";
        if (jsonAttachment.hasAndNotNull(key)) {
            // TODO:
        }
    }

}
