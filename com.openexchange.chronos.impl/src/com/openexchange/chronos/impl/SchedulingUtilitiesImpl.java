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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.impl.Utils.postProcess;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.performer.ResolvePerformer;
import com.openexchange.chronos.impl.scheduling.AddProcessor;
import com.openexchange.chronos.impl.scheduling.AttendeeUpdateProcessor;
import com.openexchange.chronos.impl.scheduling.CancelProcessor;
import com.openexchange.chronos.impl.scheduling.ReplyProcessor;
import com.openexchange.chronos.impl.scheduling.RequestProcessor;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.SchedulingSource;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SchedulingUtilities;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SchedulingUtilitiesImpl} - Class providing utility method for processing and updating the calendar based
 * on am incoming scheduling message
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
@SingletonService
public class SchedulingUtilitiesImpl implements SchedulingUtilities {

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link SchedulingUtilitiesImpl}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     *
     */
    public SchedulingUtilitiesImpl(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public CalendarResult processAdd(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message, Attendee attendee) throws OXException {
        return postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                AddProcessor addProcessor = new AddProcessor(storage, session, getTargetFolder(session, storage, message));
                addProcessor.process(message);
                return new AttendeeUpdateProcessor(addProcessor, message.getMethod()).process(attendee);
            }
        }.executeUpdate()).getUserizedResult();
    }

    @Override
    public CalendarResult processCancel(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message) throws OXException {
        return postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new CancelProcessor(storage, session, getTargetFolder(session, storage, message)).process(message);
            }
        }.executeUpdate()).getUserizedResult();
    }

    @Override
    public CalendarResult processReply(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message) throws OXException {
        return postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ReplyProcessor(storage, session, getTargetFolder(session, storage, message), source).process(message);
            }
        }.executeUpdate()).getUserizedResult();
    }

    @Override
    public CalendarResult processRequest(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message, Attendee attendee) throws OXException {
        return postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                RequestProcessor requestProcessor = new RequestProcessor(session, storage, getTargetFolder(session, storage, message));
                requestProcessor.process(message.getResource());
                return new AttendeeUpdateProcessor(requestProcessor, message.getMethod()).process(attendee);
            }
        }.executeUpdate()).getUserizedResult();
    }

    /*
     * ============================== HELPERS ==============================
     */

    static CalendarFolder getTargetFolder(CalendarSession session, CalendarStorage storage, IncomingSchedulingMessage message) throws OXException {
        String uid = message.getResource().getUid();
        int calendarUserId = message.getTargetUser();
        boolean fallbackToDefault = SchedulingMethod.REQUEST.equals(message.getMethod());
        String folderId = new ResolvePerformer(session, storage).resolveFolderIdByUID(uid, calendarUserId, fallbackToDefault);
        if (null == folderId) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(uid);
        }
        return Utils.getFolder(session, folderId);
    }
}
