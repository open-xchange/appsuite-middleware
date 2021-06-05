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

package com.openexchange.config.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.Reloadables;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.config.json.ConfigAJAXRequest;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SetPropertyAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class SetPropertyAction extends AbstractConfigAction {

    /**
     * Initializes a new {@link SetPropertyAction}.
     *
     * @param services The service look-up
     */
    public SetPropertyAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(ConfigAJAXRequest req) throws OXException, JSONException {
        ConfigViewFactory factory = getService(ConfigViewFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        // Check for parameters
        String propertyName = req.checkParameter("name");
        if (Strings.isEmpty(propertyName)) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("name", propertyName);
        }

        JSONObject jRequest = req.getData();
        String propertyValue = jRequest.optString("value", null);
        if (Strings.isEmpty(propertyName)) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("value", propertyName);
        }

        // Check for context administrator
        ServerSession session = req.getSession();
        if (session.getUserId() != session.getContext().getMailadmin()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("config");
        }

        // Get the configuration view associated with current user
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());

        ConfigProperty<String> property = view.property(ConfigViewScope.CONTEXT.getScopeName(), propertyName, String.class);
        property.set(propertyValue); // Fails if protected

        Reloadables.propagatePropertyChange(propertyName);

        return new AJAXRequestResult(new JSONObject(2).put("name", propertyName).put("value", property.get())); // Defaults to "json" format
    }

}
