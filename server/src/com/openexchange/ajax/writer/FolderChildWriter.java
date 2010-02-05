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
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.container.FolderChildObject;

/**
 * {@link FolderChildWriter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FolderChildWriter extends DataWriter {

    /**
     * Initializes a new {@link FolderChildWriter}.
     * @param timeZone
     * @param writer
     */
    public FolderChildWriter(TimeZone timeZone, JSONWriter writer) {
        super(timeZone, writer);
    }

    protected void writeFields(FolderChildObject obj, JSONArray json) throws JSONException {
        super.writeFields(obj, json);
        for (FieldWriter<FolderChildObject> writer : WRITER_MAP.values()) {
            writer.write(obj, timeZone, json);
        }
    }

    protected void writeFields(FolderChildObject obj, JSONObject json) throws JSONException {
        super.writeFields(obj, json);
        for (FieldWriter<FolderChildObject> writer : WRITER_MAP.values()) {
            writer.write(obj, timeZone, json);
        }
    }

    protected static final FieldWriter<FolderChildObject> FOLDER_ID_WRITER = new FieldWriter<FolderChildObject>() {
        public void write(FolderChildObject obj, TimeZone timeZone, JSONArray json) {
            writeValue(obj.getParentFolderID(), json, obj.containsParentFolderID());
        }
        public void write(FolderChildObject obj, TimeZone timeZone, JSONObject json) throws JSONException {
            writeParameter(FolderChildFields.FOLDER_ID, obj.getParentFolderID(), json, obj.containsParentFolderID());
        }
    };

    static {
        final Map<Integer, FieldWriter<FolderChildObject>> m = new HashMap<Integer, FieldWriter<FolderChildObject>>(1, 1);
        m.put(I(FolderChildObject.FOLDER_ID), FOLDER_ID_WRITER);
        WRITER_MAP = Collections.unmodifiableMap(m);
    }

    private static final Map<Integer, FieldWriter<FolderChildObject>> WRITER_MAP;

}
