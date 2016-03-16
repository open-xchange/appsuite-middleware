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

package com.openexchange.xing.json.actions;

import java.util.Collection;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.xing.UserField;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;

/**
 * {@link GetCommentsActivityAction}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetCommentsActivityAction extends AbstractXingAction {
    /**
     * Initializes a new {@link GetCommentsActivityAction}.
     */
    public GetCommentsActivityAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.xing.json.actions.AbstractXingAction#perform(com.openexchange.xing.json.XingRequest)
     */
    @Override
    protected AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException {
        String activityId = getMandatoryStringParameter(req, "activity_id");
        int optLimit = getOptIntParameter(req, "limit");
        int optOffset = getOptIntParameter(req, "offset");
        Collection<UserField> optUserFields = getUserFields(req.getParameter("user_fields"));

        Map<String, Object> response = getXingAPI(req).getComments(activityId, optLimit, optOffset, optUserFields);
        return new AJAXRequestResult(JSONCoercion.coerceToJSON(response));
    }
}
