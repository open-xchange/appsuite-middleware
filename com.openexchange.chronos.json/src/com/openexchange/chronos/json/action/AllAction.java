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

package com.openexchange.chronos.json.action;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_FIELDS;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_INCLUDE_PRIVATE;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER_BY;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_UPDATE_CACHE;
import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.json.converter.EventResultConverter;
import com.openexchange.chronos.json.oauth.ChronosOAuthScope;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AllAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@OAuthAction(ChronosOAuthScope.OAUTH_READ_SCOPE)
public class AllAction extends ChronosAction {

    private static final Set<String> REQUIRED_PARAMETERS = unmodifiableSet(PARAM_RANGE_START, PARAM_RANGE_END);

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_EXPAND, PARAMETER_ORDER_BY, PARAMETER_ORDER, PARAMETER_FIELDS, PARAMETER_INCLUDE_PRIVATE, PARAMETER_UPDATE_CACHE);

    private static final String PARAMETER_FOLDERS = "folders";

    /**
     * Initializes a new {@link AllAction}.
     *
     * @param services A service lookup reference
     */
    protected AllAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        List<Event> events = calendarAccess.getEventsInFolders(parseFolderIds(requestData));
        return new AJAXRequestResult(events, new Date(CalendarUtils.getMaximumTimestamp(events)), EventResultConverter.INPUT_FORMAT);
    }

    private static List<String> parseFolderIds(AJAXRequestData requestData) throws OXException {
        JSONObject jsonObject = requestData.getData(JSONObject.class);
        if (null == jsonObject) {
            return Collections.singletonList(requestData.requireParameter(AJAXServlet.PARAMETER_FOLDERID));
        }
        try {
            JSONArray jsonArray = jsonObject.getJSONArray(PARAMETER_FOLDERS);
            List<String> folderIds = new ArrayList<String>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                folderIds.add(jsonArray.getString(i));
            }
            return folderIds;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
    }

}
