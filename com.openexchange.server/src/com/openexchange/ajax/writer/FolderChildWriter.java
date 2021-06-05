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

package com.openexchange.ajax.writer;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.session.Session;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
