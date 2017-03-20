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
