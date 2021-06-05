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

package com.openexchange.file.storage.mail;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailMetadataField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class MailMetadataField implements AdditionalFileField {

    /**
     * Initializes a new {@link MailMetadataField}.
     */
    public MailMetadataField() {
        super();
    }

    @Override
    public Field[] getRequiredFields() {
        return new Field[0];
    }

    @Override
    public int getColumnID() {
        return 7030;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.file.storage.mail.mailMetadata";
    }

    @Override
    public Object getValue(File file, ServerSession session) {
        return getMailMetadata(file);
    }

    @Override
    public List<Object> getValues(List<File> files, ServerSession session) {
        if (null == files) {
            return null;
        }
        List<Object> values = new ArrayList<Object>(files.size());
        for (File file : files) {
            values.add(getMailMetadata(file));
        }
        return values;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        if (MailMetadata.class.isInstance(value)) {
            return ((MailMetadata) value).renderJSON();
        }
        return value;
    }

    private static MailMetadata getMailMetadata(File file) {
        MailDriveFile mailDriveFile = FileStorageUtility.launderDelegate(file, MailDriveFile.class);
        return null != mailDriveFile ? mailDriveFile.getMetadata() : null;
    }

}
