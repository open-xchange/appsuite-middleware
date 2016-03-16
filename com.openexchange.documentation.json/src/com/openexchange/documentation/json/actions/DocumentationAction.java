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

package com.openexchange.documentation.json.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.DocumentationRegistry;
import com.openexchange.documentation.descriptions.ActionDescription;
import com.openexchange.documentation.descriptions.AttributeDescription;
import com.openexchange.documentation.descriptions.ContainerDescription;
import com.openexchange.documentation.descriptions.Description;
import com.openexchange.documentation.descriptions.ModuleDescription;
import com.openexchange.documentation.descriptions.ParameterDescription;
import com.openexchange.documentation.json.DocumentationAJAXRequest;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DocumentationAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DocumentationAction implements AJAXActionService {

    /**
     * The service look-up.
     */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link DocumentationAction}.
     *
     * @param services The service look-up
     */
    protected DocumentationAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            final DocumentationAJAXRequest ajaxRequest = new DocumentationAJAXRequest(requestData, session);
            return perform(ajaxRequest);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the {@link DocumentationRegistry} service.
     * @return
     */
    protected DocumentationRegistry getRegistry() {
        return this.getService(DocumentationRegistry.class);
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(final Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    /**
     * Writes the given module description as a JSON object.
     *
     * @param module the module description to write
     * @return the written JSON object
     * @throws JSONException if JSON serialization fails
     */
    protected JSONObject write(final ModuleDescription module) throws JSONException {
        final JSONObject jsonObject = this.write((Description)module);
        final ContainerDescription[] containers = module.getContainers();
        if (null != containers) {
            final JSONArray jsonArray = new JSONArray();
            for (final ContainerDescription container : containers) {
            	jsonArray.put(write(container));
    		}
        	jsonObject.put("containers", jsonArray);
        }
        final ActionDescription[] actions = module.getActions();
        if (null != actions) {
            final JSONArray jsonArray = new JSONArray();
            for (final ActionDescription action : actions) {
            	jsonArray.put(write(action));
    		}
        	jsonObject.put("actions", jsonArray);
        }
        return jsonObject;
    }

    /**
     * Writes the given container description as a JSON object.
     *
     * @param container the container description to write
     * @return the written JSON object
     * @throws JSONException if JSON serialization fails
     */
    protected JSONObject write(final ContainerDescription container) throws JSONException {
        final JSONObject jsonObject = this.write((Description)container);
        final AttributeDescription[] attributes = container.getAttributes();
        if (null != attributes) {
            final JSONArray jsonArray = new JSONArray();
            for (final AttributeDescription attribute : attributes) {
            	jsonArray.put(write(attribute));
    		}
        	jsonObject.put("attributes", jsonArray);
        }
        return jsonObject;
    }

    /**
     * Writes the given action description as a JSON object.
     *
     * @param action the action description to write
     * @return the written JSON object
     * @throws JSONException if JSON serialization fails
     */
    protected JSONObject write(final ActionDescription action) throws JSONException {
        final JSONObject jsonObject = this.write((Description)action);
        final ParameterDescription[] parameters = action.getParameters();
        if (null != parameters) {
            final JSONArray jsonArray = new JSONArray();
            for (final ParameterDescription parameter : parameters) {
            	jsonArray.put(write(parameter));
    		}
        	jsonObject.put("parameters", jsonArray);
        }
    	jsonObject.put("method", action.getMethod().toString());
    	jsonObject.put("defaultFormat", action.getDefaultFormat());
    	jsonObject.put("responseDescription", action.getResponseDescription());
    	jsonObject.put("requestBody", action.getRequestBody());
    	jsonObject.put("deprecated", action.isDeprecated());
        return jsonObject;
    }

    /**
     * Writes the given attribute description as a JSON object.
     *
     * @param attribute the attribute description to write
     * @return the written JSON object
     * @throws JSONException if JSON serialization fails
     */
    protected JSONObject write(final AttributeDescription attribute) throws JSONException {
        final JSONObject jsonObject = this.write((Description)attribute);
        jsonObject.put("mandatory", attribute.isMandatory());
        jsonObject.put("type", attribute.getType().toString());
        return jsonObject;
    }

    /**
     * Writes the given parameter description as a JSON object.
     *
     * @param parameter the parameter description to write
     * @return the written JSON object
     * @throws JSONException if JSON serialization fails
     */
    protected JSONObject write(final ParameterDescription parameter) throws JSONException {
        final JSONObject jsonObject = this.write((Description)parameter);
        jsonObject.put("optional", parameter.isOptional());
        jsonObject.put("type", parameter.getType().toString());
        return jsonObject;
    }

    /**
     * Writes the given description as a JSON object.
     *
     * @param description the description to write
     * @return the written JSON object
     * @throws JSONException if JSON serialization fails
     */
    private JSONObject write(final Description description) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", description.getName());
        jsonObject.put("description", description.getDescription());
        return jsonObject;
    }

    /**
     * Performs specified AJAX request.
     *
     * @param request The AJAX request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(DocumentationAJAXRequest request) throws OXException, JSONException;

}
