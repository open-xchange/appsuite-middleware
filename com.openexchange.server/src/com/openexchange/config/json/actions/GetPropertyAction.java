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

package com.openexchange.config.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.json.ConfigAJAXRequest;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetPropertyAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@Action(method = RequestMethod.GET, name = "get_property", description = "Gets a user property", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "name", description = "The property name.")
}, responseDescription = "A JSON object providing the property's name and its value")
public class GetPropertyAction extends AbstractConfigAction {

    /**
     * Initializes a new {@link GetPropertyAction}.
     *
     * @param services The service look-up
     */
    public GetPropertyAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(ConfigAJAXRequest req) throws OXException, JSONException {
        ConfigViewFactory factory = getService(ConfigViewFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        // Check for property name parameter
        String propertyName = req.checkParameter("name");
        if (Strings.isEmpty(propertyName)) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("name", propertyName);
        }

        // Get the configuration view associated with current user
        ServerSession session = req.getSession();
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());

        ConfigProperty<String> property = view.property("context", propertyName, String.class);
        if (false == property.isDefined()) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(propertyName);
        }

        if (Boolean.parseBoolean(property.get("protected"))) {
            // Do not expose protected properties via HTTP-API
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(propertyName);
        }

        ComposedConfigProperty<String> composedProperty = view.property(propertyName, String.class);
        return new AJAXRequestResult(new JSONObject(2).put("name", propertyName).put("value", composedProperty.get())); // Defaults to "json" format
    }

}
