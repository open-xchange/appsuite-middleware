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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.jslob.json.action;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSONUpdate;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.json.JSlobRequest;
import com.openexchange.server.ServiceLookup;

/**
 * {@link UpdateAction}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateAction extends JSlobAction {

    /**
     * Initializes a new {@link UpdateAction}.
     * 
     * @param services The service look-up
     */
    public UpdateAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final JSlobRequest jslobRequest) throws OXException, JSONException {
        String serviceId = jslobRequest.getParameter("serviceId", String.class);
        if (null == serviceId) {
            serviceId = DEFAULT_SERVICE_ID;
        }
        final JSlobService jslobService = getJSlobService(serviceId);

        final String id = jslobRequest.checkParameter("id");
        final int userId = jslobRequest.getUserId();
        final int contextId = jslobRequest.getContextId();

        JSlob jslob;
        {
            final AJAXRequestData requestData = jslobRequest.getRequestData();
            final String serlvetRequestURI = requestData.getSerlvetRequestURI();
            if (!isEmpty(serlvetRequestURI)) {
                /*
                 * Update by request path
                 */
                final JSONUpdate jsonUpdate = new JSONUpdate(serlvetRequestURI, jslobRequest.getRequestData().getData());
                /*
                 * Update...
                 */
                jslobService.update(id, jsonUpdate, userId, contextId);
                /*
                 * ... and write back
                 */
                jslob = jslobService.get(id, userId, contextId);
            } else {
                /*
                 * Update by JSON data
                 */
                final JSONObject jsonData = (JSONObject) jslobRequest.getRequestData().getData();
                if (jsonData.hasAndNotNull("path")) {
                    final JSONUpdate jsonUpdate = new JSONUpdate(jsonData.getString("path"), jsonData.get("value"));
                    /*
                     * Update...
                     */
                    jslobService.update(id, jsonUpdate, userId, contextId);
                    /*
                     * ... and write back
                     */
                    jslob = jslobService.get(id, userId, contextId);
                } else {
                    /*
                     * Perform Set
                     */
                    jslob = new JSlob(jsonData);
                    jslobService.set(id, jslob, userId, contextId);
                    /*
                     * ... and write back
                     */
                    jslob = jslobService.get(id, userId, contextId);
                }
            }
        }
        return new AJAXRequestResult(jslob, "jslob");
    }

    @Override
    public String getAction() {
        return "update";
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
