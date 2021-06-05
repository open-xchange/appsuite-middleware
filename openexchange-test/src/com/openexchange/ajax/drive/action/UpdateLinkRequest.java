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
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.xing.util.JSONCoercion;

/**
 * {@link UpdateLinkRequest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class UpdateLinkRequest extends AbstractDriveRequest<UpdateLinkResponse> {

    private final boolean failOnError;
    private final DriveShareTarget target;
    private final Long expiry;
    private final String password;
    private final Map<String, Object> meta;

    public UpdateLinkRequest(Integer root, DriveShareTarget target) {
        this(root, target, null, null, null, true);
    }

    public UpdateLinkRequest(Integer root, DriveShareTarget target, Long expiry, String password, Map<String, Object> meta, boolean failOnError) {
        super(root);
        this.target = target;
        this.expiry = expiry;
        this.password = password;
        this.meta = meta;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, "updateLink"), new Parameter("root", root.intValue())
        };
    }

    @Override
    public UpdateLinkParser getParser() {
        return new UpdateLinkParser(failOnError);
    }

    @Override
    public JSONObject getBody() throws IOException, JSONException {
        JSONObject retval = new JSONObject();
        DriveShareWriter.writeDriveTarget(target, retval);
        retval.putOpt("expiry_date", expiry);
        retval.putOpt("meta", null != meta ? JSONCoercion.coerceToJSON(meta) : null);
        retval.putOpt("password", password);
        return retval;
    }

}
