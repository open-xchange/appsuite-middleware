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

package com.openexchange.chronos.provider.google.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.SingleFolderCalendarAccess;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.google.converter.GoogleEventConverter;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;

/**
 * {@link GoogleCalendarAccess}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarAccess extends SingleFolderCalendarAccess {

    private final GoogleOAuthAccess oauthAccess;

    /**
     * Initializes a new {@link GoogleCalendarAccess}.
     *
     * @param session The user session
     * @param account The calendar account
     * @param parameters The calendar parameters
     * @throws OXException
     */
    public GoogleCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super(session, account, parameters);
        oauthAccess = new GoogleOAuthAccess(account, session);
        oauthAccess.initialize();
    }

    @Override
    public void close() {
        oauthAccess.dispose();
    }

    @Override
    protected Event getEvent(String eventId, RecurrenceId recurrenceId) throws OXException {
        try {
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            String calendar = account.getUserConfiguration().getString(GoogleCalendarConfigField.calendar_id.name());
            com.google.api.services.calendar.model.Event event = googleCal.events().get(calendar, eventId).execute();
            return GoogleEventConverter.getInstance().convertToEvent(event);
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(account.getUserConfiguration());
        }
    }

    @Override
    protected List<Event> getEvents() throws OXException {
        try {
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            String calendar = account.getUserConfiguration().getString(GoogleCalendarConfigField.calendar_id.name());
            Events events = googleCal.events().list(calendar).execute();
            List<Event> result = new ArrayList<>(events.size());
            for(com.google.api.services.calendar.model.Event event : events.getItems()){
                result.add(GoogleEventConverter.getInstance().convertToEvent(event));
            }
            return result;
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(account.getUserConfiguration());
        }
    }

    @Override
    protected CalendarAccountService getAccountService() throws OXException {
        return Services.getService(CalendarAccountService.class);
    }

}
