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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.find.Module;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link FindRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class FindRequest {

    private static final String PARAM_MODULE = "module";

    private static final String PARAM_PREFIX = "prefix";

    private static final String MODULE_MAIL = "mail";

    private static final String MODULE_CONTACTS = "contacts";

    private static final String MODULE_CALENDAR = "calendar";

    private static final String MODULE_TASKS = "tasks";

    private static final String MODULE_DRIVE = "drive";

    private final AJAXRequestData request;

    private final ServerSession session;

    /**
     * Initializes a new {@link FindRequest}.
     * @param request
     * @param session
     */
    public FindRequest(AJAXRequestData request, ServerSession session) {
        super();
        this.request = request;
        this.session = session;
    }

    public ServerSession getServerSession() {
        return session;
    }

    public Module getModule() {
        String module = request.getParameter(PARAM_MODULE);
        if (module == null) {
            return null;
        }

        return getModuleForName(module);
    }

    public Module requireModule() throws OXException {
        String moduleValue = request.getParameter(PARAM_MODULE);
        if (moduleValue == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAM_MODULE);
        }

        Module module = getModuleForName(moduleValue);
        if (module == null) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(PARAM_MODULE, moduleValue);
        }

        return module;
    }

    public String requirePrefix() throws OXException {
        JSONObject json = (JSONObject) request.requireData();
        try {
            String prefix = json.getString(PARAM_PREFIX);
            if (prefix == null) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAM_PREFIX);
            }

            return prefix;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    public String requireParameter(String name) throws OXException {
        return request.requireParameter(name);
    }

    public String getParameter(String name) {
        return request.getParameter(name);
    }

    public int getIntParameter(String name) throws OXException {
        return request.getIntParameter(name);
    }

    public <T> T getParameter(String name, Class<T> coerceTo) throws OXException {
        return request.getParameter(name, coerceTo);
    }

    private Module getModuleForName(String module) {
        if (MODULE_MAIL.equals(module)) {
            return Module.MAIL;
        } else if (MODULE_CONTACTS.equals(module)) {
            return Module.CONTACTS;
        } else if (MODULE_CALENDAR.equals(module)) {
            return Module.CALENDAR;
        } else if (MODULE_TASKS.equals(module)) {
            return Module.TASKS;
        } else if (MODULE_DRIVE.equals(module)) {
            return Module.DRIVE;
        }

        return null;
    }

}
