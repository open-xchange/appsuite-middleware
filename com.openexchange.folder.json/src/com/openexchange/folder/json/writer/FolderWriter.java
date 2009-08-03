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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folder.json.writer;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.AbstractOXException;

/**
 * {@link FolderWriter} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderWriter {

    /**
     * Initializes a new {@link FolderWriter}.
     */
    private FolderWriter() {
        super();
    }

    private static abstract class FolderFieldWriter {

        /**
         * Initializes a new {@link FolderFieldWriter}
         */
        protected FolderFieldWriter() {
            super();
        }

        public abstract void writeField(JSONValue jsonValue, UserizedFolder folder, Locale locale, boolean withKey) throws JSONException, AbstractOXException;
    }

    private static final Map<Integer, FolderFieldWriter> STATIC_WRITERS_MAP;

    static {
        final Map<Integer, FolderFieldWriter> m = new HashMap<Integer, FolderFieldWriter>();
        m.put(Integer.valueOf(FolderField.ID.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final String id = folder.getID();
                if (withKey) {
                    if (null != id) {
                        ((JSONObject) jsonValue).put(FolderField.ID.getName(), id);
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(null == id ? JSONObject.NULL : id);
            }
        });
        m.put(Integer.valueOf(FolderField.CREATED_BY.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final int id = folder.getCreatedBy();
                if (withKey) {
                    if (-1 != id) {
                        ((JSONObject) jsonValue).put(FolderField.CREATED_BY.getName(), id);
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(id);
            }
        });
        m.put(Integer.valueOf(FolderField.MODIFIED_BY.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final int id = folder.getModifiedBy();
                if (withKey) {
                    if (-1 != id) {
                        ((JSONObject) jsonValue).put(FolderField.MODIFIED_BY.getName(), id);
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(id);
            }
        });
        m.put(Integer.valueOf(FolderField.CREATION_DATE.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final Date d = folder.getCreationDate();
                if (withKey) {
                    if (null != d) {
                        ((JSONObject) jsonValue).put(FolderField.CREATION_DATE.getName(), d.getTime());
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(d.getTime());
            }
        });
        m.put(Integer.valueOf(FolderField.LAST_MODIFIED.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final Date d = folder.getLastModified();
                if (withKey) {
                    if (null != d) {
                        ((JSONObject) jsonValue).put(FolderField.LAST_MODIFIED.getName(), d.getTime());
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(d == null ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(Integer.valueOf(FolderField.LAST_MODIFIED_UTC.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                // TODO!!!!
            }
        });
        m.put(Integer.valueOf(FolderField.FOLDER_ID.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final String id = folder.getParentID();
                if (withKey) {
                    if (null != id) {
                        ((JSONObject) jsonValue).put(FolderField.FOLDER_ID.getName(), id);
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(null == id ? JSONObject.NULL : id);
            }
        });
        m.put(Integer.valueOf(FolderField.FOLDER_NAME.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final String id = folder.getLocalizedName(locale);
                if (withKey) {
                    if (null != id) {
                        ((JSONObject) jsonValue).put(FolderField.FOLDER_NAME.getName(), id);
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(null == id ? JSONObject.NULL : id);
            }
        });
        m.put(Integer.valueOf(FolderField.MODULE.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final ContentType obj = folder.getContentType();
                if (withKey) {
                    if (null != obj) {
                        ((JSONObject) jsonValue).put(FolderField.MODULE.getName(), obj.toString());
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(null == obj ? JSONObject.NULL : obj.toString());
            }
        });
        m.put(Integer.valueOf(FolderField.TYPE.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final Type obj = folder.getType();
                if (withKey) {
                    if (null != obj) {
                        ((JSONObject) jsonValue).put(FolderField.TYPE.getName(), obj.getType());
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(null == obj ? JSONObject.NULL : Integer.valueOf(obj.getType()));
            }
        });
        m.put(Integer.valueOf(FolderField.SUBFOLDERS.getColumn()), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValue jsonValue, final UserizedFolder folder, final Locale locale, final boolean withKey) throws JSONException, AbstractOXException {
                final String[] obj = folder.getSubfolderIDs();
                if (withKey) {
                    if (null != obj) {
                        ((JSONObject) jsonValue).put(FolderField.SUBFOLDERS.getName(), obj.length > 0);
                    }
                    return;
                }
                ((JSONArray) jsonValue).put(null == obj ? JSONObject.NULL : Boolean.valueOf(obj.length > 0));
            }
        });
        // TODO: Continue

        STATIC_WRITERS_MAP = Collections.unmodifiableMap(m);
    }

    public static JSONArray write2Array(final int[] fields, final UserizedFolder folder, final Locale locale) {
        final JSONArray jsonArray = new JSONArray();

        return jsonArray;
    }

}
