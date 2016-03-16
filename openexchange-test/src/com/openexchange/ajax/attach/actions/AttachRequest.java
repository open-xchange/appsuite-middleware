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

import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.attach.AttachmentTools;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.container.CommonObject;

/**
 * {@link AttachRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AttachRequest extends AbstractAttachmentRequest<AttachResponse> {

    private final int moduleId;

    private final int folderId;

    private final int attachedId;

    private final String fileName;

    private final InputStream data;

    private final String mimeType;

    public AttachRequest(CommonObject obj, String fileName, InputStream data, String mimeType) {
        super();
        moduleId = AttachmentTools.determineModule(obj);
        folderId = obj.getParentFolderID();
        attachedId = obj.getObjectID();
        this.fileName = fileName;
        this.data = data;
        this.mimeType = mimeType;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.UPLOAD;
    }

    private String writeJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(AttachmentField.MODULE_ID_LITERAL.getName(), moduleId);
        json.put(AttachmentField.FOLDER_ID_LITERAL.getName(), folderId);
        json.put(AttachmentField.ATTACHED_ID_LITERAL.getName(), attachedId);
        return json.toString();
    }

    @Override
    public Parameter[] getParameters() throws JSONException {
        return new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_ACTION, Attachment.ACTION_ATTACH),
            new FieldParameter("json_0", writeJSON()),
            new FileParameter("file_0", fileName, data, mimeType)
        };
    }

    @Override
    public AttachParser getParser() {
        return new AttachParser(true);
    }
}
