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

package com.openexchange.snippet.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;

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
     */
    public static void parse(final JSONObject jsonSnippet, final DefaultSnippet snippet) throws JSONException {
        parse(jsonSnippet, snippet, null);
    }

    /**
     * Parses specified JSON into given snippet.
     *
     * @param jsonSnippet The JSON snippet
     * @param snippet The snippet
     * @throws JSONException If a JSON error occurs
     */
    public static void parse(final JSONObject jsonSnippet, final DefaultSnippet snippet, final Set<Property> set) throws JSONException {
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
            snippet.setDisplayName(jsonSnippet.getString(key));
            if (null != set) {
                set.add(Property.DISPLAY_NAME);
            }
        }
        key = Property.ID.getPropName();
        if (jsonSnippet.hasAndNotNull(key)) {
            snippet.setId(jsonSnippet.getString(key));
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
            snippet.setModule(jsonSnippet.getString(key));
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
            snippet.setType(jsonSnippet.getString(key));
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
