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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class NewInfostoreRequest extends AbstractInfostoreRequest<NewInfostoreResponse> {

    private com.openexchange.file.storage.File metadata;
    private final InputStream input;
    private Transport notificationTransport;
    private String notificationMessage;
    private boolean tryAddVersion;

    /**
     * Initializes a new {@link NewInfostoreRequest}.
     */
    public NewInfostoreRequest() {
        this(null, (InputStream) null);
        this.tryAddVersion = false;
    }

    /**
     * Initializes a new {@link NewInfostoreRequest}.
     *
     * @param data The document
     */
    public NewInfostoreRequest(com.openexchange.file.storage.File data) {
        this(data, (InputStream) null);
        this.tryAddVersion = false;
    }

    /**
     * Initializes a new {@link NewInfostoreRequest}.
     *
     * @param data The document
     * @param tryAddVersion <code>true</code> to add a new file version
     */
    public NewInfostoreRequest(com.openexchange.file.storage.File data, boolean tryAddVersion) {
        this(data, (InputStream) null);
        this.tryAddVersion = tryAddVersion;
    }

    /**
     * Initializes a new {@link NewInfostoreRequest}.
     * 
     * @param data The document
     * @param upload The file data
     * @throws FileNotFoundException
     */
    public NewInfostoreRequest(com.openexchange.file.storage.File data, File upload) throws FileNotFoundException {
        this(data, new FileInputStream(upload));
    }

    /**
     * Initializes a new {@link NewInfostoreRequest}.
     *
     * @param data The document
     * @param upload The file data
     * @param tryAddVersion <code>true</code> to add a new file version
     * @throws FileNotFoundException
     */
    public NewInfostoreRequest(com.openexchange.file.storage.File data, File upload, boolean tryAddVersion) throws FileNotFoundException {
        this(data, new FileInputStream(upload));
        this.tryAddVersion = tryAddVersion;
    }

    /**
     * Initializes a new {@link NewInfostoreRequest}.
     * 
     * @param data The document
     * @param input The file data
     */
    public NewInfostoreRequest(com.openexchange.file.storage.File data, InputStream input) {
        super();
        this.metadata = data;
        this.input = input;
    }

    public void setMetadata(com.openexchange.file.storage.File metadata) {
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

    public com.openexchange.file.storage.File getMetadata() {
        return metadata;
    }

    @Override
    public String getBody() throws JSONException {
        JSONObject jFile = prepareJFile();
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

    private JSONObject prepareJFile() throws JSONException {
        final JSONObject originalObject = new JSONObject(writeJSON(getMetadata()));
        final JSONObject retVal = new JSONObject();
        final Set<String> set = originalObject.keySet();

        for (String string : set) {
            final Object test = originalObject.get(string);
            if (test != JSONObject.NULL) {
                if (test instanceof JSONArray) {
                    if (((JSONArray) test).length() > 0) {
                        retVal.put(string, test);
                    }
                } else {
                    retVal.put(string, test);
                }
            }
        }

        return retVal;
    }

    @Override
    public Method getMethod() {
        return null == input ? Method.PUT : Method.UPLOAD;
    }

    @Override
    public Parameter[] getParameters() throws JSONException {
        List<Parameter> tmp = new ArrayList<Parameter>(3);
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        tmp.add(new Parameter("try_add_version", tryAddVersion));
        if (null != input) {
            tmp.add(new FieldParameter("json", getBody()));
            tmp.add(new FileParameter("file", metadata.getFileName(), input, metadata.getFileMIMEType()));
        }
        return tmp.toArray(new Parameter[tmp.size()]);
    }

    @Override
    public NewInfostoreParser getParser() {
        return new NewInfostoreParser(getFailOnError(), null != input);
    }
}
