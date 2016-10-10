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

package com.openexchange.jslob.json.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.json.JSlobRequest;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public final class ListAction extends JSlobAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListAction.class);

    /**
     * Initializes a new {@link ListAction}.
     *
     * @param services The service look-up
     */
    public ListAction(final ServiceLookup services, final Map<String, JSlobAction> actions) {
        super(services, actions);
    }

    @Override
    protected AJAXRequestResult perform(final JSlobRequest jslobRequest) throws OXException, JSONException {
        try {
            String serviceId = jslobRequest.getParameter("serviceId", String.class, true);
            if (null == serviceId) {
                serviceId = DEFAULT_SERVICE_ID;
            }
            final JSlobService jslobService = getJSlobService(serviceId);
            final JSONArray ids = (JSONArray) jslobRequest.getRequestData().requireData();
            final int length = ids.length();
            // Check length
            if (length <= 1) {
                if (0 == length) {
                    return new AJAXRequestResult(Collections.<JSlob> emptyList(), "jslob");
                }
                return new AJAXRequestResult(Collections.<JSlob> singletonList(jslobService.get(ids.getString(0), jslobRequest.getSession())), "jslob");
            }
            // More than one to load
            final List<String> sIds = new ArrayList<String>(length);
            for (int i = 0; i < length; i++) {
                sIds.add(ids.getString(i));
            }
            return new AJAXRequestResult(jslobService.get(sIds, jslobRequest.getSession()), "jslob");
        } catch (final RuntimeException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getAction() {
        return "list";
    }

}
