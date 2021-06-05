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

package com.openexchange.ajax.customizer.file;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.tools.filename.FileNameTools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SanitizedFilename}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.4
 */
public class SanitizedFilename implements AdditionalFileField {

    @Override
    public int getColumnID() {
        return 7040;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.file.sanitizedFilename";
    }

    @Override
    public Object getValue(File item, ServerSession session) {
        String fileName = item.getFileName();
        return null == fileName ? "" : FileNameTools.sanitizeFilename(fileName);
    }

    @Override
    public List<Object> getValues(List<File> items, ServerSession session) {
        if (items == null) {
            return null;
        }
        List<Object> retval = new ArrayList<Object>(items.size());
        for (File file : items) {
            retval.add(getValue(file, session));
        }
        return retval;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        return value;
    }

    @Override
    public Field[] getRequiredFields() {
        return new Field[] { Field.FILENAME };
    }

}
