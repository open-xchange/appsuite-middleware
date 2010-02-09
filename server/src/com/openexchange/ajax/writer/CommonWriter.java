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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.groupware.container.CommonObject;

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
     * @throws JSONException If a JSON error occurs
     */
    public void writeCommonFields(final CommonObject commonObj, final JSONObject jsonObj) throws JSONException {
        writeFields(commonObj, jsonObj);
    }

    protected boolean writeField(CommonObject obj, int column, TimeZone tz, JSONArray json) throws JSONException {
        FieldWriter<CommonObject> writer = WRITER_MAP.get(I(column));
        if (null == writer) {
            return super.writeField(obj, column, tz, json);
        }
        writer.write(obj, timeZone, json);
        return true;
    }

    protected void writeFields(CommonObject obj, JSONObject json) throws JSONException {
        super.writeFields(obj, json);
        for (FieldWriter<CommonObject> writer : WRITER_MAP.values()) {
            writer.write(obj, timeZone, json);
        }
    }

    protected static final FieldWriter<CommonObject> CATEGORIES_WRITER = new FieldWriter<CommonObject>() {
        public void write(CommonObject obj, TimeZone timeZone, JSONArray json) {
            writeValue(obj.getCategories(), json, obj.containsCategories());
        }
        public void write(CommonObject obj, TimeZone timeZone, JSONObject json) throws JSONException {
            writeParameter(CommonFields.CATEGORIES, obj.getCategories(), json, obj.containsCategories());
        }
    };

    protected static final FieldWriter<CommonObject> PRIVATE_FLAG_WRITER = new FieldWriter<CommonObject>() {
        public void write(CommonObject obj, TimeZone timeZone, JSONArray json) {
            writeValue(obj.getPrivateFlag(), json, obj.containsPrivateFlag());
        }
        public void write(CommonObject obj, TimeZone timeZone, JSONObject json) throws JSONException {
            writeParameter(CommonFields.PRIVATE_FLAG, obj.getPrivateFlag(), json, obj.containsPrivateFlag());
        }
    };

    protected static final FieldWriter<CommonObject> COLORLABEL_WRITER = new FieldWriter<CommonObject>() {
        public void write(CommonObject obj, TimeZone timeZone, JSONArray json) {
            writeValue(obj.getLabel(), json, obj.containsLabel());
        }
        public void write(CommonObject obj, TimeZone timeZone, JSONObject json) throws JSONException {
            writeParameter(CommonFields.COLORLABEL, obj.getLabel(), json, obj.containsLabel());
        }
    };

    protected static final FieldWriter<CommonObject> NUMBER_OF_ATTACHMENTS_WRITER = new FieldWriter<CommonObject>() {
        public void write(CommonObject obj, TimeZone timeZone, JSONArray json) {
            writeValue(obj.getNumberOfAttachments(), json, obj.containsNumberOfAttachments());
        }
        public void write(CommonObject obj, TimeZone timeZone, JSONObject json) throws JSONException {
            writeParameter(CommonFields.NUMBER_OF_ATTACHMENTS, obj.getNumberOfAttachments(), json, obj.containsNumberOfAttachments());
        }
    };

    protected static final FieldWriter<CommonObject> LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC_WRITER = new FieldWriter<CommonObject>() {
        public void write(CommonObject obj, TimeZone timeZone, JSONArray json) {
            writeValue(obj.getLastModifiedOfNewestAttachment(), UTC, json, obj.containsLastModifiedOfNewestAttachment());
        }
        public void write(CommonObject obj, TimeZone timeZone, JSONObject json) throws JSONException {
            writeParameter(
                CommonFields.LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC,
                obj.getLastModifiedOfNewestAttachment(),
                UTC,
                json,
                obj.containsLastModifiedOfNewestAttachment());
        }
    };

    protected static final FieldWriter<CommonObject> NUMBER_OF_LINKS_WRITER = new FieldWriter<CommonObject>() {
        public void write(CommonObject obj, TimeZone timeZone, JSONArray json) {
            writeValue(obj.getNumberOfLinks(), json, obj.containsNumberOfLinks());
        }
        public void write(CommonObject obj, TimeZone timeZone, JSONObject json) {
            // This value is nowhere written to a JSON object.
        }
    };
    static {
        final Map<Integer, FieldWriter<CommonObject>> m = new HashMap<Integer, FieldWriter<CommonObject>>(6, 1);
        m.put(I(CommonObject.CATEGORIES), CATEGORIES_WRITER);
        m.put(I(CommonObject.PRIVATE_FLAG), PRIVATE_FLAG_WRITER);
        m.put(I(CommonObject.COLOR_LABEL), COLORLABEL_WRITER);
        m.put(I(CommonObject.NUMBER_OF_ATTACHMENTS), NUMBER_OF_ATTACHMENTS_WRITER);
        m.put(I(CommonObject.LAST_MODIFIED_OF_NEWEST_ATTACHMENT), LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC_WRITER);
        m.put(I(CommonObject.NUMBER_OF_LINKS), NUMBER_OF_LINKS_WRITER);
        WRITER_MAP = Collections.unmodifiableMap(m);
    }

    private static final Map<Integer, FieldWriter<CommonObject>> WRITER_MAP;
}
