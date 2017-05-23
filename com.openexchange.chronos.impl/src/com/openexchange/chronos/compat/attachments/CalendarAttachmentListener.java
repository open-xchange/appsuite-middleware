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

package com.openexchange.chronos.compat.attachments;

import java.util.Date;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.StorageOperation;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.AttachmentStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.session.Session;

/**
 * {@link CalendarAttachmentListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAttachmentListener implements AttachmentListener {

    private final CalendarService calendarService;

    /**
     * Initializes a new {@link CalendarAttachmentListener}.
     *
     * @param calendarService A reference to the calendar service
     */
    public CalendarAttachmentListener(CalendarService calendarService) {
        super();
        this.calendarService = calendarService;
    }

    @Override
    public long attached(AttachmentEvent e) throws Exception {
        return touch(e);
    }

    @Override
    public long detached(AttachmentEvent e) throws Exception {
        return touch(e);
    }

    private long touch(AttachmentEvent event) throws OXException {
        if (isManagedInternally(event)) {
            return -1;
        }
        final String objectId = String.valueOf(event.getAttachedId());
        CalendarSession session = calendarService.init(event.getSession());
        return new StorageOperation<Long>(session) {

            @Override
            protected Long execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return touch(session, storage, objectId);
            }
        }.executeUpdate().longValue();
    }

    private static long touch(CalendarSession session, CalendarStorage storage, String objectId) throws OXException {
        Date now = new Date();
        Event eventUpdate = new Event();
        eventUpdate.setId(objectId);
        Consistency.setModified(now, eventUpdate, session.getUserId());
        storage.getEventStorage().updateEvent(eventUpdate);
        return now.getTime();
    }

    private static boolean isManagedInternally(AttachmentEvent event) {
        Session session = event.getSession();
        return null != session && Boolean.TRUE.equals(session.getParameter(AttachmentStorage.class.getName()));
    }

}
