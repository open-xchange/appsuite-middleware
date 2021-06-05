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

package com.openexchange.ajax.parser;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class AttachmentParser {

    /**
     * TODO Error codes
     */
    public static final class UnknownColumnException extends OXException {
        private static final long serialVersionUID = -6760923740785771286L;

        private final String idString;

        public UnknownColumnException(final String idString){
            this.idString = idString;
        }

        public String getColumn(){
            return idString;
        }
    }

    public AttachmentMetadata getAttachmentMetadata(final JSONObject json) {
        return new JSONAttachmentMetadata(json);
    }

    public AttachmentMetadata getAttachmentMetadata(final String json) throws JSONException {
        return new JSONAttachmentMetadata(json);
    }

    public AttachmentField[] getColumns(final String[] parameterValues) throws UnknownColumnException{
        if (parameterValues == null) {
            return null;
        }
        final AttachmentField[] columns = new AttachmentField[parameterValues.length];
        int i = 0;
        for(final String idString : parameterValues) {
            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (NumberFormatException x) {
                throw new UnknownColumnException(idString);
            }
            final AttachmentField f = AttachmentField.get(id);
            if (f == null) {
                throw new UnknownColumnException(idString);
            }
            columns[i++] = f;
        }
        return columns;
    }
}
