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

import static com.openexchange.chronos.json.fields.ChronosJsonFields.COMMENT;
import static com.openexchange.chronos.json.fields.ChronosJsonFields.ORGANIZER;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_PUSH_TOKEN;
import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.Date;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.converter.CalendarResultConverter;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.property.ChronosLeanConfigurationProperties;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ChangeOrganizerAction} - "/chronos/changeOrganizer" endpoint for updating an organizer
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public class ChangeOrganizerAction extends ChronosAction {

    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAMETER_PUSH_TOKEN);

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    private static final EventField[] ORGANIZER_FIELD = new EventField[] { EventField.ORGANIZER };

    /**
     * Initializes a new {@link ChangeOrganizerAction}.
     * 
     * @param services The {@link ServiceLookup}
     */
    public ChangeOrganizerAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        LeanConfigurationService service = services.getServiceSafe(LeanConfigurationService.class);
        if (false == service.getBooleanProperty(ChronosLeanConfigurationProperties.ALLOWED_ORGANIZER_CHANGE.getProperty())) {
            throw AjaxExceptionCodes.DISABLED_ACTION.create("changeOrganizer");
        }

        long clientTimestamp = parseClientTimestamp(requestData);
        EventID eventId = parseIdParameter(requestData);

        JSONObject jsonObject = requestData.getData(JSONObject.class);
        if (null == jsonObject) {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }

        try {
            String comment = jsonObject.getString(COMMENT);
            if (Strings.isNotEmpty(comment)) {
                calendarAccess.set(CalendarParameters.PARAMETER_COMMENT, comment);
            }
        } catch (JSONException e) {
            LOG.debug("Unable to read comment parameter", e);
        }

        try {
            Event deserialize = EventMapper.getInstance().deserialize(jsonObject, ORGANIZER_FIELD);
            CalendarResult calendarResult = calendarAccess.changeOrganizer(eventId, deserialize.getOrganizer(), clientTimestamp);
            return new AJAXRequestResult(calendarResult, new Date(calendarResult.getTimestamp()), CalendarResultConverter.INPUT_FORMAT);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.MISSING_FIELD.create(ORGANIZER);
        }
    }

}
