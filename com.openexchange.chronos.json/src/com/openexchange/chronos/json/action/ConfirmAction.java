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

import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ConfirmAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ConfirmAction extends ChronosAction {

    /**
     * Initializes a new {@link ConfirmAction}.
     * @param services
     */
    protected ConfirmAction(ServiceLookup services) {
        super(services);
    }

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet("timezone",CalendarParameters.PARAMETER_TIMESTAMP );

    private static final String CONFIRMATION_FIELD="confirmation";
    private static final String CONFIRM_MESSAGE_FIELD="confirmmessage";
    private static final String URI_FIELD="uri";

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {

        Object data = requestData.getData();
        Attendee attendee = new Attendee();
        if(data instanceof JSONObject){
            try {
                String confirmation = ((JSONObject) data).getString(CONFIRMATION_FIELD);
                attendee.setPartStat(new ParticipationStatus(confirmation));
            } catch (JSONException e) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
            }
            String confirmMSG = ((JSONObject) data).optString(CONFIRM_MESSAGE_FIELD);
            if(Strings.isNotEmpty(confirmMSG)){
                attendee.setComment(confirmMSG);
            }
            String uri = ((JSONObject) data).optString(URI_FIELD);

            if(Strings.isNotEmpty(uri)){
                attendee.setUri(uri);
            } else {
                attendee.setUri(CalendarUtils.getURI(requestData.getSession().getUser().getMail()));
            }
        } else {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }


        CalendarResult calResult = calendarAccess.updateAttendee(parseIdParameter(requestData), attendee);
        List<UpdateResult> updates = calResult.getUpdates();
        if(updates.size()==1){
            Event update = updates.get(0).getUpdate();
            return new AJAXRequestResult(update, update.getLastModified(), "event");
        } else {
            List<Event> events = new ArrayList<>(updates.size());
            for(UpdateResult result: updates){
                events.add(result.getUpdate());
            }
            return new AJAXRequestResult(events, "event");
        }

    }

}
