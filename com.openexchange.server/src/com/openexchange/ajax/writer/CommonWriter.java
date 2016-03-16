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

package com.openexchange.ajax.writer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.json.JSONWriter;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.session.Session;

/**
 * {@link CommonWriter} - Writer for common fields
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CommonWriter extends FolderChildWriter {

    /**
     * Initializes a new {@link CommonWriter}
     *
     * @param tz
     *            The user time zone
     * @param jsonwriter
     *            The JSON writer to write to
     */
    protected CommonWriter(final TimeZone tz, final JSONWriter jsonwriter) {
        super(tz, jsonwriter);
    }

    /**
     * Writes common field from given {@link CommonObject} instance to specified {@link JSONObject}
     *
     * @param commonObj The common object
     * @param jsonObj The JSON object
     * @param session The associated session
     * @throws JSONException If a JSON error occurs
     */
    public void writeCommonFields(final CommonObject commonObj, final JSONObject jsonObj, Session session) throws JSONException {
        writeFields(commonObj, timeZone, jsonObj, session);
    }

    protected boolean writeField(final CommonObject obj, final int column, final TimeZone tz, final JSONArray json, Session session) throws JSONException {
        final FieldWriter<CommonObject> writer = WRITER_MAP.get(column);
        if (null == writer) {
            return super.writeField(obj, column, tz, json, session);
        }
        writer.write(obj, timeZone, json, session);
        return true;
    }

    protected void writeFields(final CommonObject obj, final TimeZone tz, final JSONObject json, final Session session) throws JSONException {
        super.writeFields(obj, tz, json, session);
        final WriterProcedure<CommonObject> procedure = new WriterProcedure<CommonObject>(obj, json, tz, session);
        if (!WRITER_MAP.forEachValue(procedure)) {
            final JSONException je = procedure.getError();
            if (null != je) {
                throw je;
            }
        }
    }

    private static final FieldWriter<CommonObject> CATEGORIES_WRITER = new FieldWriter<CommonObject>() {
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONArray json, Session session) {
            writeValue(obj.getCategories(), json, obj.containsCategories());
        }
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONObject json, Session session) throws JSONException {
            writeParameter(CommonFields.CATEGORIES, obj.getCategories(), json, obj.containsCategories());
        }
    };

    private static final FieldWriter<CommonObject> PRIVATE_FLAG_WRITER = new FieldWriter<CommonObject>() {
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONArray json, Session session) {
            writeValue(obj.getPrivateFlag(), json, obj.containsPrivateFlag());
        }
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONObject json, Session session) throws JSONException {
            writeParameter(CommonFields.PRIVATE_FLAG, obj.getPrivateFlag(), json, obj.containsPrivateFlag());
        }
    };

    private static final FieldWriter<CommonObject> COLORLABEL_WRITER = new FieldWriter<CommonObject>() {
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONArray json, Session session) {
            writeValue(obj.getLabel(), json, obj.containsLabel());
        }
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONObject json, Session session) throws JSONException {
            writeParameter(CommonFields.COLORLABEL, obj.getLabel(), json, obj.containsLabel());
        }
    };

    private static final FieldWriter<CommonObject> NUMBER_OF_ATTACHMENTS_WRITER = new FieldWriter<CommonObject>() {
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONArray json, Session session) {
            writeValue(obj.getNumberOfAttachments(), json, obj.containsNumberOfAttachments());
        }
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONObject json, Session session) throws JSONException {
            writeParameter(CommonFields.NUMBER_OF_ATTACHMENTS, obj.getNumberOfAttachments(), json, obj.containsNumberOfAttachments());
        }
    };

    private static final FieldWriter<CommonObject> LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC_WRITER = new FieldWriter<CommonObject>() {
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONArray json, Session session) {
            writeValue(obj.getLastModifiedOfNewestAttachment(), UTC, json, obj.containsLastModifiedOfNewestAttachment());
        }
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONObject json, Session session) throws JSONException {
            writeParameter(
                CommonFields.LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC,
                obj.getLastModifiedOfNewestAttachment(),
                UTC,
                json,
                obj.containsLastModifiedOfNewestAttachment());
        }
    };

    private static final FieldWriter<CommonObject> EXTENDED_PROPERTIES_WRITER = new FieldWriter<CommonObject>() {
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONArray json, Session session) throws JSONException {
            final Map<String, Object> extendedProperties = obj.getExtendedProperties();
            writeValue(null == extendedProperties ? null : (JSONValue) JSONCoercion.coerceToJSON(extendedProperties), json, obj.containsExtendedProperties());
        }
        @Override
        public void write(final CommonObject obj, final TimeZone timeZone, final JSONObject json, Session session) throws JSONException {
            final Map<String, Object> extendedProperties = obj.getExtendedProperties();
            writeParameter(CommonFields.EXTENDED_PROPERTIES,null == extendedProperties ? null : (JSONValue) JSONCoercion.coerceToJSON(extendedProperties), json, obj.containsExtendedProperties());
        }
    };
    static {
        final TIntObjectMap<FieldWriter<CommonObject>> m = new TIntObjectHashMap<FieldWriter<CommonObject>>(6, 1);
        m.put(CommonObject.CATEGORIES, CATEGORIES_WRITER);
        m.put(CommonObject.PRIVATE_FLAG, PRIVATE_FLAG_WRITER);
        m.put(CommonObject.COLOR_LABEL, COLORLABEL_WRITER);
        m.put(CommonObject.NUMBER_OF_ATTACHMENTS, NUMBER_OF_ATTACHMENTS_WRITER);
        m.put(CommonObject.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC_WRITER);
        m.put(CommonObject.EXTENDED_PROPERTIES, EXTENDED_PROPERTIES_WRITER);
        WRITER_MAP = m;
    }

    private static final TIntObjectMap<FieldWriter<CommonObject>> WRITER_MAP;
}
