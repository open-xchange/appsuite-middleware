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

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.config.json.ConfigAJAXRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GETAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction()
public final class GETAction extends AbstractConfigAction {

    /**
     * Initializes a new {@link GETAction}.
     */
    public GETAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ConfigAJAXRequest req) throws OXException, JSONException {
        String path = req.getRequest().getSerlvetRequestURI();
        if (path.length() > 0 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        final ServerSession session = req.getSession();
        final SettingStorage stor = SettingStorage.getInstance(session);
        final Setting setting = ConfigTree.getInstance().getSettingByPath(path);
        stor.readValues(setting);
        /*
         * TODO: What format?!
         */
        final Object object = convert2JS(setting);
        if (object instanceof JSONValue) {
            return new AJAXRequestResult(object, "json");
        }
        if (object instanceof Number) {
            return new AJAXRequestResult(object, "int");
        }
        if (object instanceof Date) {
            return new AJAXRequestResult(object, "date");
        }
        return new AJAXRequestResult(object.toString(), "string");
    }

    /**
     * Converts a tree of settings into the according java script objects.
     * @param setting Tree of settings.
     * @return java script object representing the setting tree.
     * @throws JSONException if the conversion to java script objects fails.
     */
    public static Object convert2JS(final Setting setting) throws JSONException {
        Object retval = null;
        if (setting.isLeaf()) {
            final Object[] multiValue = setting.getMultiValue();
            if (null == multiValue) {
                final Object singleValue = setting.getSingleValue();
                if (null == singleValue) {
                    retval = JSONObject.NULL;
                } else if (singleValue instanceof JSONObject) {
                    retval = singleValue;
                } else {
                    try {
                        retval = new JSONObject(singleValue.toString());
                    } catch (JSONException e) {
                        retval = singleValue;
                    }
                }
            } else {
                final JSONArray array = new JSONArray(multiValue.length);
                for (final Object value : multiValue) {
                    array.put(value);
                }
                retval = array;
            }
        } else {
            final Setting[] elements = setting.getElements();
            final JSONObject json = new JSONObject(elements.length);
            for (final Setting subSetting : elements) {
                if (null != subSetting) {
                    json.put(subSetting.getName(), convert2JS(subSetting));
                }
            }
            retval = json;
        }
        return retval;
    }

}
