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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipSpecialHandling;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link UpdateITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateITipAnalyzer extends AbstractITipAnalyzer {

    private final static Logger LOGGER = LoggerFactory.getLogger(UpdateITipAnalyzer.class);

    public UpdateITipAnalyzer(final ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.REQUEST, ITipMethod.COUNTER, ITipMethod.PUBLISH);
    }

    @Override
    public ITipAnalysis analyze(final ITipMessage message, final Map<String, String> header, final TypeWrapper wrapper, final Locale locale, final User user, final Context ctx, final CalendarSession session) throws OXException {
        final ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        ITipChange change = new ITipChange();

        Event update = message.getEvent();
        String uid = null;
        if (update != null) {
            uid = update.getUid();
        } else if (message.exceptions().iterator().hasNext()) {
            uid = message.exceptions().iterator().next().getUid();
        }
        Event original = util.resolveUid(uid, session);

        if (null == update) {
            if (null == original) {
                if (message.numberOfExceptions() > 0) {
                    analysis.addAnnotation(new ITipAnnotation(Messages.ADD_TO_UNKNOWN, locale));
                    analysis.recommendAction(ITipAction.IGNORE);
                    return analysis;
                }
                throw new OXException(new IllegalArgumentException("No appointment instance given"));
            }
            update = original;
        }
        analysis.setUid(update.getUid());

        Event master = update;
        List<Event> exceptions = Collections.emptyList();

        boolean differ = true;

        if (original != null) {
            // TODO: Needs to be removed, when we handle external resources.
            addResourcesToUpdate(original, update);
            if (isOutdated(update, original)) {
                analysis.addAnnotation(new ITipAnnotation(Messages.OLD_UPDATE, locale));
                analysis.recommendAction(ITipAction.IGNORE);
                change.setCurrentEvent(original);
                change.setType(ITipChange.Type.UPDATE);
                analysis.addChange(change);
                return analysis;
            }
            change.setType(ITipChange.Type.UPDATE);
            change.setCurrentEvent(original);
            differ = doAppointmentsDiffer(update, original);
            exceptions = new ArrayList<Event>(util.getExceptions(original, session));
        } else {
            if (message.getMethod() == ITipMethod.COUNTER) {
                analysis.addAnnotation(new ITipAnnotation(Messages.COUNTER_UNKNOWN_APPOINTMENT, locale));
                analysis.recommendAction(ITipAction.IGNORE);
                return analysis;
            }
            change.setType(ITipChange.Type.CREATE);
        }
        int owner = session.getUserId();
        if (message.getOwner() > 0 && message.getOwner() != session.getUserId()) {
            owner = message.getOwner();

            OXFolderAccess oxfs = new OXFolderAccess(ctx);
            FolderObject defaultFolder = oxfs.getDefaultFolder(owner, FolderObject.CALENDAR);
            EffectivePermission permission = oxfs.getFolderPermission(defaultFolder.getObjectID(), session.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx));
            if (permission.canCreateObjects() && original != null) {
                original.setFolderId(Integer.toString(defaultFolder.getObjectID()));
            } else {
                analysis.addAnnotation(new ITipAnnotation(Messages.SHARED_FOLDER, locale));
                return analysis;
            }
        }

        if (differ && message.getEvent() != null) {
            Event event = session.getUtilities().copyEvent(message.getEvent(), (EventField[]) null);
            event = handleMicrosoft(message, analysis, original, event);
            ensureParticipant(original, event, session, owner);
            if (original != null) {
                event.setFolderId(original.getFolderId());
            }
            session.getUtilities().adjustTimeZones(owner, event, original);
            change.setNewEvent(event);

            change.setConflicts(util.getConflicts(message.getEvent(), session));

            describeDiff(change, wrapper, session, message);
            analysis.addChange(change);

        } else {
            master = original;
        }

        for (Event exception : message.exceptions()) {
            exception = session.getUtilities().copyEvent(exception, (EventField[]) null);

            final Event matchingException = findAndRemoveMatchingException(master, exception, exceptions);
            change = new ITipChange();
            change.setException(true);
            change.setMaster(master);

            exception = handleMicrosoft(message, analysis, matchingException, exception);
            exception.setSeriesId(update.getSeriesId());

            differ = true;
            if (matchingException != null) {
                session.getUtilities().adjustTimeZones(owner, exception, matchingException);
                change.setType(ITipChange.Type.UPDATE);
                change.setCurrentEvent(matchingException);
                ensureParticipant(matchingException, exception, session, owner);
                differ = doAppointmentsDiffer(exception, matchingException);
            } else {
                if (isDeleteException(original, exception)) {
                    analysis.addAnnotation(new ITipAnnotation(Messages.CHANGE_PARTICIPANT_STATE_IN_DELETED_APPOINTMENT, locale));
                    analysis.recommendAction(ITipAction.IGNORE);
                    return analysis;
                }
                // Exception is not yet created
                session.getUtilities().adjustTimeZones(owner, exception, master);
                exception.removeUid();
                ensureParticipant(original, exception, session, owner);
                change.setType(ITipChange.Type.CREATE);
            }
            if (master == null) {
                final ITipAnnotation annotation = new ITipAnnotation(Messages.COUNTER_UNKNOWN_APPOINTMENT, locale); // FIXME: Choose better message once we can introduce new sentences again.
                annotation.setEvent(exception);
                analysis.addAnnotation(annotation);
                break;
            } else if (differ) {
                if (original != null) {
                    exception.setFolderId(original.getFolderId());
                }
                change.setNewEvent(exception);
                change.setConflicts(util.getConflicts(exception, session));

                describeDiff(change, wrapper, session, message);
                analysis.addChange(change);
            }
        }

        // Purge conflicts of irrelevant conflicts

        purgeConflicts(analysis);
        if (updateOrNew(analysis)) {
            if (message.getMethod() == ITipMethod.COUNTER) {
                analysis.recommendActions(ITipAction.UPDATE, ITipAction.DECLINECOUNTER);
            } else if (rescheduling(analysis)) {
                analysis.recommendActions(ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
                if (hasConflicts(analysis)) {
                    analysis.recommendAction(ITipAction.ACCEPT_AND_IGNORE_CONFLICTS);
                } else {
                    analysis.recommendAction(ITipAction.ACCEPT);
                }
            } else {
                if (isCreate(analysis)) {
                    if (message.getMethod() == ITipMethod.COUNTER) {
                        analysis.recommendActions(ITipAction.CREATE);
                    } else {
                        if (change.isException()) {
                            analysis.recommendActions(ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.UPDATE);
                        } else {
                            analysis.recommendActions(ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
                        }
                    }
                } else {
                    if (message.getMethod() == ITipMethod.COUNTER) {
                        analysis.recommendActions(ITipAction.UPDATE);
                    } else {
                        analysis.recommendActions(ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
                        if (false == (change.getDiff().isEmpty() || change.getDiff().isAboutCertainParticipantsStateChangeOnly(String.valueOf(user.getId())))) {
                            analysis.recommendAction(ITipAction.UPDATE);
                        }
                    }
                }
            }
        }
        if (analysis.getChanges().isEmpty() && analysis.getAnnotations().isEmpty()) {
            change = new ITipChange();
            if (null == original) {
                session.getUtilities().adjustTimeZones(owner, update, null);
                change.setNewEvent(update);
            } else {
                change.setNewEvent(session.getUtilities().copyEvent(original, (EventField[]) null));
                change.setCurrentEvent(original);
            }
            change.setType(ITipChange.Type.UPDATE);
            analysis.addChange(change);
        }

        return analysis;
    }

    /**
     * Adds all existing Resources to the participant list of the update.
     *
     * @param original The original event to get the resources from
     * @param update The update to add resource to
     */
    protected void addResourcesToUpdate(Event original, Event update) {
        if (original == null || update == null || original.getAttendees() == null || original.getAttendees().size() == 0) {
            return;
        }

        List<Attendee> toAdd = new ArrayList<>();
        for (Attendee a : original.getAttendees()) {
            if (CalendarUserType.RESOURCE.equals(a.getCuType())) {
                if (update.getAttendees() == null) {
                    update.setAttendees(new ArrayList<>());
                }
                toAdd.add(a);
            }
        }
        update.getAttendees().addAll(toAdd);
    }

    // TODO: redesign
    private boolean isOutdated(Event update, Event original) {
        if (original.containsSequence() && update.containsSequence()) {
            if (original.getSequence() > update.getSequence()) {
                return true;
            }
            if (original.getSequence() <= update.getSequence()) {
                return false;
            }
        }
        Calendar originalLastTouched = null;
        if (original.containsLastModified() && original.getLastModified() != null) {
            originalLastTouched = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            originalLastTouched.setTime(original.getLastModified());
        } else if (original.containsCreated() && original.getCreated() != null) {
            originalLastTouched = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            originalLastTouched.setTime(original.getCreated());
        }
        Calendar updateLastTouched = null;
        if (update.containsLastModified() && update.getLastModified() != null) {
            updateLastTouched = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            updateLastTouched.setTime(update.getLastModified());
        } else if (update.containsCreated() && update.getCreated() != null) {
            updateLastTouched = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            updateLastTouched.setTime(update.getCreated());
        }

        if (originalLastTouched != null && updateLastTouched != null) {
            if (timeInMillisWithoutMillis(originalLastTouched) > timeInMillisWithoutMillis(updateLastTouched)) { //Remove millis, since ical accuracy is just of seconds.
                return true;
            }
        }
        return false;
    }

    private long timeInMillisWithoutMillis(Calendar cal) {
        return cal.getTimeInMillis() - cal.get(Calendar.MILLISECOND);
    }

    private boolean updateOrNew(final ITipAnalysis analysis) {
        for (final ITipChange change : analysis.getChanges()) {
            if (change.getType() == Type.UPDATE || change.getType() == Type.CREATE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fields that are adopted on a COUNTER of a Microsoft client. See {@link #handleMicrosoft(ITipMessage, ITipAnalysis, Event, Event)} for further details
     */
    private final static EventField[] MICROSOFT_COUNTER_FIELDS = new EventField[] { EventField.START_DATE, EventField.END_DATE, EventField.EXTENDED_PROPERTIES };

    /**
     * Handles Microsoft special COUNTER method..
     * <p>
     * Microsoft doesn't allow attendees to add or remove other attendees. Therefore their counter method
     * only contains the MS attendee. This normally is a indicator that the COUNTER is about a removal of
     * an attendee.
     * <p>
     * Nevertheless we got to work around this.. To do so we base the attendees of the updated event on the
     * attendees of the original event and then overwrite the MS attendee object.
     * <p>
     * Moreover MS users can only modify the start date and the end data of an event. Changes made to other
     * properties are done automatically by the client. E.g.: Office365 changes the title of the event to
     * "<code>Appointment changed: The original title</code>". Thus we ignore all other properties changed by
     * the MS clients (expect adding extended properties).
     * 
     * @param message The {@link ITipMessage}
     * @param analysis The {@link ITipChange}
     * @param original The original {@link Event} containing all other attendees
     * @param update The updated {@link Event} to add the original attendees to
     * @return The updated {@link Event}
     * @throws OXException If original event can't be copied
     */
    private Event handleMicrosoft(ITipMessage message, ITipAnalysis analysis, Event original, Event update) throws OXException {
        if (message.getMethod() == ITipMethod.COUNTER && message.hasFeature(ITipSpecialHandling.MICROSOFT)) {
            if (null == original || null == update || null == original.getAttendees() || null == update.getAttendees() || update.getAttendees().size() != 1) {
                LOGGER.debug("Microsoft special handling unnecessary");
                return update;
            }
            Event copy = EventMapper.getInstance().copy(original, new Event(), (EventField[]) null);
            List<Attendee> attendees = new LinkedList<>(copy.getAttendees());
            Attendee microsoftAttendee = update.getAttendees().get(0);
            boolean isPartyCrasher = true;
            for (Iterator<Attendee> iterator = attendees.iterator(); iterator.hasNext();) {
                Attendee a = iterator.next();
                if (CalendarUtils.extractEMailAddress(microsoftAttendee.getUri()).equals(CalendarUtils.extractEMailAddress(a.getUri()))) {
                    iterator.remove();
                    isPartyCrasher = false;
                    break;
                }
            }
            if (isPartyCrasher) {
                // Party crasher on a COUNTER ..
                LOGGER.debug("Party crasher on a COUNTER ..");
                analysis.recommendAction(ITipAction.ACCEPT_PARTY_CRASHER);
            }
            // Add Microsoft attendee
            attendees.add(microsoftAttendee);
            copy.setAttendees(attendees);

            // Copy start, end date and extended properties
            copy = EventMapper.getInstance().copy(update, copy, MICROSOFT_COUNTER_FIELDS);

            return copy;
        }
        return update;
    }
}
