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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.ajax;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.settings.ConfigTree;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.settings.SettingStorage;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * This class implements the servlet for sending and reading user specific
 * configuration settings inside the AJAX GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigMenu extends SessionServlet {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ConfigMenu.class);
    
    /**
     * Size for the read buffer.
     */
    private static final int BUFFER_SIZE = 512;

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -7113587607566553771L;

    /**
     * {@inheritDoc}
     */
    protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        String path = getServletSpecificURI(req);
        if (path.length() > 0 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        final Session sessionObj = getSessionObject(req);

        final SettingStorage stor = SettingStorage.getInstance(sessionObj);
        Setting setting;
        final Response response = new Response();
        try {
            setting = ConfigTree.getSettingByPath(path);
            stor.readValues(setting);
            response.setData(convert2JS(setting));
        } catch (AbstractOXException e) {
            response.setException(e);
        } catch (JSONException e) {
            final OXJSONException oje = new OXJSONException(OXJSONException.Code
                .JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            Response.write(response, resp.getWriter());
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
    private static Object convert2JS(final Setting setting)
        throws JSONException {
        Object retval = null;
        if (setting.isLeaf()) {
            final Object[] multiValue = setting.getMultiValue();
            if (null == multiValue) {
                final Object singleValue = setting.getSingleValue();
                if (null == singleValue) {
                    retval = JSONObject.NULL;
                } else {
                    try {
                        retval = new JSONObject(singleValue.toString());
                    } catch (JSONException e) {
                        retval = singleValue;
                    }
                }
            } else {
                final JSONArray array = new JSONArray();
                for (Object value : multiValue) {
                    array.put(value);
                }
                retval = array;
            }
        } else {
            final JSONObject json = new JSONObject();
            for (Setting subSetting : setting.getElements()) {
                json.put(subSetting.getName(), convert2JS(subSetting));
            }
            retval = json;
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    protected void doPut(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        final Session session = getSessionObject(req);
        final InputStream input = req.getInputStream();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(
            input.available());
        final byte[] buf = new byte[BUFFER_SIZE];
        int length = input.read(buf);
        while (length != -1) {
            baos.write(buf, 0, length);
            length = input.read(buf);
        }
        String encoding = req.getCharacterEncoding();
        if (null == encoding) {
            log("Client did not specify the character encoding.");
            encoding = ServerConfig.getProperty(Property.DefaultEncoding);//"UTF-8";
        }
        String value = new String(baos.toByteArray(), encoding);
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
        final Response response = new Response();
        try {
            final Setting setting = ConfigTree.getSettingByPath(path);
            setting.setSingleValue(value);
            saveSettingWithSubs(stor, setting);
        } catch (AbstractOXException e) {
            log(e.getMessage(), e);
            response.setException(e);
        } catch (JSONException e) {
            final OXJSONException oje = new OXJSONException(OXJSONException.Code
                .JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        }
        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
            if (response.hasError()) {
                Response.write(response, resp.getWriter());
            }
        } catch (JSONException e) {
            log(RESPONSE_ERROR, e);
            sendError(resp);
        }
    }

    /**
     * Splits a value for a not leaf setting into its subsettings and stores
     * them.
     * @param storage setting storage.
     * @param setting actual setting.
     * @throws SettingException if an error occurs.
     * @throws JSONException if the json object can't be parsed.
     */
    private void saveSettingWithSubs(final SettingStorage storage,
        final Setting setting) throws SettingException,
        JSONException {
        if (setting.isLeaf()) {
            final String value = (String) setting.getSingleValue();
            if (null != value) {
                try {
                    final JSONArray array = new JSONArray(value);
                    for (int i = 0; i < array.length(); i++) {
                        setting.addMultiValue(array.getString(i));
                    }
                    setting.setSingleValue(null);
                } catch (JSONException e) {
                    // I check if there is a JSON array in the value.
                    // No logging here because this is not an error.
                	if (LOG.isTraceEnabled()) { // Added to remove PMD warning about an empty catch clause
                		LOG.trace(e.getMessage(), e);
                	}
                }
            }
            storage.save(setting);
        } else {
            final JSONObject json = new JSONObject((String) setting
                .getSingleValue());
            final int numOfKeys = json.length();
            final Iterator iter = json.keys();
            SettingException exc = null;
            for (int k = 0; k < numOfKeys; k++) {
                final String key = (String) iter.next();
                final Setting sub = ConfigTree.getSettingByPath(setting, key);
                sub.setSingleValue(json.getString(key));
                try {
                    // FIXME catch single exceptions if GUI writes not writable
                    // fields.
                    saveSettingWithSubs(storage, sub);
                } catch (SettingException e) {
                    exc = e;
                }
            }
            if (null != exc) {
                throw exc;
            }
        }
    }
}
