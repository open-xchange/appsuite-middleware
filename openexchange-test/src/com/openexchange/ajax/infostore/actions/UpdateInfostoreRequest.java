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

package com.openexchange.ajax.infostore.actions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * @author <a href="mailto:markus.wagner@open-xchange.com">Markus Wagner</a>
 */
public class UpdateInfostoreRequest extends AbstractInfostoreRequest<UpdateInfostoreResponse> {

    private File metadata;
    private java.io.File upload;
    private Field[] fields;
    private final String id;
    private final Date lastModified;
    private Transport notificationTransport;
    private String notificationMessage;
    private long offset;

    public UpdateInfostoreRequest(String id, Date lastModified, java.io.File upload) {
        this.id = id;
        this.upload = upload;
        this.lastModified = lastModified;
    }

    public UpdateInfostoreRequest(File data, Field[] fields, java.io.File upload, Date lastModified) {
        this.metadata = data;
        this.id = data.getId();
        this.lastModified = lastModified;
        this.upload = upload;
        this.fields = fields;
    }

    public UpdateInfostoreRequest(File data, Field[] fields, Date lastModified) {
        this.metadata = data;
        this.id = data.getId();
        this.lastModified = lastModified;
        this.fields = fields;
    }

    public void setMetadata(File metadata) {
        this.metadata = metadata;
    }

    /**
     * Enables the notification of added permission entities via the given transport.
     *
     * @param transport The transport
     */
    public void setNotifyPermissionEntities(Transport transport) {
        setNotifyPermissionEntities(transport, null);
    }

    /**
     * Enables the notification of added permission entities via the given transport.
     *
     * @param transport The transport
     * @param message The user-defined message
     */
    public void setNotifyPermissionEntities(Transport transport, String message) {
        notificationTransport = transport;
        notificationMessage = message;
    }

    /**
     * Gets the offset
     *
     * @return The offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Sets the offset
     *
     * @param offset The offset to set
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    public File getMetadata() {
        return metadata;
    }

    @Override
    public String getBody() throws JSONException {
        JSONObject jFile = writeJSON(getMetadata(), fields);
        if (notificationTransport != null) {
            JSONObject data = new JSONObject();
            data.put("file", jFile);
            JSONObject jNotification = new JSONObject();
            jNotification.put("transport", notificationTransport.getID());
            jNotification.put("message", notificationMessage);
            data.put("notification", jNotification);
            return data.toString();
        }
        return jFile.toString();
    }

    @Override
    public Method getMethod() {
        return null == upload ? Method.PUT : Method.UPLOAD;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> tmp = new ArrayList<Parameter>(3);
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ID, id));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified));
        if (null != upload) {
            tmp.add(new FieldParameter("json", getBody()));
            tmp.add(new FileParameter("file", upload.getName(), new FileInputStream(upload), metadata.getFileMIMEType()));
        }
        if (0 < offset) {
            tmp.add(new Parameter("offset", String.valueOf(offset)));
        }
        return tmp.toArray(new Parameter[tmp.size()]);
    }

    @Override
    public UpdateInfostoreParser getParser() {
        return new UpdateInfostoreParser(getFailOnError(), null != upload);
    }

}
