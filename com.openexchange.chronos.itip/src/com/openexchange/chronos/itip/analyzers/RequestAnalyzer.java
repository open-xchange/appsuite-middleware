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

package com.openexchange.chronos.itip.analyzers;

import static com.openexchange.chronos.common.CalendarUtils.matches;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

/**
 * {@link RequestAnalyzer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 8.0.0
 */
public class RequestAnalyzer extends AbstractITipAnalyzer {

    private final static Logger LOG = LoggerFactory.getLogger(RequestAnalyzer.class);

    /**
     * Initializes a new {@link RequestAnalyzer}.
     *
     * @param util The {@link ITipIntegrationUtility}
     */
    public RequestAnalyzer(ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Collections.singletonList(ITipMethod.REQUEST);
    }

    @Override
    public ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, CalendarSession session) throws OXException {
        return analyze(session, session.getUserId(), message, wrapper, locale);
    }

    private ITipAnalysis analyze(CalendarSession session, int calendarUserId, ITipMessage message, TypeWrapper wrapper, Locale locale) throws OXException {
        /*
         * prepare analysis & construct calendar object resource from itip message
         */
        ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);
        CalendarObjectResource resource;
        try {
            resource = getResource(session, message, true);
        } catch (OXException e) {
            analysis.addAnnotation(new ITipAnnotation(e.getDisplayMessage(locale), locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }
        analysis.setUid(resource.getUid());
        /*
         * get currently stored resource & determine changes
         */
        CalendarObjectResource originalResource = getOriginalResource(session, resource.getUid(), calendarUserId);
        for (ITipChange change : determineChanges(session, calendarUserId, originalResource, message, wrapper)) {
            analysis.addChange(change);
        }
        /*
         * set recommendation & return resulting analysis
         */
        if (null != originalResource && false == matches(originalResource.getOrganizer(), resource.getOrganizer())) {
            /*
             * organizer change, needs to be ignored
             */
            analysis.addAnnotation(new ITipAnnotation(Messages.UNALLOWED_ORGANIZER_CHANGE, locale));
            analysis.recommendAction(ITipAction.IGNORE);
        } else if (isOutOfSequence(analysis.getChanges())) {
            /*
             * old update, replace changes to only transport currently stored event as single change to client
             */
            analysis.getChanges().clear();
            Event currentEvent = null != originalResource ? originalResource.getFirstEvent() : null;
            analysis.addChange(getChange(session, calendarUserId, Type.UPDATE, currentEvent, null, null, false));
            analysis.addAnnotation(new ITipAnnotation(Messages.OLD_UPDATE, locale));
            analysis.recommendAction(ITipAction.IGNORE);
        } else if (analysis.getChanges().isEmpty()) {
            /*
             * transport first incoming event as single change to client,
             * advise to update so insignificant changes can still be processed
             * TODO Evaluate "auto-scheduled" flag and send IGNORE action if mail was already processed
             */
            Event newEvent = patchEvent(session, resource.getFirstEvent(), null != originalResource ? originalResource.getFirstEvent() : null, calendarUserId);
            analysis.addChange(getChange(session, calendarUserId, Type.UPDATE, null, newEvent, null, false));
            analysis.recommendAction(ITipAction.UPDATE);
        } else {
            if (null != originalResource && false == rescheduling(analysis)) {
                analysis.recommendAction(ITipAction.UPDATE);
            }
            analysis.recommendActions(ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
            if (hasConflicts(analysis)) {
                analysis.recommendAction(ITipAction.ACCEPT_AND_IGNORE_CONFLICTS);
            } else {
                analysis.recommendAction(ITipAction.ACCEPT);
            }
        }
        return analysis;
    }

    protected static boolean isOutOfSequence(List<ITipChange> changes) throws OXException {
        if (null == changes || changes.isEmpty()) {
            return false;
        }
        for (ITipChange change : changes) {
            if (null != change.getCurrentEvent() && null != change.getNewEvent() && isOutOfSequence(change.getNewEvent(), change.getCurrentEvent())) {
                return true;
            }
        }
        return false;
    }

    private List<ITipChange> determineChanges(CalendarSession session, int calendarUserId, CalendarObjectResource originalResource, ITipMessage message, TypeWrapper wrapper) throws OXException {
        CalendarObjectResource resource = getResource(session, message, true);
        Event seriesMaster = null != originalResource && null != originalResource.getSeriesMaster() ? originalResource.getSeriesMaster() : null;
        List<ITipChange> changes = new ArrayList<ITipChange>(resource.getEvents().size());
        for (Event event : resource.getEvents()) {
            /*
             * patch event & get or derive change based on currently stored resource
             */
            Event patchedEvent = patchEvent(session, event, null != originalResource ? originalResource.getFirstEvent() : null, calendarUserId);
            ITipChange change;
            if (null == originalResource) {
                change = getChange(session, calendarUserId, Type.CREATE, null, patchedEvent, seriesMaster, null != patchedEvent.getRecurrenceId());
            } else {
                change = optChange(session, calendarUserId, originalResource, patchedEvent, seriesMaster);
            }
            if (null != change) {
                describeDiff(change, wrapper, session, message);
                changes.add(change);
            }
        }
        return changes;
    }

    private ITipChange optChange(CalendarSession session, int calendarUserId, CalendarObjectResource originalResource, Event event, Event seriesMaster) {
        if (null != event.getRecurrenceId()) {
            Event originalChangeException = originalResource.getChangeException(event.getRecurrenceId());
            if (null != originalChangeException) {
                /*
                 * update of existing change exception, add change if different
                 */
                if (containsChanges(session, originalChangeException, event)) {
                    return getChange(session, calendarUserId, Type.UPDATE, originalChangeException, event, null, true);
                    //                    return getChange(session, calendarUserId, Type.UPDATE, originalChangeException, event, seriesMaster, true);
                }
                return null;
            }
            /*
             * creation of new change exception
             */
            Event originalOccurrence = optEventOccurrence(session, seriesMaster, event.getRecurrenceId());
            return getChange(session, calendarUserId, Type.UPDATE, originalOccurrence, event, seriesMaster, true);
        }
        if (null != originalResource.getFirstEvent() && null == originalResource.getFirstEvent().getRecurrenceId()) {
            /*
             * update of series master or non-recurring
             */
            if (containsChanges(session, originalResource.getFirstEvent(), event)) {
                return getChange(session, calendarUserId, Type.UPDATE, originalResource.getFirstEvent(), event, null, false);
            }
            return null;
        }
        /*
         * new series master for previously orphaned instances, or transition from single to series
         */
        return getChange(session, calendarUserId, Type.CREATE, null, event, null, false);
    }

    protected boolean containsChanges(CalendarSession session, Event originalEvent, Event updatedEvent) {
        //TODO: consider SEQUENCE number in diff? 
        return isOutOfSequence(updatedEvent, originalEvent) || doAppointmentsDiffer(updatedEvent, originalEvent, session);
    }

    private static boolean isOutOfSequence(Event updatedEvent, Event originalEvent) {
        return null != originalEvent && updatedEvent.containsSequence() && updatedEvent.getSequence() < originalEvent.getSequence();
    }

    protected static List<EventConflict> getConflicts(CalendarSession session, Event event, int calendarUserId) {
        try {
            Attendee attendee = session.getEntityResolver().prepareUserAttendee(calendarUserId);
            return session.getFreeBusyService().checkForConflicts(session, event, Collections.singletonList(attendee));
        } catch (OXException e) {
            LOG.warn("Unexpected error checking conflicts for {}", event, e);
            session.addWarning(e);
            return Collections.emptyList();
        }
    }

    protected static ITipChange getChange(CalendarSession session, int calendarUserId, Type type, Event currentEvent, Event newEvent, Event seriesMaster, boolean isException) {
        ITipChange change = new ITipChange();
        if (null != newEvent) {
            change.setConflicts(getConflicts(session, newEvent, calendarUserId));
        }
        change.setType(type);
        change.setCurrentEvent(currentEvent);
        change.setNewEvent(newEvent);
        change.setMaster(seriesMaster);
        change.setException(isException);
        return change;
    }

}
