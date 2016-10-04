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

package com.openexchange.calendar.itip.analyzers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnnotation;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipChange.Type;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.TypeWrapper;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link UpdateITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateITipAnalyzer extends AbstractITipAnalyzer {

    public UpdateITipAnalyzer(final ITipIntegrationUtility util, final ServiceLookup services) {
        super(util, services);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.REQUEST, ITipMethod.COUNTER, ITipMethod.PUBLISH);
    }

    @Override
    public ITipAnalysis analyze(final ITipMessage message, final Map<String, String> header, final TypeWrapper wrapper, final Locale locale, final User user, final Context ctx, final Session session) throws OXException {
        final ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        ITipChange change = new ITipChange();

        CalendarDataObject update = message.getDataObject();
        String uid = null;
        if (update != null) {
            uid = update.getUid();
        } else if (message.exceptions().iterator().hasNext()) {
            uid = message.exceptions().iterator().next().getUid();
        }
        CalendarDataObject original = util.resolveUid(uid, session);

        if (original == null && update == null && message.numberOfExceptions() > 0) {
            analysis.addAnnotation(new ITipAnnotation(Messages.ADD_TO_UNKNOWN,  locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }

        if (update == null) {
            update = original;
        }
    	analysis.setUid(update.getUid());
    	if (update.getAttachmentLink() != null) {
    	    analysis.getAttributes().put("attach", update.getAttachmentLink());
    	}

    	CalendarDataObject master = update;
        List<Appointment> exceptions = Collections.emptyList();

        boolean differ = true;

        if (original != null) {
            // TODO: Needs to be removed, when we handle external resources.
            addResourcesToUpdate(original, update);
            if (isOutdated(update, original)) {
                analysis.addAnnotation(new ITipAnnotation(Messages.OLD_UPDATE, locale));
                analysis.recommendAction(ITipAction.IGNORE);
                change.setCurrentAppointment(original);
                change.setType(ITipChange.Type.UPDATE);
                analysis.addChange(change);
                return analysis;
            }
            change.setType(ITipChange.Type.UPDATE);
            change.setCurrentAppointment(original);
            differ = doAppointmentsDiffer(update, original);
            exceptions = new ArrayList<Appointment>(util.getExceptions(original, session));
        } else {
            if (message.getMethod() == ITipMethod.COUNTER) {
                analysis.addAnnotation(new ITipAnnotation(Messages.COUNTER_UNKNOWN_APPOINTMENT,  locale));
                analysis.recommendAction(ITipAction.IGNORE);
                return analysis;
            }
            change.setType(ITipChange.Type.CREATE);
        }
        int owner = session.getUserId();
        if (message.getOwner() > 0 && message.getOwner() != session.getUserId()) {
            owner = message.getOwner();
        }
        if (owner != session.getUserId()) {
            OXFolderAccess oxfs = new OXFolderAccess(ctx);
            FolderObject defaultFolder = oxfs.getDefaultFolder(owner, FolderObject.CALENDAR);
            EffectivePermission permission = oxfs.getFolderPermission(
                defaultFolder.getObjectID(),
                session.getUserId(),
                UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx));
            if (permission.canCreateObjects()) {
                original.setParentFolderID(defaultFolder.getObjectID());
            } else {
                analysis.addAnnotation(new ITipAnnotation(Messages.SHARED_FOLDER, locale));
                return analysis;
            }
        }

        if (differ && message.getDataObject() != null) {
            CalendarDataObject dataObject = message.getDataObject().clone();
            ensureParticipant(dataObject, session, owner);
            if (original != null) {
                dataObject.setParentFolderID(original.getParentFolderID());
            }

        	change.setNewAppointment(dataObject);

            change.setConflicts(util.getConflicts(message.getDataObject(), session));

            describeDiff(change, wrapper, session, message);
            analysis.addChange(change);

        } else {
            master = original;
        }

        for (CalendarDataObject exception : message.exceptions()) {
        	exception = exception.clone();
        	ensureParticipant(exception, session, owner);

            final Appointment matchingException = findAndRemoveMatchingException(exception, exceptions);
            change = new ITipChange();
            change.setException(true);
            change.setMaster(master);

            differ = true;
            if (matchingException != null) {
                change.setType(ITipChange.Type.UPDATE);
                change.setCurrentAppointment(matchingException);
                differ = doAppointmentsDiffer(exception, matchingException);
            } else {
                change.setType(ITipChange.Type.CREATE);
            }
            if (master == null) {
            	final ITipAnnotation annotation = new ITipAnnotation(Messages.COUNTER_UNKNOWN_APPOINTMENT, locale); // FIXME: Choose better message once we can introduce new sentences again.
            	annotation.setAppointment(exception);
            	analysis.addAnnotation(annotation);
            	break;
            } else if (differ) {
                if (original != null) {
                    exception.setParentFolderID(original.getParentFolderID());
                }
                change.setNewAppointment(exception);
                change.setConflicts(util.getConflicts(exception, session));

                describeDiff(change, wrapper, session, message);
                analysis.addChange(change);
            }
        }
        if (exceptions != null && !exceptions.isEmpty()) {
            for (final Appointment unmentionedExceptions : exceptions) {
                change = new ITipChange();
                change.setException(true);
                change.setType(ITipChange.Type.DELETE);
                change.setDeleted(unmentionedExceptions);
                change.setMaster(master);

                change.setDiffDescription(new ArrayList<String>());
                analysis.addChange(change);
                analysis.recommendAction(ITipAction.DELETE);
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
                        analysis.recommendActions(ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
                	}
                } else {
                	if (message.getMethod() == ITipMethod.COUNTER) {
                        analysis.recommendActions(ITipAction.UPDATE);
                	} else {
                    	analysis.recommendActions(ITipAction.UPDATE, ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
                	}
                }
            }
        }
        if (analysis.getChanges().isEmpty() && analysis.getAnnotations().isEmpty()) {
            change = new ITipChange();
            change.setNewAppointment((original != null) ? original.clone() : update);
            if (original != null) {
            	change.setCurrentAppointment(original);
            }
            change.setType(Type.UPDATE);
            analysis.addChange(change);
            analysis.recommendActions(ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);

        }
        return analysis;
    }

    /**
     * Adds all existing Resources to the participant list of the update.
     *
     * @param original
     * @param update
     */
    private void addResourcesToUpdate(CalendarDataObject original, CalendarDataObject update) {
        if (original.getParticipants() == null || original.getParticipants().length == 0) {
            return;
        }

        List<Participant> newParticipants = new ArrayList<Participant>();
        for (Participant p : original.getParticipants()) {
            if (p.getType() == Participant.RESOURCE || p.getType() == Participant.RESOURCEGROUP) {
                newParticipants.add(p);
            }
        }

        if (newParticipants.isEmpty()) {
            return;
        }

        if (update.getParticipants() == null || update.getParticipants().length == 0) {
            update.setParticipants(newParticipants);
            return;
        }

        List<Participant> participants = Arrays.asList(update.getParticipants());
        for (Participant p : newParticipants) {
            if (!participants.contains(p)) {
                update.addParticipant(p);
            }
        }
    }

    private boolean isOutdated(CalendarDataObject update, CalendarDataObject original) {
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
        } else if (original.containsCreationDate() && original.getCreationDate() != null) {
            originalLastTouched = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            originalLastTouched.setTime(original.getCreationDate());
        }
        Calendar updateLastTouched = null;
        if (update.containsLastModified() && update.getLastModified() != null) {
            updateLastTouched = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            updateLastTouched.setTime(update.getLastModified());
        } else if (update.containsCreationDate() && update.getCreationDate() != null) {
            updateLastTouched = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            updateLastTouched.setTime(update.getCreationDate());
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

}
