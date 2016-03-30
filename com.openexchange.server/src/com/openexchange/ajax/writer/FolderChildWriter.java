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
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.session.Session;

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
    public FolderChildWriter(final TimeZone timeZone, final JSONWriter writer) {
        super(timeZone, writer);
    }

    protected boolean writeField(final FolderChildObject obj, final int column, final TimeZone tz, final JSONArray json, final Session session) throws JSONException {
        final FieldWriter<FolderChildObject> writer = WRITER_MAP.get(column);
        if (null == writer) {
            return super.writeField(obj, column, tz, json, session);
        }
        writer.write(obj, tz, json, session);
        return true;
    }

    protected void writeFields(final FolderChildObject obj, final TimeZone tz, final JSONObject json, final Session session) throws JSONException {
        super.writeFields(obj, tz, json, session);
        final WriterProcedure<FolderChildObject> procedure = new WriterProcedure<FolderChildObject>(obj, json, tz, session);
        if (!WRITER_MAP.forEachValue(procedure)) {
            final JSONException je = procedure.getError();
            if (null != je) {
                throw je;
            }
        }
    }

    private static final FieldWriter<FolderChildObject> FOLDER_ID_WRITER = new FieldWriter<FolderChildObject>() {
        @Override
        public void write(final FolderChildObject obj, final TimeZone timeZone, final JSONArray json, Session session) {
            writeValue(obj.getParentFolderID(), json, obj.containsParentFolderID());
        }
        @Override
        public void write(final FolderChildObject obj, final TimeZone timeZone, final JSONObject json, Session session) throws JSONException {
            writeParameter(FolderChildFields.FOLDER_ID, obj.getParentFolderID(), json, obj.containsParentFolderID());
        }
    };

    static {
        final TIntObjectMap<FieldWriter<FolderChildObject>> m = new TIntObjectHashMap<FieldWriter<FolderChildObject>>(1, 1);
        m.put(FolderChildObject.FOLDER_ID, FOLDER_ID_WRITER);
        WRITER_MAP = m;
    }

    private static final TIntObjectMap<FieldWriter<FolderChildObject>> WRITER_MAP;

}
