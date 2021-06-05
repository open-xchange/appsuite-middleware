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

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.util.GetSwitch;
import com.openexchange.tools.filename.FileNameTools;
import com.openexchange.tools.iterator.SearchIterator;


public class AttachmentWriter extends TimedWriter<AttachmentMetadata> {

    public AttachmentWriter(final JSONWriter writer) {
        super(writer);
    }

    public void writeAttachments(final SearchIterator iterator, final AttachmentField[] columns, final TimeZone tz) throws JSONException, OXException {
        jsonWriter.array();
        fillArray(iterator,columns,tz);
        jsonWriter.endArray();
    }

    @Override
    protected void fillArray(final SearchIterator iterator, final Object[] columns, final TimeZone tz) throws OXException, JSONException {
        while (iterator.hasNext()) {
            jsonWriter.array();
            final AttachmentMetadata attachment = (AttachmentMetadata) iterator.next();
            final GetSwitch get = new GetSwitch(attachment);
            for(final AttachmentField column : (AttachmentField[])columns) {
                Object o = column.doSwitch(get);
                o = jsonCompat(o,column,tz);
                jsonWriter.value(o);
            }
            jsonWriter.endArray();
        }
    }

    private Object jsonCompat(final Object o, final AttachmentField column, final TimeZone tz) {
        if (column.getId() == AttachmentField.CREATION_DATE) {
            final long time = ((Date)o).getTime();
            final int offset = tz.getOffset(time);
            return Long.valueOf(time + offset);
        } else if (column.getId() == AttachmentField.FILENAME) {
            return FileNameTools.sanitizeFilename((String) o);
        }
        return o;
    }

    public void write(final AttachmentMetadata attachment, final TimeZone tz) throws JSONException {
        jsonWriter.object();
        final GetSwitch get = new GetSwitch(attachment);
        for(final AttachmentField column : AttachmentField.VALUES) {
            jsonWriter.key(column.getName());
            jsonWriter.value(jsonCompat(column.doSwitch(get),column, tz));
        }
        jsonWriter.endObject();
    }

    @Override
    protected int getId(final Object object) {
        return ((AttachmentMetadata)object).getId();
    }
}
