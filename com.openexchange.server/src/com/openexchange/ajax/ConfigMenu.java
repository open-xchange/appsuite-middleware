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

package com.openexchange.ajax;

import static com.openexchange.config.json.actions.PUTAction.sanitizeJsonSetting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.java.Charsets;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * This class implements the servlet for sending and reading user specific
 * configuration settings inside the AJAX GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigMenu extends SessionServlet {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigMenu.class);

    /**
     * Size for the read buffer.
     */
    private static final int BUFFER_SIZE = 512;

    private static final long serialVersionUID = -7113587607566553771L;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        String path = getServletSpecificURI(req);
        if (path.length() > 0 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        final ServerSession sessionObj = getSessionObject(req);
        final SettingStorage stor = SettingStorage.getInstance(sessionObj);
        Setting setting;
        final Response response = new Response(sessionObj);
        try {
            setting = ConfigTree.getInstance().getSettingByPath(path);
            stor.readValues(setting);
            response.setData(convert2JS(setting));
        } catch (OXException e) {
            response.setException(e);
        } catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        setDefaultContentType(resp);
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(sessionObj));
        } catch (JSONException e) {
            log(RESPONSE_ERROR, e);
            sendError(resp);
        }
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
                final JSONArray array = new JSONArray();
                for (final Object value : multiValue) {
                    array.put(value);
                }
                retval = array;
            }
        } else {
            final Setting[] elements = setting.getElements();
            final JSONObject json = new JSONObject(elements.length);
            for (final Setting subSetting : elements) {
                json.put(subSetting.getName(), convert2JS(subSetting));
            }
            retval = json;
        }
        return retval;
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final ServerSession session = getSessionObject(req);
        final InputStream input = req.getInputStream();
        final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(input.available());
        final byte[] buf = new byte[BUFFER_SIZE];
        int length = input.read(buf);
        while (length != -1) {
            baos.write(buf, 0, length);
            length = input.read(buf);
        }
        String encoding = req.getCharacterEncoding();
        if (null == encoding) {
            log("Client did not specify the character encoding.");
            encoding = ServerConfig.getProperty(Property.DefaultEncoding);
        }
        String value = new String(baos.toByteArray(), Charsets.forName(encoding));
        if (value.length() > 0 && value.charAt(0) == '"') {
            value = value.substring(1);
        }
        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
        String path = getServletSpecificURI(req);
        if (path.length() > 0 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        final SettingStorage stor = SettingStorage.getInstance(session);
        final Response response = new Response(session);
        try {
            final Setting setting = ConfigTree.getInstance().getSettingByPath(path);
            setting.setSingleValue(value);
            saveSettingWithSubs(stor, setting);
        } catch (OXException e) {
            log(e.getMessage(), e);
            response.setException(e);
        } catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        }
        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(AJAXServlet.CONTENTTYPE_JSON);
            if (response.hasError()) {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
            }
        } catch (JSONException e) {
            log(RESPONSE_ERROR, e);
            sendError(resp);
        }
    }

    /**
     * Splits a value for a not leaf setting into its subsettings and stores them.
     * @param storage setting storage.
     * @param setting actual setting.
     * @throws OXException if an error occurs.
     * @throws JSONException if the json object can't be parsed.
     */
    private void saveSettingWithSubs(final SettingStorage storage, final Setting setting) throws OXException, JSONException {
        if (setting.isLeaf()) {
            final String value = (String) setting.getSingleValue();
            if (null != value && value.length() > 0) {
                if ('[' == value.charAt(0)) {
                    final JSONArray array = new JSONArray(value);
                    if (array.length() == 0) {
                        setting.setEmptyMultiValue();
                    } else {
                        for (int i = 0; i < array.length(); i++) {
                            setting.addMultiValue(array.getString(i));
                        }
                    }
                    setting.setSingleValue(null);
                } else if ('{' == value.charAt(0)) {
                    sanitizeJsonSetting(setting);
                }
            }
            storage.save(setting);
        } else {
            final JSONObject json;
            {
                final Object singleValue = setting.getSingleValue();
                if (singleValue instanceof JSONObject) {
                    json = new JSONObject((JSONObject) singleValue);
                } else {
                    json = new JSONObject(singleValue.toString());
                }
            }
            final Iterator<String> iter = json.keys();
            OXException exc = null;
            while (iter.hasNext()) {
                final String key = iter.next();
                final Setting sub = ConfigTree.getSettingByPath(setting, new String[] { key });
                sub.setSingleValue(json.getString(key));
                try {
                    // Catch single exceptions if GUI writes not writable fields.
                    saveSettingWithSubs(storage, sub);
                } catch (OXException e) {
                    exc = e;
                }
            }
            if (null != exc) {
                throw exc;
            }
        }
    }
}
