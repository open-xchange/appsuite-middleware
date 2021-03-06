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

package com.openexchange.ajax.drive.action;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.drive.DriveShareTarget;

/**
 * {@link DeleteLinkRequest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class DeleteLinkRequest extends AbstractDriveRequest<DeleteLinkResponse> {

    private final boolean failOnError;
    private final DriveShareTarget target;

    public DeleteLinkRequest(Integer root, DriveShareTarget target) {
        this(root, target, true);
    }

    public DeleteLinkRequest(Integer root, DriveShareTarget target, boolean failOnError) {
        super(root);
        this.target = target;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, "deleteLink"), new Parameter("root", root.intValue()) };
    }

    @Override
    public DeleteLinkParser getParser() {
        return new DeleteLinkParser(failOnError);
    }

    @Override
    public JSONObject getBody() throws IOException, JSONException {
        JSONObject retval = new JSONObject();
        DriveShareWriter.writeDriveTarget(target, retval);
        return retval;
    }

}
