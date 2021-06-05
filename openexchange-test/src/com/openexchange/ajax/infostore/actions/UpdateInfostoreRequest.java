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

package com.openexchange.ajax.infostore.actions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * @author <a href="mailto:markus.wagner@open-xchange.com">Markus Wagner</a>
 */
public class UpdateInfostoreRequest extends AbstractInfostoreRequest<UpdateInfostoreResponse> {

    private File metadata;
    private final java.io.File upload;
    private final Field[] fields;
    private final String id;
    private final Date lastModified;
    private Transport notificationTransport;
    private String notificationMessage;
    private long offset;

    public UpdateInfostoreRequest(File data, Field[] fields, java.io.File upload, Date lastModified) {
        this.metadata = data;
        this.id = data.getId();
        Assert.assertNotNull(lastModified);
        this.lastModified = lastModified;
        this.upload = upload;
        this.fields = fields;
    }

    public UpdateInfostoreRequest(File data, Field[] fields, Date lastModified) {
        this(data, fields, null, lastModified);
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
        List<Parameter> tmp = new ArrayList<Parameter>();
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
