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

package com.openexchange.importexport.importers.ical;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ICalCompositeEventImporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalCompositeEventImporter extends AbstractICalEventImporter {

    IDBasedCalendarAccess calendarAccess;

    public ICalCompositeEventImporter(ServerSession session) {
        super(session);
    }

    @Override
    public CalendarResult createEvent(String folderId, Event event) throws OXException {
        return calendarAccess.createEvent(folderId, event);
    }

    @Override
    public CalendarResult updateEvent(EventID eventId, Event event) throws OXException {
        return calendarAccess.updateEvent(eventId, event, System.currentTimeMillis());
    }

    @Override
    protected TruncationInfo initImporter(UserizedFolder userizedFolder, List<Event> eventList, Map<String, String[]> optionalParams, List<ImportResult> list) throws OXException {
        this.calendarAccess = initCalendarAccess(optionalParams);
        TruncationInfo truncationInfo;
        boolean committed = false;
        try {
            calendarAccess.startTransaction();
            truncationInfo = importEvents(userizedFolder, eventList, optionalParams, list);
            calendarAccess.commit();
            committed = true;
        } finally {
            if (false == committed) {
                calendarAccess.rollback();
            }
            calendarAccess.finish();
        }
        return truncationInfo;
    }

    private IDBasedCalendarAccess initCalendarAccess(Map<String, String[]> optionalParams) throws OXException {
        IDBasedCalendarAccess calendarAccess = ImportExportServices.getIDBasedCalendarAccessFactory().createAccess(session);
        calendarAccess.set(CalendarParameters.PARAMETER_IGNORE_CONFLICTS, Boolean.TRUE);
        if (isSupressNotification(optionalParams)) {
            calendarAccess.set(CalendarParameters.PARAMETER_NOTIFICATION, Boolean.FALSE);
        }
        return calendarAccess;
    }

}
