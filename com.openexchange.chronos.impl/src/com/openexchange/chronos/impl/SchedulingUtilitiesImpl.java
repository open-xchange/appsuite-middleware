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

import static com.openexchange.chronos.impl.Utils.getCalendarFolder;
import static com.openexchange.chronos.impl.Utils.postProcess;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.scheduling.AddProcessor;
import com.openexchange.chronos.impl.scheduling.CancelProcessor;
import com.openexchange.chronos.impl.scheduling.PostProcessor;
import com.openexchange.chronos.impl.scheduling.ReplyProcessor;
import com.openexchange.chronos.impl.scheduling.RequestProcessor;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.SchedulingProperties;
import com.openexchange.chronos.scheduling.SchedulingSource;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SchedulingUtilities;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.OXFolderAccess;

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
    public CalendarResult processAdd(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message) throws OXException {
        InternalCalendarResult result = postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                Event event = message.getResource().getFirstEvent();
                return new AddProcessor(storage, session, getCalendarFolder(session, storage, event.getUid(), event.getRecurrenceId(), message.getTargetUser())).process(message);
            }
        }.executeUpdate());
        return postProcessScheduling(session, source, message, result);
    }

    @Override
    public CalendarResult processCancel(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message) throws OXException {
        return postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                Event event = message.getResource().getFirstEvent();
                return new CancelProcessor(storage, session, getCalendarFolder(session, storage, event.getUid(), event.getRecurrenceId(), message.getTargetUser())).process(message);
            }
        }.executeUpdate()).getUserizedResult();
    }

    @Override
    public CalendarResult processReply(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message) throws OXException {
        return postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                Event event = message.getResource().getFirstEvent();
                return new ReplyProcessor(storage, session, getCalendarFolder(session, storage, event.getUid(), event.getRecurrenceId(), message.getTargetUser()), source).process(message);
            }
        }.executeUpdate()).getUserizedResult();
    }

    @Override
    public CalendarResult processRequest(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message) throws OXException {
        InternalCalendarResult result = postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                Event event = message.getResource().getFirstEvent();
                CalendarFolder calendarFolder;
                try {
                    calendarFolder = getCalendarFolder(session, storage, event.getUid(), event.getRecurrenceId(), message.getTargetUser());
                } catch (OXException e) {
                    if (false == e.equalsCode(CalendarExceptionCodes.EVENT_NOT_FOUND.getNumber(), CalendarExceptionCodes.PREFIX)) {
                        throw e;
                    }
                    LOG.debug("Unable to find event. Using default calendar folder");
                    calendarFolder = Utils.getFolder(session, getPrivateCalendarFolderId(session.getContextId(), session.getUserId()));
                }
                return new RequestProcessor(storage, session, calendarFolder).process(message);
            }
        }.executeUpdate());
        return postProcessScheduling(session, source, message, result);
    }

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Get an additional property from the message
     *
     * @param message The message
     * @param key The key
     * @param defaultValue The default value to return if no value is present
     * @return
     */
    private String getProperty(IncomingSchedulingMessage message, String key, String defaultValue) {
        return message.getAdditional(key, String.class).orElse(defaultValue);
    }

    /**
     * Get the private default calendar folder of the acting user
     *
     * @param contextId The context of the user
     * @param userId The user identifier
     * @return The default folder identifier of the users private calendar folder
     * @throws OXException In case folder can't be get
     */
    protected String getPrivateCalendarFolderId(int contextId, int userId) throws OXException {
        ContextService contextService = serviceLookup.getServiceSafe(ContextService.class);
        final Context ctx = contextService.getContext(contextId);
        final OXFolderAccess acc = new OXFolderAccess(ctx);
        return String.valueOf(acc.getDefaultFolderID(userId, FolderObject.CALENDAR));
    }

    /**
     * Post processes a scheduling action to e.g. adjust attendee status to transmitted data and returns
     * the userized result
     *
     * @param session The calendar session
     * @param source The scheduling source
     * @param message The message
     * @param result The result from the processed action
     * @return Userized result
     * @throws OXException In case post processing fails
     */
    private CalendarResult postProcessScheduling(CalendarSession session, SchedulingSource source, IncomingSchedulingMessage message, InternalCalendarResult result) throws OXException {
        if (SchedulingSource.API.equals(source)) {
            /*
             * Post process data, e.g. set correct attendee status
             */
            return postProcessScheduling(session, result, SchedulingMethod.REQUEST, getProperty(message, SchedulingProperties.ACTION, null), getProperty(message, SchedulingProperties.COMMENT, null)).getUserizedResult();
        }
        return result.getUserizedResult();
    }

    /**
     * Post processes a scheduling action to e.g. adjust attendee status to transmitted data
     *
     * @param session The calendar session
     * @param result The result from the processed action
     * @param method The method
     * @param action The action to perform in the post processing
     * @param comment The optional comment to set for the current user
     * @return A merged result
     * @throws OXException In case processing fails
     */
    private InternalCalendarResult postProcessScheduling(CalendarSession session, InternalCalendarResult result, SchedulingMethod method, String action, String comment) throws OXException {
        /*
         * Send messages to organizer or other attendees as usual
         */
        return postProcess(serviceLookup, new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new PostProcessor(session, storage, result.getFolder(), method).process(result, action, comment);
            }
        }.executeUpdate());
    }
}
