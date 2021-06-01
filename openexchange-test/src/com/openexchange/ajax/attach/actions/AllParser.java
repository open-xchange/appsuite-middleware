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

package com.openexchange.ajax.attach.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractListParser;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.attach.util.SetSwitch;

/**
 * {@link AllParser}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AllParser extends AbstractListParser<AllResponse> {

    protected AllParser(final boolean failOnError, int[] columns) {
        super(failOnError, columns);
    }

    @Override
    protected AllResponse createResponse(final Response response) throws JSONException {
        AllResponse createResponse = super.createResponse(response);
        if (createResponse.hasError()) {
            return createResponse;
        }
        final List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
        for (final Object[] data : createResponse) {
            assertEquals("Object data array length is different as column array length.", getColumns().length, data.length);
            final AttachmentMetadata attachment = new AttachmentImpl();

            final SetSwitch set = new SetSwitch(attachment);
            int z = 0;
            for (int i : getColumns()) {
                AttachmentField field = AttachmentField.get(i);
                Object value = data[z];
                z++;
                value = patchValue(value, field);
                set.setValue(value);
                field.doSwitch(set);
            }

            attachments.add(attachment);
        }
        createResponse.setAttachments(attachments);
        return createResponse;
    }

    Object patchValue(final Object value, final AttachmentField field) {
        if (value instanceof Integer) {
            if (field.equals(AttachmentField.FILE_SIZE_LITERAL)) {
                return Long.valueOf(((Integer) value).longValue());
            }
        }
        if (value instanceof Long) {
            if (isDateField(field)) {
                return new Date(((Long) value).longValue());
            }
            if (!field.equals(AttachmentField.FILE_SIZE_LITERAL)) {
                return Integer.valueOf(((Long) value).intValue());
            }
        }
        return value;
    }

    private boolean isDateField(final AttachmentField field) {
        return field.equals(AttachmentField.CREATION_DATE_LITERAL);
    }

    @Override
    protected AllResponse instantiateResponse(Response response) {
        return new AllResponse(response);
    }
}
