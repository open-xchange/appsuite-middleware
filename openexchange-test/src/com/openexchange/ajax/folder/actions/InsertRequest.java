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

package com.openexchange.ajax.folder.actions;

import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - added additional constructor and handling to allow for the
 *         creation of mail folders
 */
public class InsertRequest extends AbstractFolderRequest<InsertResponse> {

    private final FolderObject folder;

    /**
     * Should the parser fail on error in server response.
     */
    boolean failOnError;

    private Transport notificationTransport;

    private String notificationMessage;

    /**
     * Initializes a new {@link InsertRequest}.
     *
     * @param api The folder tree to use
     * @param folder The folder to create
     */
    public InsertRequest(API api, FolderObject folder) {
        this(api, folder, true);
    }

    /**
     * Initializes a new {@link InsertRequest}.
     *
     * @param api The folder tree to use
     * @param folder The folder to create
     * @param failOnError <code>true</code> to let the the test fail in case of an erroneous response, <code>false</code>, otherwise
     */
    public InsertRequest(API api, FolderObject folder, boolean failOnError) {
        super(api);
        this.failOnError = failOnError;
        this.folder = folder;
    }

    /**
     * Initializes a new {@link InsertRequest}.
     *
     * @param api The folder tree to use
     * @param folder The folder to create
     * @param timeZone The client timezone
     */
    public InsertRequest(API api, FolderObject folder, TimeZone timeZone) {
        this(api, folder, timeZone, true);
    }

    /**
     * Initializes a new {@link InsertRequest}.
     *
     * @param api The folder tree to use
     * @param folder The folder to create
     * @param timeZone The client timezone
     * @param failOnError <code>true</code> to let the the test fail in case of an erroneous response, <code>false</code>, otherwise
     */
    public InsertRequest(API api, FolderObject folder, TimeZone timeZone, boolean failOnError) {
        super(api, timeZone);
        this.failOnError = failOnError;
        this.folder = folder;
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

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        if (notificationTransport != null) {
            JSONObject data = new JSONObject();
            data.put("folder", convert(folder));
            JSONObject jNotification = new JSONObject();
            jNotification.put("transport", notificationTransport.getID());
            jNotification.put("message", notificationMessage);
            data.put("notification", jNotification);
            return data;
        }

        return convert(folder);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    protected void addParameters(List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));

        if (folder.containsModule() && folder.getFullName() != null && folder.getModule() == FolderObject.MAIL) {
            final String[] parts = folder.getFullName().split("/");
            final StringBuilder parentBuilder = new StringBuilder();
            for (int i = 0; i < (parts.length - 1); i++) {
                parentBuilder.append(parts[i]);
                parentBuilder.append('/');
            }
            final String parent = parentBuilder.substring(0, parentBuilder.length() - 1);
            params.add(new Parameter(FolderFields.FOLDER_ID, parent));
        } else {
            params.add(new Parameter(FolderFields.FOLDER_ID, folder.getParentFolderID()));
        }
    }

    @Override
    public InsertParser getParser() {
        return new InsertParser(failOnError);
    }
}
